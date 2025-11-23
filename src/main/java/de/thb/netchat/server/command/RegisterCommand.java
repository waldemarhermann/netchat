package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;

public class RegisterCommand implements Command {
    private final Gson gson = new Gson();

    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {
        String username = msg.getFrom();
        String[] parts = msg.getText().split("\\|\\|");
        String email = parts[0];
        String password = parts[1];

        if (service.userExists(username)) {
            client.sendError("Benutzername bereits vergeben.");
            return;
        }
        if (service.emailExists(email)) {
            client.sendError("E-Mail wird bereits verwendet.");
            return;
        }

        service.createUser(username, email, password);

        Message ok = new Message("info", "server", username, "Registrierung erfolgreich!");
        client.send(gson.toJson(ok));
    }
}