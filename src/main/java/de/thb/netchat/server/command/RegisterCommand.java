package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;
import de.thb.netchat.util.SecurityUtil;

public class RegisterCommand implements Command {
    private final Gson gson = new Gson();

    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {
        String username = msg.getFrom();

        // Textformat war: "email||passwort"
        String[] parts = msg.getText().split("\\|\\|");

        // Sicherheitscheck: Wurde alles übertragen?
        if (parts.length < 2) {
            client.sendError("Fehlerhafte Daten übertragen.");
            return;
        }

        String email = parts[0];
        String plainPassword = parts[1];

        // 1. Prüfen ob User/Email schon existiert
        if (service.userExists(username)) {
            client.sendError("Benutzername bereits vergeben.");
            return;
        }
        if (service.emailExists(email)) {
            client.sendError("E-Mail wird bereits verwendet.");
            return;
        }

        // 2. Passwort hashen (Sicherheit!)
        String hashedPassword = SecurityUtil.hashPassword(plainPassword);

        // 3. User mit Hash in der Datenbank anlegen
        service.createUser(username, email, hashedPassword);

        // 4. Erfolg melden
        Message ok = new Message("info", "server", username, "Registrierung erfolgreich!");
        client.send(gson.toJson(ok));
    }
}