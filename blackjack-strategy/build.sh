#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
javac -d out $(find src -name "*.java")
echo "Built to out/. Run with: ./run.sh [--rounds=N] [--threads=N] [--out=DIR]"
