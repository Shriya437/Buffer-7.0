package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import engine.ThreatEngine;
import models.Alert;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SimpleServer {
    private ThreatEngine engine;

    public SimpleServer(ThreatEngine engine) {
        this.engine = engine;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/signup", new SignupHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/analyze_url", new UrlAnalysisHandler());
        server.createContext("/api/simulate_login", new SimulateLoginHandler());
        server.createContext("/api/dashboard_data", new DashboardDataHandler());
        server.createContext("/api/reset", new ResetHandler());
        server.createContext("/api/simulate_scenario", new SimulateScenarioHandler());
        
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8080. Open http://localhost:8080 in your browser.");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    
    private String getRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = is.read()) != -1) {
            sb.append((char) ch);
        }
        return sb.toString();
    }

    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            
            try {
                byte[] bytes = Files.readAllBytes(Paths.get("web" + path));
                String contentType = "text/html";
                if (path.endsWith(".css")) contentType = "text/css";
                if (path.endsWith(".js")) contentType = "application/javascript";
                
                exchange.getResponseHeaders().add("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (Exception e) {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    class SignupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = getRequestBody(exchange);
                String[] parts = body.split("&");
                String user = parts[0].split("=")[1];
                String pass = parts[1].split("=")[1];
                
                boolean success = engine.authSystem.signup(user, pass);
                if (success) {
                    sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"Signup successful.\"}");
                } else {
                    sendResponse(exchange, 400, "{\"status\":\"error\", \"message\":\"Username taken.\"}");
                }
            }
        }
    }

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = getRequestBody(exchange);
                String[] parts = body.split("&");
                String user = parts[0].split("=")[1];
                String pass = parts[1].split("=")[1];
                
                boolean success = engine.authSystem.login(user, pass);
                if (success) {
                    sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"Login successful.\"}");
                } else {
                    sendResponse(exchange, 401, "{\"status\":\"error\", \"message\":\"Invalid credentials.\"}");
                }
            }
        }
    }

    class UrlAnalysisHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = getRequestBody(exchange);
                String[] parts = body.split("&");
                String url = java.net.URLDecoder.decode(parts[0].split("=")[1], "UTF-8");
                String user = parts[1].split("=")[1];

                engine.processUrlRequest(url, user);
                boolean isPhish = engine.phishingDetector.isPhishing(url);
                String msg = isPhish ? "Phishing detected!" : "URL is safe.";
                
                sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"" + msg + "\", \"isPhishing\":" + isPhish + "}");
            }
        }
    }

    class SimulateLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = getRequestBody(exchange);
                String[] parts = body.split("&");
                String ip = parts[0].split("=")[1];
                
                engine.processLoginAttempt(ip, false);
                
                sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"Simulated failed login step.\"}");
            }
        }
    }
    
    class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                engine.resetSystem();
                sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"System reset complete.\"}");
            }
        }
    }
    
    class SimulateScenarioHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                engine.processUrlRequest("securebanking-login.com", "admin");
                for(int i=0; i<6; i++) {
                    engine.processLoginAttempt("10.0.0.42", false);
                }
                sendResponse(exchange, 200, "{\"status\":\"success\", \"message\":\"Simulated massive attack scenario.\"}");
            }
        }
    }

    class DashboardDataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String reqQuery = exchange.getRequestURI().getQuery();
                String ipQuery = "10.0.0.42";
                String userQuery = null;
                
                if (reqQuery != null) {
                    for (String param : reqQuery.split("&")) {
                        if (param.startsWith("ip=")) ipQuery = param.split("=")[1];
                        if (param.startsWith("user=")) userQuery = param.split("=")[1];
                    }
                }
                
                if (!engine.authSystem.validateSession(userQuery)) {
                    sendResponse(exchange, 401, "{\"error\":\"Unauthorized access.\"}");
                    return;
                }
                
                int totalScore = engine.riskEvaluator.calculateSystemRiskScore();
                List<Alert> topAlerts = engine.riskEvaluator.getTopAlerts(10);
                
                StringBuilder alertsJson = new StringBuilder("[");
                for (int i = 0; i < topAlerts.size(); i++) {
                    Alert a = topAlerts.get(i);
                    alertsJson.append("{\"message\":\"").append(a.message).append("\",")
                              .append("\"level\":\"").append(a.level).append("\",")
                              .append("\"severity\":").append(a.severity).append("}");
                    if (i < topAlerts.size() - 1) alertsJson.append(",");
                }
                alertsJson.append("]");

                String riskLevel = "LOW";
                if (totalScore >= 400) riskLevel = "CRITICAL";
                else if (totalScore >= 200) riskLevel = "HIGH";
                else if (totalScore >= 80) riskLevel = "MODERATE";

                int attemptCount = engine.loginMonitor.getAttemptCount(ipQuery);
                boolean isBlocked = engine.getBlockedIps().contains(ipQuery);
                
                List<String> pathNodes = engine.attackPathPredictor.getShortestPath("Internet", "Database");
                StringBuilder pathJson = new StringBuilder("[");
                for(int i=0; i<pathNodes.size(); i++) {
                    pathJson.append("\"").append(pathNodes.get(i)).append("\"");
                    if(i < pathNodes.size()-1) pathJson.append(",");
                }
                pathJson.append("]");

                StringBuilder blockedJson = new StringBuilder("[");
                int idx = 0;
                for(String bip : engine.getBlockedIps()) {
                    blockedJson.append("\"").append(bip).append("\"");
                    if(idx < engine.getBlockedIps().size()-1) blockedJson.append(",");
                    idx++;
                }
                blockedJson.append("]");

                int topSeverity = topAlerts.isEmpty() ? 0 : topAlerts.get(0).severity;
                int blastRadius = engine.attackPathPredictor.calculateBlastRadiusPercentage("Internet", topSeverity);

                String response = "{"
                        + "\"alerts\":" + alertsJson.toString() + ","
                        + "\"totalAlerts\":" + topAlerts.size() + ","
                        + "\"riskLevel\":\"" + riskLevel + "\","
                        + "\"loginCount\":" + attemptCount + ","
                        + "\"isBlocked\":" + isBlocked + ","
                        + "\"attackPath\":" + pathJson.toString() + ","
                        + "\"blockedIps\":" + blockedJson.toString() + ","
                        + "\"blastRadius\":" + blastRadius
                        + "}";
                        
                sendResponse(exchange, 200, response);
            }
        }
    }
}
