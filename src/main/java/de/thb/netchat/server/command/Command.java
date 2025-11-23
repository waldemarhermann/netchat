package de.thb.netchat.server.command;

import de.thb.netchat.model.Message;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;

public interface Command {
    /**
     * Führt einen Befehl aus.
     * @param msg Die empfangene Nachricht (Daten)
     * @param client Der ClientHandler (um zu antworten oder Usernamen zu setzen)
     * @param service Der ChatService (für DB-Zugriffe)
     */
    void execute(Message msg, ClientHandler client, ChatService service);
}