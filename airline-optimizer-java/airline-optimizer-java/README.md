# ✈ SkyRoute AI — Airline Route + Cargo Optimizer
### Computing Algorithms — Group Project 5 (Java Edition)

---

## 📁 Project Structure

```
airline-optimizer-java/
│
├── src/airline/
│   ├── Main.java          ← Entry point (run this)
│   ├── MainWindow.java    ← Main GUI window with 5 tabs
│   ├── NetworkCanvas.java ← Draws the airport network map
│   ├── CargoCanvas.java   ← Draws the cargo hold layout
│   ├── Algorithms.java    ← All 5 algorithm implementations
│   ├── DataStore.java     ← Airport, route, and cargo data
│   ├── Airport.java       ← Airport data model
│   ├── Route.java         ← Route (edge) data model
│   └── CargoItem.java     ← Cargo shipment data model
│
├── run.bat                ← Double-click to run on Windows
├── run.sh                 ← Run on Mac / Linux
└── README.md              ← This file
```

---

## 🚀 How to Run

### Step 1 — Install Java (if you haven't)
Download Java JDK 11 or newer from: https://adoptium.net  
*(Free, open source — choose "LTS" version)*

### Step 2 — Run the project

**Windows:**
> Double-click `run.bat`

**Mac / Linux:**
```bash
chmod +x run.sh
./run.sh
```

**Manual (any OS):**
```bash
mkdir out
javac -d out -sourcepath src src/airline/Main.java
java -cp out airline.Main
```

---

## 🗂 Application Tabs

| Tab | What It Does |
|-----|-------------|
| 🗺 Route Network | Shows all 10 airports and 20 routes on the map |
| 📍 Dijkstra | Find the cheapest flight path between any two airports |
| 🌐 MST | Run Prim's and Kruskal's to find the minimum-cost network |
| 📦 Cargo | Load cargo using DP Knapsack (exact) or Greedy (fast) |
| 📊 Comparison | Full algorithm comparison table and stats |

---

## 🧮 Algorithms Implemented

### 1. Dijkstra's Algorithm (Shortest Path)
- **Where:** Tab 2 — Dijkstra
- **What it does:** Finds the cheapest flight connection between two airports
- **Complexity:** O((V + E) log V)
- **Output:** Path highlighted on map, distance table for all airports

### 2. Prim's Algorithm (MST)
- **Where:** Tab 3 — MST
- **What it does:** Builds minimum-cost hub network starting from one airport
- **Complexity:** O(E log V)
- **Output:** MST edges drawn in green on the map

### 3. Kruskal's Algorithm (MST)
- **Where:** Tab 3 — MST
- **What it does:** Same result as Prim's but sorts all edges first, uses Union-Find
- **Complexity:** O(E log E)
- **Output:** Same MST total cost as Prim's (verified in UI)

### 4. 0/1 Knapsack DP (Cargo Selection)
- **Where:** Tab 4 — Cargo
- **What it does:** Selects cargo items that maximise revenue within weight limit
- **Complexity:** O(n × W)
- **Output:** DP table shown, selected items loaded into hold zones

### 5. Greedy Knapsack + Bin Packing
- **Where:** Tab 4 — Cargo
- **What it does:** Fast approximation — sorts by revenue/weight ratio
- **Output:** Compared against DP, approximation ratio shown in Tab 5

---

## 📊 Data

**10 Airports:** Cairo, Dubai, London, Paris, Frankfurt, Istanbul, Mumbai, Singapore, New York, Nairobi

**20 Routes** with operating costs ($k) and flight times (hours)

**12 Cargo Items** with weight, volume, and revenue values

**3 Hold Zones:** Forward (15t), Center (20t), Aft (15t)

---

## 👥 Group Project Info

- **Subject:** Computing Algorithms
- **Project:** #5 — Airline Route + Cargo Optimization
- **Language:** Java (Swing GUI)
- **Required algorithms covered:** ✅ Dijkstra · ✅ Prim's · ✅ Kruskal's · ✅ Knapsack DP · ✅ Greedy

---

*Built with Java Swing — no external libraries required.*
