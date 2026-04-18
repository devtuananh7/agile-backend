## ADDED Requirements

### Requirement: Prompt assembly orchestration
The system SHALL assemble a final LLM request by combining 4 components in order: system prompt, RAG context, conversation context (summary + recent messages), and the new user message.

#### Scenario: Assemble prompt for short conversation (≤20 messages)
- **WHEN** a conversation has 20 or fewer messages
- **THEN** the system SHALL assemble the prompt with: system prompt + RAG context (if available) + all conversation messages + new user message, without summary

#### Scenario: Assemble prompt for long conversation (>20 messages)
- **WHEN** a conversation has more than 20 messages and a summary exists
- **THEN** the system SHALL assemble the prompt with: system prompt + RAG context (if available) + summary as system context + recent messages (from after summary_until_id, max 20) + new user message

### Requirement: System prompt resolution
The system SHALL load the system prompt from the system_prompts table based on the conversation's prompt_name field. The system_prompts table SHALL have fields: id, name (unique), content (text), version (int), is_active (boolean), metadata (JSONB for model config), created_at, updated_at.

#### Scenario: Resolve system prompt by name
- **WHEN** the PromptAssembler needs a system prompt for a conversation with prompt_name="medical_general"
- **THEN** the system SHALL load the active system_prompt record where name="medical_general" and is_active=true

#### Scenario: System prompt includes model config
- **WHEN** a system_prompt record has metadata containing model and temperature
- **THEN** the system SHALL use those values (e.g., model="gpt-4o", temperature=0.3) in the LLM request

#### Scenario: System prompt not found
- **WHEN** no active system_prompt matches the conversation's prompt_name
- **THEN** the system SHALL fall back to a default hardcoded system prompt and log a warning

### Requirement: RAG context injection
The system SHALL retrieve relevant knowledge documents using semantic search and inject them into the prompt as additional system context.

#### Scenario: RAG retrieval with relevant documents
- **WHEN** the user message is embedded and similar documents exist (similarity > 0.75)
- **THEN** the system SHALL include the top 3 most relevant document contents as a [MEDICAL KNOWLEDGE] section in the prompt

#### Scenario: RAG retrieval with no relevant documents
- **WHEN** no documents exceed the similarity threshold
- **THEN** the system SHALL proceed without RAG context (the section is omitted)

### Requirement: Conversation context building
The system SHALL build conversation context by selecting either full message history or summary + recent messages based on the message count threshold.

#### Scenario: Build context without summary
- **WHEN** total messages ≤ 20
- **THEN** the system SHALL include all messages as the conversation context

#### Scenario: Build context with summary
- **WHEN** total messages > 20 and summary exists
- **THEN** the system SHALL include the summary as a system message plus the most recent messages (those after summary_until_id)
