package de.thb.netchat.server;

import de.thb.netchat.service.ChatService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final ChatService chatService = new ChatService();

    private static final List<ClientHandler> connectedClients = new ArrayList<>();


    public static synchronized void registerClient(ClientHandler handler) {
        connectedClients.add(handler);
    }

    public static synchronized void removeClient(ClientHandler handler) {
        connectedClients.remove(handler);
    }

    public static synchronized void sendToUser(String receiverName, String message) {
        for (ClientHandler client : connectedClients) {
            if (receiverName.equals(client.getUsername())) {
                client.send(message);
            }
        }
    }

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


