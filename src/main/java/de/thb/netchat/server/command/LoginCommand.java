package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ChatServer;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;

public class LoginCommand implements Command {
    private final Gson gson = new Gson();

    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {
        String username = msg.getFrom();
        String password = msg.getText();

        String storedPassword = service.getPasswordForUser(username);

        if (storedPassword == null) {
            client.sendError("Benutzer existiert nicht.");
            return;
        }

        if (!storedPassword.equals(password)) {
            client.sendError("Falsches Passwort.");
            return;
        }

        if (ChatServer.isUserOnline(username)) {
            client.sendError("Dieser Benutzer ist bereits angemeldet.");
            return;
        }

        // Erfolg: Username im ClientHandler setzen
        client.setUsername(username);
        ChatServer.registerClient(client);

        Message ok = new Message("info", "server", username, "Login erfolgreich!");
        client.send(gson.toJson(ok));

        // Liste aktualisieren
        client.sendUpdatedUserlistToAll();
    }
}