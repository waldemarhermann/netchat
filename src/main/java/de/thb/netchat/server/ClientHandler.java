package de.thb.netchat.server;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import de.thb.netchat.server.command.*; // Importiert unsere neuen Commands
import de.thb.netchat.service.ChatService;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatService chatService;
    private final Gson gson = new Gson();
    private PrintWriter out;
    private String username;

    // DIE STRATEGIE-MAP
    private final Map<String, Command> commands = new HashMap<>();

    public ClientHandler(Socket socket, ChatService chatService) {
        this.socket = socket;
        this.chatService = chatService;

        // Hier registrieren wir die Strategien (Commands)
        // Wenn du einen neuen Befehl hast, einfach hier eine Zeile hinzufügen!
        commands.put("register", new RegisterCommand());
        commands.put("login", new LoginCommand());
        commands.put("message", new MessageCommand());
        // history_request ist der wichtige, "history" (console) brauchen wir meist gar nicht mehr
        commands.put("history_request", new HistoryRequestCommand());
        commands.put("exit", new ExitCommand());
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = writer;

            // Willkommensnachricht
            Message welcome = new Message("info", "server", null, "Willkommen bei NetChat!");
            out.println(gson.toJson(welcome));

            String input;
            while ((input = in.readLine()) != null) {
                Message message = gson.fromJson(input, Message.class);

                if (message == null || message.getType() == null) {
                    sendError("Ungültiges JSON-Format");
                    continue;
                }

                // --- STRATEGY PATTERN IN ACTION ---
                // 1. Befehl aus der Map suchen
                Command cmd = commands.get(message.getType());

                if (cmd != null) {
                    // 2. Ausführen
                    cmd.execute(message, this, chatService);
                } else {
                    sendError("Unbekannter Befehl: " + message.getType());
                }
                // ----------------------------------
            }

        } catch (Exception e) {
            System.err.println("Verbindung zu " + (username != null ? username : "Unbekannt") + " unterbrochen.");
        } finally {
            // Aufräumen (Egal ob Exit oder Absturz)
            ChatServer.removeClient(this);
            try {
                socket.close();
            } catch (IOException ignored) {}

            // Liste aktualisieren
            sendUpdatedUserlistToAll();
            System.out.println("Client aufgeräumt: " + (username != null ? username : "Unbekannt"));
        }
    }

    // --- Öffentliche Methoden, die von den Commands genutzt werden ---

    public void send(String jsonMessage) {
        if (out != null) out.println(jsonMessage);
    }

    public void sendMessageObject(Message msg) {
        if (out != null) out.println(gson.toJson(msg));
    }

    public void sendError(String text) {
        Message error = new Message("error", "server", username, text);
        send(gson.toJson(error));
    }

    // Setter für den LoginCommand
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    // Diese Methode muss jetzt public (oder package-private) sein, damit Commands sie nutzen können
    public void sendUpdatedUserlistToAll() {
        List<String> allUsers = chatService.listAllUsers();
        List<String> onlineUsers = ChatServer.getOnlineUsernames();

        String all = String.join(",", allUsers);
        String online = String.join(",", onlineUsers);

        Message userlistMessage = new Message("userlist", "server", null, all + "||" + online);

        for (ClientHandler ch : ChatServer.getConnectedClients()) {
            ch.send(gson.toJson(userlistMessage));
        }
    }
}