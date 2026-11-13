# Lingo – Vokabeltrainer für Android

„Lingo" ist eine kleine Sprachlern-App (aktuell: Englisch → Deutsch) für Android.
Du verwaltest Vokabeln mit Übersetzung und Beispielsatz und übst sie per
Karteikarten-Modus („Gewusst" / „Nochmal üben").

## Tech-Stack

- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose
- Room (SQLite) als lokale Datenbank, MVVM mit ViewModel
- Gradle (Kotlin DSL), AGP 8.7, minSdk 24 / targetSdk 34

## Entwicklung im Podman-Dev-Container (empfohlen)

Damit auf dem CachyOS-Host nichts installiert werden muss, liegt in
`.devcontainer/` eine fertige **Dockerfile** (funktioniert unverändert mit Podman
und Docker). Sie enthält Android Studio, das Android SDK (`platform-tools` inkl.
`adb`, `platforms;android-34`, `build-tools;34.0.0`) und ein **JDK 21** – das wird
über `org.gradle.java.home` in `gradle.properties` als Gradle-JVM erzwungen, damit
Gradle 8.10.2 nicht auf dem JBR 25 der IDE laufen muss.

### Host-Voraussetzungen (einmalig, minimal)

```bash
sudo pacman -S podman xorg-xhost
# optional, nur für USB-Passthrough (Variante 3 unten):
sudo pacman -S android-tools android-udev
sudo usermod -aG adbusers $USER   # danach neu einloggen
```

### Image bauen und Container starten

```bash
podman build -t lingo-dev -f .devcontainer/Dockerfile .devcontainer
./scripts/run-devcontainer.sh
```

`run-devcontainer.sh` hängt u. a. ein:

- das X11-Socket (`/tmp/.X11-unix`), damit Android Studio ein Fenster auf deinem Desktop öffnen kann (auf Wayland läuft das über XWayland; alternativ Wayland-Socket durchreichen: `-e WAYLAND_DISPLAY=$WAYLAND_DISPLAY -e XDG_RUNTIME_DIR=$XDG_RUNTIME_DIR -v $XDG_RUNTIME_DIR/$WAYLAND_DISPLAY:$XDG_RUNTIME_DIR/$WAYLAND_DISPLAY`)
- das Projektverzeichnis als `/workspace`
- Volumes für Gradle-Cache und Android-Studio-Konfiguration (überleben Container-Neustarts)

Im Container dann:

```bash
studio                  # Android Studio starten (erster Start: Setup-Wizard durchklicken)
./gradlew installDebug  # baut die App und installiert sie auf dem verbundenen Gerät
```

> Hinweis: `scripts/ensure-gradle-wrapper.sh` lädt einmalig `gradlew`, `gradlew.bat`
> und die `gradle-wrapper.jar` passend zur gepinnten Gradle-Version nach (wird von
> `run-devcontainer.sh` automatisch aufgerufen).

### Tablet-Verbindung: funktioniert USB im Container?

USB direkt in einen Container durchzureichen ist grundsätzlich möglich, aber
**nicht zuverlässig** – das Gerätenode (`/dev/bus/usb/001/005`) wechselt beim
Umstecken, und rootless Podman braucht dafür zusätzliche Gruppenrechte. Deshalb
drei Varianten, von empfohlen nach „geht auch":

1. **Drahtloses Debugging (empfohlen, ganz ohne USB):** Auf dem Tablet
   Entwickleroptionen → „Drahtloses Debugging" aktivieren (Android 11+; das Tab
   beherrscht das). Im Container genügt dann:
   ```bash
   adb connect <tablet-ip>:<port>   # IP/Port steht im Drahtloses-Debugging-Dialog
   adb devices                      # Tablet taucht als "device" auf
   ```
   Der Container erreicht das Tablet direkt übers LAN. Android Studio zeigt das
   Gerät danach ganz normal im Device Manager an – „Run" und `installDebug`
   funktionieren, als säße das Tablet lokal am Rechner.
2. **adb-Server auf dem Host (empfohlen bei USB, ohne Container-Neustart):**
   Auf dem CachyOS-Host `android-tools` + `android-udev` installieren, User in
   Gruppe `adbusers` aufnehmen, Tablet per USB verbinden und `adb -a start-server`
   ausführen. Container dann mit Flag starten – das Skript setzt
   `ADB_SERVER_SOCKET=tcp:host.containers.internal:5037` im Container:
   ```bash
   LINGO_USE_HOST_ADB=1 ./scripts/run-devcontainer.sh
   # im Container: adb devices  -> Tab sichtbar; ./gradlew installDebug funktioniert
   ```
   Falls `host.containers.internal` nicht auflöst, Gateway-IP übergeben:
   `LINGO_ADB_HOST=10.0.2.2 LINGO_USE_HOST_ADB=1 ./scripts/run-devcontainer.sh`
   (Gateway siehe `ip route | awk '/default/ {print $3}'` im Container).
3. **USB-Passthrough:** `LINGO_USB_PASSTHROUGH=1 ./scripts/run-devcontainer.sh`
   reicht `/dev/bus/usb` in den Container durch (Host braucht `android-udev` +
   Gruppe `adbusers`). Klappt meist, ist aber hakelig – nimm Variante 1.

### Fehlende/unichtbare Icons und Buttons (XWayland, Wayland, HiDPI)

X11-Forwarding über XWayland funktioniert grundsätzlich sauber – zwei typische
Ursachen für fehlende Icons/Radio-Buttons im Container:

1. **Keine Schriftarten im Image** – ist gefixt: die Dockerfile installiert jetzt
   `fontconfig fonts-dejavu-core fonts-noto-core` und baut den Font-Cache (`fc-cache -f`).
2. **Fractional Scaling auf dem Wayland-Host** – KDE skaliert XWayland-Fenster
   (z. B. 125 %/150 %), Android Studio skaliert zusätzlich selbst → Elemente
   verschwinden oder werden unscharf. Lösungen, eine davon genügt:
   - Beim Login auf CachyOS eine **X11-Session** wählen („Plasma (X11)"), dann
     ist das Studio-Fenster natives X11 und nicht XWayland, oder
   - IDE-Skalierung fest auf 1.0 zwingen (im Container, wirkt dank Volume persistent):
     ```bash
     d=$(ls -d ~/.config/Google/AndroidStudio* | head -1)
     echo "ide.ui.scale=1.0" >> "$d/idea.properties"
     # dann Studio neu starten
     ```
   - oder die Skalierung in den KDE-Anzeigeeinstellungen vorübergehend auf 100 % stellen.

### Host-Bildschirm friert ein, während Studio im Container läuft

Häufige Ursachen und Gegenmaßnahmen (der Reihe nach prüfen):

1. **RAM/CPU des Hosts überlastet** (Studio + Gradle-Daemon im Container + KDE).
   Auf dem Host während des Freezes prüfen: `free -h`, `top`, `journalctl -b -p err`
   (OOM-Killer?) bzw. `dmesg | tail` (GPU-Reset?). Container limitieren:
   ```bash
   LINGO_CPUS=4 LINGO_MEMORY=6g ./scripts/run-devcontainer.sh
   ```
   Studio-Heap kleiner machen (im Container, persistent dank Volume):
   ```bash
   d=$(ls -d ~/.config/Google/AndroidStudio* | head -1)
   echo "-Xmx2048m" >> "$d/idea.properties"
   ```
2. **Software-Rendering (llvmpipe) im Container** – ohne GPU-Zugriff rendert Studio
   auf der CPU, jeder Frame läuft durch XWayland. GPU durchreichen:
   ```bash
   LINGO_GPU=1 ./scripts/run-devcontainer.sh
   ```
   (reicht `/dev/dri` durch; die nötigen Gruppen kommen über `--group-add keep-groups`
   aus deiner Host-Session – Host-User sollte in `video`/`render` sein, auf Desktop-
   Systemen Standard, sonst einmalig `sudo usermod -aG video,render $USER`. Im Container
   dann `glxinfo -B` prüfen – sollte die echte GPU melden, nicht llvmpipe.)
3. **Wayland/XWayland-Probleme mit KWin** – Test: beim Login eine **X11-Session**
   („Plasma (X11)") wählen und Studio dort laufen lassen. Ansonsten Host updaten
   (`sudo pacman -Syu`) – KWin/XWayland-Hänger mit schweren X11-Clients sind bekannt.

### Emulator mit KVM im Container (optional)

Die KVM-Meldung von Android Studio betrifft den **Emulator** (x86_64-AVDs brauchen
`/dev/kvm`). Für das echte Galaxy Tab brauchst du das nicht – aber so richtest du
es ein, wenn du doch mal einen Emulator im Container starten willst:

Host (CachyOS), nur nötig wenn du den Emulator im Container nutzen willst:

```bash
sudo modprobe kvm-intel        # Intel-CPU; bei AMD: kvm-amd (Modul liegt im Kernel-Paket)
getent group kvm || sudo groupadd -r kvm
sudo usermod -aG kvm $USER     # danach neu einloggen
ls -l /dev/kvm                 # muss existieren
```

`qemu-full` (~1,5 GB) brauchst du dafür **nicht** auf dem Host: Das QEMU des
Android-Emulators liegt im SDK-Paket `emulator` *im Container*. `qemu-full` auf
dem Host wäre nur nötig, um einen Emulator direkt auf dem Host zu betreiben –
für den Dev-Container + echtes Tablet entfällt das komplett.

Image mit Emulator-Paketen bauen (größeres Image):

```bash
podman build -t lingo-dev --build-arg WITH_EMULATOR=1 -f .devcontainer/Dockerfile .devcontainer
```

Container mit KVM starten:

```bash
LINGO_KVM=1 ./scripts/run-devcontainer.sh
```

Das Skript reicht dann `--device /dev/kvm` durch; `--group-add keep-groups` (ist
bereits gesetzt) sorgt dafür, dass die `kvm`-Gruppe des Host-Users im Container
ankommt. In Android Studio im AVD Manager ein x86_64-Image anlegen – die
KVM-Meldung sollte dann nicht mehr kommen bzw. „accelerated" melden.

## Alternative: native Installation auf CachyOS (Arch-basiert)

### 1. Pakete installieren

```bash
sudo pacman -S jdk21-openjdk android-tools android-udev
```

- `jdk21-openjdk` – JDK 21. Achtung: `org.gradle.java.home` in `gradle.properties`
  zeigt auf den Container-Pfad (`/usr/lib/jvm/java-21-openjdk-amd64`). Beim nativen
  Bauen auf dem Host dort `/usr/lib/jvm/java-21-openjdk` eintragen oder die Zeile
  auskommentieren (dann greift `JAVA_HOME`).
- `android-tools` – enthält `adb` (die Brücke zum Tablet)
- `android-udev` – udev-Regeln, damit `adb` ohne Root auf Samsung-Geräte zugreifen darf

### 2. Android SDK einrichten

Die SDK-Plattformen liegen nicht in den Arch-Repos, daher manuell von Google holen:

```bash
mkdir -p ~/Android/Sdk/cmdline-tools
cd /tmp
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip -d ~/Android/Sdk/cmdline-tools
mv ~/Android/Sdk/cmdline-tools/cmdline-tools ~/Android/Sdk/cmdline-tools/latest
```

In `~/.bashrc` bzw. `~/.zshrc` eintragen:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
```

Danach neu einloggen (oder `source ~/.bashrc`) und folgendes ausführen:

```bash
sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

### 3. Galaxy Tab vorbereiten

1. **Entwickleroptionen freischalten:** Einstellungen → Über das Tablet →
   Softwareinformationen → 7× auf „Buildnummer" tippen.
2. **USB-Debugging aktivieren:** Entwickleroptionen → „USB-Debugging" einschalten.
3. Tablet per USB-Kabel mit dem CachyOS-Rechner verbinden und auf dem Tablet
   „USB-Debugging erlauben" bestätigen (Häkchen „Immer von diesem Computer
   zulassen" setzen).
4. Verbindung testen:

   ```bash
   adb devices
   ```

   Das Tablet muss als `device` auftauchen.

> Hinweis: Das Tablet ist etwa vier Jahre alt und läuft vermutlich mit
> Android 11–13. Die App hat `minSdk = 24` (Android 7.0) und läuft damit
> problemlos. Auf Android 11+ steht alternativ „Drahtloses Debugging" in den
> Entwickleroptionen zur Verfügung, falls USB Probleme macht.

## App bauen und auf das Tablet laden

Repository klonen und ins Projektverzeichnis wechseln:

```bash
git clone https://github.com/clemenssielaff/Lingo.git
cd Lingo
```

**Gradle Wrapper erzeugen (einmalig):** Das Repo enthält die
`gradle-wrapper.properties`; `gradlew` und `gradle-wrapper.jar` lädt
`scripts/ensure-gradle-wrapper.sh` nach.
Einmaligig erzeugen – entweder über das System-Gradle:

```bash
sudo pacman -S gradle
gradle wrapper --gradle-version 8.10.2
```

oder indem du das Projekt einmal in Android Studio öffnest
(`yay -S android-studio`, dann „Open" → Projektordner). Danach steht
`./gradlew` zur Verfügung.

**Debug-APK bauen und direkt auf dem verbundenen Tablet installieren:**

```bash
./gradlew installDebug
```

Alternativ zweistufig:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Beim ersten Build werden Gradle und die Abhängigkeiten aus dem Internet geladen
(Google Maven / Maven Central) – das dauert ein paar Minuten.

## Projektstruktur

```
app/src/main/java/com/clemenssielaff/lingo/
├── LingoApplication.kt      Application-Klasse, hält Datenbank & Repository
├── MainActivity.kt          Einstiegspunkt, setzt das Compose-Theme
├── data/
│   ├── Word.kt              Room-Entity (Vokabel)
│   ├── WordDao.kt           Datenbankzugriff (Flows + suspend-Funktionen)
│   ├── AppDatabase.kt       Room-Datenbank (Singleton)
│   └── WordRepository.kt    Repository inkl. Beispiel-Vokabeln fürs Seeding
└── ui/
    ├── LingoApp.kt          NavHost (home / practice / add)
    ├── LingoViewModel.kt    ViewModel mit StateFlows
    ├── theme/Theme.kt       Material-3-Farbschema
    └── screens/             HomeScreen, PracticeScreen, AddWordScreen
```

## Nächste Schritte (Ideen)

- Mehrere Stapel (z. B. pro Sprache) und Sprachauswahl
- Echte Spaced-Repetition-Logik statt einfacher Zähler
- Import/Export der Vokabeln als JSON/CSV
- Widget oder Quick-Settings-Kachel zum Schnellüben
