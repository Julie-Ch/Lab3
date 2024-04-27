import java.io.Serializable;
import java.util.Objects;

/**
 * Classe che rappresenta un utente del sistema, con informazioni come nome utente, password, badge e numero di recensioni.
 * Implementa l'interfaccia Serializable per consentire la serializzazione dell'oggetto.
 */
public class User implements Serializable {

    private final String username;
    private final String password;
    private Badge badge;
    private int number_review;

    /**
     * Costruttore che inizializza un nuovo utente con nome utente e password specificati, assegnando un badge di livello RECENSORE e inizializzando il numero di recensioni a 0.
     *
     * @param username Nome utente dell'utente.
     * @param password Password dell'utente.
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.badge = new Badge(Level.RECENSORE);
        this.number_review = 0;
    }

    /**
     * Costruttore che consente di inizializzare un utente con tutte le sue informazioni.
     *
     * @param username Nome utente dell'utente.
     * @param password Password dell'utente.
     * @param badge    Badge associato all'utente.
     * @param number_review Numero di recensioni dell'utente.
     */
    public User(String username, String password, Badge badge, int number_review) {
        this.username = username;
        this.password = password;
        this.badge = badge;
        this.number_review = number_review;
    }

    /**
     * Restituisce il nome utente dell'utente.
     *
     * @return Nome utente dell'utente.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Restituisce l'oggetto User stesso.
     *
     * @return Oggetto User.
     */
    public User getUser() {
        return this;
    }

    /**
     * Restituisce la password dell'utente.
     *
     * @return Password dell'utente.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Restituisce il badge associato all'utente.
     *
     * @return Badge associato all'utente.
     */
    public Badge getBadge() {
        return this.badge;
    }

    /**
     * Restituisce il numero di recensioni dell'utente.
     *
     * @return Numero di recensioni dell'utente.
     */
    public int getNumber_review() {
        return this.number_review;
    }

    /**
     * Imposta il badge dell'utente con un livello specificato.
     *
     * @param level Livello da associare al badge dell'utente.
     */
    public void setBadge(Level level) {
        this.badge = new Badge(level);
    }

    /**
     * Incrementa il numero di recensioni dell'utente e aggiorna il badge corrispondente.
     */
    public void setNumber_review() {
        this.number_review = this.number_review + 1;
        updateBadge();
    }

    /**
     * Override del metodo equals per consentire il confronto tra oggetti User in base al nome utente.
     *
     * @param obj Oggetto da confrontare con l'utente corrente.
     * @return true se i due oggetti User hanno lo stesso nome utente, false altrimenti.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return username.equals(user.username);
    }

    /**
     * Override del metodo hashCode per generare un codice hash basato sul nome utente dell'utente.
     *
     * @return Codice hash generato.
     */
    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    /**
     * Override del metodo toString per ottenere una rappresentazione testuale dell'oggetto User.
     *
     * @return Stringa che rappresenta l'oggetto User.
     */
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", badge=" + badge +
                ", number_review=" + number_review +
                '}';
    }

    /**
     * Aggiorna il badge dell'utente in base al numero di recensioni effettuate.
     */
    public void updateBadge() {
        if (this.number_review > Level.CONTRIBUTORE_SUPER.getValue()) {
            return;
        } else if (this.number_review >= Level.CONTRIBUTORE_SUPER.getValue()) {
            this.badge.setLevel(Level.CONTRIBUTORE_SUPER);
        } else if (this.number_review >= Level.CONTRIBUTORE_ESPERTO.getValue()) {
            this.badge.setLevel(Level.CONTRIBUTORE_ESPERTO);
        } else if (this.number_review >= Level.CONTRIBUTORE.getValue()) {
            this.badge.setLevel(Level.CONTRIBUTORE);
        } else if (this.number_review >= Level.RECENSORE_ESPERTO.getValue()) {
            this.badge.setLevel(Level.RECENSORE_ESPERTO);
        }
    }
}
