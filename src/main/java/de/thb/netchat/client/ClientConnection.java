package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection {

    private final String host;
    private final int port;

    private Socket socket;
    private PrintWriter out;
    private final Gson gson = new Gson();

    public ClientConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Verbindung herstellen
    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    // JSON senden
    public void send(Message message) {
        if (out != null) {
            String json = gson.toJson(message);
            out.println(json);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    // Verbindung schließen
    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

}
