import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Classe che rappresenta un badge associato a un determinato livello e data di riscatto.
 * Implementa l'interfaccia Serializable per consentire la serializzazione dell'oggetto.
 */
public class Badge implements Serializable {

    private Level level;
    private String date;

    /**
     * Costruttore che inizializza un oggetto Badge con un livello specificato e una data specificata.
     *
     * @param level Livello associato al badge.
     * @param date  Data di riscatto del badge.
     */
    public Badge(Level level, Date date) {
        this.level = level;
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        this.date = format.format(date);
    }

    /**
     * Costruttore che accetta solo un oggetto Level come parametro e imposta la data di riscatto come la data corrente.
     *
     * @param level Livello associato al badge.
     */
    public Badge(Level level) {
        this.level = level;
        date = new Date().toString();
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto Badge.
     *
     * @return Stringa rappresentante l'oggetto Badge.
     */
    public String toString() {
        return "Badge: " + this.level + ", Date of redeem: " + this.date;
    }

    /**
     * Restituisce una rappresentazione testuale del Badge.
     *
     * @return Stringa con le informazioni del Badge.
     */
    public String prettyPrint() {

        int numMedals = this.getLevel().getValue();
        String medal = 	"\uD83C\uDFC5"; 
        StringBuilder medalString = new StringBuilder();

        for (int i = 0; i < numMedals; i++) {
            medalString.append(medal).append(" ");
        }
        return "Your Badge is: \n" + medalString + this.level.toString().replace("_", " ") + "\nDate of redeem: " + this.date;
    }

    /**
     * Restituisce il livello associato al badge.
     *
     * @return Livello associato al badge.
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Imposta il livello associato al badge con il valore passato come parametro.
     *
     * @param level Nuovo livello da associare al badge.
     */
    public void setLevel(Level level) {
        this.level = level;
        Date currentDate = new Date();
        // Formatta la data corrente
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        this.date = format.format(currentDate);
    }
}
