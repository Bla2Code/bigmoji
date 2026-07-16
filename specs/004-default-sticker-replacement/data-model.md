# Data Model: Default Emoji Sticker Replacement and Diagnostic Logging

## Entity: IncomingMessageEvent

**Description**: A single Discord message event observed by the listener.

**Fields**:
- `eventId` (string): Correlation identifier for tracking processing flow.
- `channelId` (string): Source channel identifier.
- `authorId` (string): Message author identifier.
- `content` (string): Message content used for emoji detection.
- `receivedAt` (datetime): Intake timestamp.

**Validation Rules**:
- `eventId`, `channelId`, `authorId` must be non-empty.
- `content` may be empty but must be treated as valid input.
- `receivedAt` must be present.

## Entity: EmojiDetectionResult

**Description**: Result of detection and normalization stage for a message.

**Fields**:
- `eventId` (string): Links result to message event.
- `detected` (boolean): Whether any emoji candidate was found.
- `rawEmojiToken` (string, optional): Extracted source token before normalization.
- `normalizedEmojiCode` (string, optional): Canonical code used for mapping lookup.

**Validation Rules**:
- If `detected=false`, `rawEmojiToken` and `normalizedEmojiCode` must be absent.
- If `detected=true`, at least one of `rawEmojiToken` or `normalizedEmojiCode` must be present.

## Entity: DefaultStickerMappingEntry

**Description**: Supported mapping between normalized emoji code and bundled default sticker key.

**Fields**:
- `normalizedEmojiCode` (string): Canonical lookup key.
- `stickerKey` (enum): One of `fire`, `heart`, `party`, `smile`, `thumbsup`.
- `resourcePath` (string): Relative resource location for sticker asset.

**Validation Rules**:
- `normalizedEmojiCode` must be unique within default mapping set.
- `stickerKey` must belong to supported default key list.
- `resourcePath` must reference an existing bundled sticker file.

## Entity: ReplacementDecisionRecord

**Description**: Final outcome of message processing with respect to default replacement.

**Fields**:
- `eventId` (string): Correlation key.
- `decisionState` (enum): `MAPPED_DEFAULT`, `UNMAPPED_EMOJI`, `NO_EMOJI`, `RESOURCE_MISSING`.
- `normalizedEmojiCode` (string, optional): Present when a detection path exists.
- `resolvedStickerKey` (string, optional): Present only when mapped.
- `decisionAt` (datetime): Decision timestamp.

**Validation Rules**:
- `decisionState` is mandatory.
- `resolvedStickerKey` required only for `MAPPED_DEFAULT`.
- `normalizedEmojiCode` required for `MAPPED_DEFAULT` and `UNMAPPED_EMOJI`.

## Relationships

- `IncomingMessageEvent` 1→1 `EmojiDetectionResult`
- `EmojiDetectionResult` 0..1 → 1 `DefaultStickerMappingEntry`
- `IncomingMessageEvent` 1→1 `ReplacementDecisionRecord`

## State Transitions (Decision Flow)

1. `MESSAGE_RECEIVED` → detect emoji
2. If no emoji: `NO_EMOJI`
3. If emoji detected: normalize code
4. If normalized code maps and asset exists: `MAPPED_DEFAULT`
5. If normalized code has no mapping: `UNMAPPED_EMOJI`
6. If mapping exists but asset unavailable: `RESOURCE_MISSING`