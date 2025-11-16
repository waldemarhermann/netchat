package de.thb.netchat.server;

import de.thb.netchat.service.ChatService;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatService chatService;

    public ClientHandler(Socket socket, ChatService chatService) {
        this.socket = socket;
        this.chatService = chatService;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println("Willkommen bei NetChat!");
            out.println("Gib einen Befehl ein: ");

            String input;

            while ((input = in.readLine()) != null) {

                if (input.startsWith("REGISTER")) {
                    // REGISTER name email pass
                    String[] parts = input.split(" ");
                    chatService.createUser(parts[1], parts[2], parts[3]);
                    out.println("Benutzer registriert!");

                } else if (input.startsWith("SEND")){
                    // SEND senderId receiverId text...
                    String[] parts = input.split(" ", 4);
                    int sender = Integer.parseInt(parts[1]);
                    int receiver = Integer.parseInt(parts[2]);
                    chatService.sendMessage(sender, receiver, parts[3]);
                    out.println("Nachricht gespeichert!");

                } else if (input.equals("LIST_USERS")) {
                    chatService.listUser();
                    out.println("Benutzerliste wurden angegeben!");

                } else if (input.startsWith("HISTORY")) {
                    int uId = Integer.parseInt(input.split(" ")[1]);
                    chatService.showMessagesByUser(uId);
                    out.println("Nachrichten wurden in der Server-Konsole ausgegeben");

                } else if (input.equals("EXIT")) {
                    out.println("Verbindung wird beendet...");
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
}
