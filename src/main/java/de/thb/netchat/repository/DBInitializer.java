package de.thb.netchat.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/* Quelle: https://github.com/xerial/sqlite-jdbc */

public class DBInitializer {

    public static void initialize() {
        String createUsers = """
                create table if not exists users (
                    id integer primary key autoincrement,
                    name text unique not null,
                    email text unique not null,
                    password text not null,
                    joined_at datetime default current_timestamp
                );
        """;

        String createMessages = """
                create table if not exists messages (
                    id integer primary key autoincrement,
                    sender_name text not null,
                    receiver_name text not null,
                    text text not null,
                    timestamp datetime default current_timestamp
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

    public static void resetDB() {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("drop table if exists messages;");
            statement.execute("drop table if exists users;");

            System.out.println("Alle Tabellen gelöscht!");

        } catch (SQLException e) {
            System.err.println("Error beim Zurücksetzen der Tabellen: ");
            e.printStackTrace();
        }
    }

}
