package de.thb.netchat.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository-Klasse für den Zugriff auf die "users"-Tabelle.
 * (DAO - Data Access Object)
 *
 * Verwaltet alle Datenbank-Operationen, die Benutzerkonten betreffen:
 * Registrierung, Login-Überprüfung und Abfrage der Benutzerliste.
 */
public class UserRepo {

    /**
     * Registriert einen neuen Benutzer in der Datenbank.
     *
     * @param name     Der gewählte Benutzername
     * @param email    Die E-Mail-Adresse
     * @param password Das Passwort
     */
    public void addUser(String name, String email, String password) {
        String sql = "insert into users(name, email, password) values(?, ?, ?)";

        // Try-with-resources schließt die Verbindung automatisch nach dem Block
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

    /**
     * Lädt eine Liste aller registrierten Benutzernamen.
     * Sortiert nach Beitrittsdatum (neueste zuerst).
     * Wird z.B. verwendet, um im Client anzuzeigen, wer alles registriert ist.
     *
     * @return Liste der Namen als Strings
     */
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

    /**
     * Holt das Passwort zu einem Benutzernamen aus der Datenbank.
     * Wird beim Login benötigt, um das eingegebene Passwort mit dem gespeicherten zu vergleichen.
     *
     * @param username Der Benutzername, der sich einloggen will
     * @return Das gespeicherte Passwort oder null, wenn der User nicht existiert.
     */
    public String getPassword(String username) {
        String sql = "SELECT password FROM users WHERE name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("password");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // kein User gefunden oder Fehler
    }

    /**
     * Prüft, ob ein Benutzername bereits vergeben ist.
     * Wichtig für die Registrierung, um Duplikate zu vermeiden.
     *
     * @param username Der zu prüfende Name
     * @return true, wenn der Name schon existiert.
     */
    public boolean userExists(String username) {
        // "SELECT 1" ist eine Performance-Optimierung.
        // Wir brauchen nicht die Daten des Users, sondern nur die Info "Ja/Nein".
        String sql = "SELECT 1 FROM users WHERE name = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next(); // true, wenn ein Ergebnis gefunden wurde
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Prüft, ob eine E-Mail-Adresse bereits vergeben ist.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}