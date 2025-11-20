package de.thb.netchat.server;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.service.ChatService;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatService chatService;
    private final Gson gson = new Gson();

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

            Message welcome = new Message(
                    "info",
                    "server",
                    null,
                    "Willkommen bei NetChat! Bitte sende JSON-Befehle.",
                    String.valueOf(System.currentTimeMillis())
            );
            out.println(gson.toJson(welcome));

            String input;

            while ((input = in.readLine()) != null) {

                Message message = gson.fromJson(input, Message.class);

                if (message == null || message.getType() == null) {
                    sendError("Ungültiges JSON-Format");
                    continue;
                }

                switch (message.getType()) {

                    //Registrierung
                    case "register" -> handleRegister(message);

                    // Nachricht senden
                    case "message" -> handleMessage(message);

                    // Verlauf anzeigen
                    case "history" -> handleHistory(message);

                    // Client beendet
                    case "exit" -> {
                        handleExit(message);
                        return;
                    }

                    default -> sendError("Unbekannter Nachrichtentyp: " + message.getType());
                }
            }

        } catch (Exception e) {
            System.err.println("Fehler im ClientHandler:");
            e.printStackTrace();
        } finally {
            ChatServer.removeClient(this);
            try {
                socket.close();
            } catch (IOException ignored) {

            }
        }
    }

    // Handler

    private void handleRegister(Message message) {
        username = message.getFrom(); // Name speichern
        chatService.createUser(message.getFrom(), message.getText(), "pass");
        ChatServer.registerClient(this);

        Message response = new Message(
                "info",
                "server",
                username,
                "Registrierung erfolgreich!",
                String.valueOf(System.currentTimeMillis())
        );
        out.println(gson.toJson(response));
    }

    private void handleMessage(Message message) {
        // speichern
        chatService.sendMessage(message.getFrom(), message.getTo(), message.getText());
        // Weiterleiten an Empfänger
        ChatServer.sendToUser(message.getTo(), gson.toJson(message));

        // Bestätigung für Absender
        Message confirm = new Message(
                "info",
                "server",
                message.getFrom(),
                "Nachricht gesendet",
                String.valueOf(System.currentTimeMillis())
        );
        out.println(gson.toJson(confirm));
    }

    private void handleHistory(Message message) {
        // History ausgeben
        chatService.showMessagesByUser(message.getFrom());

        Message info = new Message(
                "info",
                "server",
                message.getFrom(),
                "Historie wurde auf dem Server ausgegeben.",
                String.valueOf(System.currentTimeMillis())
        );
        out.println(gson.toJson(info));
    }

    private void handleExit(Message message) {
        ChatServer.removeClient(this);

        Message exit = new Message(
                "info",
                "server",
                message.getFrom(),
                "Verbindung wird beendet.",
                String.valueOf(System.currentTimeMillis())
        );
        out.println(gson.toJson(exit));
    }

    // Hilfsmethoden

    public String getUsername() {
        return username;
    }

    public void send(String jsonMessage) {
        out.println(jsonMessage);
    }

    private void sendError(String text) {
        Message error = new Message(
                "error",
                "server",
                username,
                text,
                String.valueOf(System.currentTimeMillis())
        );
        out.println(gson.toJson(error));
    }
}
