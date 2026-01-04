package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;
import java.util.List;

/**
 * Implementiert die Logik zum Abruf historischer Chatverläufe.
 * Diese Klasse fungiert als Schnittstelle zwischen der Client-Anfrage
 * und dem MessageRepo im Service Layer.
 */
public class HistoryRequestCommand implements Command {

    private final Gson gson = new Gson();

    /**
     * Führt die Datenbankabfrage aus und sendet das Ergebnis zurück.
     *
     * @param msg Die Anfrage-Nachricht (enthält im "to"-Feld den gewünschten Chatpartner).
     * @param client Der anfragende ClientHandler.
     * @param service Der Service für den Datenbankzugriff.
     */
    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {
        // Identifikation der Gesprächspartner
        String userA = msg.getFrom(); // Der Anfragende
        String userB = msg.getTo(); // Der Partner

        // 1. Datenabfrage (Query)
        // Der Service holt die Rohdaten (Liste von Strings) aus dem Repository.
        // Die Liste ist chronologisch sortiert.
        List<String> msgs = service.getConversation(userA, userB);

        // 2. Daten-Aggregation
        // Das Message-Objekt erlaubt im "text"-Feld nur einen einzelnen String.
        // Um eine Liste von Nachrichten zu übertragen, werden diese konkateniert.
        // Das Trennzeichen "||" dient als Delimiter, damit der Client den String wieder in Einzelnachrichten zerlegen kann.
        String aggregatedHistory = String.join("||", msgs);

        // 3. Response-Objekt
        // Erstellung einer Antwortnachricht vom Typ "history_response".
        // Der Server fungiert als Absender.
        Message response = new Message(
                "history_response",
                "server",
                userA,
                aggregatedHistory
        );

        // 4. Unicast
        // Antwort wird ausschließlich an den Client gesandt, der die Anfrage gestellt hat (Request-Response-Prinzip).
        client.send(gson.toJson(response));
    }
}