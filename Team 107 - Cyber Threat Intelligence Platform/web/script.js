const API_URL = "http://localhost:8080/api";
let currentUser = null;
let pollTimer = null;

// --- Tab Management ---
function switchTab(tabId) {
    document.querySelectorAll('.nav-links li').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.pane').forEach(el => el.classList.remove('active-pane'));
    
    document.getElementById(`tab-${tabId}`).classList.add('active');
    document.getElementById(`pane-${tabId}`).classList.add('active-pane');
}

// --- Activity Log ---
function logEvent(message, type = "info") {
    const list = document.getElementById("activity-log");
    const li = document.createElement("li");
    const time = new Date().toLocaleTimeString();
    
    let css = "";
    if (type === "warn") css = "log-warn";
    else if (type === "error") css = "log-err";

    li.innerHTML = `<span class="ts">[${time}]</span><span class="${css}">${message}</span>`;
    list.appendChild(li);
    // Auto-scroll logic:
    const container = document.querySelector('.terminal-log');
    if(container) container.scrollTop = container.scrollHeight;
}

// --- Authentication ---
async function authRequest(endpoint) {
    const user = document.getElementById("username").value.trim();
    const pass = document.getElementById("password").value;
    const msg = document.getElementById("auth-msg");

    if (!user || !pass) {
        msg.innerText = "Please fill all fields.";
        return;
    }

    try {
        const res = await fetch(`${API_URL}/${endpoint}`, {
            method: 'POST',
            body: `username=${encodeURIComponent(user)}&password=${encodeURIComponent(pass)}`,
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
        
        if (res.ok) {
            currentUser = user;
            document.getElementById("auth-screen").style.display = "none";
            document.getElementById("app-container").style.display = "flex";
            document.getElementById("user-display").innerText = `User: ${user}`;
            logEvent(`Operator ${user} connected. Session initialized.`, "info");
            
            // Initial poll and set interval
            await pollSystemData();
            pollTimer = setInterval(pollSystemData, 2000);
        } else {
            const data = await res.json();
            msg.innerText = data.error || data.message || "Authentication failed.";
        }
    } catch(e) {
        msg.innerText = "Server offline or unreachable.";
    }
}
const login = () => authRequest('login');
const signup = () => authRequest('signup');
const logout = () => {
    currentUser = null;
    clearInterval(pollTimer);
    location.reload();
};

// --- Phishing Analysis (Step-by-step Process) ---
async function analyzeUrl() {
    const url = document.getElementById("url-input").value;
    if(!url || !currentUser) return;

    const btn = document.getElementById("btn-analyze");
    btn.disabled = true;

    const box = document.getElementById("url-process-box");
    const steps = document.getElementById("url-steps");
    const verdict = document.getElementById("url-verdict");
    
    box.style.display = "block";
    steps.innerHTML = "";
    verdict.style.display = "none";

    logEvent(`User requested analysis for domain: ${url}`, "info");

    const delays = [500, 1200, 1800];
    const msgs = [
        `Extracting domain elements...`,
        `Checking similarity against known malicious patterns...`,
        `Finalizing pattern evaluation...`
    ];

    msgs.forEach((m, idx) => {
        setTimeout(() => {
            steps.innerHTML += `<li>${m}</li>`;
        }, delays[idx]);
    });

    setTimeout(async () => {
        try {
            const res = await fetch(`${API_URL}/analyze_url`, {
                method: 'POST',
                body: `url=${encodeURIComponent(url)}&user=${encodeURIComponent(currentUser)}`,
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            });
            const data = await res.json();
            
            verdict.style.display = "block";
            if(data.isPhishing) {
                verdict.className = "verdict-banner bg-red";
                verdict.innerText = "VERDICT: Phishing threat detected. Match found in suspicious patterns.";
                logEvent(`SYSTEM: Phishing threat confirmed for ${url}. Exploit logged.`, "error");
            } else {
                verdict.className = "verdict-banner bg-green";
                verdict.innerText = "VERDICT: No known suspicious structures found. URL appears safe.";
                logEvent(`SYSTEM: Domain ${url} cleared. No threat found.`, "info");
            }
            // Real-time synchronization
            await pollSystemData();
        } catch(e) {
            logEvent("SYSTEM ERROR: Analysis payload failed to reach server.", "error");
        } finally {
            btn.disabled = false;
        }
    }, 2000);
}

// --- Login Simulation ---
async function simulateLoginFail() {
    const ip = document.getElementById("ip-input").value || "10.0.0.42";
    const btn = document.getElementById("btn-inject");
    btn.disabled = true;
    
    logEvent(`User injected mock failed authentication from IP: ${ip}`, "warn");
    
    try {
        await fetch(`${API_URL}/simulate_login`, {
            method: 'POST',
            body: `ip=${encodeURIComponent(ip)}`,
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        });
        
        await pollSystemData();
    } catch(e) {
        console.error(e);
    } finally {
        btn.disabled = false;
    }
}

// --- Simulations / System ---
async function resetSystem() {
    if(!currentUser) return;
    const btn = document.getElementById("btn-reset");
    btn.disabled = true;

    try {
        await fetch(`${API_URL}/reset`, { method: 'POST' });
        
        // Reset local UI manually as an instant acknowledgement
        document.getElementById("url-process-box").style.display = "none";
        document.getElementById("activity-log").innerHTML = "<li><span class='ts'>["+new Date().toLocaleTimeString()+"]</span><span class='log-info'>System matrices wiped and queues cleared.</span></li>";
        
        await pollSystemData();
    } catch(e) {} finally {
        btn.disabled = false;
    }
}

async function simulateScenario() {
    if(!currentUser) return;
    const btn = document.getElementById("btn-simulate");
    btn.disabled = true;
    
    logEvent(`User initiated comprehensive attack scenario macro.`, "error");
    
    try {
        await fetch(`${API_URL}/simulate_scenario`, { method: 'POST' });
        await pollSystemData();
    } catch(e) {} finally {
        btn.disabled = false;
    }
}

// --- Data Polling & UI Synchronization ---
// We keep a record of last topAlerts so we can log ONLY new alerts coming dynamically
let knownAlerts = [];

async function pollSystemData() {
    if(!currentUser) return;
    const ip = document.getElementById("ip-input").value || "10.0.0.42";
    
    try {
        // Enforce user token mapping for authentication logic
        const res = await fetch(`${API_URL}/dashboard_data?user=${encodeURIComponent(currentUser)}&ip=${encodeURIComponent(ip)}`);
        
        if (res.status === 401) {
            logout();
            return;
        }
        
        const data = await res.json();

        // 1. DASHBOARD METRICS:
        document.getElementById("metric-alerts").innerText = data.totalAlerts;
        document.getElementById("metric-blocked").innerText = data.blockedIps.length;
        
        const riskEl = document.getElementById("metric-risk");
        riskEl.innerText = data.riskLevel;
        riskEl.className = data.riskLevel === 'CRITICAL' ? 'text-red' : (data.riskLevel === 'HIGH' ? 'text-yellow' : 'text-green');

        // 2. ACTIVITY LOG (Track real backend events):
        if(data.alerts && data.alerts.length > 0) {
            data.alerts.forEach(newAlert => {
                // If we don't already have this exact message in our active known block:
                let exists = knownAlerts.find(a => a.message === newAlert.message);
                if(!exists) {
                    let type = newAlert.level === 'CRITICAL' ? 'error' : (newAlert.level === 'HIGH' ? 'warn' : 'info');
                    logEvent(`SYSTEM: Generated Threat Alert -> ${newAlert.message}`, type);
                }
            });
            knownAlerts = [...data.alerts];
        } else {
            knownAlerts = []; // Reset locally if system was reset
        }

        // 3. LOGIN MONITOR VISUALIZATION:
        const count = data.loginCount;
        document.getElementById("login-count").innerText = count;
        document.getElementById("login-progress").style.width = Math.min((count / 5) * 100, 100) + "%";
        
        const statusTxt = document.getElementById("login-status-text");
        if(data.isBlocked) {
            statusTxt.innerHTML = "<span class='text-red'><strong>ACTION TAKEN:</strong> Threshold exceeded. Implementing automated IP restriction on network edge layer.</span>";
        } else if (count > 0) {
            statusTxt.innerHTML = "<span class='text-yellow'>Tracking recent failed attempts... actively evaluating thresholds.</span>";
        } else {
            statusTxt.innerHTML = "<span class='text-muted'>Monitoring recent login attempts... System nominal.</span>";
        }

        // 4. NETWORK GRAPH PATH:
        const pathEl = document.getElementById("network-path");
        if (data.totalAlerts > 0) {
            // Assume attack triggered if alerts exist
            document.getElementById("network-process-text").innerText = "Attack pattern detected. Simulating possible lateral expansion paths:";
            
            if(data.attackPath && data.attackPath.length > 0) {
                let htmlStr = "";
                data.attackPath.forEach((node, idx) => {
                    let isLast = idx === data.attackPath.length - 1;
                    htmlStr += `<span class="node ${isLast ? 'node-critical' : ''}">${node}</span>`;
                    if(!isLast) htmlStr += `<span class="arrow">➔</span>`;
                });
                pathEl.innerHTML = htmlStr;
                document.getElementById("network-blast-radius").innerText = data.blastRadius + "%";
            }
        } else {
            pathEl.innerHTML = "";
            document.getElementById("network-blast-radius").innerText = "0%";
            document.getElementById("network-process-text").innerText = "No attack detected yet.";
        }

        // 5. ALERTS RISK PANEL:
        const tbody = document.getElementById("risk-table-body");
        tbody.innerHTML = "";
        if(data.alerts.length === 0) {
            tbody.innerHTML = "<tr><td colspan='4' class='text-center text-muted'>Safety queue empty. No active threats.</td></tr>";
        } else {
            data.alerts.forEach(a => {
                let css = a.level.toLowerCase();
                let reasoning = "";
                if(a.message.includes("Brute force") || a.message.includes("KILL SWITCH")) reasoning = "Authentication attempts exceeded timeline thresholds.";
                else if(a.message.includes("phishing") || a.message.includes("visited")) reasoning = "Matched suspicious structure against domain database.";
                
                tbody.innerHTML += `<tr>
                    <td class="text-${css == 'critical'?'red':(css=='high'?'yellow':'blue')} text-center"><strong>${a.severity}</strong></td>
                    <td><span class="badge bg-${css == 'critical'?'red':'green'}">${a.level}</span></td>
                    <td>${a.message}</td>
                    <td class="text-muted"><small>${reasoning}</small></td>
                </tr>`;
            });
        }
    } catch(e) {
        console.error("Polling fault:", e);
    }
}
