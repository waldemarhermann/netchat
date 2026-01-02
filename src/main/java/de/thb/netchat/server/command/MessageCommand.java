package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ChatServer;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;

/**
 * Implementiert die Logik für den Nachrichtenaustausch zwischen Benutzern.
 * Nachrichten werden persistent gespeichert und auch in Echtzeit an den Empfänger geschickt.
 */
public class MessageCommand implements Command {

    private final Gson gson = new Gson();

    /**
     * Verarbeitet eine eingehende Chat-Nachricht.
     *
     * @param msg Das Nachrichtenobjekt (enthält Sender, Empfänger, Text).
     * @param client Der ClientHandler des Absenders.
     * @param service Der ChatService zur Speicherung.
     */
    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {

        // 1. Speicherung der Daten
        // Die Nachricht wird über den Service in das MessageRepo geschrieben.
        // Stellt sicher, dass der Chatverlauf auch nach einem Server-Neustart
        // oder für die HistoryRequest-Funktion verfügbar bleibt.
        service.sendMessage(msg.getFrom(), msg.getTo(), msg.getText());

        // 2. Echtzeit-Routing
        // Die Nachricht wird an den ChatServer übergeben, um den aktiven Socket des Empfängers zu finden.
        // Der Server prüft die Liste connectedClients und leitet das Objekt direkt weiter.
        // Falls der Empfänger offline ist, passiert hier nichts, die Nachricht befindet sich aber sicher in Schritt 1.
        ChatServer.sendToUser(msg.getTo(), msg);

        // 3. Bestätigung
        // Der Absender erhält eine technische Bestätigung vom Server. Nachricht wurde also erfolgreich verarbeitet.
        Message confirm = new Message("info", "server", msg.getFrom(), "Nachricht gesendet");
        client.send(gson.toJson(confirm));
    }
}