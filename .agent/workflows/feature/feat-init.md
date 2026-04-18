---
description: Initialize a feature change - gather all input sources and create feat_overview.md before feat-propose
---

Initialize a feature change by collecting and inventorying all input sources (business docs, rules, standards, structures, satellite docs, knowledge, reference code) into `feat_overview.md` for developer review before running `feat-propose`.

**Input**: The argument after the command is the feature name (kebab-case), OR a description of what the user wants to build. The user may also provide input sources (Jira links, Confluence pages, specific rules/standards, satellite docs, reference code).

**Steps**

1. **Invoke feat-init skill**
   Read and rigorously follow the instructions in `.agent/skills/feat-init/SKILL.md`.

This skill creates:
- **`metadata.yaml`** — Change tracking information
- **`feat_overview.md`** — Consolidated input inventory with 7 sections

**Key Capabilities:**
- 7-section input inventory: Business References, Rules, Standards, Structures, Satellite Docs, Related Knowledge, Reference Codebase
- Dual mode: explicit input (developer lists files) or auto-scan (`base_knowledge/` directories)
- Sections 2-4 include key points summary for each rule/standard/structure file
- Developer review checkpoint before artifact generation

When reviewed and ready, run `/feat-propose` to generate all artifacts.

**Guardrails**
- MUST NOT generate openspec artifacts (proposal, design, specs, tasks) — only `metadata.yaml` and `feat_overview.md`
- MUST auto-scan `base_knowledge/` when developer doesn't provide explicit input for rules/standards/structures
- MUST include key points summary (not just file names) for each inventoried file
