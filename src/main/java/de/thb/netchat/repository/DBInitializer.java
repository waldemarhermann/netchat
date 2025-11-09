package de.thb.netchat.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInitializer {

    public static void initialize() {
        String createUsers = """
                create table if not exists users (
                    id intger primary key autoincrement,
                    name text not null,
                    joined_at datetime default current_timestamp
                );
        """;

        String createMessages = """
                create table if not exists messages (
                    id integer primary key autoincrement
                    sender_id integer not null,
                    text text not null,
                    timestamp datetime default current_timestamp
                    foreign key(sender_id) references users(id)
                );
                """;

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(createUsers);
            statement.execute(createMessages);
            System.out.println("Datenbanktabelle erfolgreich überprüft/erstellt!");
        } catch (SQLException e) {
            System.err.println("Fehler beim Initialisieren der Datenbank: ");
            e.printStackTrace();
        }
    }
}
