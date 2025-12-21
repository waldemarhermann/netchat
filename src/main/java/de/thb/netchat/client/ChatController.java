package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode; // WICHTIG: Import für die Enter-Taste

import java.util.ArrayList;
import java.util.List;

// Controller für das Chat-Fenster. Manager der grafischen Oberfläche (GUI).
// Er läuft ausschließlich im JavaFX Application Thread, außer dort wo der ClientListener eingreift.
public class ChatController {

    // FXML Referenzen
    // Diese Variablen sind direkt mit den sichtbaren Elementen auf dem Bildschirm verknüpft.
    // ListView ist keine einfache Liste, sondern ein komplexes GUI-Bauteil.
    // Jede Änderung hier (wie z.B. getItems().add...) führt sofort zu Grafik-Berechnungen.
    @FXML private ListView<String> messagesList;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private ListView<String> userList;
    @FXML private Label chatTitle;

    // Verbindung zum Server.
    private ClientConnection connection;

    // Benutzername
    private String username;

    // Chatpartner, wird gesetzt sobald man den User aus der Userliste clickt.
    private String selectedReceiver = null;

    // Initialisierungsmethode: Wird vom LoginController manuell aufgerufen, um die Verbindung zu übergeben.
    public void init(ClientConnection connection, String username) {
        this.connection = connection;
        this.username = username;

        // Listener starten. Es wird das Socket und this::onMessageReceived übergeben. Das ist der Callback (Consumer).
        // Wenn der Listener Daten hat, für diese Methode aus.
        ClientListener listener = new ClientListener(connection.getSocket(), this::onMessageReceived);

        // Neuer Thread wird gestartet. Muss unbedingt parallel passieren, damit die GUI nicht einfriert, während der
        // Listener auf Nachrichten wartet (blockierendes Lesen).
        new Thread(listener).start();

        // Aussehen der ListView wird definiert (CellFactory).
        // ListView erhält Anweisung, wie sie Daten malen soll (Farben, Ausrichtung).
        // Der ListView wird kein fester Inhalt übergeben, sondern eine Art Bauplan, Factory.
        // Der Lambda-Ausdruch (list -> ...) nimmt die ListView entgegen und gibt eine neue Zelle zurück.
        // JavaFX nutzt diesen Bauplan, um nur so viele Zellen zu erzeugen wie auf den Bildschirm passen.

        messagesList.setCellFactory(list -> new ListCell<String>() {


            /**
             * Methode wird von JavaFX automatisch aufgerufen. Dies passiert ständig: Beim Start, bei neuen Nachrichten, beim Scrollen.
             * @param item Der Text der Nachricht (z.B. "Waldemar: Hallo"), kommt aus der internen Datenliste
             * @param empty true, wenn die Zeile gerade keine Daten hat (z.B. ganz unten im leeren Bereich).
             */
            @Override
            protected void updateItem(String item, boolean empty) {
                // Aufruf der Basis-Implementierung.
                // Dies ist zwingend erforderlich, um die korrekte interne Zustandsverwaltung durch das FW sicherzustellen.
                super.updateItem(item, empty); // --> Standard-Verhalten beibehalten.

                // Wenn Zeile Leer ist (keine Nachricht), nichts anzeigen.
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                // Text setzen.
                setText(item);

                // CSS-Styling je nach Art der Nachricht.
                // Eigene Nachrichten: Rechtsbündig, blau
                if (item.startsWith(username + ": ")) {
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-background-color: #D0E8FF; -fx-padding: 5px;");
                }
                // Info / Error: Mittig, Grau, kursiv
                else if (item.startsWith("[INFO]") || item.startsWith("[ERROR]")) {
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #555; -fx-font-style: italic;");
                }
                // Nachricht von anderen: Linksbündig, hellgrau
                else if (item.contains(": ")) {
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-background-color: #EFEFEF; -fx-padding: 5px;");
                }
                // Fallback (Normal)
                else {
                    setStyle("");
                }
            }
        });

        /**
         * Event-Handler: Senden per Button.
         * Registrierung eines Listeners für das ActionEvent des Buttons.
         * Lambda-Ausdruck leitet die Ausführung direkt an die sendMessage()-Methode weiter.
         */
        sendButton.setOnAction(event -> sendMessage());

        // Event-Handler: Sender per Enter-Taste
        messageField.setOnKeyPressed(event -> {
            // Nur beim Drücken der Enter-Taste wird die Aktion ausgeführt.
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // Interaktion Userliste
        // Handler für Maus-Ereignisse innerhalb der ListView.
        userList.setOnMouseClicked(event -> {

            // 1 Click: Selektion, 2 Click: Interaktion.
            if (event.getClickCount() == 2) {

                // Datenabruf über das SelectionModel. Zugriff auf das ausgewählte Item der Liste (z.B. "Waldemar (on)").
                String rawUser = userList.getSelectionModel().getSelectedItem();

                // Null-Safety Check
                // Verhindert Exceptions, falls in leeren Bereich der Liste geclickt wird.
                if (rawUser != null) {

                    // Daten-Normalisierung. Statusanzeige (on/off) wird entfernt, um den Username für die Backend-Logik zu extrahieren.
                    // .trim() entfernt potenitelle Whitespaces.
                    String cleanUser = rawUser.replace(" (on)", "").replace(" (off)", "").trim();

                    // Validierung, Plausibilitätsprüfung: Ein Chat mit eigenem User ist nicht zulässig.
                    if (!cleanUser.equals(username)) {
                        // Setzen des aktuellen Chatpartners.
                        selectedReceiver = cleanUser;
                        // Fenstertitel wird aktualisiert.
                        chatTitle.setText("Chat mit " + selectedReceiver);
                        // View-Reset: Lokale Nachrichtenliste wird bereinigt.
                        messagesList.getItems().clear();
                        /* messagesList.getItems().add("[INFO] Lade Chatverlauf..."); */

                        // Erstellung und Versand eines history_request-Objekts an den Sever.
                        // Die Antwort erfolgt asynchron und wird später im CLientListener (onMeesageReceived) bearbeitet.
                        Message req = new Message("history_request", username, selectedReceiver, null);
                        connection.send(req);
                        // Setzt den Fokus in das Textfeld.
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
        // Sicherheitscheck: Leere Nachrichten werden ignoriert.
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