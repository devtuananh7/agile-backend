---
name: ctm-archive
description: Archive a completed change and strictly reverse-sync actual code changes back to documentation. Use when the user wants to finalize a change and ensure documentation reflects reality.
license: VNPAY-DVNH
compatibility: Requires project codebase with existing change directory and documentation.
metadata:
  author: anhdt8
  version: "1.0"
---

Archive a completed change and reverse-sync from actual source code back to documentation to ensure specs always reflect reality.

I'll perform:
- Task completion check
- Actual code change analysis (what was really implemented)
- Code vs. original specs deviation detection
- Auto-append deviations to documentation
- Sync updated delta specs to main specs
- Move change to archive
- **Generate impact scope document** (API-level impact listing) for client/tester handoff
- **Generate changelog** with explicit config keys, message codes, and error codes

---

**Input**: Change name to archive (e.g., `/ctm:archive update-payment-flow`). If omitted, prompt for available changes.

**Steps**

## 1. Identify the Change to Archive

- If no change name provided, scan `openspec/changes/` for active (non-archived) changes.
- Present the list and let the user select.
- If only one active change exists, confirm with the user before proceeding.

**IMPORTANT**: Do NOT guess or auto-select a change. Always let the user confirm.

## 2. Check Task Completion Status

Read the tasks file (`cr-tasks.md` or `tasks.md`) to check for incomplete tasks.

Count tasks marked with `- [ ]` (incomplete) vs `- [x]` (complete).

**If incomplete tasks found:**
- Display warning showing count: `"Warning: X of Y tasks are still incomplete."`
- Ask user for confirmation to continue.
- Proceed only if user confirms.

**If no tasks file exists:** Proceed without task-related warning.

## 3. Scan Actual Code Changes (Actual Code Analysis)

Analyze the source files that were modified or created within the scope of this change:

a. **Identify changed files**:
   - Use `git diff` or manual file scanning to find modified/new files related to this change.
   - Focus on Controller, Service, Model/Entity, and Config layers.

b. **Extract actual implementation details**:
   - New or modified endpoints (path, method, request/response schema).
   - New or modified service methods (method name, parameters, return type, key logic).
   - New or modified entities/DTOs (fields, annotations).
   - New or modified config keys.
   - New error codes introduced.

c. **Build Actual Implementation Summary**:

   ```
   ┌─────────────────────────────────────────────────┐
   │         ACTUAL IMPLEMENTATION SUMMARY           │
   ├─────────────────────────────────────────────────┤
   │ Controllers modified/added: [count]             │
   │ Services modified/added:    [count]             │
   │ Models modified/added:      [count]             │
   │ Config changes:             [count]             │
   │ New error codes:            [count]             │
   └─────────────────────────────────────────────────┘
   ```

## 4. Compare Actual Code vs. Original Specs (Deviation Detection)

Compare the results from Step 3 with the original CR documents:
- `cr-proposal.md` / `proposal.md`
- `cr-design.md` / `design.md`
- `specs/*.md`

a. **Build deviation table**:

   | Item | Original Spec | Actual Implementation | Deviation Type |
   |---|---|---|---|
   | Endpoint `/v1/xxx` | Described in design.md | Implemented as described | None |
   | Service `YyyService.methodZ()` | Not in original spec | Added during implementation | Addition |
   | Field `newField` in `EntityA` | Described as `String` | Implemented as `Long` | Modification |
   | Config `KEY_ABC` | Not mentioned | Added with default "10" | Addition |

b. **Categorize deviations**:
   - **None**: Implemented exactly as specified.
   - **Addition**: New logic/files/fields added that were not in the original spec.
   - **Modification**: Implemented differently from the original spec.
   - **Omission**: Specified in docs but not implemented (or deferred).

c. **Generate deviation summary**:

   ```
   ┌─────────────────────────────────────────────────┐
   │           DEVIATION DETECTION REPORT            │
   ├─────────────────────────────────────────────────┤
   │ Matched (no deviation):  [count] items          │
   │ Additions:               [count] items          │
   │ Modifications:           [count] items          │
   │ Omissions:               [count] items          │
   └─────────────────────────────────────────────────┘
   ```

## 5. Append Deviations to Documentation

If deviations were found in Step 4:

a. **Append to `cr-design.md` / `design.md`**:
   Add a new section at the end of the file:

   ```markdown
   ## Actual Implementation Deviations

   _Auto-generated during archive on YYYY-MM-DD._

   ### Additions (not in original spec)
   | Component | File/Method | Description |
   |---|---|---|
   | ... | ... | ... |

   ### Modifications (differs from original spec)
   | Component | Original | Actual | Reason |
   |---|---|---|---|
   | ... | ... | ... | ... |

   ### Omissions (specified but not implemented)
   | Component | Original Spec | Status |
   |---|---|---|
   | ... | ... | Deferred / Cancelled |
   ```

b. **Append to `spec.md`** (if delta specs exist):
   Add a similar section to each relevant spec file.

c. **Verify** each file was successfully updated before proceeding.

## 6. Sync Updated Specs to Main (Sync to Main)

Check for delta specs at `openspec/changes/<name>/specs/`.

**If delta specs exist:**
- Compare each delta spec with its corresponding main spec at `openspec/specs/<capability>/spec.md`.
- Show a summary of what changes would be applied (adds, modifications, removals).
- Prompt user:
  - "Sync now (recommended)" — merge delta specs into main specs.
  - "Archive without syncing" — skip sync.

**If no delta specs exist:** Proceed directly to archive.

## 7. Perform the Archive

a. **Create archive directory** if it doesn't exist:
   ```bash
   mkdir -p openspec/changes/archive
   ```

b. **Generate target name** using current date: `YYYY-MM-DD-<change-name>`

c. **Check if target already exists:**
   - If yes: Fail with error, suggest renaming existing archive or using a different date.
   - If no: Move the change directory to archive.

   ```bash
   mv openspec/changes/<name> openspec/changes/archive/YYYY-MM-DD-<name>
   ```

d. **Verify** the move was successful (target directory exists, source directory is gone).

## 8. Generate Changelog

Generate `changelog.md` documenting all changes made in this CR. The changelog **MUST** explicitly list:

a. **Config keys**: All configuration keys retrieved via `commonService.getConfig(...)` or `configService.getConfig(...)` that were added, modified, or removed.

   | Config Key | Default Value | Action | Used In | Description |
   |-----------|---------------|--------|---------|-------------|
   | `FREE_FEE_HKD` | `"0"` | Added | `TransactionFeeService.getFeeTransfer()` | Enable free fee for HKD customers |

b. **Message codes**: All message codes used via `CommonService.getMessage(...)` or `Constants.MessageCode.*` that were added or changed.

   | Message Code | Constant | Used In | Description |
   |-------------|----------|---------|-------------|
   | `Constants.MessageCode.ERROR_112` | `ERROR_112` | `TransferOutService` | Insufficient balance error |

c. **Error/Response codes**: All response codes from `Constants.ResCode.*` that were added or changed.

   | Error Code | Constant | HTTP Context | Trigger | Description |
   |-----------|----------|-------------|---------|-------------|
   | `Constants.ResCode.ERROR_112` | `ERROR_112` | POST /v1/maker/init | Balance check failed | Insufficient account balance |

d. **General changes**: Summary of all other modifications (new fields, updated logic, etc.)

e. **Save file** at `openspec/changes/archive/YYYY-MM-DD-<name>/changelog.md`

## 9. Generate Impact Scope Document

Generate `impact-scope.md` listing all affected API endpoints for client/tester handoff.

a. **From the changed file list (Step 3)**, trace back to Controller layer:
   - Modified Service method → find calling Controller → extract `@RequestMapping` path
   - Modified DTO/Entity → find using Service → find calling Controller
   - Directly modified Controller → extract `@RequestMapping` path

b. **Group results by module/service**, format as table:

   | Module | API Path | Method | Controller | Affected Service | Change Description |
   |--------|----------|--------|------------|-----------------|--------------------|
   | transferin-service | /v1/maker/init | POST | TransferController | TransferInService | Added cusType to fee calculation |

c. **Important notes at end of document**:
   - Any input/output API changes? (request/response schema)
   - Any new error codes introduced?
   - Any configuration changes (config/DB)?
   - Special notes for testers (if any)

d. **Save file** at `openspec/changes/archive/YYYY-MM-DD-<name>/impact-scope.md`

## 10. Display Archive Report

Show a detailed archive completion report.

---

**Output On Success**

```
## Archive Complete

**Change:** <change-name>
**Archived to:** openspec/changes/archive/YYYY-MM-DD-<name>/
**Specs:** Synced to main specs / Sync skipped / No delta specs

### Task Status
All X tasks complete.

### Deviation Report
- Matched: [count] items (implemented as specified)
- Additions: [count] items (auto-appended to documentation)
- Modifications: [count] items (auto-appended to documentation)
- Omissions: [count] items (noted in documentation)

### Impact Scope
- Total APIs affected: [count]
- Modules: [list of affected modules]
- Document: impact-scope.md

### Changelog
- Config keys changed: [count]
- Message codes changed: [count]
- Error codes changed: [count]
- Document: changelog.md

### Updated Documents
- cr-design.md — Appended "Actual Implementation Deviations" section
- specs/xxx/spec.md — Appended deviations (if applicable)
- impact-scope.md — API impact scope for client/tester handoff
- changelog.md — Detailed changelog with config/message/error code listings
```

**Output On Success (No Deviations)**

```
## Archive Complete

**Change:** <change-name>
**Archived to:** openspec/changes/archive/YYYY-MM-DD-<name>/
**Specs:** Synced to main specs

All tasks complete. No deviations detected — implementation matches original specifications.
```

**Output With Warnings**

```
## Archive Complete (with warnings)

**Change:** <change-name>
**Archived to:** openspec/changes/archive/YYYY-MM-DD-<name>/

**Warnings:**
- Archived with X incomplete tasks
- Spec sync was skipped (user chose to skip)
- Y deviations detected and appended to documentation

Review the archive if this was not intentional.
```

**Output On Error (Archive Exists)**

```
## Archive Failed

**Change:** <change-name>
**Target:** openspec/changes/archive/YYYY-MM-DD-<name>/

Target archive directory already exists.

**Options:**
1. Rename the existing archive
2. Delete the existing archive if it's a duplicate
3. Wait until a different date to archive
```

---

**Artifact Update Guidelines**

- When appending deviations, **DO NOT modify existing content** — only append new sections at the end.
- Use the heading `## Actual Implementation Deviations` consistently across all files.
- Include the auto-generation date in the deviation section.
- Comply with **Planning Rule**: file changes in tree format, 1-to-1 logic mapping.
- Comply with **Error Handling Rule**: document any new error codes discovered during code analysis.
- Each deviation entry must reference the actual file path and method name.

**Guardrails**

- **MUST** scan actual code changes before archiving — do not skip deviation detection.
- **MUST** append deviations to documentation if any are found — this is the core value of this workflow.
- **MUST** generate `impact-scope.md` listing all affected API endpoints grouped by module.
- **MUST** trace from changed Service files → Controller → `@RequestMapping` path to identify affected APIs.
- **MUST** generate `changelog.md` with explicit tables for config keys, message codes, and error codes.
- **MUST** search for all `getConfig(...)`, `getMessage(...)`, `Constants.ResCode.*`, `Constants.MessageCode.*` in changed files.
- **MUST** prompt user before syncing specs to main — never auto-sync.
- **DO NOT** modify existing content in spec files — only append deviation sections.
- **DO NOT** block archive on warnings — inform, confirm, and proceed.
- **DO NOT** auto-select a change — always let the user confirm.
- **DO NOT** guess API paths — only include paths confirmed by tracing actual `@RequestMapping` annotations.
- **DO NOT** omit config keys, message codes, or error codes from the changelog — these are critical for deployment and QA.
- If deviation analysis fails, warn the user and offer to archive without deviations.
- Preserve all existing files when moving to archive (the entire directory moves).
- Verify the archive directory exists after the move before reporting success.
