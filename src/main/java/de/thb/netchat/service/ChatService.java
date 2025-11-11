package de.thb.netchat.service;

import de.thb.netchat.repository.MessageRepo;
import de.thb.netchat.repository.UserRepo;

import java.util.List;

public class ChatService {

    private final UserRepo userRepo = new UserRepo();
    private final MessageRepo messageRepo = new MessageRepo();

    public void createUser(String name, String email, String password) {
        userRepo.addUser(name, email, password);
    }

    public void sendMessage(int sender_id, int receiver_id, String text) {
        messageRepo.addMessage(sender_id, receiver_id, text);
    }

    public void listUser() {
        List<String> users = userRepo.getAllUserNames();
        System.out.println("--- Benuterübersicht ---");
        for (String name : users) {
            System.out.println(", " + name);
        }
    }

}
