#!/bin/bash
# ============================================================================
# GitHub Actions Self-Hosted Runner — Mac Setup
# Run this on YOUR Mac to turn it into a CI runner
# ============================================================================
# Prerequisites:
#   - Java 17 installed (brew install openjdk@17)
#   - Maven installed (brew install maven)
#   - Google Chrome installed
#   - Go to: https://github.com/<your-org>/MY-Bharat/settings/actions/runners/new
#   - Select: macOS + ARM64
#   - Copy the TOKEN
# ============================================================================

set -e

# --- Configuration (UPDATE THESE) ---
GITHUB_REPO_URL="https://github.com/YOUR_ORG/MY-Bharat"  # <-- Update this
RUNNER_TOKEN="YOUR_TOKEN_HERE"                             # <-- Paste token from GitHub
RUNNER_NAME="nisha-mac-runner"
RUNNER_LABELS="self-hosted,macOS,ARM64"
RUNNER_DIR="$HOME/actions-runner"

echo "========================================"
echo " GitHub Actions Runner — Mac Setup"
echo "========================================"

# --- 1. Check prerequisites ---
echo "[1/5] Checking prerequisites..."
echo -n "  Java: "; java -version 2>&1 | head -1
echo -n "  Maven: "; mvn -version 2>&1 | head -1
echo -n "  Chrome: "; /Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --version 2>/dev/null || echo "NOT FOUND — please install Chrome"

# --- 2. Create runner directory ---
echo "[2/5] Setting up runner directory..."
mkdir -p "$RUNNER_DIR"
cd "$RUNNER_DIR"

# --- 3. Download runner ---
echo "[3/5] Downloading GitHub Actions runner (macOS ARM64)..."
RUNNER_VERSION=$(curl -s https://api.github.com/repos/actions/runner/releases/latest | grep '"tag_name"' | sed 's/.*"v\(.*\)".*/\1/')
echo "  Latest version: ${RUNNER_VERSION}"

curl -o actions-runner-osx-arm64-${RUNNER_VERSION}.tar.gz -L \
  "https://github.com/actions/runner/releases/download/v${RUNNER_VERSION}/actions-runner-osx-arm64-${RUNNER_VERSION}.tar.gz"

tar xzf actions-runner-osx-arm64-${RUNNER_VERSION}.tar.gz
rm -f actions-runner-osx-arm64-${RUNNER_VERSION}.tar.gz

# --- 4. Configure ---
echo "[4/5] Configuring runner..."
./config.sh \
  --url "${GITHUB_REPO_URL}" \
  --token "${RUNNER_TOKEN}" \
  --name "${RUNNER_NAME}" \
  --labels "${RUNNER_LABELS}" \
  --unattended \
  --replace

# --- 5. Install as LaunchAgent (runs on login, survives reboots) ---
echo "[5/5] Installing as LaunchAgent (auto-start on login)..."
./svc.sh install
./svc.sh start

echo ""
echo "========================================"
echo " Runner Installed & Running!"
echo "========================================"
echo ""
echo "Runner name:   ${RUNNER_NAME}"
echo "Labels:        ${RUNNER_LABELS}"
echo "Directory:     ${RUNNER_DIR}"
echo ""
echo "Verify at: ${GITHUB_REPO_URL}/settings/actions/runners"
echo ""
echo "Commands:"
echo "  Stop:    cd ${RUNNER_DIR} && ./svc.sh stop"
echo "  Start:   cd ${RUNNER_DIR} && ./svc.sh start"
echo "  Status:  cd ${RUNNER_DIR} && ./svc.sh status"
echo "  Remove:  cd ${RUNNER_DIR} && ./svc.sh stop && ./svc.sh uninstall"
echo "========================================"
