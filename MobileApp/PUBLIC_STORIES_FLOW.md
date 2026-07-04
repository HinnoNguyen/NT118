# Public Stories Flow

Generated on 2026-06-03 for the current Firestore standard.

## Source of truth

- Private user stories live in `stories/{storyId}`
- Public community feed lives in `public_stories/{storyId}`

This means:

- `stories` is the editable private source
- `public_stories` is a public projection
- do not edit public content as the main source record

## Publish flow

1. User creates or edits a private story in `stories/{storyId}`
2. User chooses to publish it
3. App copies selected fields into `public_stories/{storyId}`
4. Community screens read only from `public_stories`

## Unpublish flow

1. User keeps the private story in `stories/{storyId}`
2. App deletes the corresponding `public_stories/{storyId}`
3. Story becomes private again without losing original content

## Fields copied to `public_stories`

- `id`
- `storyId`
- `authorId`
- `authorName`
- `authorAvatarUrl`
- `title`
- `contentPreview`
- `coverImageUrl`
- `likeCount`
- `commentCount`
- `createdAt`
- `sharedAt`
- `visibility`

## Security expectations

- `public_stories` is readable by everyone
- only the owner can create their own public projection
- only the owner can update/delete their own public projection
- immutable ownership fields stay locked after create

## Current code

- model: `app/src/main/java/com/example/mobileapp/domain/model/PublicStory.kt`
- dto: `app/src/main/java/com/example/mobileapp/data/dto/PublicStoryDto.kt`
- mapper: `app/src/main/java/com/example/mobileapp/data/mapper/PublicStoryMapper.kt`
- repository: `app/src/main/java/com/example/mobileapp/domain/repository/PublicStoryRepository.kt`
- implementation: `app/src/main/java/com/example/mobileapp/data/repository/PublicStoryRepositoryImpl.kt`

## Current limitation

The private `Story` model does not yet store `isPublic`, `sharedAt`, or `coverImageUrl`.

For now:

- those values are handled during publish/unpublish flow
- `public_stories` remains the public projection only

When Story feature is implemented fully, add:

- `isPublic`
- `sharedAt`
- optional `coverImageUrl`

to the private story model as well.
