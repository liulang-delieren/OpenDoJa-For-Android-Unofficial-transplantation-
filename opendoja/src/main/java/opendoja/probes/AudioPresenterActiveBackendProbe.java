package opendoja.probes;

import com.nttdocomo.ui.AudioPresenter;

import javax.sound.midi.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public final class AudioPresenterActiveBackendProbe {
    private AudioPresenterActiveBackendProbe() {
    }

    public static void main(String[] args) throws Exception {
        AudioPresenter presenter = AudioPresenter.getAudioPresenter();
        setField(presenter, "activeSoundKind", mediaKind("MIDI"));
        setField(presenter, "sequencer", new ProbeSequencer(1_234_000L, 9_876_000L));

        check(presenter.getCurrentTime() == 1234,
                "MIDI current time should come from the active sequencer, not the idle eager MLD player");
        check(presenter.getTotalTime() == 9876,
                "MIDI total time should come from the active sequencer, not the idle eager MLD player");

        System.out.println("AudioPresenter active backend probe OK");
    }

    private static Object mediaKind(String name) throws Exception {
        Class<?> kindClass = Class.forName("com.nttdocomo.ui.MediaManager$PreparedSound$Kind");
        @SuppressWarnings({"unchecked", "rawtypes"})
        Object value = Enum.valueOf((Class<? extends Enum>) kindClass.asSubclass(Enum.class), name);
        return value;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static final class ProbeSequencer implements Sequencer {
        private final long microsecondPosition;
        private final long microsecondLength;

        private ProbeSequencer(long microsecondPosition, long microsecondLength) {
            this.microsecondPosition = microsecondPosition;
            this.microsecondLength = microsecondLength;
        }

        @Override
        public long getMicrosecondPosition() {
            return microsecondPosition;
        }

        @Override
        public long getMicrosecondLength() {
            return microsecondLength;
        }

        @Override
        public void setSequence(javax.sound.midi.Sequence sequence) {
        }

        @Override
        public void setSequence(java.io.InputStream stream) {
        }

        @Override
        public javax.sound.midi.Sequence getSequence() {
            return null;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public void startRecording() {
        }

        @Override
        public void stopRecording() {
        }

        @Override
        public boolean isRecording() {
            return false;
        }

        @Override
        public void recordEnable(Track track, int channel) {
        }

        @Override
        public void recordDisable(Track track) {
        }

        @Override
        public float getTempoInBPM() {
            return 120.0f;
        }

        @Override
        public void setTempoInBPM(float bpm) {
        }

        @Override
        public float getTempoInMPQ() {
            return 500_000.0f;
        }

        @Override
        public void setTempoInMPQ(float mpq) {
        }

        @Override
        public void setTempoFactor(float factor) {
        }

        @Override
        public float getTempoFactor() {
            return 1.0f;
        }

        @Override
        public long getTickLength() {
            return 0;
        }

        @Override
        public long getTickPosition() {
            return 0;
        }

        @Override
        public void setTickPosition(long tick) {
        }

        @Override
        public void setMicrosecondPosition(long microseconds) {
        }

        @Override
        public void setMasterSyncMode(Sequencer.SyncMode sync) {
        }

        @Override
        public Sequencer.SyncMode getMasterSyncMode() {
            return Sequencer.SyncMode.INTERNAL_CLOCK;
        }

        @Override
        public Sequencer.SyncMode[] getMasterSyncModes() {
            return new Sequencer.SyncMode[]{Sequencer.SyncMode.INTERNAL_CLOCK};
        }

        @Override
        public void setSlaveSyncMode(Sequencer.SyncMode sync) {
        }

        @Override
        public Sequencer.SyncMode getSlaveSyncMode() {
            return Sequencer.SyncMode.NO_SYNC;
        }

        @Override
        public Sequencer.SyncMode[] getSlaveSyncModes() {
            return new Sequencer.SyncMode[]{Sequencer.SyncMode.NO_SYNC};
        }

        @Override
        public void setLoopStartPoint(long tick) {
        }

        @Override
        public long getLoopStartPoint() {
            return 0L;
        }

        @Override
        public void setLoopEndPoint(long tick) {
        }

        @Override
        public long getLoopEndPoint() {
            return -1L;
        }

        @Override
        public void setLoopCount(int count) {
        }

        @Override
        public int getLoopCount() {
            return 0;
        }

        @Override
        public void setTrackMute(int track, boolean mute) {
        }

        @Override
        public boolean getTrackMute(int track) {
            return false;
        }

        @Override
        public void setTrackSolo(int track, boolean solo) {
        }

        @Override
        public boolean getTrackSolo(int track) {
            return false;
        }

        @Override
        public boolean addMetaEventListener(javax.sound.midi.MetaEventListener listener) {
            return true;
        }

        @Override
        public void removeMetaEventListener(javax.sound.midi.MetaEventListener listener) {
        }

        @Override
        public int[] addControllerEventListener(javax.sound.midi.ControllerEventListener listener, int[] controllers) {
            return controllers == null ? new int[0] : controllers.clone();
        }

        @Override
        public int[] removeControllerEventListener(javax.sound.midi.ControllerEventListener listener, int[] controllers) {
            return controllers == null ? new int[0] : controllers.clone();
        }

        @Override
        public Info getDeviceInfo() {
            return new Info("ProbeSequencer", "openDoJa", "Probe sequencer", "1.0") {
            };
        }

        @Override
        public void open() throws MidiUnavailableException {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public int getMaxReceivers() {
            return 0;
        }

        @Override
        public int getMaxTransmitters() {
            return 0;
        }

        @Override
        public Receiver getReceiver() throws MidiUnavailableException {
            throw new MidiUnavailableException("probe");
        }

        @Override
        public List<Receiver> getReceivers() {
            return Collections.emptyList();
        }

        @Override
        public Transmitter getTransmitter() throws MidiUnavailableException {
            throw new MidiUnavailableException("probe");
        }

        @Override
        public List<Transmitter> getTransmitters() {
            return Collections.emptyList();
        }
    }
}
