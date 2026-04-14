# MoisesTest

A music player Android app that lets users search for songs via the iTunes Search API, browse albums, and play 30-second previews.

---

## Architecture

The project follows **MVVM + Clean Architecture**, organised by feature with a shared module for cross-cutting concerns.

```
app/
├── shared/          # Domain models, repository interfaces, Room entity & DAO
├── home/            # Recently played list and song search
├── song/            # Full-screen song player
├── album/           # Album detail and track list
├── player/          # Audio playback engine (ExoPlayer + MediaSession)
├── navigation/      # Nav graph and route definitions
└── ui/              # Shared Compose components and theme
```

### Layers

**Presentation** — Compose screens observe a `StateFlow<UiState>` exposed by a `@HiltViewModel`. All UI state, including bottom sheet visibility, lives in the ViewModel.

**Domain** — Use cases contain the business logic. Each use case file also defines its own result and error sealed interfaces, keeping them co-located with the code that produces them.

**Data** — Repository implementations wire together a Room local database (single source of truth) and a Retrofit remote data source. The database is the only thing the domain layer reads from; network responses are written to it and never returned directly.

---

## Features

- **Song search** — Searches the iTunes API with a 300 ms debounce. Results are paginated and cached in Room.
- **Recently played** — Songs are marked as played the moment the player loads their preview. The home screen shows them ordered by most recently played, driven by a Room `Flow`.
- **Full-screen player** — Displays artwork, title, artist, a seek slider with elapsed/remaining time, and play/pause, skip previous, skip next, and repeat controls.
- **Album view** — Shows album artwork, track list and artist info. Navigating to a song from an album enables the skip previous/next controls in the player.
- **Mini player** — A persistent bar at the bottom of the app, visible whenever a song is loaded. Shows artwork, title, artist, a thin progress bar, and inline play/pause and dismiss controls. Slides in/out with an animation and tapping it navigates to the full-screen player.
- **Song options bottom sheet** — Available from both the home list and the full-screen player. Provides a shortcut to navigate to the song's album.

---

## Libraries

| Library | Purpose |
|---|---|
| **Jetpack Compose + Material 3** | UI toolkit |
| **Navigation Compose** | In-app navigation |
| **Hilt** | Dependency injection |
| **Room** | Local database and reactive queries |
| **Retrofit 3** | HTTP client for the iTunes Search API |
| **Kotlinx Serialization** | JSON deserialisation |
| **OkHttp Logging Interceptor** | Network request logging |
| **Coil 3** | Async image loading |
| **Media3 ExoPlayer** | Audio playback |
| **Media3 MediaSession** | Background playback and system media controls |
| **Splash Screen** | Android 12+ splash screen support |
| **MockK** | Mocking in unit tests |
| **Kotlinx Coroutines Test** | Coroutine testing utilities |
