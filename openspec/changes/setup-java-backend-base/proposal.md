## Why

To support the CareTalk Chatbot system, the backend requires a real-time communication architecture and a robust data layer. The chosen approach integrates Server-Sent Events (SSE) for AI streaming and WebSockets for live doctor-patient consultations, alongside PostgreSQL for reliable transactional data storage. This setup provides a scalable, modern foundation for the MVP.

## What Changes

- Add Spring Web and WebFlux dependencies to `build.gradle` for REST and SSE support.
- Add Spring WebSocket (STOMP) dependency for real-time consultation chat.
- Add PostgreSQL driver and Spring Data JPA dependencies.
- Add related utility libraries if needed (e.g., Lombok).

## Capabilities

### New Capabilities
- `realtime-communication`: Defines the setup and usage of SSE for chatbot streaming and WebSocket/STOMP for consultation chat.
- `database-integration`: Defines the setup of PostgreSQL integration using Spring Data JPA.

### Modified Capabilities

## Impact

- Updates `build.gradle` to include new Spring Boot starter dependencies.
- Modifies `src/main/resources/application.properties` to support database and socket configurations.
