#!/usr/bin/env bash
# Startet den Lingo-Dev-Container mit Podman (GUI + Projekt-Mounts).
# Aufruf vom Repo-Root: ./scripts/run-devcontainer.sh
#
# Umgebungsvariablen:
#   LINGO_IMAGE            Image-Name (Default: lingo-dev:latest)
#   LINGO_CONTAINER        Container-Name (Default: lingo-dev)
#   LINGO_USB_PASSTHROUGH  1 = /dev/bus/usb in den Container durchreichen
#                          (adb direkt per USB; hakelig – empfohlen ist adb
#                          übers Netzwerk, siehe README)
#   LINGO_KVM              1 = /dev/kvm durchreichen, damit der Android-Emulator
#                          im Container mit KVM-Beschleunigung läuft
#   LINGO_USE_HOST_ADB     1 = adb im Container über den adb-Server des Hosts
#                          laufen lassen (setzt ADB_SERVER_SOCKET als -e).
#                          Host: android-tools + android-udev, 'adb -a start-server'.
#                          Hostname/IP per LINGO_ADB_HOST überschreibbar
#                          (Default: host.containers.internal)
#   LINGO_GPU              1 = /dev/dri in den Container durchreichen, damit
#                          Android Studio die Host-GPU nutzt statt llvmpipe
#                          (Software-Rendering frisst CPU und kann den Host
#                          einfrieren lassen)
#   LINGO_CPUS / LINGO_MEMORY  optionale Ressourcen-Limits für den Container,
#                          z. B. LINGO_CPUS=4 LINGO_MEMORY=6g
#   LINGO_CMD              Startkommando im Container (Default: bash)
set -euo pipefail
cd "$(dirname "$0")/.."

IMAGE="${LINGO_IMAGE:-lingo-dev:latest}"
CONTAINER="${LINGO_CONTAINER:-lingo-dev}"

./scripts/ensure-gradle-wrapper.sh

if ! podman image exists "$IMAGE"; then
  echo "Baue Image $IMAGE ..."
  podman build -t "$IMAGE" -f .devcontainer/Dockerfile .devcontainer
fi

# X11: lokale Verbindungen für den Container erlauben (lokaler Desktop, pro Session einmal nötig)
if command -v xhost >/dev/null 2>&1; then
  xhost +local: >/dev/null 2>&1 || true
fi

args=(
  --rm -it
  --name "$CONTAINER"
  -e "DISPLAY=${DISPLAY:?DISPLAY ist nicht gesetzt – bitte aus einer grafischen Session starten}"
  -v /tmp/.X11-unix:/tmp/.X11-unix:ro
  -v "$PWD:/workspace"
  -w /workspace
  -v lingo-gradle-cache:/home/ubuntu/.gradle
  -v lingo-studio-config:/home/ubuntu/.config/Google
  -v lingo-studio-share:/home/ubuntu/.local/share/Google
  -v lingo-android-home:/home/ubuntu/.android
  -v lingo-jetbrains-cache:/home/ubuntu/.cache/JetBrains
  --userns=keep-id
)
# WICHTIG: Alle Optionen müssen VOR dem Image-Namen stehen – alles danach wäre
# das Container-Kommando.

if [ "${LINGO_USB_PASSTHROUGH:-0}" = "1" ]; then
  args+=(--device /dev/bus/usb)
fi

if [ "${LINGO_KVM:-0}" = "1" ]; then
  args+=(--device /dev/kvm)
fi

if [ "${LINGO_USE_HOST_ADB:-0}" = "1" ]; then
  args+=(-e "ADB_SERVER_SOCKET=tcp:${LINGO_ADB_HOST:-host.containers.internal}:5037")
fi

if [ "${LINGO_GPU:-0}" = "1" ]; then
  args+=(--device /dev/dri)
fi

# Gruppen: '--group-add keep-groups' ist nicht mit anderen --group-add kombinierbar.
# Ohne GPU -> keep-groups (nimmt alle Host-Gruppen mit, z. B. adbusers).
# Mit GPU -> explizit video/render (Host-User ist das auf Desktop-Systemen eh);
# bei gleichzeitigem USB-Passthrough zusätzlich adbusers.
if [ "${LINGO_GPU:-0}" = "1" ]; then
  args+=(--group-add video --group-add render)
  if [ "${LINGO_USB_PASSTHROUGH:-0}" = "1" ]; then
    args+=(--group-add adbusers)
  fi
else
  args+=(--group-add keep-groups)
fi

if [ -n "${LINGO_CPUS:-}" ]; then
  args+=(--cpus "$LINGO_CPUS")
fi
if [ -n "${LINGO_MEMORY:-}" ]; then
  args+=(--memory "$LINGO_MEMORY")
fi

exec podman run "${args[@]}" "$IMAGE" "${LINGO_CMD:-bash}"
