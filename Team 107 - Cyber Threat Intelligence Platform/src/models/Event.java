package models;

public class Event {
    public String ip;
    public long timestamp;
    public String action;

    public Event(String ip, long timestamp, String action) {
        this.ip = ip;
        this.timestamp = timestamp;
        this.action = action;
    }
}
