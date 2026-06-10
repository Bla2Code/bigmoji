# Feature Specification: Default Emoji Sticker Replacement and Diagnostic Logging

**Feature Branch**: `004-default-sticker-replace`

**Created**: 2026-06-09

**Status**: Draft

**Input**: User description: "Делаем новую feature в новой ветке. Сообщения в логах перехватываются слушателем нашего приложения, но не заменяются на default-sticker из `src/main/resources/default-stickers/`. Нужно сделать замену на default-sticker, если пришел соответствующий эмоджи, и добавить логирование ключевых мест, чтобы понять найден код эмоджи или нет, и является ли он дефолтным кодом."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Replace known default emojis with stickers (Priority: P1)

As a Discord user, I want incoming messages that contain supported default emojis to trigger the corresponding default sticker, so that emoji reactions are converted into visual sticker responses.

**Why this priority**: Core business value is missing today because recognized emojis are not converted into expected default stickers.

**Independent Test**: Send a message containing each supported default emoji and verify that the listener triggers the matching sticker response for every supported emoji.

**Acceptance Scenarios**:

1. **Given** a message contains a supported default emoji code, **When** the listener processes the message, **Then** the system selects and sends the mapped default sticker from the default sticker set.
2. **Given** a message contains multiple supported default emoji codes, **When** the listener processes the message, **Then** each detected supported emoji results in the corresponding default sticker action according to current listener behavior rules.

---

### User Story 2 - Distinguish mapped, unmapped, and fallback paths in logs (Priority: P2)

As a maintainer, I want logs that clearly show whether an emoji code was found, mapped to a default sticker, or not mapped, so that I can quickly diagnose replacement failures.

**Why this priority**: Without diagnostic visibility, maintainers cannot determine whether failures come from emoji detection, mapping lookup, or fallback handling.

**Independent Test**: Process three message types (mapped emoji, unmapped emoji-like token, and message without emoji) and verify logs explicitly indicate the decision path for each.

**Acceptance Scenarios**:

1. **Given** a mapped default emoji is detected, **When** processing runs, **Then** logs include that the emoji code was found and matched to a default sticker key.
2. **Given** an emoji-like input is detected but has no default mapping, **When** processing runs, **Then** logs include that no default mapping was found and replacement was skipped or fallback path was used.
3. **Given** no emoji code is detected, **When** processing runs, **Then** logs include that detection returned no supported emoji and no replacement was attempted.

---

### User Story 3 - Preserve existing non-target behavior (Priority: P3)

As a product owner, I want the listener to keep existing behavior for unsupported inputs while adding default replacement logic, so that only intended scenarios change.

**Why this priority**: The feature should fix missing default replacement and add observability without introducing regressions in unrelated message handling.

**Independent Test**: Re-run current listener regression checks for supported and unsupported message inputs and confirm no unrelated behavior changes beyond expected replacement and logs.

**Acceptance Scenarios**:

1. **Given** a message previously ignored by listener rules, **When** the updated listener receives it, **Then** it remains ignored under the same rules.
2. **Given** a message with unsupported emoji content, **When** the updated listener processes it, **Then** no incorrect default sticker is sent.

### Edge Cases

- What happens when a message contains emoji variants (for example with modifiers) that normalize to a supported default key?
- How does the listener behave when message text contains tokens similar to emoji codes but not valid emoji?
- What happens when the mapped default sticker asset is unavailable at runtime (missing or unreadable resource)?
- How are duplicate emoji occurrences in one message handled to avoid unintended duplicate sticker sends?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST detect emoji content from each incoming message processed by the listener using existing message intake flow.
- **FR-002**: System MUST normalize detected emoji content into a comparable code format before default sticker lookup.
- **FR-003**: System MUST map supported normalized emoji codes to corresponding default sticker assets located in the configured default sticker set.
- **FR-004**: System MUST trigger default sticker replacement when a supported emoji code is detected and mapped.
- **FR-005**: System MUST NOT trigger default sticker replacement when no supported mapping exists for the detected emoji code.
- **FR-006**: System MUST emit diagnostic logs at key processing points: message received, emoji extraction result, normalization result, mapping lookup result, and replacement decision outcome.
- **FR-007**: Diagnostic logs MUST explicitly distinguish between three states: mapped default emoji, unmapped emoji code, and no emoji detected.
- **FR-008**: System MUST keep existing listener behavior unchanged for message flows outside default emoji replacement scope.
- **FR-009**: System MUST handle missing default sticker resources gracefully by recording an actionable log entry and continuing without application crash.

### Key Entities *(include if feature involves data)*

- **Incoming Message Event**: Represents a single Discord message observed by the listener and eligible for emoji inspection.
- **Emoji Detection Result**: Represents extracted and normalized emoji code information used for mapping decisions.
- **Default Sticker Mapping Entry**: Represents a supported relation between a normalized emoji code and a default sticker asset key.
- **Replacement Decision Record**: Represents the outcome path for a processed message (mapped, unmapped, or no emoji) reflected in logs.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of messages containing supported default emojis result in the corresponding default sticker replacement during acceptance testing.
- **SC-002**: 100% of processed messages produce a diagnostic decision log that identifies one of the required states: mapped, unmapped, or no emoji.
- **SC-003**: Maintainers can determine the replacement decision path for any processed message in under 1 minute using logs alone.
- **SC-004**: Regression verification confirms no unintended behavior changes for unsupported inputs compared with baseline listener behavior.

## Assumptions

- The supported default sticker set is the current resource set under `src/main/resources/default-stickers/` and includes stable keys for fire, heart, party, smile, and thumbsup.
- Existing listener pipeline remains the source of message events and is not redesigned by this feature.
- Logging output is available in environments where listener troubleshooting is performed.
- Replacement should occur only for explicitly supported default emoji mappings; unsupported emoji content remains non-replaced.
