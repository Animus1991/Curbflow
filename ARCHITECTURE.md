# CurbFlow AI — Architecture Decision Record

> Last updated: June 2026 · App version 1.0 · DB schema v4

## 1. Overview

CurbFlow AI follows **MVVM + Repository** with a strict separation:

```
UI (Compose) → ViewModel (StateFlow) → Repository → Room DAO / Simulation / WebSocket
```

- **No business logic in composables** — screens only render state and dispatch events.
- **Repositories are swappable** — Room-backed today, Firebase/REST-ready by interface design.
- **All real-time data flows through Kotlin Flows** (StateFlow for state, SharedFlow for events).

## 2. Layer Responsibilities

### 2.1 Data Layer (`data/`)
| Component | Responsibility |
|-----------|----------------|
| `Models.kt` | Room entities + domain enums. Single source of truth for shapes. |
| `ParkingDatabase` | Room DB v4: zones, fleet, profiles, bookings, H3 cells, consents, parking events |
| DAOs | Suspend/Flow-based access. TTL-aware queries (`deleteExpired`). |
| `MockData` | Deterministic demo dataset (Athens zones). |

### 2.2 Domain Layer (`domain/`)
| Component | Responsibility |
|-----------|----------------|
| `ParkingRepository` | Zone ranking (via `ParkingScoringEngine`), outcome feedback |
| `BookingRepository` | Booking CRUD + **transparent AES-256-GCM plate encryption** |
| `PrivacyEngine` | H3 anonymization, plate crypto, consent validity, TTL policy, payload validation |
| `SecurityManager` | Encrypted prefs (AES-256-GCM), cert pinning, anonymous device ID |
| `HeatmapWebSocketService` | Live stream w/ exponential backoff (1s→60s max), connection state |
| `RealTimeSimulationService` | Coroutine tick every 30s: probability drift, sensor events, freshness |
| `ParkingScoringEngine` | Composite probability score (supply, demand, freshness, competition) |
| `PredictiveAnalyticsEngine` | Peak hour prediction, CO₂ savings estimation |
| `FleetTelemetryEngine` | Device health (ONLINE/DEGRADED/OFFLINE/MAINTENANCE), data quality |

### 2.3 UI Layer (`ui/`)
- **CDL (Curbflow Design Language)**: Material 3 + custom tokens (`NeonCyan`, `EmeraldLive`, `AmberWarning`, `CrimsonDanger`)
- All colors via `MaterialTheme.colorScheme` or theme tokens — no hardcoded hex in screens
- AutoMirrored icons for RTL support
- Min 48dp touch targets in Driving Mode (anti-distraction)

## 3. Key Decisions (ADRs)

### ADR-001: osmdroid over Google Maps SDK
**Why**: Zero API-key dependency, fully offline-capable map tiles, open data. Google Maps Compose remains a dependency for future migration if needed.

### ADR-002: Simulated real-time over live backend
**Why**: `RealTimeSimulationService` + `HeatmapWebSocketService` (simulated stream) provide the full UX without infrastructure. `api/ParkingWebSocketService` is the production-endpoint client, ready when backend ships. Both honor the same data shapes.

### ADR-003: Privacy-by-design enforced at repository boundary
**Why**: Encryption/anonymization happens in repositories — UI and DAOs never see the crypto. `BookingRepository` encrypts plates on write, decrypts on read; callers are oblivious.

### ADR-004: Manual AES over androidx.security-crypto
**Why**: `security-crypto` is alpha and adds dependency weight. Manual AES-256-GCM with random IV per operation covers the threat model. **TODO(production)**: migrate key storage to Android Keystore.

### ADR-005: ViewModelFactory over Hilt/Koin
**Why**: App-scale doesn't justify DI framework overhead yet. A single factory wires repositories. Revisit if module count grows.

### ADR-006: `fallbackToDestructiveMigration`
**Status**: Accepted for pre-release. **Must** be replaced with proper `Migration` objects before first public release to avoid user data loss.

## 4. Real-time Data Flow

```
RealTimeSimulationService (30s tick)
   └─> ZoneDao.update ──> ParkingRepository.getRankedZones(): Flow
                              └─> MapViewModel._zones: StateFlow
                                    └─> MapScreen / ZoneList / Route / TurnByTurn (collectAsState)

HeatmapWebSocketService (5s simulated emit)
   └─> connectionState: StateFlow ──> MapScreen LIVE/OFFLINE badge
   └─> cellUpdates: SharedFlow ──> (future) H3 hexagon overlay
```

## 5. Driving Safety Model

- GPS speed via `LocationManager` (FusedLocationProvider) → `MapViewModel.speed`
- Driving mode trigger: speed > 15 km/h with **2.5s exit hysteresis** (prevents flicker in stop-and-go)
- `DrivingModeScreen`: simplified UI, voice guidance overlay, locked fine interactions

## 6. Security Posture

| Control | Status |
|---------|--------|
| HTTPS/WSS only | ✅ endpoints defined as `wss://` |
| Certificate pinning | ✅ configured (placeholder pins — set real SHA-256 before prod) |
| Plates encrypted at rest | ✅ AES-256-GCM, fresh IV per op |
| Anonymous device ID | ✅ UUID, no hardware fingerprint |
| No PII in logs | ✅ verified by review |
| Play Integrity | ⏳ placeholder (`isEnvironmentSecure`) |
| Keystore-backed keys | ⏳ TODO before production |

## 7. Known Debt / Roadmap

1. **Room migrations** — replace destructive fallback (ADR-006)
2. **H3 hexagon rendering** — wire `HeatmapWebSocketService.cellUpdates` into a map overlay
3. **Accessibility prefs** — colorblind/high-contrast toggles persist but don't yet re-theme; needs `LocalAccessibilityPrefs` CompositionLocal
4. **Real TTS** — voice guidance is visual simulation; integrate `TextToSpeech` API
5. **Play Integrity API** — replace `isEnvironmentSecure` stub
6. **Version bumping** — automate `versionCode` in CI
