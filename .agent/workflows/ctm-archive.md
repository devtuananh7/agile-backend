---
description: Archive a completed change and strictly reverse-sync actual code changes back to documentation.
---

Archive a completed change with strict reverse-sync — analyzing actual code changes, detecting deviations between implementation and specs, auto-correcting documentation, and generating changelog + impact scope documents.

**Input**: Optionally specify a change name. If omitted, check if it can be inferred from conversation context. If vague or ambiguous, prompt for available changes.

**Steps**

1. **Invoke ctm-archive skill**
   Read and rigorously follow the instructions in `.agent/skills/ctm-archive/SKILL.md`.

This skill extends the standard archive flow with:

- **Deviation Detection** — Compares actual code changes (git diff) against original specs to identify unplanned additions, modifications, and omissions
- **Auto-correction** — Reverse-syncs documentation to match actual implementation, adding `> [!WARNING]` markers on corrected sections
- **Changelog Generation** — Creates `changelog.md` summarizing all changes (Added/Modified/Removed/Fixed)
- **Impact Scope Document** — Creates `impact-scope.md` listing all affected API endpoints grouped by module, for handoff to clients and testers

**Output artifacts (in archive directory):**
- `changelog.md` — Structured changelog
- `impact-scope.md` — API impact scope by module
- Auto-corrected spec/design files with deviation warnings

**Guardrails**
- MUST perform deviation detection before archiving
- MUST only edit documentation to match code — never edit code to match documentation
- MUST add `> [!WARNING]` markers on every auto-corrected section
