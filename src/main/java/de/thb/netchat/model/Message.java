package de.thb.netchat.model;

public class Message {

    private String type;
    private String from;
    private String to;
    private String text;

    public Message() {}

    public Message(String type, String from, String to, String text) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.text = text;
    }

    public String getType() { return type; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public String getText() { return text; }

    public void setType(String type) { this.type = type; }
    public void setFrom(String from) { this.from = from; }
    public void setTo(String to) { this.to = to; }
    public void setText(String text) { this.text = text; }
}

