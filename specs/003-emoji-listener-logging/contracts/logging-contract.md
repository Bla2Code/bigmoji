# Logging Contract: EmojiMessageListener

## Purpose

Define expected diagnostic log behavior for listener intake and processing visibility.

## Contract Scope

- Producer: `EmojiMessageListener`
- Stages:
  - `MESSAGE_RECEIVED`
  - `MESSAGE_PROCESSING_STARTED`
- Level: `DEBUG`

## Required Log Events

### 1) Message Received

- **When emitted**: Immediately after listener receives an event.
- **Level**: `DEBUG`
- **Required context keys**:
  - `guildId`
  - `channelId`
  - `authorId`
  - `isBot`
  - `content`

### 2) Message Processing Started

- **When emitted**: After message is validated as eligible for handling.
- **Level**: `DEBUG`
- **Required context keys**:
  - `guildId`
  - `normalizedEmoji`

## Non-Functional Contract Rules

- Adding logs must not alter functional decision flow.
- Event ordering must preserve causality (`MESSAGE_RECEIVED` before `MESSAGE_PROCESSING_STARTED` for handled messages).
- Log format should remain compatible with existing centralized collection.

## Verification Contract

- In a verification environment with logger level `com.bigmoji.discord=DEBUG`:
  - Every received event has one `MESSAGE_RECEIVED` log.
  - Every handled message has one `MESSAGE_PROCESSING_STARTED` log.
- Regression checks confirm no change in sticker replacement behavior for previously covered scenarios.