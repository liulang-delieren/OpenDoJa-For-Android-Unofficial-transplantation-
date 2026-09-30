package opendoja.audio.mld;

import opendoja.audio.mld.fuetrek.FueTrekSamplerProvider;
import opendoja.audio.mld.ma3.MA3SamplerProvider;
import opendoja.host.DoJaRuntime;
import opendoja.host.LaunchConfig;
import opendoja.host.OpenDoJaLaunchArgs;

import java.util.Locale;
import java.util.Map;

public enum MLDSynth {
    FUETREK("fuetrek", FueTrekSamplerProvider.SAMPLE_RATE, 1024) {
        @Override
        public SamplerProvider createSamplerProvider() {
            return new FueTrekSamplerProvider();
        }
    },
    MA3("ma3", MA3SamplerProvider.SAMPLE_RATE, 1024) {
        @Override
        public SamplerProvider createSamplerProvider() {
            return new MA3SamplerProvider(
                    MA3SamplerProvider.FM_MA3_4OP,
                    MA3SamplerProvider.FM_MA3_4OP,
                    MA3SamplerProvider.WAVE_DRUM_MA3);
        }
    };

    private static final String PARAMETER_KEY = "OpenDoJaMldSynth";

    public static final MLDSynth DEFAULT = FUETREK;

    public final String id;
    public final float defaultSampleRate;
    public final int defaultBufferFrames;

    MLDSynth(String id, float defaultSampleRate, int defaultBufferFrames) {
        this.id = id;
        this.defaultSampleRate = defaultSampleRate;
        this.defaultBufferFrames = defaultBufferFrames;
    }

    public abstract SamplerProvider createSamplerProvider();

    public static MLDSynth fromId(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        for (MLDSynth synth : values()) {
            if (synth.id.equals(normalized)) {
                return synth;
            }
        }
        return null;
    }

    public static MLDSynth resolveConfigured() {
        MLDSynth fromProperty = fromId(OpenDoJaLaunchArgs.get(OpenDoJaLaunchArgs.MLD_SYNTH, null));
        if (fromProperty != null) {
            return fromProperty;
        }
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            MLDSynth fromRuntime = fromParameters(runtime.parameters());
            if (fromRuntime != null) {
                return fromRuntime;
            }
        }
        LaunchConfig prepared = DoJaRuntime.peekPreparedLaunch();
        if (prepared != null) {
            MLDSynth fromPrepared = fromParameters(prepared.parameters());
            if (fromPrepared != null) {
                return fromPrepared;
            }
        }
        return DEFAULT;
    }

    public static MLDSynth fromParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return null;
        }
        return fromId(parameters.get(PARAMETER_KEY));
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT).replace('-', '_').replace('.', '_');
    }
}
