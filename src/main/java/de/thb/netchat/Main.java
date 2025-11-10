package de.thb.netchat;

import de.thb.netchat.repository.DBInitializer;


public class Main {
    public static void main(String[] args) {
        DBInitializer.resetDB();
        DBInitializer.initialize();
        System.out.println("NetChat läuft!");
    }
}
