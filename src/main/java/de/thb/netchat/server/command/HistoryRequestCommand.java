package de.thb.netchat.server.command;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.ClientHandler;
import de.thb.netchat.service.ChatService;
import java.util.List;

public class HistoryRequestCommand implements Command {
    private final Gson gson = new Gson();

    @Override
    public void execute(Message msg, ClientHandler client, ChatService service) {
        String userA = msg.getFrom();
        String userB = msg.getTo();

        List<String> msgs = service.getConversation(userA, userB);

        Message response = new Message(
                "history_response",
                "server",
                userA,
                String.join("||", msgs)
        );
        client.send(gson.toJson(response));
    }
}