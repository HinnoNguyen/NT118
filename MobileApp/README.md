# Narrativize

## Firestore Schema

This document records the Day 3 Firestore structure for the Android app. All timestamps are stored as Unix epoch milliseconds (`Long`) and every user-owned document stores the Firebase Auth UID in `userId`.

### Collections

#### `users/{uid}`

Created after registration and also backfilled on first login if the Firestore document is missing.

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
| `completedTaskCount` | `Int` | Completed task count for gamification/profile stats. |
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
| `type` | `String` | Defaults to `note`; can later support `reminder` or `flashcard`. |
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

Stores checklist/quest/task data.

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `String` | Same as document id. |
| `userId` | `String` | Firebase Auth UID of owner. |
| `title` | `String` | Task title. |
| `description` | `String` | Optional task detail. |
| `dueAt` | `Long` | Due timestamp; `0` means no due date yet. |
| `completed` | `Boolean` | Completion status. |
| `priority` | `String` | Defaults to `normal`; examples: `low`, `normal`, `high`. |
| `createdAt` | `Long` | Creation timestamp. |
| `updatedAt` | `Long` | Last update timestamp. |

Suggested queries:
- `whereEqualTo("userId", uid).whereEqualTo("completed", false).orderBy("dueAt")`
- `whereEqualTo("userId", uid).whereEqualTo("completed", true).orderBy("updatedAt", DESCENDING)`

Related code:
- `app/src/main/java/com/example/mobileapp/data/dto/TaskDto.kt`
- `app/src/main/java/com/example/mobileapp/domain/model/Task.kt`
- `app/src/main/java/com/example/mobileapp/data/mapper/TaskMapper.kt`

#### `stories/{storyId}`

Stores reflection stories written by the user.

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `String` | Same as document id. |
| `userId` | `String` | Firebase Auth UID of owner. |
| `title` | `String` | Story title. |
| `content` | `String` | Story body. |
| `relatedNoteIds` | `List<String>` | Notes connected to the story. |
| `createdAt` | `Long` | Creation timestamp. |
| `updatedAt` | `Long` | Last update timestamp. |

Suggested queries:
- `whereEqualTo("userId", uid).orderBy("updatedAt", DESCENDING)`

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

Suggested queries:
- `whereEqualTo("userId", uid).orderBy("startedAt", DESCENDING)`
- `whereEqualTo("userId", uid).whereEqualTo("completed", true)`

Related code:
- `app/src/main/java/com/example/mobileapp/data/dto/TimerSessionDto.kt`
- `app/src/main/java/com/example/mobileapp/domain/model/TimerSession.kt`
- `app/src/main/java/com/example/mobileapp/data/mapper/TimerSessionMapper.kt`

### Security Rules

The current local rules file is `firestore.rules`. It is intentionally in temporary test mode for Phase 3 and should not be used for production.

Before final submission, replace test mode with owner-based rules:
- users can read/write only `users/{uid}` where `request.auth.uid == uid`
- users can read/write only documents whose `userId` equals `request.auth.uid`

### Day 3 Completion Checklist

- [x] Firebase Gradle setup exists.
- [x] Firebase Auth flow exists.
- [x] `users` collection model exists.
- [x] `notes` collection model exists.
- [x] `tasks` collection model exists.
- [x] `stories` collection model exists.
- [x] `timer_sessions` collection model exists.
- [x] Firestore schema documented.
- [x] Temporary Firestore rules file added.
- [ ] Deploy/check rules in Firebase Console if required by the team.
- [ ] Implement repository persistence for Notes next.
