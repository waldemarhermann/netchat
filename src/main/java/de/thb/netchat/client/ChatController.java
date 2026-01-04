package de.thb.netchat.client;

import de.thb.netchat.model.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;

public class ChatController {

    @FXML private ListView<String> messagesList;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private ListView<String> userList;
    @FXML private Label chatTitle;
    @FXML private Label statusLabel;

    private ClientConnection connection;
    private String username;
    private String selectedReceiver = null;

    public void init(ClientConnection connection, String username) {
        this.connection = connection;
        this.username = username;

        // Status anzeigen
        if (statusLabel != null) {
            statusLabel.setText("(verbunden)");
        }
        if (chatTitle != null) {
            chatTitle.setText("NetChat – Chat");
        }

        // 1) Listener starten
        ClientListener listener = new ClientListener(connection.getSocket(), this::onMessageReceived);
        Thread t = new Thread(listener, "ChatListener");
        t.setDaemon(true);
        t.start();

        // Messages: Bubble-Styling
        messagesList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                setText(item);
                setWrapText(true);

                // Baseline: keine Standard-Selection-Paintings
                setStyle("-fx-background-color: transparent;");

                String u = (username == null) ? "" : username;

                // System-Meldungen
                if (item.startsWith("[INFO]") || item.startsWith("[ERROR]")) {
                    setStyle(
                            "-fx-background-color: transparent;" +
                                    "-fx-text-fill: #777;" +
                                    "-fx-font-style: italic;" +
                                    "-fx-padding: 6 10 6 10;" +
                                    "-fx-alignment: CENTER;"
                    );
                }
                // eigene Nachricht
                else if (item.startsWith(u + ":") || item.startsWith("Du:")) {
                    setStyle(
                            "-fx-background-color: #d8efe2;" +
                                    "-fx-background-radius: 12;" +
                                    "-fx-padding: 10 12 10 12;" +
                                    "-fx-alignment: CENTER-LEFT;" +
                                    "-fx-text-fill: #2e2e2e;"
                    );
                }
                // fremde Nachricht
                else {
                    setStyle(
                            "-fx-background-color: #e8f1ff;" +
                                    "-fx-background-radius: 12;" +
                                    "-fx-padding: 10 12 10 12;" +
                                    "-fx-alignment: CENTER-LEFT;" +
                                    "-fx-text-fill: #2e2e2e;"
                    );
                }
            }
        });

        //  Userliste
        userList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);

                // leichte Hervorhebung bei Auswahl
                if (isSelected()) {
                    setStyle(
                            "-fx-background-color: #f0f4ff;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-padding: 6 8 6 8;" +
                                    "-fx-text-fill: #2e2e2e;"
                    );
                } else {
                    setStyle(
                            "-fx-background-color: transparent;" +
                                    "-fx-padding: 6 8 6 8;" +
                                    "-fx-text-fill: #2e2e2e;"
                    );
                }
            }
        });

        // Sende-Button
        sendButton.setOnAction(e -> sendMessage());

        // enter = senden
        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // Doppelklick auf Userliste = Empfänger wählen + History laden
        userList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String rawUser = userList.getSelectionModel().getSelectedItem();
                if (rawUser != null) {
                    String cleanUser = rawUser
                            .replace(" (on)", "")
                            .replace(" (off)", "")
                            .trim();

                    if (!cleanUser.equals(username)) {
                        selectedReceiver = cleanUser;
                        chatTitle.setText("Chat mit " + selectedReceiver);
                        messagesList.getItems().clear();
                        messagesList.getItems().add("[INFO] Lade Chatverlauf...");

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
        if (text == null || text.isBlank()) {
            return;
        }

        if (selectedReceiver == null) {
            messagesList.getItems().add("[INFO] Bitte einen Empfänger auswählen.");
            messageField.clear();
            return;
        }

        // Nachricht an Server senden
        Message msg = new Message("message", username, selectedReceiver, text);
        connection.send(msg);

        // Lokal anzeigen als eigene Nachricht
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
                        messagesList.scrollTo(messagesList.getItems().size() - 1);
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
                    messagesList.scrollTo(messagesList.getItems().size() - 1);
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
            for (String on : onlineUsers) {
                if (on.trim().equals(u)) {
                    isOnline = true;
                    break;
                }
            }

            finalList.add(u + (isOnline ? " (on)" : " (off)"));
        }

        if (!userList.getItems().equals(finalList)) {
            userList.getItems().setAll(finalList);
        }
    }
}

