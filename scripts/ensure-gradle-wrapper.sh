#!/usr/bin/env bash
# Lädt die gradle-wrapper.jar zur in gradle/wrapper/gradle-wrapper.properties
# gepinnten Gradle-Version nach (einmalig, braucht Netzwerk).
set -euo pipefail
cd "$(dirname "$0")/.."

GRADLE_VERSION=$(sed -n 's/^distributionUrl=.*gradle-\([0-9.]*\)-bin\.zip$/\1/p' gradle/wrapper/gradle-wrapper.properties)
if [ -z "$GRADLE_VERSION" ]; then
  echo "Konnte die Gradle-Version nicht aus gradle-wrapper.properties lesen." >&2
  exit 1
fi

if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
  echo "Lade gradle-wrapper.jar für Gradle $GRADLE_VERSION ..."
  curl -fsSL -o gradle/wrapper/gradle-wrapper.jar \
    "https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}/gradle/wrapper/gradle-wrapper.jar"
fi
