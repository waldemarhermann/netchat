package de.thb.netchat.server;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.command.*; // Importiert unsere neuen Commands
import de.thb.netchat.service.ChatService;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repräsentiert die serverseitige Logik für genau eine Client-Verbindung.
 * Sie implementiert das Interface Runnable, um in einem separaten Thread ausgeführt zu werden.
 * Hauptaufgaben:
 * 1. Aufrechterhalten der TCP-Verbindung (Session).
 * 2. Deserialisierung eingehender JSON-Nachrichten.
 * 3. Weiterleitung der Nachrichten an das entsprechende Command-Objekt.
 */
public class ClientHandler implements Runnable {

    // Physische Verbindung zum Client.
    private final Socket socket;

    // Referenz auf die Geschäftslogik (Singleton-artig instanziiert im ChatServer).
    private final ChatService chatService;

    // JSON-Parser Instanz.
    private final Gson gson = new Gson();

    // Output-Stream Wrapper zum Senden von Textdaten an den Client
    private PrintWriter out;

    // Identität der Sitzung (wird erst nach erfolgreichem Login gesetzt).
    private String username;

    // Command Registry (Command Pattern).
    // Diese Map verknüpft eine Protokoll-String (z.B. "login") mit der auszuführenden Logikklasse.
    // Dies ermöglicht eine Erweiterung um neue Befehle ohne Änderung der run()-Methode.
    private final Map<String, Command> commands = new HashMap<>();

    /**
     * Initialisiert den Handler und registriert die verfügbaren Befehle.
     *
     * @param socket Der verbundene Client-Socket (aus serverSocket.accept()).
     * @param chatService chatService Der Service für Datenbankzugriffe.
     */
    public ClientHandler(Socket socket, ChatService chatService) {
        this.socket = socket;
        this.chatService = chatService;

        // Initialisierung der Befehls-Map (Command Registration).
        // Jeder Nachrichtentyp (Key) wird einer konkreten Implementierung (Value) zugeordnet.
        commands.put("register", new RegisterCommand());
        commands.put("login", new LoginCommand());
        commands.put("message", new MessageCommand());
        commands.put("history_request", new HistoryRequestCommand());
        commands.put("exit", new ExitCommand());
    }

    /**
     * Entry-Point des Threads.
     * Beinhaltet den Lebenszyklus der Client-Verbindung.
     */
    @Override
    public void run() {
        // Try-with-resources: Garantiert das Schließen der Streams und des Sockets,
        // selbst wenn eine Exception auftritt oder der Thread beendet wird.
        try (
                // Input: Liest Daten vom Client.
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Output: Sendet Daten an den Client. "true" aktiviert Auto-Flush (Puffer leeren bei println).
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = writer;

            // Verbindungsbestätigung: Senden des "Handshake-Grußes".
            Message welcome = new Message("info", "server", null, "Willkommen bei NetChat!");
            out.println(gson.toJson(welcome));

            String input;

            // Die Main-Loop (Event Loop) des Threads.
            // in.readline() ist eine blockierende Operation. Der Thread pausiert hier,
            // bis Daten vom Client eintreffen oder die Verbindung getrennt wird.
            while ((input = in.readLine()) != null) {

                // Deserialisierung: JSON-String -> Java-Objekt.
                Message message = gson.fromJson(input, Message.class);

                // Protokoll-Validierung.
                if (message == null || message.getType() == null) {
                    sendError("Ungültiges JSON-Format");
                    continue;
                }

                // --- STRATEGY PATTERN IMPLEMENTIERUNG ---
                // 1. Lockup: Suchen der passenden Strategie für den Nachrichtentyp.
                Command cmd = commands.get(message.getType());

                if (cmd != null) {
                    // 2. Ausführen der Logik im Command-Objekt.
                    // Der Handler übergibt sich selbst ('this'), damit das Command antworten kann.
                    cmd.execute(message, this, chatService);
                } else {
                    sendError("Unbekannter Befehl: " + message.getType());
                }
            }

        } catch (Exception e) {
            // Logging bei unerwartetem Verbindungsabbruch (z.B. Timeout, Client-Crash).
            System.err.println("Verbindung zu " + (username != null ? username : "Unbekannt") + " unterbrochen.");
        } finally {
            // Aufräumen (Teardown-Phase). Wird immer ausgeführt, egal ob Absturz oder normaler Logout.

            // 1. Entfernen aus der globalen Server-Liste.
            ChatServer.removeClient(this);

            // 2. Sicherstellen, dass der Socket geschlossen ist.
            try {
                socket.close();
            } catch (IOException ignored) {}

            // 3. Status-Update Broadcast: Informiert alle verbleibenen Clients über den Abgang.
            sendUpdatedUserlistToAll();
            System.out.println("Client aufgeräumt: " + (username != null ? username : "Unbekannt"));
        }
    }

    // --- API für Commands (Callback-Methoden) ---

    // Sendet einen rohen JSON-String an den Client.
    // Thread-Safe durch die interne Synchronisation des PrintWriters (teils) bzw. Socket-Streams.
    public void send(String jsonMessage) {
        if (out != null) out.println(jsonMessage);
    }

    // Konvertiert ein Message-Objekt in JSON und sendet es.
    public void sendMessageObject(Message msg) {
        if (out != null) out.println(gson.toJson(msg));
    }

    // Sendet eine standardisierte Fehlernachricht
    public void sendError(String text) {
        Message error = new Message("error", "server", username, text);
        send(gson.toJson(error));
    }

    // Setter für den LoginCommand: Identitäts-Management.
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Triggered ein Update der Benutzeroberfläche aller verbundenen Clients.
     * Aggregiert alle User aus der Datenbank und die aktuell eingeloggten User.
     * Format: "UserA,UserB||UserA" (Alle||Online)
     */
    public void sendUpdatedUserlistToAll() {
        List<String> allUsers = chatService.listAllUsers();
        List<String> onlineUsers = ChatServer.getOnlineUsernames();

        String all = String.join(",", allUsers);
        String online = String.join(",", onlineUsers);

        Message userlistMessage = new Message("userlist", "server", null, all + "||" + online);

        // Iteriert über alle aktiven Verbindungen und sendet das Update.
        for (ClientHandler clientHandler : ChatServer.getConnectedClients()) {
            clientHandler.send(gson.toJson(userlistMessage));
        }
    }
}