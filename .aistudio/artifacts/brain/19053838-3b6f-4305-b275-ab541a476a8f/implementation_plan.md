# Implementierungsplan: Unified Streaming Dashboard & Berechtigungs-Ampel in der Medienbibliothek

Dieser Plan beschreibt die Umsetzung eines zentralen **Unified Dashboards** in der Medienbibliothek mit Live-Verbindungs-Schaltern für **Spotify**, **YouTube Music** und **Amazon Music**, Echtzeit-Wiedergabestatus, Stream-Synchronisation sowie einer kompakten **Berechtigungs-Ampel** in der Kopfzeile.

---

## 1. Übersicht & Benutzeroberfläche (UI / UX)

### Platzierung:
- Im oberen Bereich der Medienbibliothek (`LibraryScreen.kt`) wird ein interaktives **Unified Dashboard** verankert, das vor den Track-Listen den gesamten Audio- und Streaming-Status übersichtlich bündelt.

### Komponenten des Dashboards:

#### A. Kompakte Berechtigungs-Ampel (Kopfzeile)
- Status-Pills mit Farbkodierung (🟢 Grün = Erteilt/Aktiv, 🟡 Gelb/Orange = Optional/Inaktiv):
  - **📁 Speicher / Lokale Musik**: Zeigt Status der Speicherrechte; Klick öffnet optionalen One-Click-Rechte-Flow.
  - **🎤 Mikrofon**: Status für Echtzeit-Visualizer & Sampler.
  - **🔔 Benachrichtigung**: Status für DJ-Lockscreen & HUD-Meldungen.
  - **🌐 Cloud Stream**: Immer 🟢 Grün (*Zero-Permission Cloud Audio*).

#### B. Drei aktive Streaming-Verbindungskarten:
- **Spotify** (Markengrün `#1DB954`):
  - Schalter: Live-Verbindung AN / AUS (schaltet Spotify-Streams im DJ-Katalog aktiv).
  - Status: Aktuelle Wiedergabe (z. B. *„Deck A: Midnight Club Synth • 128 BPM“* oder *„Bereit“*).
  - Quick-Aktionen: *In Spotify öffnen*, *Stream Sync*.
- **YouTube Music** (Markenrot `#FF0000`):
  - Schalter: Live-Verbindung AN / AUS.
  - Status: Aktuelle Wiedergabe / Deck-Zuweisung.
  - Quick-Aktionen: *In YT Music öffnen*, *Stream Sync*.
- **Amazon Music** (Markenblau `#00A8E1`):
  - Schalter: Live-Verbindung AN / AUS.
  - Status: Aktuelle Wiedergabe / Deck-Zuweisung.
  - Quick-Aktionen: *In Amazon Music öffnen*, *Stream Sync*.

---

## 2. Architektur & ViewModel Integration

### Datenmodell & StateFlows (`LocalMusicViewModel.kt`):
- `isSpotifyActive: StateFlow<Boolean>` (Standard: `true`)
- `isYouTubeMusicActive: StateFlow<Boolean>` (Standard: `true`)
- `isAmazonMusicActive: StateFlow<Boolean>` (Standard: `true`)
- Schalter-Funktionen:
  - `toggleSpotifyConnection()`
  - `toggleYouTubeMusicConnection()`
  - `toggleAmazonMusicConnection()`
- Dynamische Katalog-Filterung:
  - Die Streaming-Titelliste passt sich sofort an; deaktivierte Dienste werden ausgeblendet, aktivierte Dienste bleiben voll mixbar.
- Echtzeit-Stream-Status:
  - Verknüpfung mit dem aktuellen Track auf Deck A und Deck B zur Anzeige, welcher Dienst gerade live auf den Lautsprechern läuft.

---

## 3. Berechtigungs-Flow

- **Keine Blockaden**:
  - Sind Rechte nicht erteilt, bleibt die Ampel auf 🟡 Optional; Streaming läuft nahtlos weiter.
  - Tippen auf die Ampel-Pill startet den `launchMultiplePermissionRequest()`-Dialog gezielt und ohne Zwang.

---

## 4. Test- & Validierungsplan

- **Kompilierung**: Verifikation mit `compile_applet`.
- **Unit & Robolectric Tests**:
  - Testen des Umschaltens der Streaming-Verbindungen (`toggleSpotifyConnection()`, etc.).
  - Validierung der dynamischen Filterung der Titelliste im ViewModel.
  - Überprüfung der Berechtigungs-Ampel-Logik.
