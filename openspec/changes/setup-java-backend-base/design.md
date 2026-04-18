## Context

The CareTalk system requires real-time capabilities to handle two distinct communication flows:
1. AI Chatbot interactions, which involve streaming tokens back to the user to simulate real-time typing (like ChatGPT).
2. Live Consultation Chat between Doctor and Patient, which requires bidirectional real-time communication.
In addition, the system requires a structured, resilient database (PostgreSQL) to store application data and medical records.

## Goals / Non-Goals

**Goals:**
- Provide a responsive AI Chatbot module using SSE.
- Establish a bidirectional channel using WebSocket STOMP for live chat.
- Set up a robust persistence layer connected to PostgreSQL using Spring Data JPA.

**Non-Goals:**
- Implementation of the full business logic or authentication flows in this step.
- Frontend mobile integration.

## Decisions

- **Use SSE for Chatbot:** Selected over WebSocket to keep AI logic stateless, highly scalable, and simpler to implement since it's a unidirectional stream (Server -> Client).
- **Use WebSocket (STOMP) for Consultation:** Selected for the live chat component because it supports bidirectional events, routing (`/topic/`), and is standard in the Spring ecosystem.
- **Use PostgreSQL:** Chosen due to its strict ACID compliance suitable for health tech, robust community support, and future expandability via extensions like `pgvector`.

## Risks / Trade-offs

- **[Risk] SSE Mobile Client Support** -> *Mitigation*: Ensure frontend libraries inherently support or can polyfill chunked HTTP responses for Server-Sent Events.
- **[Risk] WebSocket Scalability** -> *Mitigation*: At MVP scale, a single instance is enough. For future scale, Redis Pub/Sub will need to be introduced to share STOMP messages across clustered nodes.
