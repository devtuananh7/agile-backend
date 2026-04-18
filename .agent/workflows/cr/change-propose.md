---
description: Propose a Change Request (CR) based on reasoning existing features and documentation mapping.
---

Propose a Change Request (CR) by deeply analyzing the current system — scanning Controller/Service/Model/Infrastructure layers, mapping code-to-documentation, performing gap analysis, and generating 5 comprehensive CR artifacts.

**Input**: The argument after the command is the CR name (kebab-case), OR a description of the feature to change/add (e.g., `update-payment-flow`).

**Steps**

1. **Invoke change-propose skill**
   Read and rigorously follow the instructions in `.agent/skills/change-propose/SKILL.md`.

This skill performs deep codebase analysis before proposing changes, generating 6 artifacts:

- **`cr-proposal.md`** — Why this CR is needed, impact analysis, risk assessment
- **`cr-spec.md`** — Scope definition: API contracts, business rules, edge cases, security, data model changes
- **`cr-design.md`** — How to modify code (file tree, logic mapping, API/DB/config changes)
- **`cr-tasks.md`** — Specific implementation checklist
- **`current-code-logic.md`** — Detailed snapshot of current code logic (processing flow, config keys, message codes, error codes, external integrations)
- **`compare-logic.md`** — Code vs documentation comparison (matched/divergent/missing/new/remove)

**Key Capabilities:**
- Scans all layers: Controller → Service → Model → Infrastructure
- Maps Code ↔ Documentation with status tracking ([V] Matched / [!] Divergent / [X] Missing)
- Performs Gap Analysis (No Change / Modify / New / Remove)
- Lists ALL config keys, message codes, and error codes in `current-code-logic.md`

When ready to implement, run `/opsx:apply`

**Guardrails**
- MUST analyze actual codebase before writing — do NOT guess
- MUST create all 6 artifacts
- MUST generate `cr-spec.md` AFTER `cr-proposal.md` and BEFORE `cr-design.md`
- MUST map code ↔ doc — if no documentation found, state explicitly
- DO NOT implement code — only create CR documents
