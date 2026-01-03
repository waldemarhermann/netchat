package de.thb.netchat.server.command;

import de.thb.netchat.model.Message;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;

/**
 * Definiert die abstrakte Schnittstelle für alle ausführbaren Server-Operationen.
 * 1. Entkoppelung: ClientHandler (Aufrufer) muss nicht wissen, welche Logik aufgeführt wird,
 * sondern nur, dass eine Methode execute() existiert.
 * 2. Erweiterbarkeit: Neue Befehle können hinzugefügt werden, ohne bestehende Klassen zu ändern.
 */
public interface Command {
    /**
     * Führt einen Befehl aus.
     * @param msg Daten-Transfer-Objekt(DTO). Enthält Payload - was soll getan werden?
     * @param client Kontext der aktuellen Sitzung - wer führt es aus?
     *               Wird benötigt, um Antworten direkt an diesen Socket zurückzusenden oder den Sitzungsstatus (z.B. Username) zu ändern.
     * @param service Die Verbindung zur Datenhaltung- womit wird gearbeitet?
     *                Ermöglicht den Zugriff auf die Datenbank (Speichern, Lesen, Validieren).
     */
    void execute(Message msg, ClientHandler client, ChatService service);
}