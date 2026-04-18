## ADDED Requirements

### Requirement: LLM client abstraction
The system SHALL provide an interface LlmClient with a streaming method that accepts an LlmRequest and returns a reactive stream of string tokens (Flux<String>).

#### Scenario: Stream chat completion
- **WHEN** the PromptAssembler sends a completed LlmRequest to the LlmClient
- **THEN** the system SHALL connect to the configured LLM provider API and stream response tokens back as a Flux<String>

#### Scenario: LLM request includes model config
- **WHEN** an LlmRequest is created with model name and temperature from system_prompts metadata
- **THEN** the LlmClient SHALL use those specific settings for the API call

### Requirement: Multi-provider support
The system SHALL support multiple LLM providers through separate implementations of the LlmClient interface.

#### Scenario: OpenAI provider
- **WHEN** the system prompt metadata specifies model="gpt-4o"
- **THEN** the system SHALL route the request to the OpenAI-compatible LlmClient implementation

#### Scenario: Provider selection based on model name
- **WHEN** the LlmRequest contains a model name
- **THEN** the system SHALL select the appropriate LlmClient implementation based on a model-to-provider mapping configuration

### Requirement: SSE streaming to client
The system SHALL stream LLM responses to the client using Server-Sent Events (SSE).

#### Scenario: Stream AI response via SSE
- **WHEN** the LlmClient returns a Flux<String> of tokens
- **THEN** the ChatbotController SHALL stream each token as an SSE event to the client, with a final [DONE] event when complete

#### Scenario: Handle LLM API error during streaming
- **WHEN** the LLM API returns an error during streaming
- **THEN** the system SHALL send an error SSE event to the client and complete the SSE emitter with error

### Requirement: LLM request structure
The LlmRequest SHALL contain: systemPrompt (String), ragContext (nullable String), summary (nullable String), recentMessages (List of message objects with role and content), userMessage (String), model (String), temperature (double), stream (boolean).

#### Scenario: Build LlmRequest from pipeline
- **WHEN** the PromptAssembler completes assembly
- **THEN** the resulting LlmRequest SHALL contain all assembled components ready for the LlmClient to consume without further transformation
