---
description: Implement tasks from an OpenSpec change request with CR-specific context loading, regression checking, and migration tracking.
---

Implement tasks from a Change Request (CR), extending the standard opsx-apply process with CR-specific context loading (`current-code-logic.md`, `compare-logic.md`), mandatory project standards compliance, regression risk tracking, and migration note generation.

**Input**: Optionally specify a change name (e.g., `/change-apply update-payment-flow`). If omitted, check if it can be inferred from conversation context. If vague or ambiguous you MUST prompt for available changes.

**Steps**

1. **Invoke change-apply skill**
   Read and rigorously follow the instructions in `.agent/skills/change-apply/SKILL.md`.

This skill extends `opsx-apply` with three CR-specific extensions:

**A. Mandatory Project Standards Compliance** (same as feat-apply)
Before writing any code, the agent MUST read and comply with 4 core standard documents. HALT if any are missing.

**B. CR-Specific Context Loading**
Before implementing, read `current-code-logic.md` and `compare-logic.md` (from change-propose) to understand current code behavior and the gaps that need to be closed.

**C. CR Tracking Artifacts**
After implementation, generate in the change directory:
- `regression-check.md` — Backward compatibility verification for each existing behavior
- `migration-note.md` — Config/DB/API changes requiring deployment action
- `delta-spec.md` — Impact scope and behavioral deltas

**Guardrails**
- MUST read `current-code-logic.md` and `compare-logic.md` before implementing any task
- MUST verify backward compatibility for each modified behavior
- NEVER generate code without first loading all 4 core standard documents
