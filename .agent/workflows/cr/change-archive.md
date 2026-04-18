---
description: Archive a completed change request with code-vs-docs sync, compare-logic update, changelog generation, and knowledge export warning.
---

Archive a completed Change Request (CR), extending the standard opsx-archive process with code-vs-documentation sync check, final compare-logic update, changelog generation, CR tracking artifact preservation, and knowledge export advisory.

**Input**: Optionally specify a change name (e.g., `/change-archive update-payment-flow`). If omitted, check if it can be inferred from conversation context. If vague or ambiguous you MUST prompt for available changes.

**Steps**

1. **Invoke change-archive skill**
   Read and rigorously follow the instructions in `.agent/skills/change-archive/SKILL.md`.

This skill extends `opsx-archive` with CR-specific steps:

**A. Code-vs-Documentation Sync Check** (same as feat-archive)
Compare actual code vs documentation. Auto-correct docs if mismatched, add `> [!WARNING]` markers.

**B. Update `compare-logic.md` (Final State)**
Update the comparison table with post-implementation state, showing Before → After for each logic point.

**C. Changelog Generation**
Generate `changelog.md` summarizing Added/Modified/Removed/Fixed items with migration steps and risk notes.

**D. CR Tracking Artifact Preservation**
Ensure `regression-check.md`, `migration-note.md`, and `delta-spec.md` travel with the archive.

**E. Knowledge Export Advisory**
Warn the user if the CR may affect `base_knowledge/` (standards, structures). Do NOT auto-update — only advise.

**Guardrails**
- MUST perform code-vs-docs sync check before archiving
- MUST only edit documentation to match code — never edit code to match documentation
- MUST NOT auto-update `base_knowledge/` — only warn and advise
