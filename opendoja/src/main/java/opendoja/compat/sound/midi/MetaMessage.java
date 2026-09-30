package opendoja.compat.sound.midi;

public class MetaMessage extends opendoja.compat.sound.midi.MidiMessage {
    public MetaMessage() { }
    public int getType()  { return 0; }
    public static final int META = 255;
}

