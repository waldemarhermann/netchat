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

    public List<String> getConversation(String userA, String userB) {
        return messageRepo.getConversation(userA, userB);
    }

    public boolean userExists(String username) {
        return userRepo.userExists(username);
    }

    public boolean emailExists(String email) {
        return userRepo.emailExists(email);
    }



    public void listUser() {
        List<String> users = userRepo.getAllUserNames();
        System.out.println("--- Benuterübersicht ---");
        for (String name : users) {
            System.out.println("**" + name + "** ");
        }
    }

    public List<String> listAllUsers() {
        return userRepo.getAllUserNames();
    }


    public void showMessagesByUser(String senderName) {
        List<String> messages = messageRepo.getMessagesByUser(senderName);
            if (messages.isEmpty()) {
                System.out.println("Keine Nachrichten gefunden.");
            } else {
                System.out.println("--- Nachrichten von Benutzer ---" + senderName + " ---");
                for (String msg : messages) {
                    System.out.println(msg);
                }
            }
    }

    public String getPasswordForUser(String username) {
        return userRepo.getPassword(username);
    }


}
