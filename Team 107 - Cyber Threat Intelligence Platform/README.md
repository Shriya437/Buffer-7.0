# 🛡️ Cyber Threat Intelligence & Attack Prevention Platform

A real-time cybersecurity simulation platform that detects, correlates, and visualizes multi-stage cyber threats using efficient data structures and algorithms.

---

## 🚨 Problem Statement

Modern cyber attacks increasingly occur as **multi-stage attack chains**, where attackers move across systems step-by-step. However, many traditional security systems fail to:

* Correlate suspicious activities across modules
* Detect coordinated attacks in real time
* Provide early warnings before critical damage occurs

As a result, threats like phishing, brute-force attacks, and lateral movement often go unnoticed until it is too late.

---

## 💡 Proposed Solution

This project simulates a **Cyber Threat Intelligence Platform** that integrates multiple detection mechanisms into a single system.

Instead of analyzing events in isolation, the platform:

* Detects individual anomalies (phishing, login attacks)
* Correlates them across the system
* Predicts attack paths through network modeling
* Dynamically evaluates overall system risk

This enables **early detection of coordinated cyber attacks** and provides a more realistic representation of how modern Security Operations Centers (SOC) function.

---

## 🚀 Overview

The platform provides an interactive dashboard where users can:

* Detect phishing attempts
* Monitor suspicious login activity
* Visualize attack propagation in a network
* Analyze real-time system risk levels

---

## 🧠 Data Structures & Algorithms Used

This project focuses heavily on applying DSA to real-world problems:

* **Trie** → Efficient phishing URL pattern detection
* **Sliding Window + HashMap** → Real-time login anomaly tracking
* **Graph (BFS + Dijkstra)** → Attack path prediction and blast radius calculation
* **Priority Queue (Max Heap)** → Alert prioritization based on severity

---

## 🔍 Core Features

### 🔐 Phishing Detection

* Detects suspicious URLs using pattern similarity
* Provides step-by-step reasoning of detection

---

### 🚨 Suspicious Login Monitoring

* Tracks login attempts over time
* Identifies brute-force attacks using thresholds
* Blocks malicious IPs dynamically

---

### 🌐 Network Attack Simulation

* Models system as a graph of interconnected nodes
* Predicts possible attack paths
* Calculates **blast radius** based on attack severity

---

### ⚠️ Risk Evaluation System

* Assigns severity to each detected threat
* Computes **cumulative system risk score**
* Dynamically updates system state:

  * LOW
  * MODERATE
  * HIGH
  * CRITICAL

---

### 📊 Interactive Dashboard

* Multi-tab interface for different modules
* Real-time activity logs
* Dynamic updates based on system events

---

## 🏗️ System Architecture

```text
User Action
   ↓
Frontend (HTML/CSS/JS)
   ↓
Java HTTP Server
   ↓
Threat Engine
   ├── Phishing Detection
   ├── Login Monitor
   ├── Attack Path Predictor
   └── Risk Evaluator
   ↓
Processed Data → UI Update
```

---

## ⚙️ Tech Stack

* **Backend:** Java (HttpServer)
* **Frontend:** HTML, CSS, JavaScript
* **Core Focus:** Data Structures & Algorithms

---

## 🎥 Demo Video

👉 [Watch Demo Video](https://drive.google.com/drive/folders/1XVJsenxCGrA-FfLTXmN_SyNDXkD9I7pI)

---

## ▶️ How to Run

### 1. Navigate to project directory

```bash
cd CTIPlatform
```

### 2. Compile the project

```bash
javac -d bin src\models\*.java src\engine\*.java src\server\*.java src\Main.java
```

### 3. Run the server

```bash
java -cp bin Main
```

### 4. Open in browser

```
http://localhost:8080
```

---

## 🎯 Key Highlights

* Real-time system simulation (not static)
* Multi-stage attack correlation
* Dynamic risk scoring based on cumulative threats
* Severity-aware blast radius calculation
* Clean, modular architecture
* Strong application of DSA concepts

---

## 📌 Future Improvements

* Integration with real-time datasets
* Machine learning-based anomaly detection
* Advanced network visualization (graphs)
* Database integration for persistence

---

## 👤 Author

**Shriya Jaripatke**

* Designed and implemented the complete system architecture
* Developed backend logic, UI, and debugging pipeline
* Focused on building a scalable and explainable cybersecurity system

---

## ⭐ Final Note

This project demonstrates how fundamental data structures and algorithms can be leveraged to solve complex real-world cybersecurity problems, particularly in detecting and analyzing coordinated multi-stage attacks.
