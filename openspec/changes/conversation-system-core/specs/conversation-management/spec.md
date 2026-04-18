## ADDED Requirements

### Requirement: Conversation creation
The system SHALL create a new conversation record when a user initiates a chat without an existing active session. The conversation SHALL include fields: id (auto-increment), session_id (UUID), ref_id (nullable FK to self), status (enum: ACTIVE/INACTIVE/DONE), user_id, username, doctor (id or "BOT"), type (enum: BOT/DOCTOR/ESCALATED), prompt_name (varchar, default "medical_general"), conclusion (nullable text), summary (nullable text), summary_until_id (nullable int), created_at, updated_at.

#### Scenario: New conversation without session_id
- **WHEN** a user sends a chat request without session_id
- **THEN** the system SHALL create a new conversation with a generated UUID as session_id, type=BOT, doctor="BOT", status=ACTIVE, and return the session_id to the client

#### Scenario: New conversation with session_id
- **WHEN** a user sends a chat request with a session_id that has no ACTIVE conversation
- **THEN** the system SHALL create a new conversation using the provided session_id, type=BOT, doctor="BOT", status=ACTIVE

### Requirement: Session resolution
The system SHALL resolve an existing conversation when a user provides a session_id that maps to an ACTIVE conversation.

#### Scenario: Resume existing conversation
- **WHEN** a user sends a chat request with a session_id that has an ACTIVE conversation
- **THEN** the system SHALL use the existing conversation and append the new message to it

#### Scenario: Session with DONE conversation
- **WHEN** a user sends a chat request with a session_id whose conversation has status=DONE
- **THEN** the system SHALL create a new conversation with a new session_id

### Requirement: Conversation status lifecycle
The system SHALL manage conversation status transitions: ACTIVE → DONE (user closes or doctor concludes), ACTIVE → INACTIVE (timeout), INACTIVE → ACTIVE (user returns).

#### Scenario: User closes conversation
- **WHEN** a user explicitly ends a conversation
- **THEN** the system SHALL update the conversation status to DONE and set updated_at

#### Scenario: Doctor concludes conversation
- **WHEN** a doctor submits a conclusion for an ESCALATED conversation
- **THEN** the system SHALL save the conclusion text, update status to DONE, and set updated_at

### Requirement: Conversation escalation
The system SHALL support escalating a BOT conversation to a DOCTOR consultation by creating a new conversation record linked via ref_id.

#### Scenario: Escalate bot to doctor
- **WHEN** a user or bot triggers escalation on an ACTIVE BOT conversation
- **THEN** the system SHALL set the BOT conversation status to DONE, create a new conversation with type=ESCALATED, ref_id pointing to the BOT conversation, a new unique session_id, doctor set to the assigned doctor ID, and status=ACTIVE

#### Scenario: Doctor views bot context after escalation
- **WHEN** a doctor opens an ESCALATED conversation
- **THEN** the system SHALL provide the summary and messages from the referenced BOT conversation (via ref_id) as context

### Requirement: Conversation listing
The system SHALL provide APIs to list conversations for a user with pagination support.

#### Scenario: List user conversations
- **WHEN** a user requests their conversation history
- **THEN** the system SHALL return a paginated list of conversations ordered by updated_at descending
