package opendoja.android;

import android.app.Activity;
import android.os.Bundle;

import com.nttdocomo.ui.IApplication;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import opendoja.host.DesktopLauncher;
import opendoja.host.DoJaRuntime;
import opendoja.host.JamGameClassLoaderFactory;
import opendoja.host.JamLauncher;
import opendoja.host.LaunchConfig;
import opendoja.host.OpenDoJaLog;

public class EntryActivity extends Activity {
    // Threads owned by the running game/runtime. When the user force-exits with BACK their
    // uncaught exceptions must not tear down the whole process - the user expects to land on
    // the title screen, not on an "app crashed" dialog.
    private static final Set<Thread> doomedThreads = ConcurrentHashMap.newKeySet();
    private static volatile boolean exiting;
    private static boolean crashGuardInstalled;
    private static Thread.UncaughtExceptionHandler previousCrashHandler;

    private JamSurfaceView surfaceView;
    private JamInputController input;
    // Static: a system-driven activity recreation must not spawn a second
    // jam-launch game loop over the one that is already running (two loops on
    // one process corrupt the game's shared static state). reset in stopGame()
    // so the next manual launch starts a fresh thread.
    private static volatile boolean launched;
    private boolean gameStopped;
    private android.app.AlertDialog gameMenu;
    // Audio-handle generation of the game launched by this activity; exit
    // cleanup may only release handles up to this generation.
    private volatile long audioGeneration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenDoJaLog.configure(OpenDoJaLog.Level.DEBUG);
        // The default artifact-derived host root points inside the read-only APK
        // directory; SD-card storage init would fail there and titles like the
        // Touhou "reading SD" screen would wait forever. Keep it in app-private
        // storage instead.
        opendoja.host.OpenDoJaPaths.setHostDataRoot(getFilesDir().toPath().resolve(".opendoja"));
        installCrashGuard();
        exiting = false;
        input = new JamInputController();
        surfaceView = new JamSurfaceView(this, input);
        setContentView(surfaceView);
        DoJaRuntime.setPlatformPresenter(frame -> surfaceView.present(frame));
        // Direct-draw games paint dozens of primitives per frame without lock()/unlock();
        // coalescing their outside-lock presents keeps half-drawn frames off the screen.
        DoJaRuntime.setPresentDebounceEnabled(true);
        if (!launched) {
            launched = true;
            boolean smoke = getIntent() != null && getIntent().getBooleanExtra("smoke", false);
            Thread launchThread = new Thread(smoke ? this::launchSmoke : this::launchGame, "jam-launch");
            launchThread.setDaemon(true);
            launchThread.start();
        }
    }

    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        if (event.getKeyCode() == android.view.KeyEvent.KEYCODE_BACK) {
            // The system back key opens the in-game menu (hide keypad / exit game);
            // it is never routed into the game (the on-screen keypad keeps SOFT2
            // available for the game itself).
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {
                showGameMenu();
            }
            return true;
        }
        opendoja.host.HostControlAction mapped = mappedAction(event.getKeyCode());
        if (mapped != null) {
            routeAction(mapped, event.getAction() == android.view.KeyEvent.ACTION_DOWN);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /** BACK menu: toggle the on-screen keypad, or quit the game back to HomeActivity. */
    private void showGameMenu() {
        if (gameMenu != null && gameMenu.isShowing()) {
            return;
        }
        if (surfaceView == null) {
            return;
        }
        boolean keypadShown = surfaceView.isKeypadVisible();
        String[] items = {
                keypadShown ? "隐藏虚拟按键" : "显示虚拟按键",
                "退出游戏",
        };
        gameMenu = new android.app.AlertDialog.Builder(this)
                .setTitle("游戏菜单")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        surfaceView.setKeypadVisible(!keypadShown);
                    } else {
                        dialog.dismiss();
                        forceExitToTitle();
                        return;
                    }
                    dialog.dismiss();
                })
                .setOnDismissListener(dialog -> gameMenu = null)
                .create();
        gameMenu.show();
    }

    private void forceExitToTitle() {
        stopGame();
        if (!isFinishing()) {
            finish();
        }
    }

    /** Kills the running game: wakes input waiters, interrupts game threads, shuts the
     *  runtime down without exiting the process, so the title screen stays alive. */
    private void stopGame() {
        if (gameStopped) {
            return;
        }
        gameStopped = true;
        exiting = true;
        launched = false;
        android.util.Log.i("OpenDoJa", "stopGame: runtime=" + DoJaRuntime.current()
                + " audioGeneration=" + audioGeneration);
        if (surfaceView != null) {
            surfaceView.releaseAllControls();
        }
        // Immediate sweep: stale BGM stops right away, even before the async
        // runtime shutdown below finishes closing registered presenters.
        releaseGameAudio(audioGeneration);
        doomGameThreads();
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            // Shutdown on a helper thread so a hung game can never block the exit itself.
            Thread shutdownThread = new Thread(() -> {
                try {
                    // exitOnShutdown is false for jam launches, so this never System.exit()s.
                    runtime.shutdown();
                } catch (Throwable throwable) {
                    OpenDoJaLog.error(EntryActivity.class, "runtime shutdown failed", throwable);
                } finally {
                    // Catch handles opened by zombie game threads while the
                    // shutdown was still in flight.
                    releaseGameAudio(audioGeneration);
                    android.util.Log.i("OpenDoJa", "runtime shutdown done, audio released");
                }
            }, "jam-shutdown");
            shutdownThread.setDaemon(true);
            shutdownThread.start();
        }
    }

    private static void releaseGameAudio(long generation) {
        try {
            opendoja.audio.mld.MLDPCMPlayer.releaseGenerationsUpTo(generation);
        } catch (Throwable throwable) {
            android.util.Log.e("OpenDoJa", "releaseGameAudio failed", throwable);
        }
    }

    private static void doomGameThreads() {
        Thread self = Thread.currentThread();
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread == self || isSystemThread(thread)) {
                continue;
            }
            doomedThreads.add(thread);
            thread.interrupt();
            if (thread.getName().equals("jam-launch")) {
                // Games like FF4 catch InterruptedException inside their frame loop
                // and keep rendering headless forever after an exit - every zombie
                // steals a full CPU core from the next game. Best-effort hard stop:
                // throws UnsupportedOperationException on VMs where it is disabled.
                try {
                    thread.stop();
                } catch (Throwable ignored) {
                }
            }
        }
    }

    private static boolean isSystemThread(Thread thread) {
        String name = thread.getName();
        return name.equals("main")
                || name.startsWith("Finalizer")
                || name.startsWith("HeapTask")
                || name.startsWith("ReferenceQueue")
                || name.startsWith("Signal Catcher")
                || name.startsWith("ADB-JDWP")
                || name.startsWith("Profile Saver")
                || name.startsWith("Jit thread")
                || name.startsWith("Binder:")
                || name.startsWith("RenderThread")
                || name.startsWith("hwuiTask")
                // Shared infrastructure that outlives a single game: interrupting
                // it once killed the static MLD audio worker, silencing BGM for
                // every later game launched in this process.
                || name.startsWith("opendoja-mld")
                || name.startsWith("opendoja-midi")
                || name.startsWith("openDoJa-media-events");
    }

    private static synchronized void installCrashGuard() {
        if (crashGuardInstalled) {
            return;
        }
        crashGuardInstalled = true;
        previousCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (doomedThreads.contains(thread) || (exiting && looksLikeGameThread(thread))) {
                android.util.Log.w("OpenDoJa", "ignored game-thread crash after exit: " + thread,
                        throwable);
                return;
            }
            if (previousCrashHandler != null) {
                previousCrashHandler.uncaughtException(thread, throwable);
            }
        });
    }

    private static boolean looksLikeGameThread(Thread thread) {
        String name = thread.getName();
        return name.startsWith("Thread-")
                || name.startsWith("Timer-")
                || name.startsWith("jam-launch")
                || name.startsWith("openDoJa-")
                || name.equals("MainEventDispatchThread")
                || name.startsWith("pool-");
    }

    private opendoja.host.HostControlAction mappedAction(int keyCode) {
        java.util.Map<String, ?> bindings = getSharedPreferences(KeyMappingActivity.PREFS_NAME, MODE_PRIVATE)
                .getAll();
        for (java.util.Map.Entry<String, ?> entry : bindings.entrySet()) {
            if (entry.getValue() instanceof Integer && (Integer) entry.getValue() == keyCode) {
                opendoja.host.HostControlAction action = opendoja.host.HostControlAction.fromId(entry.getKey());
                if (action != null) {
                    return action;
                }
            }
        }
        return null;
    }

    private void routeAction(opendoja.host.HostControlAction action, boolean pressed) {
        if (action.dispatchKind() == opendoja.host.HostControlAction.DispatchKind.HOST_SOFT_KEY) {
            if (pressed) {
                input.pressSoft(action.dispatchCode());
            } else {
                input.releaseSoft(action.dispatchCode());
            }
        } else {
            if (pressed) {
                input.pressDojaKey(action.dispatchCode());
            } else {
                input.releaseDojaKey(action.dispatchCode());
            }
        }
    }

    @Override
    protected void onPause() {
        if (surfaceView != null) {
            surfaceView.releaseAllControls();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        // Leaving the game screen (HOME / task switch) terminates the game just
        // like BACK: otherwise orphaned game threads keep rendering headless and
        // burn CPU while the app is hidden.
        if (!isFinishing()) {
            finish();
        }
        stopGame();
        super.onStop();
    }

    private void launchSmoke() {
        try {
            LaunchConfig config = LaunchConfig.builder(SmokeApp.class)
                    .scratchpadRoot(getFilesDir().toPath().resolve("scratchpad"))
                    .build();
            DesktopLauncher.launch(config);
            android.util.Log.i("OpenDoJa", "launch returned normally");
        } catch (Throwable throwable) {
            OpenDoJaLog.error(EntryActivity.class, "launch failed", throwable);
            android.util.Log.e("OpenDoJa", "launch failed", throwable);
        }
    }

    private void launchGame() {
        audioGeneration = opendoja.audio.mld.MLDPCMPlayer.bumpGeneration();
        // Pre-game sweep: any handle left over from an earlier game (e.g. a
        // zombie presenter opened between shutdown and this launch) dies now.
        releaseGameAudio(audioGeneration - 1);
        android.util.Log.i("OpenDoJa", "launchGame: audioGeneration=" + audioGeneration);
        try {
            opendoja.host.DoJaEncoding.installDefaultCharsetOverride();
            byte[] sjisA = {(byte) 0x82, (byte) 0xA0};
            String decoded = new String(sjisA);
            StringBuilder probe = new StringBuilder();
            for (char ch : decoded.toCharArray()) {
                probe.append(String.format("%04X ", (int) ch));
            }
            android.util.Log.i("OpenDoJa", "sjis probe chars=" + probe);

            File jam = resolveJamFile();
            File jar = opendoja.host.JamGameJarLocator.locate(jam.toPath()).toFile();
            File dex = JamDexHost.ensureDex(jar);
            android.util.Log.i("OpenDoJa", "dex ready: " + dex + " (" + dex.length() + " bytes)");

            android.content.Context appContext = getApplicationContext();
            JamGameClassLoaderFactory.setProvider((jamPath, parent) -> {
                File jamJar = jamPath.toFile();
                File siblingDex = JamDexHost.dexFor(jamJar);
                if (!siblingDex.exists()) {
                    return null;
                }
                return JamDexHost.createLoader(siblingDex, parent, appContext);
            });

            IApplication application = JamLauncher.launch(jam.toPath(), false);
            android.util.Log.i("OpenDoJa", "jam launch returned normally: " + application);
        } catch (Throwable throwable) {
            OpenDoJaLog.error(EntryActivity.class, "game launch failed", throwable);
            android.util.Log.e("OpenDoJa", "game launch failed", throwable);
            String message = throwable.getMessage() == null
                    ? throwable.getClass().getSimpleName()
                    : throwable.getMessage();
            runOnUiThread(() -> android.widget.Toast.makeText(this,
                    "游戏启动失败: " + message, android.widget.Toast.LENGTH_LONG).show());
        }
    }

    private File resolveJamFile() {
        String extra = getIntent() != null ? getIntent().getStringExtra("jamPath") : null;
        if (extra != null) {
            File jam = new File(extra);
            if (jam.isFile()) {
                return jam;
            }
            android.util.Log.w("OpenDoJa", "jamPath missing: " + extra);
            throw new IllegalStateException("游戏文件不存在: " + extra);
        }
        throw new IllegalStateException("未指定游戏路径 (jamPath)");
    }

    @Override
    protected void onDestroy() {
        // Covers destruction paths other than BACK (task swipe etc.): the game must never
        // outlive its activity and keep spinning headless.
        stopGame();
        DoJaRuntime.setPlatformPresenter(null);
        DoJaRuntime.setPresentDebounceEnabled(false);
        super.onDestroy();
    }
}
