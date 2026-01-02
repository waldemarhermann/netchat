package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;
import de.thb.netchat.util.SecurityUtil;

/**
 * Konkrete Implementierung des Command-Patterns für den Registrierungsprozess.
 * Diese Klasse kapselt die Logik zur Erstellung eines neuen Benutzerkontos.
 * Sie prüft die Eingabedaten, stellt korrekt Daten sicher und übergibt das Speichern an den Service-Layer.
 * delegiert die Persistierung
 */
public class RegisterCommand implements Command {
    // Instanz zur Serialisierung der Antwort-Objekte in JSON.
    private final Gson gson = new Gson();

    /**
     * Führt die Registrierungslogik aus.
     *
     * @param msg Die empfangene Nachrichtobjekt. Erwartet im Text-Feld das Format "email||password".
     * @param client Der ClientHandler, der den Request initialisiert hat
     * @param service Der ChatService für den Zugriff auf die Geschäftslogik und Datenbank.
     */
    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {
        String username = msg.getFrom();

        // Payload aufteilen
        // Trennzeichen: "||" muss im Regex escaped werden.
        String[] parts = msg.getText().split("\\|\\|");

        // Eingabeprüfung: Sicherstellung, dass sowohl E-Mail als auch Passwort übertragen wurden.
        if (parts.length < 2) {
            client.sendError("Fehlerhafte Daten übertragen.");
            return; // Abbruch der Verarbeitung.
        }

        String email = parts[0];
        String plainPassword = parts[1];

        // Wird geprüft, ob der Benutzername oder die E-Mail bereits im System existieren.
        // Verhindert Constraints-Verletzungen in der Datenbank.
        if (service.userExists(username)) {
            client.sendError("Benutzername bereits vergeben.");
            return;
        }
        if (service.emailExists(email)) {
            client.sendError("E-Mail wird bereits verwendet.");
            return;
        }

        // Sicherheit: Das Passwort wird niemals im Klartext gespeichert.
        String hashedPassword = SecurityUtil.hashPassword(plainPassword);

        // Der neue Benutzer wird über den Service in der Repository/DB angelegt.
        service.createUser(username, email, hashedPassword);

        // Bestätigung: Erfolgsmeldung vom Typ "info" wird generiert, serialisiert und an den Client zurückgesendet.
        Message ok = new Message("info", "server", username, "Registrierung erfolgreich!");
        client.send(gson.toJson(ok));
    }
}