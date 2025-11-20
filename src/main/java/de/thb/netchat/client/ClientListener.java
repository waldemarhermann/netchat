package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientListener implements Runnable {

    private final Socket socket;
    private final Gson gson = new Gson();

    public ClientListener(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))
        ) {
            String line;

            while ((line = in.readLine()) != null) {
                Message msg = gson.fromJson(line, Message.class);

                if (msg == null || msg.getType() == null) {
                    System.out.println("[SERVER SEND ERROR] Ungültiges JSON: " + line);
                    continue;
                }

                switch (msg.getType()) {

                    case "info" -> {
                        System.out.println("[INFO] " + msg.getText());
                    }

                    case "message" -> {
                        System.out.println("[" + msg.getFrom() + " --> " + msg.getTo() + "] "
                                + msg.getText());
                    }

                    case "error" -> {
                        System.out.println("[SERVER ERROR] " + msg.getText());
                    }

                    default -> {
                        System.out.println("[UNKNOWN TYPE] " + line);
                    }
                }
            }

            System.out.println("Verbindung zum Server wurde geschlossen.");

        } catch (Exception e) {
            System.out.println("Listener beendet: " + e.getMessage());
        }
    }
}
