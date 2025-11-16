package de.thb.netchat.server;

import de.thb.netchat.service.ChatService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    private final ChatService chatService = new ChatService();

    public void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ChatServer läuft auf Port: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Neuer Client erfolgreich verbunden: " + clientSocket.toString());

                new Thread(new ClientHandler(clientSocket, chatService)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.startServer(9999);
    }
}


