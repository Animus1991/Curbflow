# CurbFlow AI 🅿️

**Smart urban parking prediction — find a spot before you get there.**

CurbFlow AI is a production-grade Kotlin Android app that predicts on-street parking availability in real time using probabilistic heatmaps, fleet sensor data, and predictive analytics — all with a privacy-by-design architecture (no raw coordinates or plaintext PII ever stored).

---

## ✨ Features

### For Drivers
- **Live Probability Heatmap** — color-coded zones (red → yellow → green) show your chance of finding a spot, with map legend and freshness indicators
- **Smart Route Comparison** — direct vs. smart route with time/probability tradeoff
- **Turn-by-Turn Navigation** — arrival countdown, voice feedback, auto-reroute when probability drops
- **Driving Mode** — distraction-free safety UI activated while driving (large targets, voice guidance, locked interactions)
- **Parking Confirmation** — 1-tap arrival feedback that improves prediction accuracy for everyone
- **Private Garage Booking** — compare prices, check live availability, book with encrypted license plate
- **CO₂ Impact Dashboard** — track emissions saved by reduced cruising
- **Zone List with Smart Sorting** — by probability, freshness, ETA, or legal risk

### For Fleet Partners
- **Fleet Telemetry Dashboard** — device health, data quality scores, reward estimates
- **Live Sensor Feed** — real-time privacy-filtered event stream

### For Municipalities
- **District Analytics** — occupancy, cruising pressure, illegal parking risk
- **Enforcement Priority** — data-driven enforcement recommendations with peak hour prediction

---

## 🔐 Privacy by Design (GDPR)

| Rule | Implementation |
|------|----------------|
| No raw coordinates stored | All locations anonymized to H3-style cell index (`PrivacyEngine.anonymizeLocation`) |
| No plaintext license plates | AES-256-GCM encryption at rest (`BookingRepository` + `SecurityManager`) |
| Consent before data ops | `UserConsent` entity + `ConsentDao` + validity checks |
| TTL auto-deletion | 20-min TTL for real-time events, 7-day for trends |
| No PII in sensor payloads | `PrivacyEngine.validateEdgePayload` rejects images/device IDs |

---

## 🏗️ Architecture

```
app/src/main/java/com/example/
├── MainActivity.kt          # Entry point + runtime permissions + theme
├── MainNavigation.kt        # NavHost — all routes
├── api/                     # Production WebSocket client (OkHttp)
├── data/
│   ├── Models.kt            # Entities & enums (ParkingZone, Booking, H3CellEntity, …)
│   ├── MockData.kt          # Demo dataset
│   └── local/               # Room DB v4 + DAOs
├── domain/
│   ├── ParkingRepository.kt         # Zone ranking & feedback
│   ├── BookingRepository.kt         # Bookings + plate encryption
│   ├── PrivacyEngine.kt             # Anonymization, encryption, consent, TTL
│   ├── SecurityManager.kt           # Encrypted prefs, cert pinning, anon device ID
│   ├── HeatmapWebSocketService.kt   # Real-time stream + exponential backoff
│   ├── RealTimeSimulationService.kt # Live probability simulation
│   ├── ParkingScoringEngine.kt      # Probability scoring
│   ├── PredictiveAnalyticsEngine.kt # Peak hours, CO₂ savings
│   └── FleetTelemetryEngine.kt      # Device health & data quality
├── ui/
│   ├── screens/             # 20+ Compose screens (Map, Route, Booking, Fleet, …)
│   ├── components/          # Shared composables (map views, dialogs, charts)
│   └── theme/               # CDL (Curbflow Design Language) tokens
└── util/                    # LocationManager, ViewModelFactory, Notifications
```

**Pattern**: MVVM + Repository · **State**: StateFlow/SharedFlow · **DB**: Room · **UI**: Jetpack Compose + Material 3 · **Maps**: osmdroid (OpenStreetMap)

See [ARCHITECTURE.md](ARCHITECTURE.md) for full details.

---

## 🚀 Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio) (Ladybug or newer), JDK 17+

1. Open the project in Android Studio
2. Create `.env` from `.env.example` (set `GEMINI_API_KEY` if you use AI features)
3. Build & run:

```powershell
.\gradlew.bat assembleDebug      # build
.\gradlew.bat installDebug       # install on connected device/emulator
.\gradlew.bat testDebugUnitTest  # run unit tests
```

> The app runs fully offline with simulated real-time data — no backend or API key required for the demo experience.

---

## 🧪 Testing

| Suite | Coverage |
|-------|----------|
| `PrivacyEngineTest` | Anonymization, AES round-trip, IV uniqueness, consent, TTL |
| `ParkingScoringEngineTest` | Probability scoring logic |
| `PredictiveAnalyticsEngineTest` | Peak hour & CO₂ predictions |
| `FleetTelemetryEngineTest` | Device health states |
| `MapViewModelUiTest` | Driving mode hysteresis |
| `ZoneDaoTest` | Room persistence |

---

## 📋 Permissions

| Permission | Why |
|------------|-----|
| `INTERNET`, `ACCESS_NETWORK_STATE` | Map tiles, live data stream |
| `ACCESS_FINE/COARSE_LOCATION` | GPS speed → driving mode detection (optional; app degrades gracefully if denied) |
| `POST_NOTIFICATIONS` | Spot availability alerts (optional) |

---

## ⚠️ Disclaimer

Parking probabilities are predictions, not guarantees. Always follow local traffic laws and signage. Booking/payment flows are currently simulated.
