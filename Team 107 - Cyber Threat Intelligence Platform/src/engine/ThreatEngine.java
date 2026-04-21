package engine;

import java.util.HashSet;
import java.util.List;
import models.Alert;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ThreatEngine {
    public AuthSystem authSystem;
    public PhishingDetector phishingDetector;
    public LoginMonitor loginMonitor;
    public AttackPathPredictor attackPathPredictor;
    public RiskEvaluator riskEvaluator;

    private HashSet<String> blockedIps; // For the Kill Switch feature

    public ThreatEngine() {
        authSystem = new AuthSystem();
        phishingDetector = new PhishingDetector();
        loginMonitor = new LoginMonitor();
        attackPathPredictor = new AttackPathPredictor();
        riskEvaluator = new RiskEvaluator();
        blockedIps = new HashSet<>();
        
        loadInitialData();
    }

    private void loadInitialData() {
        // Load bad domains
        try (BufferedReader br = new BufferedReader(new FileReader("data/bad_domains.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                phishingDetector.insert(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Could not load bad domains: " + e.getMessage());
            // Fallback
            phishingDetector.insert("badsite.com");
            phishingDetector.insert("malicious-login.net");
            phishingDetector.insert("update-paypal-now.org");
        }

        // Load network topology
        try (BufferedReader br = new BufferedReader(new FileReader("data/network_topology.csv"))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    attackPathPredictor.addEdge(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()));
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load network topology: " + e.getMessage());
            // Fallback network
            attackPathPredictor.addEdge("Internet", "Firewall", 1);
            attackPathPredictor.addEdge("Firewall", "Web_Server", 2);
            attackPathPredictor.addEdge("Web_Server", "App_Server", 3);
            attackPathPredictor.addEdge("App_Server", "Database", 5);
        }
    }

    public void processUrlRequest(String url, String user) {
        if (phishingDetector.isPhishing(url)) {
            Alert alert = new Alert("User '" + user + "' visited known phishing site: " + url, 85);
            riskEvaluator.addAlert(alert);
        }
    }

    public void processLoginAttempt(String ip, boolean success) {
        // Kill Switch: check if already blocked
        if (blockedIps.contains(ip)) {
            System.out.println("Dropped packet from blocked IP: " + ip);
            return;
        }

        boolean isBruteForce = loginMonitor.logAttempt(ip, success, System.currentTimeMillis());
        if (isBruteForce) {
            Alert alert = new Alert("Brute force detected from IP: " + ip, 95);
            riskEvaluator.addAlert(alert);
            // Trigger kill switch
            blockedIps.add(ip);
            Alert killSwitchAlert = new Alert("KILL SWITCH ACTIVATED: Blocked IP " + ip, 100);
            riskEvaluator.addAlert(killSwitchAlert);
        }
    }

    // Returns a summary string
    public String getAttackPathSummary(String breachedNode) {
        List<Alert> top = riskEvaluator.getTopAlerts(1);
        int topSev = top.isEmpty() ? 0 : top.get(0).severity;
        int blastRadius = attackPathPredictor.calculateBlastRadiusPercentage(breachedNode, topSev);
        int timeToDb = attackPathPredictor.timeToCompromise(breachedNode, "Database");
        
        return "Breached Node: " + breachedNode + " | Blast Radius: " + blastRadius + "% of network | Time-to-Compromise 'Database': " + (timeToDb == -1 ? "Unreachable" : timeToDb + " units");
    }
    
    public HashSet<String> getBlockedIps() {
        return blockedIps;
    }

    public void resetSystem() {
        loginMonitor.reset();
        riskEvaluator.clearAlerts();
        blockedIps.clear();
    }
}
