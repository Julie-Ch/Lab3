/**
 * Enum che rappresenta i livelli di utenza nel sistema HOTELIER.
 */
public enum Level {
    RECENSORE(0),
    RECENSORE_ESPERTO(1),
    CONTRIBUTORE(2),
    CONTRIBUTORE_ESPERTO(3),
    CONTRIBUTORE_SUPER(4);

    private final int value;

    /**
     * Costruttore privato per l'enum Level.
     *
     * @param value Valore associato al livello.
     */
    Level(int value) {
        this.value = value;
    }

    /**
     * Restituisce il valore associato al livello.
     *
     * @return Valore del livello.
     */
    public int getValue() {
        return value;
    }
}
