#!/usr/bin/env bash
# Lädt gradlew, gradlew.bat und die gradle-wrapper.jar passend zur in
# gradle/wrapper/gradle-wrapper.properties gepinnten Gradle-Version nach
# (einmalig, braucht Netzwerk).
set -euo pipefail
cd "$(dirname "$0")/.."

GRADLE_VERSION=$(sed -n 's/^distributionUrl=.*gradle-\([0-9.]*\)-bin\.zip$/\1/p' gradle/wrapper/gradle-wrapper.properties)
if [ -z "$GRADLE_VERSION" ]; then
  echo "Konnte die Gradle-Version nicht aus gradle-wrapper.properties lesen." >&2
  exit 1
fi

BASE_URL="https://raw.githubusercontent.com/gradle/gradle/v${GRADLE_VERSION}"
for f in gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar; do
  if [ ! -f "$f" ]; then
    echo "Lade $f (Gradle $GRADLE_VERSION) ..."
    mkdir -p "$(dirname "$f")"
    curl -fsSL -o "$f" "$BASE_URL/$f"
  fi
done
chmod +x gradlew
