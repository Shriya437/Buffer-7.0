import engine.ThreatEngine;
import server.SimpleServer;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting CTI Platform...");
        ThreatEngine engine = new ThreatEngine();
        
        try {
            SimpleServer server = new SimpleServer(engine);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start server.");
        }
    }
}
