# Lingo Dev-Container: Android Studio + Android SDK + JDK auf Ubuntu 24.04.
# Funktioniert unverändert mit Podman und Docker.
#
# Bauen (vom Repo-Root aus):
#   podman build -t lingo-dev -f .devcontainer/Dockerfile .devcontainer
#
# Starten siehe scripts/run-devcontainer.sh bzw. README.md.
FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# Command-line tools des Android SDK (Version ggf. aktualisieren, siehe
# https://developer.android.com/studio unter „Command line tools only")
ARG CMDTOOLS_URL=https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
# Optionaler Build-Arg: exakter Download-Link von https://developer.android.com/studio
# (Zeile „Linux"). Wenn leer, wird der Link automatisch von der Downloadseite gelesen.
ARG STUDIO_URL=
# Optional: Android-Emulator + x86_64-Systemimage ins Image packen (deutlich groesser)
ARG WITH_EMULATOR=0

# Systempakete: JDK 21 (als Gradle-JVM; die IDE selbst nutzt ihr gebündeltes
# JBR), Tools und die X11-/GL-Bibliotheken, die Android Studio für die GUI braucht.
RUN apt-get update && apt-get install -y --no-install-recommends \
      ca-certificates curl wget unzip git openssh-client \
      openjdk-21-jdk \
      fontconfig fonts-dejavu-core fonts-noto-core \
      libx11-6 libxext6 libxrender1 libxtst6 libxi6 libxrandr2 libxinerama1 \
      libxcursor1 libxfixes3 libxkbcommon0 libgl1 libegl1 libgles2 \
      libfontconfig1 libfreetype6 libglib2.0-0 libnss3 libnspr4 \
      libdrm2 libgbm1 libasound2t64 \
    && fc-cache -f \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk

# Android SDK: command-line tools + benötigte Pakete (platform-tools enthält adb)
RUN mkdir -p "$ANDROID_HOME/cmdline-tools" \
    && curl -fSL "$CMDTOOLS_URL" -o /tmp/cmdtools.zip \
    && unzip -q /tmp/cmdtools.zip -d "$ANDROID_HOME/cmdline-tools" \
    && mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest" \
    && rm /tmp/cmdtools.zip

ENV PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$JAVA_HOME/bin:$PATH"

RUN yes | sdkmanager --licenses >/dev/null \
    && sdkmanager "platform-tools" "platforms;android-34" "platforms;android-35" \
       "build-tools;34.0.0" "build-tools;35.0.0" \
    && if [ "$WITH_EMULATOR" = "1" ]; then \
         sdkmanager "emulator" "system-images;android-34;google_apis;x86_64"; \
       fi

# Android Studio (tar.gz entpackt nach android-studio/)
RUN if [ -z "$STUDIO_URL" ]; then \
      STUDIO_URL=$(curl -fsSL https://developer.android.com/studio \
        | grep -oE 'https://[a-zA-Z0-9./_?=&%-]+linux\.tar\.gz' | head -n 1); \
    fi \
    && test -n "$STUDIO_URL" \
    && echo "Android Studio Download: $STUDIO_URL" \
    && curl -fSL "$STUDIO_URL" -o /tmp/studio.tar.gz \
    && mkdir -p /opt/android-studio \
    && tar -xzf /tmp/studio.tar.gz -C /opt/android-studio --strip-components=1 \
    && rm /tmp/studio.tar.gz

ENV PATH="/opt/android-studio/bin:$PATH"

# Vorhandenen Nicht-root-User "ubuntu" (UID 1000 im Basisimage) nutzen –
# Android Studio motzt sonst herum, und UID 1000 passt zu --userns=keep-id.
# Home-Unterverzeichnisse vorab anlegen und ubuntu übereignen, sonst legt
# Podman sie beim Mounten der Named Volumes root-gehörig an (AccessDenied).
RUN mkdir -p /workspace \
    && mkdir -p /home/ubuntu/.gradle /home/ubuntu/.cache /home/ubuntu/.config/Google \
       /home/ubuntu/.local/share/Google /home/ubuntu/.android /tmp/runtime-ubuntu \
    && chown -R ubuntu:ubuntu /workspace /opt/android-sdk /home/ubuntu /tmp/runtime-ubuntu \
    && chmod 700 /tmp/runtime-ubuntu

USER ubuntu
ENV HOME=/home/ubuntu
ENV XDG_RUNTIME_DIR=/tmp/runtime-ubuntu
# ADB_SERVER_SOCKET wird absichtlich nicht hartkodiert; bei Bedarf setzt
# scripts/run-devcontainer.sh (LINGO_USE_HOST_ADB=1) die Variable als -e.
WORKDIR /workspace

# Im Container: `studio` startet Android Studio, `./gradlew installDebug` baut & installiert.
CMD ["bash"]
