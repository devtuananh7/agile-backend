## ADDED Requirements

### Requirement: SSE Chatbot Endpoint
The backend SHALL expose a REST endpoint capable of producing `text/event-stream` for chatbot responses.

#### Scenario: Streaming a response
- **WHEN** client sends a valid POST request to the chatbot endpoint
- **THEN** the system returns an HTTP 200 with `Content-Type: text/event-stream`
- **THEN** it emits chunks of data sequentially until completion

### Requirement: WebSocket STOMP Endpoint
The backend SHALL expose a WebSocket endpoint for STOMP protocol connections to facilitate bidirectional chat.

#### Scenario: Subscribing to chat topic
- **WHEN** client successfully connects to the WebSocket endpoint
- **THEN** client can subscribe to specific room topics (e.g. `/topic/cases/{id}`) to receive broadcasted messages
