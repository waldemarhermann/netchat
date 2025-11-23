package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ChatServer;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;
import de.thb.netchat.util.SecurityUtil;

public class LoginCommand implements Command {
    private final Gson gson = new Gson();

    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {
        String username = msg.getFrom();
        String inputPassword = msg.getText(); // Das Passwort, das der User eingetippt hat

        // 1. Gespeicherten Hash aus der Datenbank holen
        String storedHash = service.getPasswordForUser(username);

        // Gibt es den User überhaupt?
        if (storedHash == null) {
            client.sendError("Benutzer existiert nicht.");
            return;
        }

        // 2. Das eingegebene Passwort hashen
        String inputHash = SecurityUtil.hashPassword(inputPassword);

        // 3. Hashes vergleichen
        if (!storedHash.equals(inputHash)) {
            client.sendError("Falsches Passwort.");
            return;
        }

        // 4. Prüfen ob User schon woanders eingeloggt ist
        if (ChatServer.isUserOnline(username)) {
            client.sendError("Dieser Benutzer ist bereits angemeldet.");
            return;
        }

        // --- LOGIN ERFOLGREICH ---

        // Username im ClientHandler setzen (wichtig für die Zuordnung)
        client.setUsername(username);

        // Client zur globalen Liste hinzufügen
        ChatServer.registerClient(client);

        // Bestätigung an den User
        Message ok = new Message("info", "server", username, "Login erfolgreich!");
        client.send(gson.toJson(ok));

        // Allen anderen Bescheid sagen (Liste aktualisieren)
        client.sendUpdatedUserlistToAll();
    }
}