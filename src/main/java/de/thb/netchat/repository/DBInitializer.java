package de.thb.netchat.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/* Quelle: https://github.com/xerial/sqlite-jdbc */

/**
 * Klasse zur Initialisierung des Datenbankschemas.
 * Diese Klasse stellt sicher, dass beim Start der Anwendung alle notwendigen
 * Tabellen in der SQLite-Datenbank existieren.
 */
public class DBInitializer {

    /**
     * Erstellt die Tabellen "users" und "messages", falls diese noch nicht existieren.
     * Wird typischerweise beim Server-Start aufgerufen.
     */
    public static void initialize() {
        // SQL-Statement für die Benutzer-Tabelle.
        // Wir nutzen Java "Text Blocks" ("""), um den SQL-Code lesbar zu formatieren.
        // "text unique" sorgt dafür, dass Namen und E-Mails nicht doppelt vorkommen dürfen.
        String createUsers = """
                create table if not exists users (
                    id integer primary key autoincrement,
                    name text unique not null,
                    email text unique not null,
                    password text not null,
                    joined_at datetime default current_timestamp
                );
        """;

        // SQL-Statement für die Nachrichten-Verlauf-Tabelle.
        String createMessages = """
                create table if not exists messages (
                    id integer primary key autoincrement,
                    sender_name text not null,
                    receiver_name text not null,
                    text text not null,
                    timestamp datetime default current_timestamp
                );
                """;

        // "try-with-resources" Block:
        // Stellt sicher, dass Connection und Statement automatisch geschlossen werden,
        // sobald der Block verlassen wird (auch bei Fehlern). Verhindert Speicherlecks.
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

    /**
     * Löscht alle Tabellen aus der Datenbank.
     * Nützlich für Unit-Tests oder um den Server komplett zurückzusetzen.
     * ACHTUNG: Führt zu unwiderruflichem Datenverlust!
     */
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