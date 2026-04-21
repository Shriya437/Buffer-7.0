package engine;

import java.util.HashMap;

public class AuthSystem {
    // HashMap for fast O(1) user lookups
    private HashMap<String, String> users;
    
    private java.util.HashSet<String> activeSessions;

    public AuthSystem() {
        users = new HashMap<>();
        activeSessions = new java.util.HashSet<>();
        // Add a default user for testing
        users.put("admin", "password123");
    }

    public boolean signup(String username, String password) {
        if (users.containsKey(username)) {
            return false; // User already exists
        }
        users.put(username, password);
        return true;
    }

    public boolean login(String username, String password) {
        if (users.containsKey(username) && users.get(username).equals(password)) {
            activeSessions.add(username);
            return true;
        }
        return false;
    }
    
    public boolean validateSession(String username) {
        return username != null && activeSessions.contains(username);
    }
}
