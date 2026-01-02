package de.thb.netchat.service;

import de.thb.netchat.repository.MessageRepo;
import de.thb.netchat.repository.UserRepo;

import java.util.List;

/**
 * Implementiert die zentrale Geschäftslogik (Service Layer) der Anwendung.
 * Diese Klasse fungiert als Facade Pattern zu den darunterliegenden Datenschichten (Repositories).
 * Aufgaben:
 * 1. Kapselung der Datenzugriffe - Server kennt keine Repositories.
 * 2. Bereitstellung von Methoden für Benutzerverwaltung und Messaging.
 * 3. Zentraler Zugriffspunkt für alle Commands.
 */
public class ChatService {

    // Instanzierung der Datenhaltungsschicht.
    // Hier werden die Speicherklassen erstellt (RAM oder Datei).
    // Der Service verwaltet die Repositories und greift ausschließlich darauf zu.
    private final UserRepo userRepo = new UserRepo();
    private final MessageRepo messageRepo = new MessageRepo();

    /**
     * Legt einen neuen Benutzer im System an.
     * Delegiert die physische Speicherung an das UserRepo.
     *
     * @param name Benutzername (Primary Key)
     * @param email E-Mail Adresse.
     * @param password Bereits gehashtes Passwort.
     */
    public void createUser(String name, String email, String password) {
        userRepo.addUser(name, email, password);
    }

    /**
     * Speichert eine Nachricht im Verlauf.
     *
     * @param senderName Absender.
     * @param receiverName Empfänger.
     * @param text Inhalt der Nachricht.
     */
    public void sendMessage(String senderName, String receiverName, String text) {
        messageRepo.addMessage(senderName, receiverName, text);
    }

    /**
     * Ruft den Chatverlauf zwischen zwei spezifischen Benutzern ab.
     * Wird vom HistoryRequestCommand genutzt.
     *
     * @return Liste von Nachrichten-Strings.
     */
    public List<String> getConversation(String userA, String userB) {
        return messageRepo.getConversation(userA, userB);
    }

    /**
     * Prüft die Existenz eines Benutzernamens.
     * Wichtig für Login und Registrierung (Vermeidung von Duplikaten).
     */
    public boolean userExists(String username) {
        return userRepo.userExists(username);
    }

    /**
     * Prüft die Existenz einer E-Mail-Adresse.
     */
    public boolean emailExists(String email) {
        return userRepo.emailExists(email);
    }

    /**
     * Debugging / Administration: Gibt alle Benutzer auf der Server-Konsole aus.
     * Methode dient der Diagnose zur Laufzeit und sendet nichts an Clients.
     */
    public void listUser() {
        List<String> users = userRepo.getAllUserNames();
        System.out.println("--- Benuterübersicht ---");
        for (String name : users) {
            System.out.println("**" + name + "** ");
        }
    }

    /**
     * Liefert eine Liste aller registrierten Benutzernamen.
     * Wird benutzt, um die "Alle Benutzer"-Liste im Client zu befüllen.
     *
     * @return Liste aller Usernamen aus der Datenbank.
     */
    public List<String> listAllUsers() {
        return userRepo.getAllUserNames();
    }

    /**
     * Debugging: Zeigt Nachrichten eines Users auf der Server-Konsole an.
     */
    public void showMessagesByUser(String senderName) {
        List<String> messages = messageRepo.getMessagesByUser(senderName);
            if (messages.isEmpty()) {
                System.out.println("Keine Nachrichten gefunden.");
            } else {
                System.out.println("--- Nachrichten von Benutzer ---" + senderName + " ---");
                for (String msg : messages) {
                    System.out.println(msg);
                }
            }
    }

    /**
     * Holt das gespeicherte Passwort-Hash für den Login-Abgleich
     *
     * @param username Der gesuchte Benutzer.
     * @return Der Passwort-Hash oder null.
     */
    public String getPasswordForUser(String username) {
        return userRepo.getPassword(username);
    }


}
