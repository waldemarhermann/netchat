package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ChatServer;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;

/**
 * Implementiert die Logik für einen kontrollierten Verbindungsabbau.
 * Diese Klasse sorgt dafür, dass ein Benutzer sauber aus dem System entfernt wird,w
 * bevor die physische Verbindung getrennt wird.
 */
public class ExitCommand implements Command {

    private final Gson gson = new Gson();

    /**
     * Führt die Abmelde-Routine aus.
     *
     * @param msg Die Exit-Nachricht vom Client.
     * @param client Der ClientHandler, der beendet werden soll.
     * @param service Wird nicht benötigt, aber wegen Interface-Signatur vorhanden.
     */
    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {

        // 1. Logische Entfernung
        // Der Client wird sofort aus der Liste der aktiven Verbindungen (connectedClients) entfernt.
        // Das verhindert, dass ab diesem Zeitpunkt neue Nachrichten an ihn geroutet werden.
        ChatServer.removeClient(client);

        // 2. Bestätigung (Handshake Finalization)
        // Der Server sendet ein letztes Paket. Diese bestätigt dem Client, dass der Server den Logout-Wunsch verstanden/verarbeitet hat.
        Message exit = new Message("info", "server", msg.getFrom(), "Verbindung wird beendet.");
        client.send(gson.toJson(exit));

        // 3. Broadcast Update
        // Alle verbleibenden Benutzer müssen informiert werden, dass dieser User nun offline ist.
        // Da "removeClient" oben bereits ausgeführt wurde, fehlt der User nun in der generierten Liste - der Status ist also korrekt offline.
        client.sendUpdatedUserlistToAll();

        // Wichtiger Architektur-Hinweis:
        // Hier wird nicht socket.close() aufgerufen.
        // Grund: Der ClientHandler(Thread) muss die Chance haben, seine Schleife sauber zu beenden
        // oder auf das Schließen durch die Client-Seite (FIN-Paket) zu reagieren.
        // Das physische Schließen der Ressourcen erfolgt im "finally-Block" der run()-Methode im ClientHandler.
    }
}