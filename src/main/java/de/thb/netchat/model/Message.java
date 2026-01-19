package de.thb.netchat.model;

/**
 * Model-Klasse für den Nachrichtenaustausch zwischen Client und Server.
 * Fungiert als Datencontainer (Data Transfer Object / DTO).
 *
 * Diese Klasse wird verwendet, um Informationen strukturiert über das Netzwerk zu senden.
 * Sie enthält nicht nur den Text, sondern auch Metadaten wie Absender, Empfänger
 * und den Typ der Nachricht (z.B. ob es eine Chat-Nachricht oder ein Login-Befehl ist).
 */
public class Message {

    // Art der Nachricht (z.B. "LOGIN", "CHAT", "ERROR").
    // Wichtig für den Controller, um zu wissen, wie die Nachricht verarbeitet werden muss.
    private String type;

    // Absender der Nachricht (z.B. Benutzername).
    private String from;

    // Empfänger der Nachricht (z.B. "ALL" für Broadcast oder spezifischer User).
    private String to;

    // Der eigentliche Inhalt der Nachricht.
    private String text;

    /**
     * Leerer Standard-Konstruktor.
     * Wird oft von Frameworks (wie Jackson für JSON) benötigt, um Objekte
     * aus Textdaten automatisch wiederherstellen zu können (Deserialisierung).
     */
    public Message() {}

    /**
     * Konstruktor zum direkten Erstellen einer vollständigen Nachricht.
     *
     * @param type Art der Nachricht (Protokoll-Befehl)
     * @param from Wer sendet?
     * @param to Wer soll empfangen?
     * @param text Der Nachrichteninhalt
     */
    public Message(String type, String from, String to, String text) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.text = text;
    }

    // --- Getter und Setter (Kapselung der Daten) ---

    public String getType() { return type; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getText() { return text; }

    public void setType(String type) { this.type = type; }
    public void setFrom(String from) { this.from = from; }
    public void setTo(String to) { this.to = to; }
    public void setText(String text) { this.text = text; }
}