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
#                          (Host: Paket qemu-full, User in Gruppe 'kvm')
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
  --group-add keep-groups
  "$IMAGE"
)

if [ "${LINGO_USB_PASSTHROUGH:-0}" = "1" ]; then
  args+=(--device /dev/bus/usb)
fi

if [ "${LINGO_KVM:-0}" = "1" ]; then
  args+=(--device /dev/kvm)
fi

exec podman run "${args[@]}" "${LINGO_CMD:-bash}"
