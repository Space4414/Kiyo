# AGENTS.md — Kiyo Agent Change Log

This file is maintained automatically by every agent session that modifies this repository.
**Append a new entry at the top of the "Entries" section for every session — do not edit past entries.**

---

## Purpose

Future agents: read the **most recent entry first** to understand the current repo state, then
scan earlier entries for design decisions and architecture rationale. This file is the single
source of ground truth for "what changed and why" across sessions.

---

## Entry Format

```
### <ISO-8601 UTC timestamp> — <Short title>
**Agent:** <agent name / role>
**Motivation:** <why this change was made>
**State after:** <one-line repo state after the change>

#### Files Added / Modified / Deleted
| Action | Path | Purpose |
|--------|------|---------|
| ADD    | path/to/file | what it does |
| MOD    | path/to/file | what changed |
| DEL    | path/to/file | why removed |

#### Design Decisions
- Bullet points for non-obvious choices a future agent couldn't infer from the code.

#### Notes for Future Agents
- Anything a future agent must know before touching this area of code.
```

---

## Entries (newest first)

---

### 2026-05-22T14:00:00Z — Fix: Android 7 startup crash (SecurityException + unguarded Future)

**Agent:** Main Agent (Replit)
**Motivation:** User reported app crashes immediately on Android 7 (API 24) — black screen then close.

**Root causes identified:**
1. `READ_EXTERNAL_STORAGE` is a dangerous permission requiring runtime grant on API 23+. The app started `syncLibrary()` immediately without the permission being granted, causing `ContentResolver.query()` to throw `SecurityException` which escaped a bare `launch {}` block and crashed the process via the thread's `UncaughtExceptionHandler`.
2. `controllerFuture?.get()` inside the `ListenableFuture` listener was not guarded — if Media3 failed to bind the service, calling `.get()` throws `ExecutionException` on the calling thread (which may be Main), crashing the app.

**Fix summary:**
- `MusicRepository.syncLibrary()`: wrapped `scanner.scan()` in `try/catch(SecurityException)` and general `Exception`; scan returns early with a `Log.w` instead of throwing.
- `PlayerViewModel`: wrapped `controllerFuture?.get()` in `try/catch`; wrapped the `launch { syncLibrary() }` body in `try/catch`; exposed `refreshLibrary()` as a public method.
- `MainActivity`: added `registerForActivityResult(RequestPermission)` launcher; on first run, requests `READ_MEDIA_AUDIO` (API 33+) or `READ_EXTERNAL_STORAGE` (API 21-32); calls `playerViewModel.refreshLibrary()` on grant. `viewModels()` and `hiltViewModel()` both resolve to the same ViewModel instance via Activity's `ViewModelStore`.

**State after:** CI triggered — awaiting result.

#### Files Modified

| Action | Path | What Changed |
|--------|------|--------------|
| MOD | `app/src/main/java/com/space4414/kiyo/MainActivity.kt` | Added `storagePermissionLauncher`, `checkAndRequestStoragePermission()`, API-level permission selection |
| MOD | `app/src/main/java/com/space4414/kiyo/data/repository/MusicRepository.kt` | `syncLibrary()` wrapped in try-catch for `SecurityException` + general `Exception`; per-track catch to skip malformed rows; post-scan cleanup catch |
| MOD | `app/src/main/java/com/space4414/kiyo/ui/viewmodel/PlayerViewModel.kt` | `connectToService()` wrapped in try-catch; listener `get()` wrapped in try-catch; `refreshLibrary()` public method added; init-block `syncLibrary()` call wrapped |

#### Notes for Future Agents

- **Permission flow**: `MainActivity` requests permission → on grant → `playerViewModel.refreshLibrary()`. The ViewModel's `init` block's scan is safe on first launch even without permission (returns empty silently).
- **`viewModels()` + `hiltViewModel()`**: Both use the Activity's `ViewModelStore`; resolves to the same instance — this is the correct pattern for getting a ViewModel in Activity AND Compose scope simultaneously.
- **`ListenableFuture.get()` in Media3**: Always wrap in try/catch — it throws `ExecutionException` on failure, not a graceful null.
- **`viewModelScope.launch` exception behaviour**: With `SupervisorJob`, unhandled exceptions from `launch` children escape to the thread's `UncaughtExceptionHandler`. Always put `try/catch` inside `launch {}` blocks that perform I/O.

---

### 2026-05-22T13:06:00Z — Fix: SortedMap compilation error in LastFmScrobbler

**Agent:** Main Agent (Replit)
**Motivation:** CI run `26289344358` failed — Kotlin compiler could not resolve `SortedMap` reference (unimported `java.util.SortedMap`).
**State after:** CI run `26289620818` — **success**. Debug APK `kiyo-debug-apk` (~20 MB) uploaded as artifact.

#### Files Modified

| Action | Path | What Changed |
|--------|------|--------------|
| MOD | `app/src/main/java/com/space4414/kiyo/integration/lastfm/LastFmScrobbler.kt` | Changed `sign()` parameter from `SortedMap<String,String>` → `Map<String,String>`; added `.sortedBy { it.key }` inline so the signature is still deterministic |

#### Notes for Future Agents

- `SortedMap` (from `java.util`) is not auto-imported in Kotlin Android. Prefer `Map<K,V>` parameters and sort at call-site if ordering is needed.
- CI badge: `https://github.com/Space4414/Kiyo/actions/workflows/ci.yml`
- Debug APK download: Actions tab → run `26289620818` → artifact `kiyo-debug-apk`

---

### 2026-05-22T00:00:00Z — Phase 2: Full Core Architecture Build

**Agent:** Main Agent (Replit)
**Motivation:** Phase 2 — generate and push complete Android project per user specification.
**State after:** Full compilable Android project on `main`; GitHub Actions CI triggered.

#### Files Added

| Action | Path | Purpose |
|--------|------|---------|
| ADD | `.github/workflows/ci.yml` | GitHub Actions: JDK 17 + Android SDK + Gradle 8.6 → assembleDebug |
| ADD | `settings.gradle.kts` | Gradle project settings, module includes (`:app`) |
| ADD | `build.gradle.kts` | Root plugin declarations — AGP 8.3.2, Kotlin 1.9.23, Hilt 2.51.1, KSP |
| ADD | `gradle.properties` | JVM 4 GB heap, parallel build, AndroidX, non-transitive R |
| ADD | `gradle/wrapper/gradle-wrapper.properties` | Gradle 8.6 distribution URL (CI uses direct install, not wrapper) |
| ADD | `app/build.gradle.kts` | App module: minSdk 21, targetSdk 35, Compose, Media3, Room, Hilt, OkHttp, Coil, DataStore |
| ADD | `app/proguard-rules.pro` | Keep rules for Kiyo, Room, Media3, OkHttp |
| ADD | `app/src/main/AndroidManifest.xml` | Permissions, PlaybackService declaration (foregroundServiceType=mediaPlayback), MainActivity |
| ADD | `.gitignore` | Standard Android gitignore |
| ADD | `app/src/main/java/.../KiyoApplication.kt` | `@HiltAndroidApp` application class |
| ADD | `app/src/main/java/.../MainActivity.kt` | Single-activity host with edge-to-edge + `KiyoNavGraph` |
| ADD | `app/src/main/java/.../data/db/AppDatabase.kt` | Room DB, version 1, 3 entities |
| ADD | `app/src/main/java/.../data/db/entity/TrackEntity.kt` | Track row: title, rawArtist, album, duration, filePath, playCount |
| ADD | `app/src/main/java/.../data/db/entity/ArtistEntity.kt` | Artist row: name (unique index), trackCount |
| ADD | `app/src/main/java/.../data/db/entity/TrackArtistCrossRef.kt` | M2M junction (track_id, artist_id) with CASCADE deletes |
| ADD | `app/src/main/java/.../data/db/dao/TrackDao.kt` | CRUD + cross-ref insert + artist-filtered + album-filtered queries |
| ADD | `app/src/main/java/.../data/db/dao/ArtistDao.kt` | CRUD + refreshTrackCounts + deleteOrphaned |
| ADD | `app/src/main/java/.../data/parser/ArtistParser.kt` | **Poweramp-style multi-artist parser** — see design decisions below |
| ADD | `app/src/main/java/.../data/repository/MediaStoreScanner.kt` | MediaStore `Audio.Media` cursor scanner, API-21-safe URI selection |
| ADD | `app/src/main/java/.../data/repository/MusicRepository.kt` | Full library sync: scan → parse → upsert tracks/artists/crossrefs → prune stale |
| ADD | `app/src/main/java/.../service/NotificationHelper.kt` | Notification channel creation guarded by `Build.VERSION_CODES.O` check |
| ADD | `app/src/main/java/.../service/PlaybackService.kt` | `MediaSessionService` (Media3) + ExoPlayer; attaches DiscordRpcClient + LastFmScrobbler listeners |
| ADD | `app/src/main/java/.../integration/discord/DiscordRpcClient.kt` | Discord RPC payload builder + HTTP dispatcher (no local IPC — see note) |
| ADD | `app/src/main/java/.../integration/lastfm/ScrobbleCache.kt` | DataStore-backed offline scrobble queue (max 500, serialised JSON) |
| ADD | `app/src/main/java/.../integration/lastfm/LastFmScrobbler.kt` | NowPlaying + scrobble client with 50% threshold, offline cache, auto-flush on reconnect |
| ADD | `app/src/main/java/.../ui/theme/Color.kt` | Kiyo colour palette: charcoal base, teal/purple/amber accents, surface/outline tokens |
| ADD | `app/src/main/java/.../ui/theme/Theme.kt` | `KiyoTheme` Compose wrapper around `darkColorScheme` |
| ADD | `app/src/main/java/.../ui/theme/Typography.kt` | Type scale: displaySmall → labelMedium |
| ADD | `app/src/main/java/.../ui/component/AmbientBackdrop.kt` | Canvas-only multi-point radial gradient backdrop — **zero OS blur dependency** |
| ADD | `app/src/main/java/.../ui/component/FrostedCard.kt` | Hardware-safe translucent card (#1AFFFFFF fill + #26FFFFFF 1dp outline) |
| ADD | `app/src/main/java/.../ui/screen/LibraryScreen.kt` | Full track list with mini-player bar and empty state |
| ADD | `app/src/main/java/.../ui/screen/PlayerScreen.kt` | Now-playing screen: album art placeholder, seek bar, animated play/pause controls |
| ADD | `app/src/main/java/.../ui/screen/QueueScreen.kt` | Scrollable queue sheet with active-track highlight |
| ADD | `app/src/main/java/.../ui/viewmodel/PlayerViewModel.kt` | `AndroidViewModel` + `MediaController` client + `MusicRepository` sync |
| ADD | `app/src/main/java/.../ui/navigation/KiyoNavGraph.kt` | 3-route nav graph: library → player → queue |
| ADD | `app/src/main/java/.../di/AppModule.kt` | Hilt `@Module`: provides `AppDatabase`, `TrackDao`, `ArtistDao`, `Context` |
| ADD | `app/src/main/res/values/strings.xml` | String resources |
| ADD | `app/src/main/res/values/colors.xml` | XML colour resources |
| ADD | `app/src/main/res/values/themes.xml` | `Theme.Kiyo` (Material NoActionBar + transparent status/nav bars) |
| ADD | `app/src/main/res/drawable/ic_launcher_background.xml` | Solid charcoal launcher background |
| ADD | `app/src/main/res/drawable/ic_launcher_foreground.xml` | Teal play-button vector launcher foreground |
| ADD | `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | Adaptive icon manifest |
| ADD | `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml` | Adaptive round icon manifest |

#### Design Decisions

- **No OS-level blur anywhere.** `AmbientBackdrop` and `FrostedCard` use pure Canvas paint
  (radial gradients + alpha fills) to guarantee 0% lag on Android 5.0 (API 21) devices.
  `RenderEffect`/`BlurMaskFilter` is NOT used.

- **ArtistParser whitelist-first algorithm.** Full-string exception check runs before any
  tokenisation. Per-segment exception guard runs inside the token-split loop so a whitelisted
  band name that appears after a valid delimiter is also protected.

- **M2M cross-ref for multi-artist indexing.** Splitting "AC/DC; Bob Dylan" produces two
  `ArtistEntity` rows linked to the same `TrackEntity` via `TrackArtistCrossRef`. Querying
  by artist uses an inner-join rather than `LIKE '%name%'` to avoid false matches.

- **Discord RPC over HTTP, not local IPC.** Android cannot open Discord's Unix domain socket
  (desktop-only). `DiscordRpcClient` posts standard Gateway Presence Update payloads to a
  user-configured webhook or bridge URL. Set `endpointUrl` from Settings.

- **Media3 `MediaSessionService` handles foreground service automatically.** No manual
  `startForeground()` / `stopForeground()` calls needed — Media3 promotes/demotes the service
  based on player state. `NotificationHelper.createChannels()` is still called on `onCreate()`
  before Media3 posts its notification, guarded by `Build.VERSION_CODES.O`.

- **CI uses `gradle assembleDebug` (not `./gradlew`).** The Gradle wrapper jar is binary and
  cannot be committed via GitHub Contents API. The CI workflow installs Gradle 8.6 via
  `gradle/actions/setup-gradle@v3` with `gradle-version: "8.6"` and calls `gradle` directly.

#### Notes for Future Agents

- **To extend the artist exception list:** edit `ArtistParser.DEFAULT_EXCEPTIONS` in
  `data/parser/ArtistParser.kt`. Do not change the split algorithm.
- **To add a new split delimiter:** add to `ArtistParser.DEFAULT_TOKENS`. Tokens are sorted
  longest-first automatically.
- **To add Last.fm auth flow:** the `LastFmScrobbler` already accepts `apiKey`, `apiSecret`,
  `sessionKey`. Add a Settings screen that runs the Last.fm web-auth OAuth dance and
  writes the resulting session key into `DataStore`/`LastFmScrobbler`.
- **To enable Discord RPC:** add a Settings field that sets `DiscordRpcClient.endpointUrl`
  to the user's Discord webhook or local bridge URL.
- **`minSdk = 21` constraint:** do not use any API that requires > 21 without a
  `Build.VERSION.SDK_INT >= Build.VERSION_CODES.X` guard. Search for existing guards with:
  `grep -r "VERSION_CODES" app/src/`
- **DB migrations:** `AppDatabase` uses `fallbackToDestructiveMigration()` for now (dev only).
  Before any production release, add proper `Migration` objects and remove the fallback.

---

### 2026-05-22T00:00:00Z — Phase 1: Repository Scaffold

**Agent:** Main Agent (Replit)
**Motivation:** Initial repository provisioning — establish upstream connection via GitHub API.
**State after:** Empty repo with README on `main`.

#### Files Added

| Action | Path | Purpose |
|--------|------|---------|
| ADD | `README.md` | Project overview, feature list, app ID, SDK targets |

#### Notes for Future Agents

- Auth method: GitHub Contents API over HTTPS Bearer token. Token stored in Replit Secrets as `GITHUB_TOKEN`.
- Repo: `https://github.com/Space4414/Kiyo` — public, default branch `main`.
