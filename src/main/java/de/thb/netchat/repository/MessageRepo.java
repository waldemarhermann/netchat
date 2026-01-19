package de.thb.netchat.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository-Klasse für den Zugriff auf die "messages"-Tabelle.
 * (Repository Pattern / DAO - Data Access Object)
 *
 * Diese Klasse kümmert sich ausschließlich um das Speichern und Laden von Nachrichten.
 * Sie trennt die Datenbank-Logik vom Rest der Anwendung.
 */
public class MessageRepo {

    /**
     * Speichert eine neue Nachricht in der Datenbank.
     *
     * @param senderName   Name des Absenders
     * @param receiverName Name des Empfängers
     * @param text         Der eigentliche Text der Nachricht
     */
    public void addMessage(String senderName, String receiverName, String text) {
        // Das SQL-Statement mit Platzhaltern (?).
        String sql = "insert into messages(sender_name, receiver_name, text) values(?, ?, ?)";

        // Wir nutzen PreparedStatements.
        // Vorteil: Schützt vor SQL-Injection (Hacker können keinen Schadcode einschleusen)
        // und ist performanter bei wiederholten Aufrufen.
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, senderName);
            preparedStatement.setString(2, receiverName);
            preparedStatement.setString(3, text);

            preparedStatement.executeUpdate(); // Führt das INSERT aus
            System.out.println("Nachricht gespeichert!");

        } catch (SQLException e) {
            System.err.println("Error beim Speichern der Nachricht:");
            e.printStackTrace();
        }
    }

    /**
     * Lädt den gesamten Chatverlauf zwischen zwei Benutzern.
     *
     * WICHTIG: Die Abfrage muss bidirektional sein. Wir wollen Nachrichten sehen,
     * die A an B geschickt hat, ABER AUCH Nachrichten, die B an A geschickt hat.
     *
     * @param a Benutzer A (z.B. der aktuell eingeloggte User)
     * @param b Benutzer B (z.B. der Chatpartner)
     * @return Eine chronologisch sortierte Liste von Strings im Format "Sender: Nachricht"
     */
    public List<String> getConversation(String a, String b) {
        List<String> list = new ArrayList<>();

        // SQL: Suche Nachrichten wo (Sender=A UND Empfänger=B) ODER (Sender=B UND Empfänger=A).
        // Sortiert nach Zeitstempel (älteste zuerst).
        String sql = """
        select sender_name, receiver_name, text, timestamp
        from messages
        where (sender_name = ? and receiver_name = ?)
           or (sender_name = ? and receiver_name = ?)
        order by timestamp asc;
        """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            // Parameter für die erste Klammer (A -> B)
            ps.setString(1, a);
            ps.setString(2, b);
            // Parameter für die zweite Klammer (B -> A)
            ps.setString(3, b);
            ps.setString(4, a);

            ResultSet rs = ps.executeQuery();

            // Durchlaufen der Ergebnisse
            while (rs.next()) {
                String sender = rs.getString("sender_name");
                String text = rs.getString("text");

                // Hier formatieren wir die Daten direkt für die Anzeige im Client.
                // Format ist: "Name: Nachricht"
                String formatted = sender + ": " + text;
                list.add(formatted);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Lädt alle Nachrichten, die von einem bestimmten User gesendet wurden.
     * (Hilfsmethode, z.B. für Logs oder Statistiken)
     *
     * @param senderName Der Name des Absenders
     * @return Liste der Nachrichtentexte
     */
    public List<String> getMessagesByUser(String senderName) {
        List<String> messages = new ArrayList<>();
        String sql = "select text from messages where sender_name = ? order by timestamp desc";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, senderName);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                messages.add(resultSet.getString("text"));
            }
        } catch (SQLException e) {
            System.err.println("Error beim Abrufen der Nachrichten:");
            e.printStackTrace();
        }
        return messages;
    }
}