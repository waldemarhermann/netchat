package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ChatServer;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;

public class ExitCommand implements Command {
    private final Gson gson = new Gson();

    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {
        ChatServer.removeClient(client);

        Message exit = new Message("info", "server", msg.getFrom(), "Verbindung wird beendet.");
        client.send(gson.toJson(exit));

        client.sendUpdatedUserlistToAll();

        // Hinweis: Das eigentliche Schließen des Sockets passiert im finally-Block
        // des ClientHandlers, sobald der Client die Verbindung trennt.
    }
}