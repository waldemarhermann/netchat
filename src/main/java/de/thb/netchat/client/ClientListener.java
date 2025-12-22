package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Netzwerk-Listener: Ist verantwortlich für das kontinuierliche Überwachen des Input Streams.
 * Sie implementiert das Interface Runnable, damit ihre Logik in einem separaten Thread ausgeführt
 * werden kann. Wichtig, da das Lesen von Netzwerkdaten "blockierend ist und sonst die GUI einfriert."
 */
public class ClientListener implements Runnable {

    // Offene Socket zum Server - physische Verbindung.
    private final Socket socket;

    /*
    * Callback-Interface (Funktionale Interface). Dient der Entkoppelung: Der Listener weiß nicht
    * Implementierung des Observer-Patterns zur Entkopplung (Loose Coupling):
    * Der Listener fungiert als reiner Daten-Produzent. Er übergibt die empfangene Message
    * an diesen Consumer, ohne die konkrete Empfänger-Klasse (ChatController)
    * oder deren Verarbeitungslogik kennen zu müssen.
    * */
    private final Consumer<Message> callback;

    // Gson-Instanz für die JSON-Deserialisierung.
    private final Gson gson = new Gson();

    /**
     * Konstruktor.
     *
     * @param socket Aktive, offene Socket-Verbindung (aus der ClientConnection). Dienst als InputStream für
     *               eingehende Nachrichten.
     * @param callback Die Methode (onMessageReceived), die aufgerufen werden soll, wenn eine Nachricht ankommt.
     */
    public ClientListener(Socket socket, Consumer<Message> callback) {
        this.socket = socket;
        this.callback = callback;
    }

    /*
    * Ausführungslogik des Threads.
    * Startet, sobald thread.start() aufgerufen wird.
    * */
    @Override
    public void run() {

        /*
        * try-with-resources Statement.
        * Erstellt den Reader und garantiert, das er und Stream automatisch geschlossen wird
        * sobald der try-Block verlassen wird (z.B. bei Verbindungsabbruch oder Fehler).
        * Chain: InputStream (Bytes) --> InputStreamReader (Zeichen) --> BufferedReader (Zeilen-Puffer).
        * */
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            String line;

            /*
            * Blocking I/O Loop, Endlosschleife.
            * in.readLine() ist eine blockierende Operation.
            * Der Thread schläft so lange, bis vom Server ein Zeilenumbruch (\n) gesendet wird.
            * Die Schleife endet erst, wenn readLine() null zurückgibt (Verbindung vom Server geschlossen).
            * */
            while ((line = in.readLine()) != null) {

                // JSON in Java-Message-Objekt.
                Message message = gson.fromJson(line, Message.class);

                // Null-Safety Check
                if (message != null) {
                    /*
                    * Event Dispatching (Callback).
                    * Nachricht wird akzeptiert und wird an den Consumer übergeben.
                    * Hier wird die Methode onMessageReceived im ChatController aufgerufen.
                    * Wichtig: Dieser Aufruf passiert noch im Thread dieses Listeners (nicht im UI-Thread)!
                    * */
                    callback.accept(message);   // GUI oder Konsole informieren
                }
            }

        } catch (Exception e) {
            // Exception Handling: Wenn Verbindung abreißt oder Socket geschlossen wird.
            System.err.println("ClientListener Fehler:");
            e.printStackTrace();
        }
    }
}
