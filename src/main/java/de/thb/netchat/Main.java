package de.thb.netchat;

import de.thb.netchat.repository.DBInitializer;

// Klasse dient Datenbank beim Start zurückzusetzen und neu zu initialisieren.
public class Main {

    public static void main(String[] args) {
        // Löscht alle existierenden Tabellen
        DBInitializer.resetDB();

        // Erstellt Tabellen (users, messages)
        DBInitializer.initialize();

        System.out.println("NetChat läuft!");
    }
}
