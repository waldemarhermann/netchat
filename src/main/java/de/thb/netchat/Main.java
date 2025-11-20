package de.thb.netchat;

import de.thb.netchat.repository.DBInitializer;
import de.thb.netchat.server.ChatServer;
import de.thb.netchat.service.ChatService;


public class Main {
    public static void main(String[] args) {
        DBInitializer.resetDB();
        DBInitializer.initialize();
        System.out.println("NetChat läuft!");
    }
}
