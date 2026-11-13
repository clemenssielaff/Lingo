# Lingo – Vokabeltrainer für Android

„Lingo" ist eine kleine Sprachlern-App (aktuell: Englisch → Deutsch) für Android.
Du verwaltest Vokabeln mit Übersetzung und Beispielsatz und übst sie per
Karteikarten-Modus („Gewusst" / „Nochmal üben").

## Tech-Stack

- Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose
- Room (SQLite) als lokale Datenbank, MVVM mit ViewModel
- Gradle (Kotlin DSL), AGP 8.7, minSdk 24 / targetSdk 34

## Voraussetzungen auf CachyOS (Arch-basiert)

### 1. Pakete installieren

```bash
sudo pacman -S jdk17-openjdk android-tools android-udev
```

- `jdk17-openjdk` – JDK 17 (Pflicht für den Android-Build)
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
`gradle-wrapper.properties`, aber noch nicht die `gradle-wrapper.jar`.
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
