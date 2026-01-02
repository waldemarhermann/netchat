package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ChatServer;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;
import de.thb.netchat.util.SecurityUtil;

/**
 * Steuert den Anmeldevorgang. Diese Klasse überprüft die Identität des Benutzers
 * und überführt die Verbindung in einen aktiven Zustand.
 */
public class LoginCommand implements Command {

    private final Gson gson = new Gson();

    /**
     * Führt die Anmeldung aus.
     * Ablauf: DB-Check -> Hash-Vergleich -> Status-Update -> Broadcast.
     *
     * @param msg Nachricht mit Username und Passwort.
     * @param client Der Handler der aktuellen Verbindung.
     * @param service Zugriff auf die Benutzerdatenbank.
     */
    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {
        String username = msg.getFrom();
        String inputPassword = msg.getText(); // Das Passwort, das der User eingetippt hat

        // 1. Datenabruf: Gespeicherten Hash aus der Datenbank laden.
        String storedHash = service.getPasswordForUser(username);

        // Validierung: Wenn kein Hash zurückkommt, ist der Benutzername unbekannt.
        if (storedHash == null) {
            client.sendError("Benutzer existiert nicht.");
            return;
        }

        // 2. Sicherheit: Hashing der Eingabe.
        // Das eingegebene Passwort wird mit demselben Algo gehasht wie in der DB.
        // Es findet kein Vergleich von Klartext-Passwörtern statt.
        String inputHash = SecurityUtil.hashPassword(inputPassword);

        // 3. Authentifizierung: Vergleich der Hashes.
        if (!storedHash.equals(inputHash)) {
            client.sendError("Falsches Passwort.");
            return;
        }

        // 4. Zugriff auf ChatServer, um zu prüfen, ob dieser Nutzer bereits eine aktive Sitzung hat.
        if (ChatServer.isUserOnline(username)) {
            client.sendError("Dieser Benutzer ist bereits angemeldet.");
            return;
        }

        // --- AB HIER: LOGIN ERFOLGREICH ---

        // Dem Thread (ClientHandler) wird eine Identität zugewiesen.
        client.setUsername(username);

        // Registrierung: Der Handler wird in die globale Liste der aktiven Clients aufgenommen.
        // Ermöglicht nun den Empfang der Nachrichten.
        ChatServer.registerClient(client);

        // Bestätigung: Senden des Erfolg-Status an den anfragenden Client.
        Message ok = new Message("info", "server", username, "Login erfolgreich!");
        client.send(gson.toJson(ok));

        // Broadcast: Aktualisierung der Online-Listen.
        // Alle anderen verbundenen Clients werden informiert, dass ein neuer User da ist.
        client.sendUpdatedUserlistToAll();
    }
}