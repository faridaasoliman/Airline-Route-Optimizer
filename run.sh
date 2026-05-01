#!/bin/bash
echo "============================================"
echo "  SkyRoute AI — Airline Optimizer (Java)"
echo "============================================"
echo ""

mkdir -p out

echo "[1/2] Compiling Java files..."
javac -d out -sourcepath src src/airline/Main.java

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Compilation failed. Make sure Java JDK is installed."
    echo "Install with: sudo apt install default-jdk  (Linux)"
    echo "           or: brew install openjdk          (Mac)"
    exit 1
fi

echo "[2/2] Launching application..."
echo ""
java -cp out airline.Main
