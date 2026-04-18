## ADDED Requirements

### Requirement: Message persistence
The system SHALL store each chat message as a separate record in the messages table with fields: id (auto-increment), conversation_id (FK), sender_id (varchar), sender_role (enum: USER/BOT/DOCTOR/SYSTEM), content (text), content_type (enum: TEXT), metadata (nullable JSONB), created_at.

#### Scenario: Save user message
- **WHEN** a user sends a message in a conversation
- **THEN** the system SHALL insert a new message record with sender_role=USER, content=message text, and conversation_id referencing the active conversation

#### Scenario: Save bot response
- **WHEN** the AI bot completes a streaming response
- **THEN** the system SHALL insert a new message record with sender_role=BOT, content=full concatenated response text

#### Scenario: Save system event message
- **WHEN** a system event occurs (e.g., escalation)
- **THEN** the system SHALL insert a message with sender_role=SYSTEM, content_type=TEXT describing the event

### Requirement: Message retrieval with pagination
The system SHALL support paginated retrieval of messages for a conversation, ordered by created_at.

#### Scenario: Get recent messages
- **WHEN** a client requests messages for a conversation without pagination params
- **THEN** the system SHALL return the most recent 20 messages ordered by created_at ascending

#### Scenario: Get messages with pagination
- **WHEN** a client requests messages with page and size parameters
- **THEN** the system SHALL return the specified page of messages ordered by created_at ascending

### Requirement: Message count
The system SHALL provide an efficient count of total messages in a conversation for summary threshold logic.

#### Scenario: Count messages for summary check
- **WHEN** the system needs to determine if summary generation is required
- **THEN** the system SHALL return the total count of messages for the given conversation_id
