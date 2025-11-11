package de.thb.netchat.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageRepo {

    public void addMessage(int senderId, int receiverId, String text) {
        String sql = "insert into messages(sender_id, recieverId, text) values(?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, senderId);
            preparedStatement.setInt(2, receiverId);
            preparedStatement.setString(3, text);
            preparedStatement.executeUpdate();
            System.out.println("Nachricht gespeichert!");
        } catch (SQLException e) {
            System.err.println("Error beim Speichern der Nachricht:");
            e.printStackTrace();
        }
    }

    public List<String> getMessagesByUser(int senderId) {
        List<String> messages = new ArrayList<>();
        String sql = "select text from message where sender_id = ? order by timestamp desc";
        try (Connection connection = DBConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, senderId);
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
