# Data Model: Emoji Message Listener Logging

## Entity: DiscordMessageEvent

Represents an incoming Discord event observed by the listener.

### Fields

- `guildId` (string, required)
- `channelId` (string, required)
- `authorId` (string, required)
- `isBotAuthor` (boolean, required)
- `rawContent` (string, required, may be empty)

### Validation Rules

- IDs must be non-empty strings.
- `rawContent` can be empty but must be present.

## Entity: ProcessingAttempt

Represents an attempt to process a message that passed the single-emoji eligibility check.

### Fields

- `guildId` (string, required)
- `normalizedEmoji` (string, required)
- `timestamp` (datetime, required)

### Validation Rules

- `normalizedEmoji` must be non-empty when processing starts.

## Entity: DiagnosticLogEntry

Represents one emitted log line used to verify listener visibility.

### Fields

- `stage` (enum: `MESSAGE_RECEIVED`, `MESSAGE_PROCESSING_STARTED`, required)
- `level` (enum: `DEBUG`, required)
- `context` (map, required)
- `timestamp` (datetime, required)

### Validation Rules

- `stage` must match one of the defined values.
- `context` must include minimum correlation keys:
  - for `MESSAGE_RECEIVED`: `guildId`, `channelId`, `authorId`, `isBotAuthor`
  - for `MESSAGE_PROCESSING_STARTED`: `guildId`, `normalizedEmoji`

## Relationships

- One `DiscordMessageEvent` may produce zero or one `ProcessingAttempt`.
- One `DiscordMessageEvent` always produces one `DiagnosticLogEntry` at receipt stage.
- One `ProcessingAttempt` produces one `DiagnosticLogEntry` at processing-start stage.

## State Transitions

1. Event received → receipt log emitted.
2. If event is ineligible → no processing attempt.
3. If event is eligible → processing attempt created conceptually and processing-start log emitted.
4. Existing replacement flow continues unchanged.