# CurbFlow AI — Production Roadmap

> **Honest status (June 2026)**: This is a polished MVP/demo, NOT production-scale.
> This document is the gap analysis and execution plan to change that.

---

## 1. Brutally Honest Gap Analysis

### 1.1 What is fake/simulated today

| Component | Reality | Production requirement |
|-----------|---------|------------------------|
| Parking data | `MockData.kt` — 8 hardcoded Athens zones | Real backend + fleet sensor ingestion |
| "H3 indexing" | String concatenation, not real H3 | `com.uber:h3` library (real hexagonal cells) |
| WebSocket | Simulated `Random` stream, fake endpoint | Real `wss://` backend + OkHttp WebSocket |
| Probability engine | Random drift ±5-15% every 30s | ML model fed by real sensor events |
| Encryption key | Static seed string in source code | Android Keystore-backed key |
| Cert pinning | Placeholder `AAAA...=` hashes | Real server cert SHA-256 pins |
| Voice guidance | Text overlay, no audio | Android `TextToSpeech` engine |
| Payments/booking | Pure UI simulation | Stripe/Google Pay + real garage partner API |
| GPS speed | Wired but emulator gives no movement | Field-tested driving detection |

### 1.2 What is missing entirely

| Area | Missing | Severity |
|------|---------|----------|
| **Backend** | Everything — API, DB, ML pipeline, fleet ingestion | 🔴 BLOCKER for real product |
| **Auth** | No user accounts, no identity | 🔴 |
| **Crash reporting** | No Crashlytics/Sentry | 🔴 |
| **CI/CD** | No pipeline at all | 🔴 |
| **R8/ProGuard** | Release builds unobfuscated, unminified | 🔴 |
| **DB migrations** | Destructive fallback wipes user data | 🔴 |
| **Localization** | Hardcoded English strings in composables | 🟠 |
| **Offline handling** | No network state UX | 🟠 |
| **DI framework** | Manual `ViewModelFactory` | 🟠 |
| **Analytics** | None (even privacy-compliant) | 🟠 |
| **Store readiness** | No privacy policy URL, data safety form, screenshots | 🟠 |
| **Performance** | No baseline profiles, no startup tracing | 🟡 |
| **Instrumented tests** | Only unit tests run | 🟡 |

---

## 2. Execution Plan

### Phase P — Client Production Hardening (CURRENT — in this repo, no backend needed)

| # | Task | Status |
|---|------|--------|
| P1 | Room proper `Migration` objects, remove `fallbackToDestructiveMigration` | ⏳ |
| P2 | R8 minification + resource shrinking + ProGuard rules for release | ⏳ |
| P3 | Real `TextToSpeech` voice guidance (Driving Mode + Turn-by-Turn) | ⏳ |
| P4 | `NetworkMonitor` (ConnectivityManager callback) + offline banner UX | ⏳ |
| P5 | GitHub Actions CI: build + unit tests on every push/PR | ⏳ |
| P6 | Android Keystore-backed AES key (eliminate static seed) | ⏳ |
| P7 | Accessibility prefs actually re-theme (colorblind palette, font scale) | ⏳ |

### Phase Q — Data Layer Realism (client-side, backend-pluggable)

| # | Task |
|---|------|
| Q1 | Real `com.uber:h3` dependency; replace fake index generation |
| Q2 | Repository **interfaces** (per original master plan A1) — `ParkingDataSource` etc., with `Local*` impls; Firebase/REST impls plug in later |
| Q3 | Retrofit API client with full endpoint definitions + graceful fallback to local simulation when unreachable (`DataSourceStrategy`) |
| Q4 | DataStore (typed, async) replacing SharedPreferences for app prefs |
| Q5 | String resources extraction → `strings.xml` (en) + `values-el/strings.xml` (Greek) |

### Phase R — Observability & Quality

| # | Task |
|---|------|
| R1 | Firebase Crashlytics + Analytics (privacy-filtered, H3-level only) |
| R2 | Timber structured logging (strip in release via R8) |
| R3 | detekt + ktlint in CI |
| R4 | Instrumented test suite (Compose UI tests on emulator in CI) |
| R5 | Baseline Profiles for startup performance |
| R6 | LeakCanary in debug builds |

### Phase S — Backend (separate repo/services — out of scope for this client repo)

| # | Component |
|---|-----------|
| S1 | API service (Kotlin/Ktor or Node) — zones, probability, bookings, auth |
| S2 | PostgreSQL + PostGIS + H3 extension |
| S3 | WebSocket fan-out service (heatmap updates) |
| S4 | ML pipeline: sensor events → probability model (start with Bayesian baseline) |
| S5 | Fleet ingestion API + edge payload validation |
| S6 | Auth (Firebase Auth or Keycloak) |
| S7 | Garage partner integration API |

### Phase T — Store Launch

| # | Task |
|---|------|
| T1 | Privacy policy + Data Safety form + GDPR DPA |
| T2 | Play Console setup, signed AAB, staged rollout |
| T3 | Store assets (screenshots, feature graphic, description) |
| T4 | Beta program (internal → closed → open) |
| T5 | versionCode automation in CI |

---

## 3. Definition of "Production-Ready Client" (Phase P+Q+R complete)

- [ ] Zero data loss across app updates (tested migrations)
- [ ] Release build minified, obfuscated, < 15 MB
- [ ] Crash-free rate measurable (Crashlytics wired)
- [ ] CI green on every commit (build + tests + lint)
- [ ] All secrets in Keystore/CI secrets — zero in source
- [ ] Offline mode: clear UX, cached data usable
- [ ] Real TTS, real H3, real network stack (even if pointed at staging)
- [ ] el + en localization
- [ ] WCAG 2.1 AA: colorblind palettes, 48dp targets, contrast 4.5:1
