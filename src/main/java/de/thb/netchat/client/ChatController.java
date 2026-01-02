package de.thb.netchat.client;

import com.google.gson.Gson;
import de.thb.netchat.model.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode; // WICHTIG: Import für die Enter-Taste

import java.util.ArrayList;
import java.util.List;

/**
 * Controller für das Chat-Fenster. Manager der grafischen Oberfläche (GUI).
 * Er läuft ausschließlich im JavaFX Application Thread, außer dort wo der ClientListener eingreift.
 */
public class ChatController {

    /* FXML Referenzen
       Diese Variablen sind direkt mit den sichtbaren Elementen auf dem Bildschirm verknüpft.
       ListView ist keine einfache Liste, sondern ein komplexes GUI-Bauteil.
       Jede Änderung hier (wie z.B. getItems().add...) führt sofort zu Grafik-Berechnungen. */
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

    /**
     * Initialisierungsmethode: Wird vom LoginController manuell aufgerufen, um die Verbindung zu übergeben.
     *
     * @param connection Aktive Serververbindung
     * @param username Benutzername des eingeloggten Clients
     */
    public void init(ClientConnection connection, String username) {
        this.connection = connection;
        this.username = username;

        // Listener starten. Es wird das Socket und this::onMessageReceived übergeben. Das ist der Callback (Consumer).
        // Wenn der Listener Daten hat, führt diese Methode aus.
        ClientListener listener = new ClientListener(connection.getSocket(), this::onMessageReceived);

        // Neuer Thread wird gestartet. Muss unbedingt parallel passieren, damit die GUI nicht einfriert, während der
        // Listener auf Nachrichten wartet (blockierendes Lesen).
        new Thread(listener).start();


        /*
         * Aussehen der ListView wird definiert (CellFactory).
         * ListView erhält Anweisung, wie sie Daten malen soll (Farben, Ausrichtung).
         * Der ListView wird kein fester Inhalt übergeben, sondern eine Art Bauplan, Factory.
         * Der Lambda-Ausdruch (list -> ...) nimmt die ListView entgegen und gibt eine neue Zelle zurück.
         * JavaFX nutzt diesen Bauplan, um nur so viele Zellen zu erzeugen wie auf den Bildschirm passen.
         */
        messagesList.setCellFactory(list -> new ListCell<String>() {

            /**
             * Methode wird von JavaFX automatisch aufgerufen. Dies passiert ständig: Beim Start, bei neuen Nachrichten, beim Scrollen.
             * @param item Der Text der Nachricht (z.B. "Waldemar: Hallo"), kommt aus der internen Datenliste
             * @param empty true, wenn die Zeile gerade keine Daten hat (z.B. ganz unten im leeren Bereich).
             */
            @Override
            protected void updateItem(String item, boolean empty) {
                /* Aufruf der Basis-Implementierung.
                Dies ist zwingend erforderlich, um die korrekte interne Zustandsverwaltung durch das FW sicherzustellen. */
                super.updateItem(item, empty); // --> Standard-Verhalten beibehalten.

                // Wenn Zeile Leer ist (keine Nachricht), nichts anzeigen.
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                // Text setzen.
                setText(item);

                /* CSS-Styling je nach Art der Nachricht.
                Eigene Nachrichten: Rechtsbündig, blau */
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

    /**
     * Sendet die aktuell eingegebene Nachricht an den Server
     * und zeigt sie sofort lokal im Chat an.
     */
    private void sendMessage() {
        // Eingabe wird validiert. Text aus dem Eingabefeld (TextField) wird geholt.
        String text = messageField.getText();
        // Wenn nichts eingetippt wurde, dann Abbruch.
        if (text.isEmpty()) return;
        // Validierung: Es muss zwingend ein ChatPartner ausgewählt sein.
        if (selectedReceiver == null) {
            messagesList.getItems().add("[INFO] Bitte einen Empfänger auswählen.");
            return;
        }

        // Erstellen des Datenobjekts und Versand an den Server.
        // Der Server kümmert sich um die Speicherung in der Datenbank (MessageRepo).
        Message msg = new Message("message", username, selectedReceiver, text);
        connection.send(msg);

        // Hier wird nicht auf die Datenbank-Bestätigung gewartet, sondern die Nachricht wird sofort
        // lokal angezeigt. Das wirkt für den User schneller.
        messagesList.getItems().add(username + ": " + text);
        // Automatisches Scrollen ans Ende der Liste, damit die neue Nachricht sichtbar ist.
        messagesList.scrollTo(messagesList.getItems().size() - 1);
        // Eingabefeld wird geleert
        messageField.clear();
    }


    /**
     * Callback-Methode (Asynchrone Verarbeitung)
     * Wird vom ClientListener (Hintergrund-Thread) aufgerufen, sobald Daten vom Server eintreffen.
     *
     * @param message Das empfangene Datenpaket (bereits von JSON zu Java konvertiert).
     */
    private void onMessageReceived(Message message) {
        // Sicherheitscheck: Leere Nachrichten werden ignoriert.
        if (message == null) return;

        /**
         * Thread-Wechsel (Context Switch)
         * Hier im Hintergrund-Thread des Listeners. Direkte Änderung an der GUI (messagesList) würde zur Exception führen.
         * Platform.runLater reiht den Code in die Warteschlange des JavaFx-UI-Threads ein.
         */
        Platform.runLater(() -> {
            // Dispatching: Abhängig vom Nachrichtentyp wird entsprechend reagiert.
            switch (message.getType()) {
                // Fall: Reguläre Chat-Nachricht kommt rein.
                case "message":
                    // Filterlogik: Nachricht wird nur angezeigt, wenn sie vom aktuell ausgewählten Chatpartner kommt.
                    if (selectedReceiver != null && message.getFrom().equals(selectedReceiver)) {
                        messagesList.getItems().add(message.getFrom() + ": " + message.getText());
                        // Auto-Scroll zur neuesten Nachricht.
                        messagesList.scrollTo(messagesList.getItems().size() - 1);
                    }
                    break;

                // Fall: Der Server meldet eine Änderung der verbundenen User (Login/Logout).
                // Seitenleiste wird aktualisiert.
                case "userlist":
                    updateUserList(message.getText());
                    break;

                // Fall: Systemnachrichten.
                case "info":
                    // Filter: Nachricht gesendet Nachrichten werden ausgelassen.
                    if (!"Nachricht gesendet".equals(message.getText())) {
                        messagesList.getItems().add("[INFO] " + message.getText());
                    }
                    break;

                // Fall: Die Antwort auf den history-request (Datenbank-Auszug) wird hier angenommen.
                case "history_response":
                    // Ansicht wird bereinigt.
                    messagesList.getItems().clear();
                    String data = message.getText();
                    if (data != null && !data.isEmpty()) {
                        // Deserialisierung: Der Server schickt alle Nachrichten als einen String, getrennt durch ||.
                        // Das wird dann aufgesplittet und für jede Zeile hinzugefügt.
                        String[] messages = data.split("\\|\\|"); // --> Ein Array ["Max: Hi", "Ich: Hallo"]
                        for (String msg : messages) {
                            messagesList.getItems().add(msg); // Jeder Schnipsel wird einzeln in die GUI-Liste geworfen.
                        }
                    // Leere History -> Neuer Chat.
                    } else {
                        messagesList.getItems().add("[INFO] Chat gestartet.");
                    }
                    // Nach dem Laden ganz nach unten scrollen.
                    messagesList.scrollTo(messagesList.getItems().size() - 1);
                    break;
                // Fall: Server meldet Fehler, z.B. User nicht gefunden.
                case "error":
                    messagesList.getItems().add("[ERROR] " + message.getText());
                    break;
            }
        });
    }

    /**
     * Verarbeitet die Benutzerliste vom Server und aktualisiert die Anzeige.
     * Erwartetes Format: "UserA,UserB,UserC||UserA,UserC" (Alle User || Online User).
     *
     * @param payload Der Roh-String vom Server (Protokoll-Format).
     */
    private void updateUserList(String payload) {
        // Validierung.
        if (payload == null) return;

        // Payload-Parsing & Deserialisierung. Zerlegung des Strings anhand des
        // Protokoll-Trenners ||.
        // Index 0: Liste aller registrierten Nutzer.
        // Index 1: Liste der aktuell verbundenen Nutzer.
        String[] parts = payload.split("\\|\\|");
        String[] allUsers = parts.length > 0 ? parts[0].split(",") : new String[0];
        String[] onlineUsers = parts.length > 1 ? parts[1].split(",") : new String[0];

        // Temporäre Liste für den Aufbau des neuen UI-Status.
        List<String> finalList = new ArrayList<>();

        // Daten-Aggregation & Filterung.
        for (String user : allUsers) {
            // Normalisierung: Entfernen von Whitespaces.
            user = user.trim();

            // Filterlogik: Leere Einträge ignorieren. Eigenen Benutzer aus der Liste ausschließen.
            if (user.isEmpty() || user.equals(username)) continue;

            // Status-Abgleich. Prüfung, ob der aktuelle User in der Online-Liste enthalten ist.
            boolean isOnline = false;
            for (String onlineUser : onlineUsers) {
                if (onlineUser.trim().equals(user)) {
                    isOnline = true;
                    break; // Innere Schleife frühzeitig verlassen.
                }
            }

            finalList.add(user + (isOnline ? " (on)" : " (off)"));
        }

        // Liste wird nur aktualisiert, wenn sich der Inhalt tatsächlich geändert hat.
        if (!userList.getItems().equals(finalList)) {
            userList.getItems().setAll(finalList);
        }
    }
}