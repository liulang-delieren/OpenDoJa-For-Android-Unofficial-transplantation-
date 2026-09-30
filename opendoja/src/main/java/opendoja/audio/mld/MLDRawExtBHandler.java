package opendoja.audio.mld;

/**
 * Sink for synth-specific raw ext-B opcodes that are not handled by the
 * normalized shared MLD event flow.
 */
public interface MLDRawExtBHandler
{
    /**
     * Handle a backend-specific raw ext-B event.
     *
     * @param eventId The ext-B opcode.
     * @param channel The normalized runtime channel, or {@code -1}.
     * @param rawParam The raw packed parameter byte.
     */
    void handleRawExtBEvent(int eventId, int channel, int rawParam);
}
