## ADDED Requirements

### Requirement: Auto-summary generation
The system SHALL automatically generate a structured medical summary when a conversation exceeds the message threshold of 20 messages.

#### Scenario: First summary generation
- **WHEN** a conversation reaches 21 messages and has no existing summary
- **THEN** the system SHALL generate a summary from messages M1 to M11 (leaving the 10 most recent for full context), store it in conversations.summary, and set summary_until_id to M11.id

#### Scenario: Summary includes structured medical data
- **WHEN** the system generates a summary for a medical conversation
- **THEN** the summary SHALL contain structured extraction of: symptoms, medications used/recommended, medical history, vital signs/measurements, treatment progress, and bot/doctor recommendations

### Requirement: Auto-summary regeneration
The system SHALL regenerate the summary when more than 20 new messages have been added since the last summary.

#### Scenario: Regenerate summary after 20 new messages
- **WHEN** a conversation has a summary with summary_until_id=M11, and the total message count reaches 41+
- **THEN** the system SHALL regenerate the summary using: previous summary text + messages from M12 to M31, update conversations.summary with the new text, and set summary_until_id to M31.id

#### Scenario: Regeneration preserves critical medical context
- **WHEN** the system regenerates a summary
- **THEN** the new summary SHALL preserve ALL medical details from the previous summary plus extract new details from recent messages, ensuring no symptom, medication, or vital sign information is lost

### Requirement: Summary generation uses lightweight model
The system SHALL use a lightweight/cheaper LLM model for summary generation to optimize cost.

#### Scenario: Summary model selection
- **WHEN** the system needs to generate or regenerate a summary
- **THEN** the system SHALL use a configured lightweight model (e.g., GPT-4o-mini or Gemini Flash) instead of the main conversation model

### Requirement: Asynchronous summary generation
The system SHALL generate summaries asynchronously to avoid blocking the chat response flow.

#### Scenario: Non-blocking summary generation
- **WHEN** a new message triggers summary generation/regeneration
- **THEN** the system SHALL process the summary asynchronously (not blocking the SSE response to the user) and update the conversation record upon completion
