package engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class LoginMonitor {
    // Sliding window HashMap: maps IP address to a queue of failure timestamps
    private HashMap<String, Queue<Long>> ipFailureLog;
    private final int MAX_ATTEMPTS = 5;
    private final long TIME_WINDOW_MS = 5 * 60 * 1000; // 5 minutes

    public LoginMonitor() {
        ipFailureLog = new HashMap<>();
    }

    // Returns true if anomalous brute force detected
    public boolean logAttempt(String ip, boolean success, long timestamp) {
        if (success) {
            ipFailureLog.remove(ip); // Clear on success
            return false;
        }

        ipFailureLog.putIfAbsent(ip, new LinkedList<>());
        Queue<Long> attempts = ipFailureLog.get(ip);
        attempts.add(timestamp);

        // Slide the window: remove attempts older than our time window
        while (!attempts.isEmpty() && (timestamp - attempts.peek() > TIME_WINDOW_MS)) {
            attempts.poll();
        }

        // If failures exceed max attempts within window, it's brute force
        return attempts.size() > MAX_ATTEMPTS;
    }

    public int getAttemptCount(String ip) {
        if (!ipFailureLog.containsKey(ip)) return 0;
        long currentTime = System.currentTimeMillis();
        Queue<Long> attempts = ipFailureLog.get(ip);
        while (!attempts.isEmpty() && (currentTime - attempts.peek() > TIME_WINDOW_MS)) {
            attempts.poll();
        }
        return attempts.size();
    }

    public void reset() {
        ipFailureLog.clear();
    }
}
