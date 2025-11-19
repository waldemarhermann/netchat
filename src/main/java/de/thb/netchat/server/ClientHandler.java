package de.thb.netchat.server;

import de.thb.netchat.service.ChatService;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatService chatService;
    private PrintWriter out;
    private String username;


    public ClientHandler(Socket socket, ChatService chatService) {
        this.socket = socket;
        this.chatService = chatService;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = writer;

            out.println("Willkommen bei NetChat!");
            out.println("Gib einen Befehl ein: ");

            String input;

            while ((input = in.readLine()) != null) {

                if (input.startsWith("REGISTER")) {
                    // REGISTER name email pass
                    String[] parts = input.split(" ");
                    username = parts[1];

                    chatService.createUser(parts[1], parts[2], parts[3]);
                    ChatServer.registerClient(this);

                    out.println("Benutzer registriert!");

                } else if (input.startsWith("SEND")){
                    String[] parts = input.split(" ", 4);

                    String senderName = (parts[1]);
                    String receiverName = parts[2];
                    String text = parts[3];

                    chatService.sendMessage(senderName, receiverName, text);
                    ChatServer.sendToUser(receiverName, senderName + ": " + text);
                    out.println("Nachricht gesendet!");

                } else if (input.equals("LIST_USERS")) {
                    chatService.listUser();
                    out.println("Benutzerliste wurden angegeben!");

                } else if (input.startsWith("HISTORY")) {
                    int uId = Integer.parseInt(input.split(" ")[1]);
                    chatService.showMessagesByUser(uId);
                    out.println("Nachrichten wurden in der Server-Konsole ausgegeben");

                } else if (input.equals("EXIT")) {
                    out.println("Verbindung wird beendet...");
                    ChatServer.removeClient(this);
                    break;

                } else {
                    out.println("Unbekannter Befehl.");
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public void send(String message) {
        out.println(message);
    }
}
