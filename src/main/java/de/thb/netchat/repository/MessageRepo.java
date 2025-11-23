package de.thb.netchat.repository;

import de.thb.netchat.repository.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageRepo {

    public void addMessage(String senderName, String receiverName, String text) {
        String sql = "insert into messages(sender_name, receiver_name, text) values(?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, senderName);
            preparedStatement.setString(2, receiverName);
            preparedStatement.setString(3, text);
            preparedStatement.executeUpdate();
            System.out.println("Nachricht gespeichert!");
        } catch (SQLException e) {
            System.err.println("Error beim Speichern der Nachricht:");
            e.printStackTrace();
        }
    }

    public List<String> getConversation(String a, String b) {
        List<String> list = new ArrayList<>();

        String sql = """
        select sender_name, receiver_name, text, timestamp
        from messages
        where (sender_name = ? and receiver_name = ?)
           or (sender_name = ? and receiver_name = ?)
        order by timestamp asc;
        """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, a);
            ps.setString(2, b);
            ps.setString(3, b);
            ps.setString(4, a);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String sender = rs.getString("sender_name");
                String text = rs.getString("text");
                // KEINE Zeitberechnung mehr
                // Format ist jetzt einfach: "Name: Nachricht"
                String formatted = sender + ": " + text;
                list.add(formatted);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


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
