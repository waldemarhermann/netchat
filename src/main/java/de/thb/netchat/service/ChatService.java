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

    public void sendMessage(String senderName, String receiverName, String text) {
        messageRepo.addMessage(senderName, receiverName, text);
    }

    public void listUser() {
        List<String> users = userRepo.getAllUserNames();
        System.out.println("--- Benuterübersicht ---");
        for (String name : users) {
            System.out.println("**" + name + "** ");
        }
    }

    public void showMessagesByUser(int sender_id) {
        List<String> messages = messageRepo.getMessagesByUser(sender_id);
            if (messages.isEmpty()) {
                System.out.println("Keine Nachrichten gefunden.");
            } else {
                System.out.println("--- Nachrichten von Benutzer ---" + sender_id + " ---");
                for (String msg : messages) {
                    System.out.println(msg);
                }
            }

    }

}
