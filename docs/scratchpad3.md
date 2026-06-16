Good, I have a thorough picture of the codebase now. Let me produce a comprehensive guide as a markdown document.Here's a full breakdown of the approach and the key design decisions:

**Architecture choice — JWT / stateless.** Since you're already using `RestClient` and a controller-as-interface pattern, stateless JWT fits cleanly. No sessions to manage, the HTTP client just sends a `Bearer` token header, and everything scales horizontally.

**What changes and what doesn't:**
- `SongLibraryService` (interface) and `SongLibraryServiceImpl` — **no changes needed**. Spring Security intercepts at the HTTP filter level, before requests reach those classes.
- `SongLibraryController` — minimal changes. You only need to widen specific method signatures with `@AuthenticationPrincipal` if you want to record *which user* played a song. Everything else is handled by `SecurityConfig`.
- `SongLibraryServiceHttpClient` — gets a new token-bearing constructor and a static `authenticated()` factory method. All the existing method implementations are unchanged; the `RestClient` sends the `Authorization` header automatically via `defaultHeader`.
- `SongLibraryServiceTest` — existing tests just need `@WithMockUser` annotations from `spring-security-test` so the security filter doesn't block them during test runs.

**Key design notes:**
- Passwords are stored as **BCrypt hashes** via Spring Security's `PasswordEncoder` — the existing `authenticateForAdminPanel` SHA-256 file-based approach can coexist or be migrated over time.
- The `songPlayHistory` on `UserEntity` uses a JPA `@ElementCollection` (simple `"albumId:songId"` strings). This is a fine starting point; for heavy-traffic production use you'd want a dedicated `SongPlayEvent` entity with a timestamp.
- Admin-protected endpoints (`/scan`, `/resetSongStatistics`, etc.) are locked to `ROLE_ADMIN` in `SecurityConfig`. You promote a user to admin by updating their `role` column in the database.
- The Postman changes are zero-friction: set collection-level Bearer auth to `{{authToken}}`, run the login request once (which auto-populates `{{authToken}}` via a Tests script), and all existing requests inherit the header automatically.