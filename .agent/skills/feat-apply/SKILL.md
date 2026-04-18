---
name: feat-apply
description: Implement tasks from an OpenSpec feature change with L1-absolute document authority, standards deviation tracking, and extended tracking documentation (todo-uncover, new-apis, delta-spec, standards-deviation).
license: VNPAY-DVNH
compatibility: Requires openspec CLI.
metadata:
  author: anhdt8
  version: "1.0"
---

Implement tasks from an OpenSpec change. L1 artifacts (proposal, design, specs, tasks) are absolute authority. Standards are applied as advisory — deviations are tracked and reported, not blocked.

**Input**: Optionally specify a change name. If omitted, infer from conversation context or prompt.

---

## Document Authority Hierarchy

All documents used during implementation follow a strict authority hierarchy. **L1 is absolute** — OpenSpec default artifacts always win. No other document may override them.

| Level | Documents | Authority | Role |
|-------|-----------|-----------|------|
| **L1 — Source of Truth (ABSOLUTE)** | `proposal.md`, `design.md`, `specs/*/spec.md`, `tasks.md` | **HIGHEST — ALWAYS WINS** | OpenSpec default artifacts. Defines WHAT to build, HOW to design, WHAT to implement. No other level may override. |
| **L2 — Standards & Rules** | `tech_requirement.md` (optimized) OR 4 core docs + `base_knowledge/*` (fallback) | **ADVISORY** — follow when NOT conflicting with L1 | Project coding patterns, conventions, error handling. Applied as best-effort; L1 overrides when conflict exists. |
| **L3 — Supplementary** | `feat_overview.md`, `todo-uncover.md`, `new-apis.md`, `delta-spec.md`, `metadata.yaml` | **INFORMATIONAL** — tracks discoveries, does NOT override | Context map, tracking artifacts. Generated during init/apply. |

### L1 Absolute: Conflict Resolution

When L1 (OpenSpec artifacts) and L2 (Standards) conflict:

> **L1 ALWAYS WINS. Implement exactly as `design.md` / `specs/` specifies.**
>
> However, when following L1 causes a **standards deviation**, the agent **MUST**:
> 1. **Implement as L1 dictates** — do NOT deviate from design.md / specs
> 2. **Emit a loud WARNING** immediately at the point of deviation
> 3. **Collect ALL deviations** into a `standards-deviation.md` report (see Step 9)

**Warning format** — display in-line during implementation:

```
[!!! STANDARDS DEVIATION DETECTED !!!]
============================================================
  L1 Source:    design.md -- [what design.md says]
  L2 Standard:  [standard name] -- [what standard says]
  Decision:     FOLLOWING L1 (design.md wins)
  Impact:       [what coding standard is violated]
============================================================
```

> **Example**: `design.md` specifies a custom error response format `{"err": "..."}` but `error_handling_standard.md` requires `{"errorCode": "...", "message": "..."}`. Agent implements `design.md`'s format, emits WARNING, records in `standards-deviation.md`.

### L3 Cannot Override L1

Supplementary documents (L3) are **tracking artifacts only**:
- `todo-uncover.md` records discoveries — it does NOT change the scope defined in `specs/`
- `new-apis.md` records new endpoints created — it does NOT override endpoints defined in `design.md`
- `delta-spec.md` records deltas — it does NOT modify the original spec
- If L3 reveals a conflict with L1 → **flag it, do NOT auto-resolve**. Suggest the user update the L1 artifact instead.

---

**Steps**

1. **Select the change**

   If a name is provided, use it. Otherwise:
   - Infer from conversation context if the user mentioned a change
   - Auto-select if only one active change exists
   - If ambiguous, run `openspec list --json` and let the user select

   Always announce: "Using change: <name>".

2. **Check status**
   ```bash
   openspec status --change "<name>" --json
   ```
   Parse `schemaName` and artifact structure.

3. **Get apply instructions**
   ```bash
   openspec instructions apply --change "<name>" --json
   ```
   Handle states:
   - `state: "blocked"`: show message, suggest completing artifacts first.
   - `state: "all_done"`: congratulate, suggest archive.
   - Otherwise: proceed.

4. **Gate Check: Verify `feat_overview.md` exists (MANDATORY)**

   Check if `openspec/changes/<name>/feat_overview.md` exists.

   **IF NOT FOUND**:
   - Stop immediately. Do NOT proceed to read contextFiles.
   - Announce: "Change `<name>` chưa có feat_overview.md. Hãy chạy `/feat-init <name>` trước."

   **IF FOUND**:
   - Announce: "Detected feat_overview.md, using as context map."
   - Proceed to Step 5.

5. **Get OpenSpec Context Files**

   Extract the list of `contextFiles` from the apply instructions output from Step 3. 
   (These usually include `proposal.md`, `design.md`, `specs/`, `tasks.md`).
   **Keep this list ready for the parallel batch read in Step 6.**

6. **Smart Context Loading (MANDATORY — before any code generation)**

   Check if `openspec/changes/<name>/tech_requirement.md` exists. This dictates how L2 Standards are loaded.

   ### Optimized Mode (tech_requirement.md EXISTS)

   For your L2 standards, you will use `tech_requirement.md` (~2-3KB) which contains condensed technical rules.
   - **DO NOT** add raw files from `feat_overview.md` Sections 2/3/4 to your reading list.
   - **DO NOT** add the 4 core documents (they are already extracted into `tech_requirement.md`).

   **Parallel batch read:**
   Agent MUST call `view_file` for **ALL OpenSpec Context Files** (from Step 5) **AND** `tech_requirement.md` using **parallel tool calls in a single turn**.
   Do NOT read files sequentially.

   > **Deviation detection in Optimized Mode**: `tech_requirement.md` contains condensed DO/DON'T rules, not full standard details. L1-vs-L2 deviation detection is **best-effort** in this mode. If a potential deviation is detected but L2 detail is insufficient to confirm, note it in `standards-deviation.md` with severity `UNCONFIRMED` and reference the original standard file path for manual review.

   > **Design Checklist Parsing**: After reading `tech_requirement.md`, check if it contains a `## Design Checklist` section. If found:
   > - Parse ALL `###` subsections (one per standard source)
   > - For each table row, extract: Item description, Status (`0` = không dùng, `1` = dùng), Note
   > - Store extracted checklist items in an internal compliance tracker for use in Step 8
   > - Log: "Design Checklist found: N items from M sources"
   >
   > If `## Design Checklist` section is NOT found:
   > - Log: "No Design Checklist found — skipping compliance check"
   > - Continue normally — no error, no block

   ### Fallback Mode (tech_requirement.md DOES NOT EXIST)

   For your L2 standards, you must read the raw files from `feat_overview.md` Sections 2/3/4, using **phase filtering** and **planning-only file exclusion**.

   **Turn 1 — Read overview and build file list:**

   Read `feat_overview.md`. Parse Section 2 (Rules), Section 3 (Standards), Section 4 (Structures).

   **Phase Filtering (when Phase column exists):**

   Detect if `feat_overview.md` has a **Phase column** in Sections 2, 3, 4:
   - **If Phase column exists**: Collect ONLY files where Phase contains `apply`. Skip files where Phase is only `propose`, only `archive`, or only `init`.
   - **If Phase column does NOT exist** (legacy 3-column format): Fallback — collect ALL files with Include=1, then apply planning-only exclusion below.

   Announce: "Phase filtering (apply): Found N files for apply phase (M total in overview)." — or "No Phase column detected, reading all N files."

   Build the final L2 file list:
   - Start with the 4 core documents (always required, regardless of phase filter):

     | # | Document | Path |
     |---|----------|------|
     | 1 | Tech Stack & System Overview | `base_knowledge/structures/overview_system.md` |
     | 2 | Coding Conventions | `base_knowledge/standards/coding_standard.md` |
     | 3 | Logging Standard | `base_knowledge/standards/logging_standard.md` |
     | 4 | Error/Exception Handling | `base_knowledge/standards/error_handling_standard.md` |

   - Add phase-filtered files from `feat_overview.md` Section 2/3/4 that are NOT already in the 4 core docs list (deduplicate).
   - **EXCLUDE planning-only files** (do NOT read even if listed in overview or matching phase):
     - `rule_planing_feature.md` — planning rules only, not relevant for code generation
     - `requirement_standards.md` — requirement writing rules only
     - `requirement_structures.md` — architecture survey templates only

   **Turn 2 — Parallel batch read ALL files (L1 + L2):**

   Agent MUST call `view_file` for **ALL OpenSpec Context Files** (from Step 5) **AND** **ALL L2 files** (4 core + phase-filtered supplementary, minus excluded) using **parallel tool calls in a single turn**. Do NOT read files sequentially one by one.

   After the parallel batch completes:

   > **WARNING CONDITION (core docs):** If ANY of the 4 core documents is missing, **emit a loud WARNING** (see format above) listing which document(s) are missing and what standards area is affected (coding/logging/error handling). **Continue implementation** -- L1 artifacts are sufficient to proceed. Record the missing standard in `standards-deviation.md` header as: "L2 coverage incomplete -- [document name] not loaded. Deviation detection for [area] is UNCONFIRMED."

   > **WARNING CONDITION:** If any supplementary file from overview is missing, log WARNING "File not found: <path> — skipped" and continue. Do NOT halt.

   Show progress: "tech_requirement.md not found — fallback to raw files mode. Loaded OpenSpec artifacts + 4 core docs + N supplementary."

   > **[WARNING] Fallback Mode — Design Checklist enforcement:**
   > In Fallback Mode, Design Checklist enforcement is SKIPPED completely because checklists are only stored in `tech_requirement.md`. Agent MUST emit:
   > ```
   > [WARNING] Fallback Mode — tech_requirement.md không tồn tại.
   > Design Checklist enforcement bị SKIP hoàn toàn.
   > Nếu feature có checklist, hãy chạy lại feat-propose để tạo tech_requirement.md.
   > ```

7. **Show current progress**

   Display schema, progress ("N/M tasks complete"), remaining tasks overview.

8. **Implement tasks (loop until done or blocked)**

   For each pending task:
   - Show which task is being worked on
   - Make the code changes required
   - **Follow L1 artifacts absolutely** — `design.md` and `specs/` dictate all implementation decisions
   - Apply L2 standards for coding patterns **ONLY when they do NOT conflict with L1**
   - Keep changes minimal and focused
   - Mark task complete: `- [ ]` → `- [x]`
   - Continue to next task

   While implementing, track: new APIs created, spec scopes impacted, uncovered TODOs, **standards deviations**, **Design Checklist compliance**.

   **Design Checklist Compliance Tracking:**

   If a Design Checklist was parsed in Step 6, for each checklist item with Status `1` (dùng):
   - Track whether the implementation actually addresses this item
   - When code concretely satisfies a checklist item → confirm `1` in internal tracker
   - If a checklist item with Status `1` (dùng) conflicts with L1 (design.md / specs) → keep `0` in tracker with note "Overridden by L1", emit WARNING block, record in `standards-deviation.md`
   - Items with Status `0` (không dùng) are informational — no enforcement needed

   **L1-Absolute Resolution During Implementation:**

   For EACH code decision:
   - **ALWAYS follow L1** (`design.md`, `specs/`, `proposal.md`, `tasks.md`) — these are the user's reviewed and approved decisions
   - **Check L2** (standards from Step 6) — if L2 agrees with L1, apply both. If L2 conflicts with L1:
     1. **Implement as L1 says** — do NOT deviate
     2. **Emit the WARNING block** (see Document Authority Hierarchy above) immediately
     3. **Record the deviation** in an internal tracker for Step 9
   - **L3 is never consulted for implementation decisions** — L3 is tracking only

   **Pause if:**
   - Task is unclear → ask for clarification
   - Implementation reveals a design issue → suggest updating L1 artifacts (design.md / specs)
   - Error or blocker encountered → report and wait
   - **Do NOT pause for L1-vs-L2 conflicts** — L1 always wins, just warn loudly

9. **Generate Extended Tracking Artifacts**

   After completing tasks, generate/update in `openspec/changes/<name>/`:

   | Artifact | When to Create | Content |
   |----------|---------------|---------| 
   | `todo-uncover.md` | Always | `TODO`, `FIXME`, uncovered edge cases discovered during implementation |
   | `new-apis.md` | If new endpoints were added | Path, HTTP Method, Request/Response payload, purpose |
   | `delta-spec.md` | If impact scope exists | Delta between original behavior and new implementation |
   | `standards-deviation.md` | **If ANY L1-vs-L2 conflict was detected** | Full deviation report (see template below) |

   **`standards-deviation.md` template** (generate ONLY if deviations exist):

   ```markdown
   # [WARNING] Standards Deviation Report: <change-name>

   > This file records ALL instances where L1 artifacts (design.md / specs) were followed
   > despite conflicting with L2 project standards. These are INTENTIONAL deviations
   > approved by following the Document Authority Hierarchy (L1 absolute).
   >
   > **Action Required**: Review each deviation. Either:
   > - Accept the deviation (design intent overrides standard)
   > - Update L1 artifacts to align with standards, then re-implement

   ## Summary

   | Total Deviations | Critical | Warning | Unconfirmed |
   |-----------------|----------|---------|-------------|
   | N | N | N | N |

   ## Deviations

   ### DEV-001: [Short title]
   - **L1 Source**: `design.md` line/section — "[what design says]"
   - **L2 Standard**: `[standard_file.md]` — "[what standard requires]"
   - **What was implemented**: [description of actual code]
   - **Standard violated**: [which specific rule/pattern]
   - **Severity**: WARNING / CRITICAL / UNCONFIRMED
   - **Recommendation**: [accept / update design / refactor later]

   ### DEV-002: ...
   ```

10. **On completion or pause, show status**

    Display:
    - Tasks completed this session
    - Overall progress: "N/M tasks complete"
    - **Standards deviations: "[WARNING] N deviations detected -- see standards-deviation.md"** (or "No deviations detected")
    - **Design Checklist compliance** (if checklist was parsed in Step 6):
      - If ALL items with Status `1` are verified: "Design Checklist: N/N items satisfied"
      - If some items unverified or overridden: display summary table with `0/1` status, notes for each item
    - Extended artifacts created/updated
    - If deviations exist: **show the full deviation summary table** inline
    - If all done: suggest archive

**Guardrails**

_Document Authority (L1 Absolute):_
- **MUST** treat OpenSpec default artifacts (`proposal.md`, `design.md`, `specs/*/spec.md`, `tasks.md`) as **L1 — ABSOLUTE Source of Truth** for ALL decisions (business logic AND coding approach)
- **MUST** follow L1 even when it conflicts with L2 standards — L1 ALWAYS wins
- **MUST** emit a loud WARNING block (see format above) at EVERY point where L1 overrides L2
- **MUST** generate `standards-deviation.md` if ANY L1-vs-L2 conflict was detected during implementation
- **MUST** treat standards/rules (from Step 6) as **L2 — Advisory** that apply ONLY when NOT conflicting with L1
- **MUST** treat supplementary artifacts (`feat_overview.md`, `todo-uncover.md`, `new-apis.md`, `delta-spec.md`) as **L3 — Informational** — they NEVER override L1 or L2
- **MUST NOT** let `tech_requirement.md` or any standard override ANY decision in `design.md` or `specs/` — if conflict exists, follow L1 and warn
- **MUST NOT** modify L1 artifacts without explicit user approval — if a deviation is found, record it and let user decide
- **MUST NOT** silently violate standards — every deviation MUST be warned loudly and recorded
- **MUST** treat Design Checklist items in `tech_requirement.md` as **L2 advisory** — they do NOT override L1. If a checklist item conflicts with `design.md` or `specs/`, follow L1, emit WARNING, and report checklist item as `0` with note "Overridden by L1"

_Gate Checks & Context Loading:_
- Keep going through tasks until done or blocked
- Always read context files before starting
- **MUST** run gate check for `feat_overview.md` (Step 4) BEFORE reading contextFiles (Step 5) — if overview is missing, HALT immediately without reading any files
- **MUST** check for `tech_requirement.md` BEFORE reading raw files — if it exists, use optimized mode
- **MUST** filter files by phase=apply in fallback mode when Phase column exists in `feat_overview.md` Sections 2/3/4. Skip files with phase-only `propose`, `archive`, or `init`
- **MUST** fallback to reading ALL Include=1 files (with planning-only exclusion) when Phase column does NOT exist
- **MUST NOT** read planning-only files in fallback mode: `rule_planing_feature.md`, `requirement_standards.md`, `requirement_structures.md`
- In fallback mode: If any of the 4 core standard documents is missing, **WARN loudly but continue** -- L1 is sufficient for implementation. Missing L2 docs reduce deviation detection accuracy, not implementation capability
- In optimized mode: 4 core docs check is NOT required (already extracted into tech_requirement.md)
- **MUST use parallel tool calls** to read all context files in a single batch — do NOT read sequentially

_Implementation:_
- If task is ambiguous, pause and ask before implementing
- If implementation reveals issues, pause and suggest updating L1 artifacts
- Keep code changes minimal and scoped to each task
- Update task checkbox immediately after completing each task
- Pause on errors, blockers, or unclear requirements — don't guess
- **Do NOT pause or halt for L1-vs-L2 conflicts** — implement L1, warn, and continue

**Fluid Workflow Integration**
- Can be invoked anytime: before all artifacts are done (if tasks exist), after partial implementation
- Allows artifact updates: if implementation reveals design issues, suggest updating artifacts
