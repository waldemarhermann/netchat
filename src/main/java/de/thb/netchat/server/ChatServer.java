package de.thb.netchat.server;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
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

    public static synchronized void sendToUser(String receiverName, Message message) {
        for (ClientHandler client : connectedClients) {
            if (receiverName.equals(client.getUsername())) {
                client.sendMessageObject(message);
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

    public static synchronized List<String> getOnlineUsernames() {
        List<String> names = new ArrayList<>();
        for (ClientHandler c : connectedClients) {
            if (c.getUsername() != null) {
                names.add(c.getUsername());
            }
        }
        return names;
    }

    public static synchronized List<ClientHandler> getConnectedClients() {
        return connectedClients;
    }

    public static boolean isUserOnline(String username) {
        for (ClientHandler ch : connectedClients) {
            if (ch.getUsername() != null && ch.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }




    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.startServer(9999);
    }
}


