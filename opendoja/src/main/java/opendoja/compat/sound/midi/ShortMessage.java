package opendoja.compat.sound.midi;

public class ShortMessage extends opendoja.compat.sound.midi.MidiMessage {
    public int getCommand()  { return 0; }
    public int getData2()  { return 0; }
    public int getChannel()  { return 0; }
    public int getData1()  { return 0; }
    public ShortMessage() { }
    public void setMessage(int p0, int p1, int p2, int p3)  throws opendoja.compat.sound.midi.InvalidMidiDataException {  }
    public static final int MIDI_TIME_CODE = 241;
    public static final int SONG_POSITION_POINTER = 242;
    public static final int SONG_SELECT = 243;
    public static final int TUNE_REQUEST = 246;
    public static final int END_OF_EXCLUSIVE = 247;
    public static final int TIMING_CLOCK = 248;
    public static final int START = 250;
    public static final int CONTINUE = 251;
    public static final int STOP = 252;
    public static final int ACTIVE_SENSING = 254;
    public static final int SYSTEM_RESET = 255;
    public static final int NOTE_OFF = 128;
    public static final int NOTE_ON = 144;
    public static final int POLY_PRESSURE = 160;
    public static final int CONTROL_CHANGE = 176;
    public static final int PROGRAM_CHANGE = 192;
    public static final int CHANNEL_PRESSURE = 208;
    public static final int PITCH_BEND = 224;
}

