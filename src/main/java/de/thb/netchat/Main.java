package de.thb.netchat;

import de.thb.netchat.repository.DBInitializer;
import de.thb.netchat.server.ChatServer;
import de.thb.netchat.service.ChatService;


public class Main {
    public static void main(String[] args) {
        DBInitializer.resetDB();
        DBInitializer.initialize();
        System.out.println("NetChat läuft!");

        ChatService chatService = new ChatService();
        chatService.createUser("Waldemar", "w@mail.de", "1234");
        chatService.listUser();
        chatService.sendMessage(1, "Wowa", "Hallo, ist die Nachricht angekommen?");
        chatService.showMessagesByUser(1);
    }
}
