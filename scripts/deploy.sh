#!/usr/bin/env bash
# Baut die Debug-APK im Dev-Container und installiert sie auf dem Tablet.
# adb läuft dabei auf dem HOST (USB-Gerät hängt am Host) – zuverlässiger als
# `./gradlew installDebug` im Container, dessen AGP-DeviceMonitor die Verbindung
# zum entfernten ADB-Server (ADB_SERVER_SOCKET) wiederholt verliert.
#
# Aufruf vom Repo-Root auf dem CachyOS-Host:  ./scripts/deploy.sh
# Voraussetzung: Container läuft (LINGO_USE_HOST_ADB=1 ./scripts/run-devcontainer.sh)
# und auf dem Host ist das Tab per `adb devices` als "device" sichtbar.
set -euo pipefail
cd "$(dirname "$0")/.."

CONTAINER="${LINGO_CONTAINER:-lingo-dev}"

echo "==> Baue APK im Container '$CONTAINER' ..."
podman exec "$CONTAINER" ./gradlew assembleDebug

APK="app/build/outputs/apk/debug/app-debug.apk"
echo "==> Installiere $APK per adb (Host) ..."
adb install -r "$APK"

echo "==> Fertig – App 'Lingo' auf dem Tablet starten."
