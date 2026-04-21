package models;

public class Alert implements Comparable<Alert> {
    public String message;
    public int severity; // 0 to 100
    public long timestamp;
    public String level; // CRITICAL, HIGH, MEDIUM, LOW

    public Alert(String message, int severity) {
        this.message = message;
        this.severity = severity;
        this.timestamp = System.currentTimeMillis();
        
        if (severity >= 80) this.level = "CRITICAL";
        else if (severity >= 60) this.level = "HIGH";
        else if (severity >= 40) this.level = "MEDIUM";
        else this.level = "LOW";
    }

    @Override
    public int compareTo(Alert other) {
        // Higher severity first
        return Integer.compare(other.severity, this.severity);
    }
    
    @Override
    public String toString() {
        return "[" + level + " | " + severity + "] " + message;
    }
}
