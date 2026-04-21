package engine;

import models.Alert;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;

public class RiskEvaluator {
    // PriorityQueue to always keep the highest severity alert at the top
    private PriorityQueue<Alert> alertQueue;

    public RiskEvaluator() {
        alertQueue = new PriorityQueue<>();
    }

    public void addAlert(Alert alert) {
        alertQueue.add(alert);
    }

    public List<Alert> getTopAlerts(int limit) {
        List<Alert> topAlerts = new ArrayList<>();
        PriorityQueue<Alert> tempQueue = new PriorityQueue<>(alertQueue);
        
        int count = 0;
        while (!tempQueue.isEmpty() && count < limit) {
            topAlerts.add(tempQueue.poll());
            count++;
        }
        return topAlerts;
    }
    
    public void clearAlerts() {
        alertQueue.clear();
    }
    
    public int calculateSystemRiskScore() {
        int totalScore = 0;
        for (Alert alert : alertQueue) {
            totalScore += alert.severity;
        }
        return totalScore;
    }
}
