## 1. Project Dependencies Configuration

- [x] 1.1 Add `spring-boot-starter-web` and `spring-boot-starter-webflux` to `build.gradle` for generic REST API APIs and SSE support.
- [x] 1.2 Add `spring-boot-starter-websocket` to `build.gradle` for STOMP socket support.
- [x] 1.3 Add `spring-boot-starter-data-jpa` and `org.postgresql:postgresql` to `build.gradle` to establish data layers.

## 2. PostgreSQL Connection Settings

- [x] 2.1 Create or verify existence of `src/main/resources/application.properties` (or `.yml`).
- [x] 2.2 Configure `spring.datasource.url`, `username`, and `password` to target PostgreSQL environment.
- [x] 2.3 Configure `spring.jpa.hibernate.ddl-auto` for MVP database bootstrapping.

## 3. Communication Endpoints Setup

- [x] 3.1 Create a generic `ChatbotController` that routes `/api/v1/chatbot/chat` to an `SseEmitter` stream.
- [x] 3.2 Create `WebSocketConfig.java` to implement `WebSocketMessageBrokerConfigurer`, registering STOMP endpoints.
- [x] 3.3 Create a placeholder `ConsultationWsController` mapped with `@MessageMapping` as proof of concept.
