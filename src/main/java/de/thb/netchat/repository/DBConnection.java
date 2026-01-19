package de.thb.netchat.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Hilfsklasse für den Datenbankzugriff.
 * Zuständig für den Aufbau der Verbindung zur SQLite-Datenbank.
 *
 * Diese Klasse kapselt die Verbindungsdetails (wie den Pfad zur DB-Datei),
 * damit andere Klassen (Repositories) sich nicht um die Konfiguration kümmern müssen.
 */
public class DBConnection {

    // Der Connection-String (JDBC URL).
    // Legt fest, dass wir SQLite nutzen und wo die Datei liegt ("db/netchat.db").
    private static final String URL = "jdbc:sqlite:db/netchat.db";

    /**
     * Erstellt eine neue Verbindung zur Datenbank.
     * Nutzt den DriverManager von Java (JDBC), um den Treiber zu laden und die Verbindung zu öffnen.
     *
     * @return Ein offenes Connection-Objekt, mit dem SQL-Befehle gesendet werden können.
     * @throws SQLException Falls die Datenbank-Datei nicht gefunden wird oder gesperrt ist.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}