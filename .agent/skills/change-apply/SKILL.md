---
name: change-apply
description: Implement tasks from an OpenSpec change request with CR-specific context loading, regression checking, and migration tracking.
license: VNPAY-DVNH
compatibility: Requires openspec CLI.
metadata:
  author: anhdt8
  version: "1.0"
---

Implement tasks from a Change Request (CR). Extends `opsx-apply` with CR-specific context loading, mandatory project standards compliance, regression tracking, and migration note generation.

**Input**: Optionally specify a change name. If omitted, infer from conversation context or prompt.

**Steps**

## 1. Select the Change

If a name is provided, use it. Otherwise:
- Infer from conversation context if the user mentioned a change
- Auto-select if only one active change exists
- If ambiguous, run `openspec list --json` and let the user select

Always announce: "Using change: <name>".

## 2. Check Status

```bash
openspec status --change "<name>" --json
```
Parse `schemaName` and artifact structure.

## 3. Get Apply Instructions

```bash
openspec instructions apply --change "<name>" --json
```
Handle states:
- `state: "blocked"`: show message, suggest completing artifacts first.
- `state: "all_done"`: congratulate, suggest archive.
- Otherwise: proceed.

## 4. Read Context Files

Read all files listed in `contextFiles` from the apply instructions output.

## 5. Load CR-Specific Context (MANDATORY — before any code generation)

Read the following CR artifacts created during `change-propose`:

| # | Document | Path | Purpose |
|---|----------|------|---------|
| 1 | Current Code Logic | `openspec/changes/<name>/current-code-logic.md` | Understand how the feature currently works |
| 2 | Logic Comparison | `openspec/changes/<name>/compare-logic.md` | Understand gaps to close (matched/divergent/new/missing) |
| 3 | CR Design | `openspec/changes/<name>/cr-design.md` | Understand the planned approach |

> **If `current-code-logic.md` or `compare-logic.md` are missing**: Warn the user — "CR context files not found. Implementation will proceed without regression baseline. Consider running `change-propose` first." Continue without halting.

After reading, show: "Loaded CR context: N existing behaviors tracked, M gaps to close."

## 6. Load and Validate Project Standards (MANDATORY)

Read the following 4 core documents:

| # | Document | Path |
|---|----------|------|
| 1 | Tech Stack & System Overview | `base_knowledge/structures/overview_system.md` |
| 2 | Coding Conventions | `base_knowledge/standards/coding_standard.md` |
| 3 | Logging Standard | `base_knowledge/standards/logging_standard.md` |
| 4 | Error/Exception Handling | `base_knowledge/standards/error_handling_standard.md` |

> **HALT CONDITION:** If ANY of the 4 documents above is missing, **STOP immediately**. Report which document(s) are missing and wait for the user to provide them. Do NOT generate any code.

Then also read (no halt if missing):
- All other files in `base_knowledge/standards/`
- All files in `base_knowledge/common_rules/`

Show progress: "Loaded N standard documents and M rule documents."

## 7. Show Current Progress

Display schema, progress ("N/M tasks complete"), remaining tasks overview.

## 8. Implement Tasks (loop until done or blocked)

For each pending task:
- Show which task is being worked on
- Make the code changes required
- **Strictly comply** with all standards and rules loaded in Step 6
- Keep changes minimal and focused
- Mark task complete: `- [ ]` → `- [x]`
- Continue to next task

**CR-Specific Implementation Awareness:**

While implementing, for EACH code modification:

a. **Check backward compatibility**: Does this change break any existing behavior listed in `current-code-logic.md`?
   - If yes → note in regression tracker
   - If intentional (per `compare-logic.md`) → note as "planned change"

b. **Track migration needs**: Does this change require:
   - Config key changes? (added/renamed/removed)
   - Database schema changes? (new columns/tables, altered types)
   - API contract changes? (path/method/request/response schema)
   - If yes → note in migration tracker

c. **Track delta impacts**: Does this change affect spec scope beyond what was planned?
   - If yes → note in delta tracker

**Pause if:**
- Task is unclear → ask for clarification
- Implementation reveals a design issue → suggest updating artifacts
- Code would violate a loaded standard → flag the conflict and ask
- Modification would break unplanned existing behavior → warn and ask
- Error or blocker encountered → report and wait

## 9. Generate CR Tracking Artifacts

After completing tasks, generate/update in `openspec/changes/<name>/`:

### `regression-check.md` (Always generate)

```markdown
# Regression Check: <CR Name>

## Backward Compatibility Verification

| # | Existing Behavior | Source (current-code-logic.md) | Still Works? | Evidence | Notes |
|---|-------------------|-------------------------------|-------------|----------|-------|
| 1 | [description] | [section ref] | [V] / [!] Changed / [X] Broken | [how verified] | [planned/unplanned] |

## Summary
- Total existing behaviors checked: N
- Unchanged: N ([V])
- Intentionally changed: N ([!] — per compare-logic.md)
- Unintentionally affected: N ([X] — needs attention)

## Unplanned Changes (if any)
### [Title]
- **Original behavior**: [what it did before]
- **New behavior**: [what it does now]
- **Risk**: [assessment]
- **Recommendation**: [revert / accept / discuss]
```

### `migration-note.md` (Generate if any migration needs detected)

```markdown
# Migration Note: <CR Name>

## Config Changes

| Key | Old Value | New Value | Action | Environment |
|-----|----------|-----------|--------|-------------|
| `KEY_NAME` | `old_default` | `new_default` | Update / Add / Remove | ALL / staging / prod |

## Database Migration

| Table | Change Type | SQL | Rollback SQL | Notes |
|-------|------------|-----|-------------|-------|
| `table_name` | ADD COLUMN | `ALTER TABLE...` | `ALTER TABLE DROP...` | [notes] |

## API Contract Changes

| Endpoint | Method | Change | Breaking? | Impact |
|----------|--------|--------|-----------|--------|
| `/v1/path` | POST | Added required field `fieldName` | [V] Yes / [X] No | [who is affected] |

## Deployment Checklist
- [ ] Apply config changes before deployment
- [ ] Run database migration script
- [ ] Notify API consumers of breaking changes
- [ ] Update API documentation
```

If no migration needs detected, write:
```markdown
# Migration Note: <CR Name>

No configuration, database, or API contract changes detected. Standard deployment applies.
```

### `delta-spec.md` (Generate if impact scope exists)

Same format as `feat-apply` — delta between original behavior and new implementation.

## 10. On Completion or Pause, Show Status

Display:
- Tasks completed this session
- Overall progress: "N/M tasks complete"
- Standards compliance confirmation
- **Regression check summary**: "N existing behaviors verified, M intentionally changed, K unplanned"
- **Migration needs**: "N config changes, M DB migrations, K API changes" (or "No migration needed")
- CR tracking artifacts created/updated
- If all done: suggest archive with `/change-archive`

---

**Guardrails**
- Keep going through tasks until done or blocked
- Always read context files before starting
- **MUST read `current-code-logic.md` and `compare-logic.md`** before implementing (warn if missing, don't halt)
- **NEVER generate code without first loading and validating all 4 core standard documents**
- **STRICTLY comply** with all rules in `base_knowledge/common_rules/` during code generation
- **CHECK backward compatibility** for each modified behavior against `current-code-logic.md`
- **TRACK migration needs** for every config/DB/API change
- If task is ambiguous, pause and ask before implementing
- If modification would break unplanned existing behavior, pause and warn
- If implementation reveals issues, pause and suggest artifact updates
- Keep code changes minimal and scoped to each task
- Update task checkbox immediately after completing each task
- Pause on errors, blockers, or unclear requirements — don't guess

**Fluid Workflow Integration**
- Can be invoked anytime: before all artifacts are done (if tasks exist), after partial implementation
- Allows artifact updates: if implementation reveals design issues, suggest updating artifacts
