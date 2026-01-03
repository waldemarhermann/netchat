package de.thb.netchat.client;

/**
 * Diese Klasse fungiert als zentraler Datenspeicher für den client-seitigen Anwendungszustand.
 * Implementierung des Singleton-Entwurfsmusters.
 * Problemstellung:
 * In JavaFX sind Controller (LoginController, ChatController etc.) an ihre Views gebunden und werden
 * beim Szenenwechsel oft neu erstellt oder verworfen.
 * Daher: Der ClientManager existiert unabhängig von der GUI als globale Instanz und ermöglicht
 * den Datenaustausch zwischen verschiedenen Controllern.
 */
public class ClientManager {

    // Die einzige statische Instanz dieser Klasse (Singleton-Prinzip).
    // "static" bedeutet, dass diese Variable an die Klasse gebunden ist, nicht an ein Objekt.
    private static ClientManager instance;

    // --- Globaler Zustand - Session State ---

    // Die aktive TCP-Verbindung zum Server
    private ClientConnection connection;

    // Benutzername des aktuell eingeloggten Users.
    private String username;

    /**
     * Privater Konstruktor.
     * Verhindert, dass andere Klassen mittels "new ClientManager()" Instanzen erstellen können.
     * Garantiert, dass es zur Laufzeit maximal ein Objekt dieser Klasse gibt.
     */
    private ClientManager() {
    }

    /**
     * Der globale Zugriffspunkt auf die Instanz.
     * Die Instanz wird erstellt, wenn sie zum ersten Mal benötigt wird.
     * "synchronized" macht die Methode threadsicher. Verhindert Race Conditions,
     * falls zwei Threads gleichzeitig versuchen, die Instanz zu erstellen
     * @return Einzigartige Instanz des ClientManagers
     */
    public static synchronized ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }
        return instance;
    }

    // --- Getter und Setter für den Zugriff auf die Session-Daten ---

    public ClientConnection getConnection() {
        return connection;
    }

    public void setConnection(ClientConnection connection) {
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
