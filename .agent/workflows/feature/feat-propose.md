---
description: Propose a new feature - gather multi-source inputs, apply planning rules, and generate all artifacts in one step
---

Propose a new feature by collecting context from multiple input sources (URD/Confluence, documentation files, codebase references), applying feature planning rules, and generating all openspec artifacts in one step.

**Input**: The argument after the command is the feature name (kebab-case), OR a description of what the user wants to build. The user may also provide input sources (URD pages, doc files, code modules).

> **MANDATORY**: `/feat-init` PHẢI được chạy trước `/feat-propose`. Agent sẽ HALT nếu không tìm thấy `feat_overview.md` trong change directory.

**Steps**

1. **Invoke feat-propose skill**
   Read and rigorously follow the instructions in `.agent/skills/feat-propose/SKILL.md`.

This skill collects multi-source inputs and generates artifacts with extended rules:

- **`proposal.md`** — What & why (via openspec)
- **`design.md`** — How (extended with Design Rules from `rule_planing_feature.md`)
- **`spec files`** — Implementation scope (extended with Spec Rules)
- **`tasks.md`** — Implementation steps

**Key Capabilities:**
- Multi-source input collection: URD/Confluence (via MCP), in-project docs, external docs, codebase references
- Loads planning rules from `base_knowledge/common_rules/rule_planing_feature.md`
- Uses `feat_overview.md` from `feat-init` as targeted input map for context collection (parallel batch read)
- All artifacts cross-reference input sources for traceability

When ready to implement, run `/feat-apply`

**Guardrails**
- MUST read and apply rules from `rule_planing_feature.md` (warn if missing, continue with defaults)
- MUST collect context from ALL input sources provided by user before creating any artifact
