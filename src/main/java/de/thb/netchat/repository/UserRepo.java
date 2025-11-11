package de.thb.netchat.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepo {

    public void addUser(String name, String email, String password) {
        String sql = "insert into users(name, email, password) values(?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, password);
            preparedStatement.executeUpdate();
            System.out.println("Neuer Benutzer hinzugefügt");
        } catch (SQLException e) {
            System.err.println("Error beim Hinzufügen eines neuen Users: ");
            e.printStackTrace();
        }
    }

    public List<String> getAllUserNames() {
        List<String> users = new ArrayList<>();
        String sql = "select name from users order by joined_at desc";
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                users.add(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error beim Abrufen der Benutzer:");
            e.printStackTrace();
        }
        return users;
    }

}
