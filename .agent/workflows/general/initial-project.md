---
description: Initialize project knowledge base including rules, standards, and visual system structures.
---

Initialize the Base Knowledge system for the project by verifying `base_knowledge/` from the git framework exists, then customizing and deriving standards and structures from actual source code.

**Input**: Optional arguments to control execution scope.

| Command | What runs |
|---------|----------|
| `/initial-project` | Full flow: verify framework → derive standards → create system_overview |
| `/initial-project --structures` | Document services listed in `requirement_structures.md` (requires system_overview already done) |
| `/initial-project --service <name>` | Document one specific service (requires system_overview already done) |

**Steps**

1. **Invoke initial-project skill**
   Read and rigorously follow the instructions in `.agent/skills/initial-project/SKILL.md`.

This skill:
- Verifies `base_knowledge/` exists (HALTS if missing)
- Reads `requirement_standards.md` → derives coding standards from codebase patterns
- Reads `requirement_structures.md` → creates system overview + listed service structures
- Asks about transaction flow standards after completion

**Key Config Files (user customizes before running):**
- `base_knowledge/standards/requirement_standards.md` — which standards to derive
- `base_knowledge/structures/requirement_structures.md` — which structures to document

When base knowledge is ready, start building features with `/feat-init` → `/feat-propose`.

**Guardrails**
- MUST HALT if `base_knowledge/` does not exist — user copies from framework manually
- MUST read config files (`requirement_standards.md`, `requirement_structures.md`) before reasoning
- MUST NOT overwrite existing files without asking
- When `--structures` or `--service` is used, verify `system_overview.md` exists first
