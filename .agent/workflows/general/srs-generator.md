---
description: Generate SRS document from change artifacts and source code. Works with any change type (feat-*, change-*, opsx-*).
---

Generate an SRS (Software Requirements Specification) document for any change. Works independently — can be invoked anytime after apply, before archive, or standalone.

**Input**: Optionally specify a change name or metadata ID (e.g., `/srs-generator add-auth`). If omitted, auto-detects from context.

**Steps**

1. **Invoke srs-generator skill**
   Read and rigorously follow the instructions in `.agent/skills/srs-generator/SKILL.md`.

This skill performs SRS generation:

- **Resolve input** → change name, metadata ID, or auto-detect (1 active → auto-select, multiple → ask)
- **Load SRS template** from `base_knowledge/common_rules/rule_generate_srs.md`
- **Collect sources** → change artifacts (proposal, design, specs, tasks) + tracking artifacts (todo-uncover, new-apis, delta-spec) + source code
- **Generate SRS** → `openspec/changes/<name>/srs.md` following IEEE 830 template with 6 sections
- **Report** → sections generated, sources used, TODOs if any

**Guardrails**
- MUST read `rule_generate_srs.md` template before generating — halt if missing
- MUST derive requirements from actual artifacts/code — never assume
- MUST include test scenarios for every System Feature
- MUST write in business language (audience: tester/client)
- Works with all change types: feat-*, change-*, opsx-*
