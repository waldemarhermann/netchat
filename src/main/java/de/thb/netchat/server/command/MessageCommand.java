package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ChatServer;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;

public class MessageCommand implements Command {
    private final Gson gson = new Gson();

    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {
        // Speichern
        service.sendMessage(msg.getFrom(), msg.getTo(), msg.getText());

        // Weiterleiten (Server sendet das Objekt weiter, Timestamp/Formatierung ist egal hier)
        ChatServer.sendToUser(msg.getTo(), msg);

        // Bestätigung an Absender
        Message confirm = new Message("info", "server", msg.getFrom(), "Nachricht gesendet");
        client.send(gson.toJson(confirm));
    }
}