package de.thb.netchat.server;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.service.ChatService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Hauptklasse des Servers.
 * Aufgaben:
 * 1. Startet den Server-Socket und wartet auf Verbindungen
 * 2. Verwaltet die zentrale Liste aller aktuell verbundenen Clients
 * 3. Dient als Verteiler (Router) für Nachrichten zwischen Clients.
 */
public class ChatServer {

    // Enthält Geschäftslogik und Datenbankzugriffe.
    // Er wird hier einmal erstellt und alle ClientHandler-Threads weitergereicht.
    private final ChatService chatService = new ChatService();

    // Alle aktiven Handler werden gespeichert.
    // Diese Liste gibt es für die gesamte Anwendung nur ein einziges Mal, unabhängig von den erstellten Objekten.
    // Alle Threads greifen auf diesen Shared State zu.
    private static final List<ClientHandler> connectedClients = new ArrayList<>();

    /**
     * Fügt einen neu eingeloggten Client zur globalen Liste hinzu.
     * synchronized: Da mehrere Threads gleichzeitig versuchen könnten,
     * sich einzuloggen, verhindert dieses Keyword, dass die Liste korrupt wird (Race Condition).
     * Es darf immer nur ein Thread gleichzeitig in diese Methode.
     */
    public static synchronized void registerClient(ClientHandler handler) {
        connectedClients.add(handler);
    }

    /**
     * Entfernt einen Client bei Logout oder Verbindungsabbruch.
     */
    public static synchronized void removeClient(ClientHandler handler) {
        connectedClients.remove(handler);
    }

    /**
     * Das Routing-System: Sendet eine Nachricht gezielt an einen bestimmten User.
     * @param receiverName Der Username des Empfängers.
     * @param message Das Nachrichten-Objekt.
     */
    public static synchronized void sendToUser(String receiverName, Message message) {
        // Iteriert durch alle verbundenen Clients (Threads)
        for (ClientHandler client : connectedClients) {
            // Identifizierung des Ziel-Clients anhand des Benutzernamens.
            if (receiverName.equals(client.getUsername())) {
                client.sendMessageObject(message);
            }
        }
    }

    /**
     * Initialisiert den ServerSocket und startet die Verbindungsschleife.
     * Implementiert das Thread-per-Client Muster.
     *
     * @param port Der TCP-Port, auf dem der Server lauscht.
     */
    public void startServer(int port) {
        // Try-with-resources: Schließt den ServerSocket automatisch bei Programmende/Absturz.
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ChatServer läuft auf Port: " + port);

            // Hauptschleife zur Entgegennahme von Verbindungen.
            while (true) {
                // Blockierender Aufruf: Der Main-Thread pausiert hier, bis ein TCP-Handshake
                // mit einem Client erfolgreich abgeschlossen wird.
                Socket clientSocket = serverSocket.accept();
                System.out.println("Neuer Client erfolgreich verbunden: " + clientSocket.toString());

                // Instanziierung eines neuen Worker-Threads.
                // Der ClientSocket und die Referenz auf den Service werden injiziert.
                // start() initiiert die nebenläufige Ausführung der run()-Methode im ClientHandler.
                new Thread(new ClientHandler(clientSocket, chatService)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aggregiert die Benutzernamen aller aktuell Clients.
     * Dient der Synchronisation der Online-Listen in den Clients.
     *
     * @return Liste der Benutzernamen (String).
     */
    public static synchronized List<String> getOnlineUsernames() {
        List<String> names = new ArrayList<>();
        for (ClientHandler clientHandler : connectedClients) {
            if (clientHandler.getUsername() != null) {
                names.add(clientHandler.getUsername());
            }
        }
        return names;
    }

    /**
     * Liefert Liste der verbundenen Clients. Zugriff erfolgt synchronisiert.
     * @return Liste der ClientHandler-Objekte.
     */
    public static synchronized List<ClientHandler> getConnectedClients() {
        return connectedClients;
    }

    /**
     * Prüfung, ob ein Benutzername bereits eine aktive Sitzung besitzt.
     * Verhindert redundante Logins desselben Accounts.
     *
     * @param username Der zu prüfende Benutzername.
     * @return true, wenn der Nutzer online ist, sonst false.
     */
    public static boolean isUserOnline(String username) {
        for (ClientHandler clientHandler : connectedClients) {
            if (clientHandler.getUsername() != null && clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }


    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.startServer(9999);
    }
}


