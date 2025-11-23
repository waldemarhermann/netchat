package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode; // WICHTIG: Import für die Enter-Taste

import java.util.ArrayList;
import java.util.List;

public class ChatController {

    @FXML private ListView<String> messagesList;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private ListView<String> userList;
    @FXML private Label chatTitle;

    private ClientConnection connection;
    private String username;
    private String selectedReceiver = null;

    public void init(ClientConnection connection, String username) {
        this.connection = connection;
        this.username = username;

        // 1. Listener starten
        ClientListener listener = new ClientListener(connection.getSocket(), this::onMessageReceived);
        new Thread(listener).start();

        // 2. CellFactory (Design der Liste)
        messagesList.setCellFactory(list -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);

                // Eigene Nachricht
                if (item.startsWith(username + ": ")) {
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-background-color: #D0E8FF; -fx-padding: 5px;");
                }
                // Info / Error
                else if (item.startsWith("[INFO]") || item.startsWith("[ERROR]")) {
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #555; -fx-font-style: italic;");
                }
                // Nachricht von anderen
                else if (item.contains(": ")) {
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-background-color: #EFEFEF; -fx-padding: 5px;");
                }
                else {
                    setStyle("");
                }
            }
        });

        // 3. Button-Klick Aktion
        sendButton.setOnAction(e -> sendMessage());

        // 4. ENTER-Taste im Textfeld (HIER IST DER NEUE CODE)
        // -------------------------------------------------------
        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });
        // -------------------------------------------------------

        // 5. Klick auf Userliste (Doppelklick)
        userList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String rawUser = userList.getSelectionModel().getSelectedItem();
                if (rawUser != null) {
                    // " (on)" / " (off)" entfernen für den reinen Namen
                    String cleanUser = rawUser.replace(" (on)", "").replace(" (off)", "").trim();

                    if (!cleanUser.equals(username)) {
                        selectedReceiver = cleanUser;
                        chatTitle.setText("Chat mit " + selectedReceiver);
                        messagesList.getItems().clear();
                        messagesList.getItems().add("[INFO] Lade Chatverlauf...");

                        // History laden
                        Message req = new Message("history_request", username, selectedReceiver, null);
                        connection.send(req);

                        messageField.requestFocus();
                    }
                }
            }
        });
    }

    private void sendMessage() {
        String text = messageField.getText();
        if (text.isEmpty()) return;

        if (selectedReceiver == null) {
            messagesList.getItems().add("[INFO] Bitte einen Empfänger auswählen.");
            return;
        }

        // An Server senden
        Message msg = new Message("message", username, selectedReceiver, text);
        connection.send(msg);

        // Lokal anzeigen
        messagesList.getItems().add(username + ": " + text);
        messagesList.scrollTo(messagesList.getItems().size() - 1);

        messageField.clear();
    }

    private void onMessageReceived(Message msg) {
        if (msg == null) return;

        Platform.runLater(() -> {
            switch (msg.getType()) {
                case "message":
                    if (selectedReceiver != null && msg.getFrom().equals(selectedReceiver)) {
                        messagesList.getItems().add(msg.getFrom() + ": " + msg.getText());
                        messagesList.scrollTo(messagesList.getItems().size() - 1);
                    }
                    break;

                case "userlist":
                    updateUserList(msg.getText());
                    break;

                case "info":
                    if (!"Nachricht gesendet".equals(msg.getText())) {
                        messagesList.getItems().add("[INFO] " + msg.getText());
                    }
                    break;

                case "history_response":
                    messagesList.getItems().clear();
                    String data = msg.getText();
                    if (data != null && !data.isEmpty()) {
                        String[] msgs = data.split("\\|\\|");
                        for (String m : msgs) {
                            messagesList.getItems().add(m);
                        }
                    } else {
                        messagesList.getItems().add("[INFO] Chat gestartet.");
                    }
                    messagesList.scrollTo(messagesList.getItems().size() - 1);
                    break;

                case "error":
                    messagesList.getItems().add("[ERROR] " + msg.getText());
                    break;
            }
        });
    }

    private void updateUserList(String payload) {
        if (payload == null) return;
        String[] parts = payload.split("\\|\\|");
        String[] allUsers = parts.length > 0 ? parts[0].split(",") : new String[0];
        String[] onlineUsers = parts.length > 1 ? parts[1].split(",") : new String[0];

        List<String> finalList = new ArrayList<>();
        for (String u : allUsers) {
            u = u.trim();
            if (u.isEmpty() || u.equals(username)) continue;

            boolean isOnline = false;
            for (String on : onlineUsers) if (on.trim().equals(u)) isOnline = true;

            finalList.add(u + (isOnline ? " (on)" : " (off)"));
        }

        if (!userList.getItems().equals(finalList)) {
            userList.getItems().setAll(finalList);
        }
    }
}