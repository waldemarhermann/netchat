package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientListener implements Runnable {

    private final Socket socket;
    private final Consumer<Message> callback;
    private final Gson gson = new Gson();

    public ClientListener(Socket socket, Consumer<Message> callback) {
        this.socket = socket;
        this.callback = callback;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            String line;
            while ((line = in.readLine()) != null) {

                Message msg = gson.fromJson(line, Message.class);

                if (msg != null) {
                    callback.accept(msg);   // GUI oder Konsole informieren
                }
            }

        } catch (Exception e) {
            System.err.println("ClientListener Fehler:");
            e.printStackTrace();
        }
    }
}
