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
  icon library: `ic_kiyo_equalizer_{filled,outline}`, `ic_kiyo_library_music_{filled,outline}`,
  `ic_kiyo_home_{filled,outline}`, `ic_kiyo_search_{filled,outline}`,
  `ic_kiyo_graphic_eq`, `ic_kiyo_shuffle`, `ic_kiyo_folder_lock`,
  `ic_kiyo_blur_on`, `ic_kiyo_palette`, `ic_kiyo_speed`, `ic_kiyo_movie`,
  `ic_kiyo_memory`, `ic_kiyo_arrow_back`, `ic_kiyo_lastfm`, `ic_kiyo_discord`.
  `BottomNavBar.NavItem` migrated from `ImageVector` to `@DrawableRes Int`.

**5. Album art placeholder (`AlbumArtBox.kt`)**
- Added optional `fallbackLabel: String = ""` parameter.
- When art load fails (or albumId ≤ 0), shows the first alphanumeric character of
  `fallbackLabel` as a bold teal letter on the charcoal background — matches the
  frosted-glass aesthetic without any bitmap asset.
- All call sites updated to pass `fallbackLabel = track.album.ifBlank { track.title }`.
- Replaced `MusicNote` icon import (no longer needed; avoids icon-extended dependency).

**6. Settings expansion (`AppSettings.kt`, `SettingsRepository.kt`, `SettingsViewModel.kt`)**
New persistent DataStore fields:
- `crossfadeEnabled: Boolean` (default `true`)
- `customDelimiters: String` — user-added artist split tokens (comma-separated)
- `customUnsplitExceptions: String` — artist names that must never be split
- `lastFmApiKey / lastFmApiSecret / lastFmUsername / lastFmSessionKey: String`
- `discordRpcEnabled: Boolean`, `discordWebhookUrl: String`
Convenience computed properties: `parsedCustomDelimiters`, `parsedCustomExceptions`.

**7. Settings UI (`SettingsScreen.kt`)**
Four new sections added below the existing visual-mode toggles:
- **Audio Crossfade** — toggle bound to `crossfadeEnabled`
- **Artist Parser** — two text fields: custom split tokens + unsplit whitelist
- **Last.fm Scrobbling** — API key, API secret, username, session-key credential fields
- **Discord Rich Presence** — enable toggle + webhook URL text field (shown only when enabled)

**Files changed in this session:**
```
app/src/main/java/com/space4414/kiyo/ui/viewmodel/PlayerViewModel.kt
app/src/main/java/com/space4414/kiyo/service/PlaybackService.kt
app/src/main/java/com/space4414/kiyo/ui/settings/AppSettings.kt
app/src/main/java/com/space4414/kiyo/ui/settings/SettingsRepository.kt
app/src/main/java/com/space4414/kiyo/ui/settings/SettingsViewModel.kt
app/src/main/java/com/space4414/kiyo/ui/screen/SettingsScreen.kt
app/src/main/java/com/space4414/kiyo/ui/component/AlbumArtBox.kt
app/src/main/java/com/space4414/kiyo/ui/component/BottomNavBar.kt
app/src/main/java/com/space4414/kiyo/ui/screen/QueueScreen.kt
app/src/main/java/com/space4414/kiyo/ui/screen/LibraryScreen.kt
app/build.gradle.kts
app/proguard-rules.pro
app/src/main/res/drawable/ic_kiyo_equalizer_filled.xml      (new)
app/src/main/res/drawable/ic_kiyo_equalizer_outline.xml     (new)
app/src/main/res/drawable/ic_kiyo_library_music_filled.xml  (new)
app/src/main/res/drawable/ic_kiyo_library_music_outline.xml (new)
app/src/main/res/drawable/ic_kiyo_home_filled.xml           (new)
app/src/main/res/drawable/ic_kiyo_home_outline.xml          (new)
app/src/main/res/drawable/ic_kiyo_search_filled.xml         (new)
app/src/main/res/drawable/ic_kiyo_search_outline.xml        (new)
app/src/main/res/drawable/ic_kiyo_graphic_eq.xml            (new)
app/src/main/res/drawable/ic_kiyo_shuffle.xml               (new)
app/src/main/res/drawable/ic_kiyo_folder_lock.xml           (new)
app/src/main/res/drawable/ic_kiyo_blur_on.xml               (new)
app/src/main/res/drawable/ic_kiyo_palette.xml               (new)
app/src/main/res/drawable/ic_kiyo_speed.xml                 (new)
app/src/main/res/drawable/ic_kiyo_movie.xml                 (new)
app/src/main/res/drawable/ic_kiyo_memory.xml                (new)
app/src/main/res/drawable/ic_kiyo_arrow_back.xml            (new)
app/src/main/res/drawable/ic_kiyo_lastfm.xml                (new)
app/src/main/res/drawable/ic_kiyo_discord.xml               (new)
AGENTS.md
```

## Constraints / invariants
- Never add secrets or real API keys to tracked files.
- All network calls go through `OkHttpClient` (already in deps) or Android's `HttpURLConnection`.
- DataStore is the single source of truth for settings; no SharedPreferences.
- All coroutines use structured concurrency (viewModelScope / serviceScope).
- The compilation target for CI is `compileAlphaDebugKotlin`.
