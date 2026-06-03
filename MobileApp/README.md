# Narrativize

## Firestore Standard

The project now uses one official Firestore architecture:

- `users/{uid}` for user profile documents
- top-level collections for private feature data
- `userId` on every private document
- dedicated public collections for community-facing data
- legacy nested data under `users/{uid}/...` is treated as old test data only

The detailed architecture reference is:

- `FIRESTORE_STANDARD_ARCHITECTURE.md`

## Official Collections

All timestamps are stored as Unix epoch milliseconds (`Long`).

### Implemented in code today

#### `users/{uid}`

Created after registration and backfilled on first login if the Firestore document is missing.

| Field | Type | Notes |
| --- | --- | --- |
| `uid` | `String` | Same as Firebase Auth UID and document id. |
| `name` | `String` | Display name. |
| `email` | `String` | Account email. |
| `avatarUrl` | `String` | Empty string until profile image upload is implemented. |
| `createdAt` | `Long` | Account profile creation time. |
| `updatedAt` | `Long` | Last profile update time. |
| `totalFocusMinutes` | `Int` | Total focus minutes across all sessions. |
| `todayFocusMinutes` | `Int` | Focus minutes for the current day. |
| `completedTaskCount` | `Int` | Completed task count for profile stats. |
| `level` | `Int` | User level, starts at `1`. |
| `exp` | `Int` | Experience points, starts at `0`. |

Related code:
- `app/src/main/java/com/example/mobileapp/data/dto/UserDto.kt`
- `app/src/main/java/com/example/mobileapp/domain/model/User.kt`
- `app/src/main/java/com/example/mobileapp/data/repository/UserRepositoryImpl.kt`

#### `notes/{noteId}`

Stores user notes for the Notes feature.

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `String` | Same as document id. |
| `userId` | `String` | Firebase Auth UID of owner. |
| `title` | `String` | Note title. |
| `content` | `String` | Note body. |
| `type` | `String` | `note`, `reminder`, or future `flashcard`. |
| `pinned` | `Boolean` | Whether the note is pinned. |
| `createdAt` | `Long` | Creation timestamp. |
| `updatedAt` | `Long` | Last update timestamp. |

Suggested queries:
- `whereEqualTo("userId", uid).orderBy("updatedAt", DESCENDING)`
- `whereEqualTo("userId", uid).whereEqualTo("pinned", true)`

Related code:
- `app/src/main/java/com/example/mobileapp/data/dto/NoteDto.kt`
- `app/src/main/java/com/example/mobileapp/domain/model/Note.kt`
- `app/src/main/java/com/example/mobileapp/data/mapper/NoteMapper.kt`

#### `tasks/{taskId}`

Stores checklist and quest-like task data.

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `String` | Same as document id. |
| `userId` | `String` | Firebase Auth UID of owner. |
| `title` | `String` | Task title. |
| `description` | `String` | Optional detail. |
| `dueAt` | `Long` | Due timestamp; `0` means no due date yet. |
| `completed` | `Boolean` | Completion status. |
| `priority` | `String` | `low`, `normal`, or `high`. |
| `createdAt` | `Long` | Creation timestamp. |
| `updatedAt` | `Long` | Last update timestamp. |

Related code:
- `app/src/main/java/com/example/mobileapp/data/dto/TaskDto.kt`
- `app/src/main/java/com/example/mobileapp/domain/model/Task.kt`
- `app/src/main/java/com/example/mobileapp/data/mapper/TaskMapper.kt`

#### `stories/{storyId}`

Stores private reflection stories written by the user.

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `String` | Same as document id. |
| `userId` | `String` | Firebase Auth UID of owner. |
| `title` | `String` | Story title. |
| `content` | `String` | Story body. |
| `relatedNoteIds` | `List<String>` | Notes connected to the story. |
| `createdAt` | `Long` | Creation timestamp. |
| `updatedAt` | `Long` | Last update timestamp. |

Related code:
- `app/src/main/java/com/example/mobileapp/data/dto/StoryDto.kt`
- `app/src/main/java/com/example/mobileapp/domain/model/Story.kt`
- `app/src/main/java/com/example/mobileapp/data/mapper/StoryMapper.kt`

#### `timer_sessions/{sessionId}`

Stores completed or interrupted focus sessions.

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `String` | Same as document id. |
| `userId` | `String` | Firebase Auth UID of owner. |
| `startedAt` | `Long` | Session start timestamp. |
| `endedAt` | `Long` | Session end timestamp; `0` if not ended yet. |
| `durationMinutes` | `Int` | Planned or completed session duration. |
| `completed` | `Boolean` | Whether the session finished successfully. |
| `createdAt` | `Long` | Creation timestamp. |

Related code:
- `app/src/main/java/com/example/mobileapp/data/dto/TimerSessionDto.kt`
- `app/src/main/java/com/example/mobileapp/domain/model/TimerSession.kt`
- `app/src/main/java/com/example/mobileapp/data/mapper/TimerSessionMapper.kt`

### Planned standard collections

These collections are part of the official schema even if full app CRUD is not finished yet:

- `public_stories/{storyId}` for community feed projection
- `events/{eventId}` for calendar
- `flashcards/{flashcardId}` for spaced repetition
- `categories/{categoryId}` for reusable labels

## Security Rules

The local rules file is `firestore.rules`.

It uses these rules:

- `users/{uid}` is readable and writable only by that same authenticated user
- private collections are protected by `userId == request.auth.uid`
- `public_stories` is public-read and owner-write
- unknown collections are denied by default

Before final submission, publish the same rules text to Firebase Console.

## Legacy Data

The following nested structure may still exist in Firestore as old test data:

- `users/{uid}/categories`
- `users/{uid}/flashcards`
- `users/{uid}/notes`
- `users/{uid}/tasks`
- `users/{uid}/timer_logs`
- `users/{uid}/user_stories`

Do not implement new repository code against that legacy structure.

## Current Status

- [x] Firebase Gradle setup exists
- [x] Firebase Auth flow exists
- [x] `users` profile document flow exists
- [x] DTOs and mappers exist for `notes`, `tasks`, `stories`, `timer_sessions`
- [x] Owner-based Firestore rules file exists locally
- [x] `NoteRepository` scaffold exists in code
- [x] `EventRepository` scaffold exists in code
- [x] `PublicStoryRepository` scaffold exists in code
- [ ] Publish/update the same rules in Firebase Console
- [ ] Implement UI integration for notes CRUD
- [ ] Implement repositories for tasks, timer sessions, and private stories
