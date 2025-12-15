package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

// Netzwerk-Schnittstelle des Clients. Kapselt Java-Socket-Logik, übernimmt Verantwortung für PrintWriter, OutputStream, flush.
// Aufgaben: 1. Verbindung zum Server physikalisch herstellen (TCP-Handshake: SYN, SYN+ACK, ACK)
// 2. Java-Objekte (Message) in JSON umwandeln. 3. Diesen JSON an den Server schicken.
public class ClientConnection {

    private final String host;
    private final int port;

    // "Verbindungskabel" zum Server
    private Socket socket;

    // "Schreiber": schiebt Text in den Socket
    private PrintWriter out;

    // Übersetzer: Java Objekt <-> JSON String
    private final Gson gson = new Gson();

    // Konstruktor: Bereitet Verbindungsdaten vor.
    public ClientConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Hier wird die eigentliche Verbindung hergestellt.
    public void connect() throws IOException {
        // TCP-Handshake wird durchgeführt.
        // Auf Serverseite in ChatServer.java wird serverSocket.accept(); ausgelöst.
        socket = new Socket(host, port);

        // Initialisiert den PrintWriter auf dem Output-Stream des Sockets.
        // Auto-Flush auf true: Erzwingt, dass bei jedem println() Daten gesendet werden, sonst bleiben diese im Puffer hängen.
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    // Nachricht wird an den Server gesendet. Objekt -> JSON -> Internet.
    public void send(Message message) {
        if (out != null) {
            // Serialisierung: Java-Objekt wird in einen String verwandelt.
            // z.B. aus new Message ("login"...) wird {"type":"login", ...}
            String json = gson.toJson(message);
            out.println(json);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    // Verbindung wird geschlossen.
    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        // Fehler beim Schließen werden ignoriert.
    }

}
