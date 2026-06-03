# Firestore Standard Architecture

Generated on 2026-06-03 for the current `MobileApp` codebase.

## Goal

Define one database architecture that is:

- consistent with the current repo
- realistic for production-style Firebase usage
- simple enough to finish in this project
- secure by default
- extensible for notes, tasks, timer, stories, calendar, flashcards, and public sharing

## Final decision

Use this model as the official standard:

- keep `users/{uid}` as the user profile root document
- keep user-owned feature data as top-level collections
- use `userId` on every private document
- keep public feed data in separate public collections
- do not continue using the legacy nested schema under `users/{uid}/...`

## Why this is the best fit for the current project

### It matches the repo better

The current code and documentation already lean toward:

- `users/{uid}`
- `notes/{noteId}`
- `tasks/{taskId}`
- `stories/{storyId}`
- `timer_sessions/{sessionId}`

This is already reflected in:

- `README.md`
- `firestore.rules`
- DTOs and mappers in `app/src/main/java/com/example/mobileapp/data/dto/`

### It is easier to implement now

This team still needs to finish repositories and real Firestore CRUD. A flat collection layout avoids reworking:

- DTO naming
- mapper structure
- rules that already rely on `userId`
- future repository code

### It is still a real-world pattern

Top-level collections with `userId` are common in Firestore when:

- each feature has its own repository
- the app may later need admin tools, moderation, analytics, exports, or Cloud Functions
- you want one consistent access pattern across all features

### It avoids deep coupling to one user document

If every feature becomes a subcollection under `users/{uid}`, you tightly couple all repositories to one path style. That is valid, but for this project it adds migration work and gives no clear benefit over the current model.

## What is legacy and should not continue

Treat the following as legacy test data only:

- `users/test_user/categories`
- `users/test_user/flashcards`
- `users/test_user/notes`
- `users/test_user/tasks`
- `users/test_user/timer_logs`
- `users/test_user/user_stories`

Do not build new repository code against that structure.

## Official Firestore collections

### 1. `users/{uid}`

Purpose:

- account profile
- aggregate stats
- user settings

Recommended fields:

```text
uid: string
name: string
email: string
avatarUrl: string
createdAt: long
updatedAt: long
totalFocusMinutes: int
todayFocusMinutes: int
completedTaskCount: int
level: int
exp: int
timezone: string
isEmailVerified: bool
```

Notes:

- document id must equal Firebase Auth UID
- profile only, not feature lists

### 2. `notes/{noteId}`

Purpose:

- personal notes
- reminders
- flashcard-ready text notes if needed

Recommended fields:

```text
id: string
userId: string
title: string
content: string
type: string        // note | reminder | flashcard
pinned: bool
reminderAt: long    // optional, 0 if none
createdAt: long
updatedAt: long
archived: bool
deleted: bool
```

### 3. `tasks/{taskId}`

Purpose:

- tasks, quests, daily work items

Recommended fields:

```text
id: string
userId: string
title: string
description: string
categoryId: string
priority: string    // low | normal | high
dueAt: long
completed: bool
completedAt: long
createdAt: long
updatedAt: long
deleted: bool
```

### 4. `timer_sessions/{sessionId}`

Purpose:

- Pomodoro/focus history

Recommended fields:

```text
id: string
userId: string
taskId: string
startedAt: long
endedAt: long
durationMinutes: int
completed: bool
mode: string        // pomodoro | stopwatch
createdAt: long
```

### 5. `stories/{storyId}`

Purpose:

- private user stories / reflections

Recommended fields:

```text
id: string
userId: string
title: string
content: string
relatedNoteIds: array<string>
relatedTaskIds: array<string>
coverImageUrl: string
isPublic: bool
createdAt: long
updatedAt: long
sharedAt: long
deleted: bool
```

Rule:

- this collection is the private source of truth
- `isPublic` alone is not the public feed

### 6. `public_stories/{storyId}`

Purpose:

- public community feed

Recommended fields:

```text
id: string
storyId: string
authorId: string
authorName: string
authorAvatarUrl: string
title: string
contentPreview: string
coverImageUrl: string
likeCount: int
commentCount: int
createdAt: long
sharedAt: long
visibility: string   // public
```

Rule:

- public projection only
- write via app logic or Cloud Function
- do not treat this as the editable source story document

### 7. `events/{eventId}`

Purpose:

- calendar feature

Recommended fields:

```text
id: string
userId: string
title: string
description: string
date: long
time: string
location: string
createdAt: long
updatedAt: long
deleted: bool
```

### 8. `flashcards/{flashcardId}`

Purpose:

- spaced repetition

Recommended fields:

```text
id: string
userId: string
categoryId: string
front: string
back: string
difficulty: string
nextReviewAt: long
lastReviewedAt: long
reviewCount: int
createdAt: long
updatedAt: long
archived: bool
```

### 9. `categories/{categoryId}`

Purpose:

- reusable labels for tasks and flashcards

Recommended fields:

```text
id: string
userId: string
name: string
colorHex: string
icon: string
createdAt: long
updatedAt: long
deleted: bool
```

## Naming rules

Use these conventions consistently:

- collection names: plural, snake_case if multi-word
- document ids: generated string ids
- user-owned docs: always include `userId`
- timestamps: Unix epoch milliseconds as `Long`
- booleans: use positive names like `completed`, `pinned`, `isPublic`

Recommended collection names:

```text
users
notes
tasks
timer_sessions
stories
public_stories
events
flashcards
categories
```

Avoid mixing these legacy names:

```text
timer_logs
user_stories
study_notes
publicStories
```

## Security model

### Principle

- private data is owner-only
- public feed is readable by everyone, writable only by trusted logic or constrained owner flow
- users never read or write another user's private data

### Private collection rule pattern

Use this pattern for:

- `notes`
- `tasks`
- `timer_sessions`
- `stories`
- `events`
- `flashcards`
- `categories`

```javascript
function isSignedIn() {
  return request.auth != null;
}

function isOwner(userId) {
  return isSignedIn() && request.auth.uid == userId;
}

function canCreateOwnedDocument() {
  return isSignedIn() && request.resource.data.userId == request.auth.uid;
}

function canReadOrDeleteOwnedDocument() {
  return isSignedIn() && resource.data.userId == request.auth.uid;
}

function canUpdateOwnedDocument() {
  return canReadOrDeleteOwnedDocument()
    && request.resource.data.userId == resource.data.userId;
}
```

### `users/{uid}` rule pattern

```javascript
match /users/{userId} {
  allow create: if request.auth != null
    && request.auth.uid == userId
    && request.resource.data.uid == userId;
  allow read, update: if request.auth != null
    && request.auth.uid == userId;
  allow delete: if false;
}
```

### `public_stories/{storyId}`

Best practical rule for this project:

- anyone signed in can read
- only the owner can create a public projection for their own story
- update/delete can be owner-only for now

If possible later, move public publishing to Cloud Functions for tighter trust boundaries.

## Storage structure

Use Firebase Storage with paths tied to user ownership:

```text
users/{uid}/avatars/{filename}
users/{uid}/stories/{storyId}/{filename}
users/{uid}/notes/{noteId}/{filename}
```

Rules principle:

- user can only write inside `users/{request.auth.uid}/...`

## Query patterns to design for

### Notes

```text
where userId == currentUserId
order by updatedAt desc
```

### Tasks

```text
where userId == currentUserId
where completed == false
order by dueAt asc
```

### Stories

```text
where userId == currentUserId
order by updatedAt desc
```

### Timer sessions

```text
where userId == currentUserId
order by startedAt desc
```

### Events

```text
where userId == currentUserId
order by date asc
```

### Public stories

```text
order by sharedAt desc
```

## Index strategy

Create indexes only when queries require them. Start with:

1. `tasks`: `userId ASC, completed ASC, dueAt ASC`
2. `tasks`: `userId ASC, completed ASC, updatedAt DESC`
3. `notes`: `userId ASC, pinned ASC, updatedAt DESC`
4. `stories`: `userId ASC, updatedAt DESC`
5. `timer_sessions`: `userId ASC, startedAt DESC`
6. `events`: `userId ASC, date ASC`
7. `public_stories`: `sharedAt DESC`

## Migration decision

### Official direction

Do not migrate the current repo back to hierarchical subcollections.

Migrate old test data mentally and operationally toward the new standard:

- keep old nested data only as legacy test data
- stop writing any new feature code to nested `users/{uid}/...`
- write all new repositories against the standard top-level collections

## Implementation order

### Phase 1

- finalize `firestore.rules`
- finalize `README.md` schema
- keep `users/{uid}` as the auth-linked profile

### Phase 2

- implement `NotesRepositoryImpl`
- implement `TasksRepositoryImpl`
- implement `TimerSessionRepositoryImpl`

### Phase 3

- implement `StoriesRepositoryImpl`
- implement `public_stories` sharing flow
- implement `EventsRepositoryImpl`

### Phase 4

- implement `FlashcardRepositoryImpl`
- implement `CategoryRepositoryImpl`
- add Storage upload flow

## Definition of done

The database architecture is considered complete when:

- every implemented feature writes to the official collection structure
- no new code depends on legacy nested test data
- rules protect all private collections by `userId`
- public collections have explicit rules
- repository code matches `README.md`
- screenshots and tests reflect the same schema

## Final recommendation

For this project, the best solution is:

- keep `users/{uid}` for profile
- use top-level private collections with `userId`
- use `public_stories` as a separate public projection
- treat old nested collections as legacy only
- standardize names now and do not mix both schemas anymore
