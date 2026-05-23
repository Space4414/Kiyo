# AGENTS.md — Kiyo Autonomous Engineering Contract

  ## Project
  Kiyo is a Material 3 Android music player built with Jetpack Compose, ExoPlayer / Media3,
  Room, Hilt, and DataStore. All source lives under `app/src/main/java/com/space4414/kiyo/`.

  ## CI / Alpha pipeline
  - **ci.yml** — compile-check for PRs only (`compileAlphaDebugKotlin`).
  - **alpha.yml** — triggered on push to `main`; builds the rolling `alpha-latest` APK.
  - Always push to `main` (never raise PRs for autonomous work).
  - Prefer one atomic tree-API commit per session so CI fires exactly once.
  - Poll `GET /repos/Space4414/Kiyo/actions/runs` after push; wait for `status=completed`.
  - A run is healthy only when `conclusion=success`.

  ## Architecture quick-ref

  | Layer         | Key classes |
  |---|---|
  | Playback      | `PlaybackService` (Media3 `MediaSessionService` + ExoPlayer) |
  | State sync    | `PlayerViewModel` → `PlayerUiState` (StateFlow) |
  | Settings      | `AppSettings` → `SettingsRepository` (DataStore) → `SettingsViewModel` |
  | Persistence   | Room (`TrackEntity`, `MusicDatabase`) |
  | Images        | Coil (`AlbumArtBox`) |
  | Scrobbling    | `LastFmScrobbler` + `ScrobbleCache` |
  | Discord RPC   | `DiscordRpcClient` (webhook mode) |
  | DI            | Hilt (`@AndroidEntryPoint`, `@HiltViewModel`, `@Singleton`) |

  ## Change log

  ### Session 2025-05-23 — Architectural overhaul (Replit Agent)

  **1. State desynchronisation fix (`PlayerViewModel.kt`)**
  - Added `onMediaItemTransition` listener: syncs `currentTrack` from the queue list
    by `currentMediaItemIndex` whenever ExoPlayer auto-advances (was missing → frozen UI).
  - Added `onPlaybackStateChanged`: captures the real `duration` from ExoPlayer once
    the track is in `STATE_READY` (duration is `-1` until buffering completes).
  - Added `startPositionPolling()`: a 1-second coroutine loop that updates `positionMs`
    and `durationMs` while the player is actively playing. Drives smooth seek-bar motion.
  - All three fixes together eliminate the "frozen UI / track counter stuck" bug.

  **2. Crossfade engine (`PlaybackService.kt`)**
  - `CrossfadeListener` (inner class) implements `Player.Listener.onMediaItemTransition`.
  - On each transition it cancels any in-progress fade job, sets `player.volume = 0`,
    then linearly ramps to 1.0 over 300 ms (20 steps × 15 ms) via a Hilt-injected
    `serviceScope` coroutine.
  - Controlled by `AppSettings.crossfadeEnabled` (read once at `onCreate`).
  - Note: single-engine fade-IN; true simultaneous crossfade requires two ExoPlayer instances.
  - `SettingsRepository` injected so crossfade setting is persistent across restarts.

  **3. APK size optimisation (`app/build.gradle.kts`)**
  - Removed `androidx.compose.material:material-icons-extended` (~5–8 MB in debug APK).
    All formerly-extended icons replaced with lightweight XML vector drawables in
    `app/src/main/res/drawable/ic_kiyo_*.xml`.
  - Removed `androidx.palette:palette-ktx` (~200 KB; was not wired to any live UI).
  - Updated `proguard-rules.pro` with OkHttp, Hilt, kotlinx-serialization keep rules,
    and `android.util.Log` stripping for release builds.

  **4. Icon system (`res/drawable/ic_kiyo_*.xml`)**
    14 new XML vector drawables cover all icons previously sourced from the extended
    icon library.

  **5. Album art placeholder (`AlbumArtBox.kt`)**
  - Added `fallbackLabel` parameter showing first alphanumeric char as a teal letter.

  **6. Settings expansion**
  New persistent DataStore fields: crossfadeEnabled, customDelimiters, lastFm*, discordRpc*.

  **7. Settings UI (`SettingsScreen.kt`)**
  Four new sections: Audio Crossfade, Artist Parser, Last.fm Scrobbling, Discord Rich Presence.

  ---

  ### Session 2026-05-23 — Poweramp-inspired UI overhaul for low-end device perf (Replit Agent)

  **Goal**: Optimise UI for Huawei P9 Lite (Android 7, 3 GB RAM, Kirin 650).
  Reference app: Poweramp — runs smoothly on the same hardware.

  **Root cause of sluggishness**: `AmbientBackdrop` + `FrostedCard` + `KiyoGlassPanel` +
  `LegacyGlassEngine` combined apply a per-frame bitmap blur pipeline and multiple
  alpha-composited translucent layers. These saturate the GPU on Android 7 devices.

  **What changed**:

  **1. `AmbientBackdrop.kt`** — gutted to a single `Box(background(Color.Black))`.
  No blur sampler, no radial gradient Canvas draws, no LegacyGlassEngine calls.
  One opaque rectangle = near-zero GPU cost on any hardware.

  **2. `FrostedCard.kt`** — replaced the three-layer glass stack with a flat
  `Color(0xFF1C1C1E)` box. Zero alpha compositing.

  **3. `KiyoGlassPanel.kt`** — flattened to the same dark surface. `LocalBlurredAmbient`
  kept as a null-providing CompositionLocal so call sites compile unchanged.

  **4. `BottomNavBar.kt`** — Poweramp-style icon-only nav bar.
  - Removed text labels (saves layout measurement every frame).
  - Removed FrostedCard container; replaced with flat `Color(0xFF0D0D0D)` row.
  - Active icon tinted KiyoTeal; inactive icon `Color(0xFF6E6E73)`.
  - Reordered items: Home → Library → Search → DSP (more natural for a music player).

  **5. `MiniPlayer.kt`** — Poweramp-style flat dark bar.
  - Removed FrostedCard pill / circular play button background.
  - Flat `Color(0xFF1C1C1E)` background, white icon with no container.
  - Progress shown as a thin 3dp rounded-rectangle bar below the info row
    (ProgressTrack = 0xFF3A3A3C, fill = 0xFF6E6E73) — matches Poweramp's pill indicator.
  - Subtitle format: "Artist — Album" in a single text view.

  **6. `LibraryScreen.kt`** — Poweramp "All Songs" redesign.
  - Pure black background, no AmbientBackdrop.
  - Section header: large circle icon (blue, 52dp) + bold "All Songs" title + track count.
  - Action row: circular icon pills (Shuffle + Play All) — Poweramp action bar style.
  - Track rows: flat black background, 56dp album art (rounded 6dp), bold title,
    "Artist — Album" subtitle, duration in small gray text.
  - 0.5dp dividers starting after album art column (76dp offset) — Poweramp style.
  - Active track highlighted with `Color(0xFF1A2C2A)` row background + KiyoTeal title.

  **7. `HomeScreen.kt`** — simplified to a LazyColumn (no chunked grid).
  - Previously used `tracks.chunked(2)` grid + nested FrostedCards = O(n) lazy re-compositions.
  - Now: flat list identical to LibraryScreen rows, with a Recently Played horizontal strip.

  **8. `SearchScreen.kt`** — flat redesign.
  - Search pill: `RoundedCornerShape(28)` + `Color(0xFF1C1C1E)` background.
  - Flat result rows matching the track row style.

  **9. `QueueScreen.kt`** — flat redesign matching LibraryScreen row style.

  **10. `Color.kt`** — added `KiyoPureBlack`, `KiyoDarkSurface`, `KiyoDarkCard`.
  `KiyoCharcoal` aliased to `Color.Black` so existing references still compile.

  **11. `Theme.kt`** — background updated to `KiyoPureBlack`.

  **Files changed in this session:**
  ```
  app/src/main/java/com/space4414/kiyo/ui/theme/Color.kt
  app/src/main/java/com/space4414/kiyo/ui/theme/Theme.kt
  app/src/main/java/com/space4414/kiyo/ui/component/AmbientBackdrop.kt
  app/src/main/java/com/space4414/kiyo/ui/component/FrostedCard.kt
  app/src/main/java/com/space4414/kiyo/ui/component/KiyoGlassPanel.kt
  app/src/main/java/com/space4414/kiyo/ui/component/BottomNavBar.kt
  app/src/main/java/com/space4414/kiyo/ui/component/MiniPlayer.kt
  app/src/main/java/com/space4414/kiyo/ui/screen/LibraryScreen.kt
  app/src/main/java/com/space4414/kiyo/ui/screen/HomeScreen.kt
  app/src/main/java/com/space4414/kiyo/ui/screen/SearchScreen.kt
  app/src/main/java/com/space4414/kiyo/ui/screen/QueueScreen.kt
  AGENTS.md
  ```

  ## What to implement next (ideas for future sessions)
  - **Player screen redesign**: Poweramp-style full-screen player with album art,
    seek bar, large play/pause, previous/next — matching Poweramp's player layout.
  - **DSP/EQ screen redesign**: Poweramp-style vertical band sliders with neon-green
    indicator lines and preamp control.
  - **Artists / Albums tabs**: Dedicated screens with circular artist photos and
    album grid — similar to Poweramp's Library categories.
  - **Alphabetical fast-scroll**: Side letter bar for quick navigation in large lists
    (visible in Poweramp Folders/Years screens).
  - **Swipe gestures**: Swipe left/right on mini player to skip tracks.
  - **Now-playing background tint**: Subtle dynamic color derived from album art
    (lightweight palette extraction using a single-pixel scaled bitmap — no palette lib).

  ## Constraints / invariants
  - Never add secrets or real API keys to tracked files.
  - All network calls go through `OkHttpClient` or Android's `HttpURLConnection`.
  - DataStore is the single source of truth for settings; no SharedPreferences.
  - All coroutines use structured concurrency (viewModelScope / serviceScope).
  - The compilation target for CI is `compileAlphaDebugKotlin`.
  - Do NOT re-introduce glass/blur effects — they are incompatible with Android 7 / 3 GB RAM.
  - Keep LegacyGlassEngine.kt in the codebase (it compiles fine, just unused) until
    a decision is made to delete it; removing it risks merge conflicts with user patches.
  