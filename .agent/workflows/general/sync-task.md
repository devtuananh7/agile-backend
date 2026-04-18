---
description: Sync documentation artifacts of a change/task with current source code. Detects drift and auto-corrects documents.
---

Synchronize a change's documentation artifacts with the current source code. Finds a change by name or metadata ID, compares docs vs code, auto-corrects documentation if drifted, and handles archived changes.

**Input**: Task name (kebab-case) or metadata ID (e.g., `VCBDIGIBIZ260323A1b2C`).

**Steps**

1. **Invoke sync-task skill**
   Read and rigorously follow the instructions in `.agent/skills/sync-task/SKILL.md`.

This skill performs documentation-to-code synchronization:

- **Find** the change by name or `id` field in `metadata.yaml` (searches both active and archived changes)
- **Compare** all documentation artifacts against current source code
- **If in sync** → report and exit
- **If drifted** → auto-correct documentation to match code, add `> [!WARNING]` markers
- **If archived** → move back to `openspec/changes/`, sync, then prompt user to re-archive
- **Update `metadata.yaml`** with `last-sync` timestamp

**Guardrails**
- MUST only edit documentation to match code — never edit code
- MUST handle archived changes by unarchiving before sync
- MUST update `last-sync` in `metadata.yaml` after every sync
