---
name: feat-propose
description: Propose a new feature with multi-source input collection and planning rules. Use when the user wants to build a new feature by gathering requirements from URD/Confluence, documentation files, and codebase references, then generating openspec artifacts with extended design and spec rules.
license: VNPAY-DVNH
compatibility: Requires openspec CLI and optionally Confluence MCP server.
metadata:
  author: anhdt8
  version: "1.0"
---

Propose a new feature - collect context from multiple input sources, apply feature planning rules, and generate all openspec artifacts in one step.

I'll create a feature change with artifacts:
- proposal.md (what & why)
- design.md (how — extended with design rules)
- spec files (implementation scope — extended with spec rules)
- tasks.md (implementation steps)

When ready to implement, run /opsx-apply

---

**Input**: The user's request should include a feature name (kebab-case) OR a description of what they want to build.

**IMPORTANT**: `feat-propose` requires `feat-init` to be run first. It does not accept URLs or file paths directly anymore. All context must be prepared in `feat_overview.md`.

**Steps**

0. **Gate Check (Mandatory feat-init + Jira validation)**

   Before proceeding, verify that `feat-init` has been run and Jira traceability exists:

   a. Derive the kebab-case change name from user input
   b. Check if `openspec/changes/<name>/` directory exists
   c. Check if `openspec/changes/<name>/feat_overview.md` exists
   d. Read `openspec/changes/<name>/metadata.yaml` and check `jira` field

   **Gate 1 -- feat_overview.md check:**

   **IF NOT FOUND**: 
   - Stop immediately.
   - Announce: "Change `<name>` chưa được khởi tạo đầy đủ. Hãy chạy `/feat-init <name>` trước để chuẩn bị input inventory."

   **Gate 2 -- Jira traceability check (from metadata.yaml):**

   Read `metadata.yaml` and inspect the `jira` field:

   - **If jira has real ticket IDs** (e.g. `VCBDB-1234`): Proceed. Announce: "Jira traceability: [N] tickets linked."
   - **If jira contains `<force flow>` marker**: Proceed with warning. Announce: "[WARNING] Feature chạy ở chế độ force flow (không có Jira). Traceability bị giới hạn."
   - **If jira is empty `[]` or metadata.yaml is missing**: HALT immediately.
     - Announce: "metadata.yaml thiếu thông tin Jira. Hãy chạy lại `/feat-init` với Jira ticket hoặc dùng flag `--force`."

   **IF ALL GATES PASS**:
   - Announce: "Detected feat-init output, using feat_overview.md as input map."
   - Proceed to Turn 1.

---

> **CRITICAL: Turn Merging Model**
>
> This workflow MUST be completed in exactly **5 turns** (5 model invocations). Each turn may contain multiple sequential tool call batches. Agent MUST merge reads+writes into sequential batches WITHIN each turn — do NOT split into separate turns.
>
> ```
> Turn 1: [Read overview] → [Phase-filter + Parallel read files] → [Write tech_requirement.md]
> Turn 2: [openspec CLI status+instructions] → [Write proposal.md] → [openspec CLI instructions design+specs]
> Turn 3: [Read proposal.md] → [Write design.md + specs parallel]
> Turn 4: [Read design+specs] → [openspec CLI instructions tasks] → [Write tasks.md]
> Turn 5: [openspec status final]
> ```

---

### Turn 1: Read Overview + Collect Context + Generate Tech Requirement

> **Goal**: Read feat_overview.md, collect ALL context files, and generate tech_requirement.md — all in ONE turn with 3 sequential batches.

**Batch 1 — Read overview:**

Read `openspec/changes/<name>/feat_overview.md`.

Extract from the overview:
- Feature name and description (from header)
- Business references: Jira links, Confluence page IDs (from Section 1)
- File list from Sections 2-7

**Phase Filtering (when Phase column exists):**

Detect if `feat_overview.md` has a **Phase column** in Sections 2, 3, 4:
- **If Phase column exists**: Collect ONLY files where Phase contains `propose`. Skip files where Phase is only `apply`, only `archive`, or only `init`.
- **If Phase column does NOT exist** (legacy 3-column format): Fallback — collect ALL files with Include=1.

For Sections 5-7 (Satellite, Knowledge, Reference): always collect all listed files (no phase filtering).

Announce: "Phase filtering: Found N files for propose phase (M total in overview)." — or "No Phase column detected, reading all N files."

**Batch 2 — Parallel read all propose-phase files:**

**Agent MUST read ALL filtered files using parallel tool calls in this batch.** Do NOT read files sequentially.

In **ONE parallel batch**, call `view_file` for:
- Section 2: Rule files with phase=propose (from `base_knowledge/common_rules/`)
- Section 3: Standard files with phase=propose (from `base_knowledge/standards/`)
- Section 4: Structure files with phase=propose (from `base_knowledge/structures/`)
- Section 5: ALL satellite documents
- Section 6: ALL knowledge documents
- Section 7: ALL reference code files

Additionally, if Section 1 contains Confluence links/IDs → use the Confluence MCP server to retrieve page content (in the same parallel batch).

**If a file does NOT exist**: skip it and note WARNING.

**Batch 3 — Write tech_requirement.md:**

Generate a **condensed technical requirements** file at `openspec/changes/<name>/tech_requirement.md`.

This is a bridge artifact: `feat-apply` reads ONLY this file (~2-3KB) instead of re-reading all raw rules/standards.

**Extract FROM** (code-relevant sources only — use content read in Batch 2):

| Source File | Section in tech_requirement.md |
|-------------|-------------------------------|
| Relevant standards (coding, logging, error_handling, dto, database) | `## Coding & Standards` |
| Security rules + security_standard | `## Security` |
| Transaction status rules | `## Transaction Processing` |
| Integration standards | `## External Integration` |
| system_overview.md | `## System Context` |

**EXCLUDE from tech_requirement.md** (planning-only sources):
- `rule_planing_feature.md` — planning/design/spec rules only
- `requirement_standards.md` — requirement writing rules only
- `requirement_structures.md` — architecture survey templates only
- Any file not read in this session (apply-only / archive-only files)

   **Format rules:**
   - Target size: **~2-3KB** for standards section. Do NOT block if file exceeds 5KB due to Design Checklist content appended in Turn 3
   - Use bullet points with DO/DON'T rules — do NOT copy full text
   - If a source file was not read (skipped by phase filter), write: "Xem chi tiết tại `<filename>` (phase: apply)."

   **Best-effort API Detection from Satellite Docs:**
   Scan the contents of Satellite Documents (Section 5 from feat_overview) for API patterns:
   - **URL patterns**: `/api/v\d+/...`, `POST /...`, `GET /...`
   - **Heading patterns**: `## API`, `### Endpoint`, `## Interface`
   - **Table patterns**: Tables containing columns like "Path", "Method", "URL"
   
   If API references are found, append this section to the END of `tech_requirement.md`:
   ```markdown
   ## Satellite API References
   | # | API Path | Method | Source Doc | Purpose |
   |---|----------|--------|------------|---------|
   ```
   - If a detected API is unconfirmed/unclear, add `[UNCONFIRMED]` in the Purpose column.
   - If NO APIs are found, do NOT add the section and log: "No API references detected in satellite docs (best-effort scan)."

   Show progress: "Turn 1 complete: loaded N files, generated tech_requirement.md (K KB)."

---

### Turn 2: Create Proposal + Prepare Next Instructions

> **Goal**: Get openspec instructions, create proposal.md, and pre-fetch instructions for design+specs — all in ONE turn.

**Batch 1 — CLI calls (parallel):**

```bash
openspec status --change "<name>" --json
openspec instructions proposal --change "<name>" --json
```

Parse status for artifact build order. Parse proposal instructions for template and guidance.

Also extract planning rules from `rule_planing_feature.md` (already in context from Turn 1):
- **Planning Rules**: Apply during overall feature planning
- **Design Rules**: Save for Turn 3
- **Spec Rules**: Save for Turn 3

**Batch 2 — Write proposal.md:**

Create `proposal.md` using:
- Template from openspec instructions
- Context from Turn 1 (all files read)
- Planning Rules from `rule_planing_feature.md`
- `context` and `rules` from openspec instructions as constraints (do NOT copy into file)

**Batch 3 — Pre-fetch instructions for design + specs (parallel):**

```bash
openspec instructions design --change "<name>" --json
openspec instructions specs --change "<name>" --json
```

This pre-fetches instructions so Turn 3 can immediately start writing without an extra CLI batch.

Show progress: "Turn 2 complete: created proposal.md."

---

### Turn 3: Create Design + Specs (Parallel)

> **Goal**: Create design.md and ALL spec files using parallel writes in ONE turn.

**Batch 1 — Read proposal.md (if needed):**

Re-read `proposal.md` if it's not fully in context from Turn 2. Extract:
- Capabilities list (for spec file creation)
- Scope and impact (for design context)

**Batch 2 -- Parallel write design + ALL specs:**

**In ONE parallel batch**, create:
- `design.md` -- apply collected context + **Design Rules** from `rule_planing_feature.md`
- ALL `specs/<capability>/spec.md` files -- one per capability listed in proposal. Apply **Spec Rules** from `rule_planing_feature.md`
- If proposal lists N capabilities -> create N spec files + 1 design file = **N+1 parallel writes**

Apply `context` and `rules` from openspec instructions (fetched in Turn 2 Batch 3) as constraints -- do NOT copy them into files.

**Batch 3 -- Design Checklist (from standards):**

After writing `design.md`, scan ALL standard files already loaded in Turn 1 context. Look for sections whose heading contains the marker `(design-checklist)` -- for example:

```markdown
## 9. Finance Flow Checklist (design-checklist)
```

**Detection logic:**

1. For each standard file read in Turn 1, scan all headings (`##`, `###`) for the literal text `(design-checklist)` in the heading.
2. If a `(design-checklist)` section is found:
   a. Read the section content. It should contain:
      - **Detection keywords**: words/phrases that indicate when this checklist applies
      - **Checklist template**: the actual checklist items
   b. Check if the `proposal.md` content + collected context match the detection keywords.
   c. **If keywords match**: Add this checklist to the matched list. For each checklist item, evaluate:
      - `1` if the design confirms this item is used/applicable to the feature
      - `0` if this item is not used/not applicable to the feature
      - Do NOT fabricate applicability -- only mark `1` when there is clear evidence in the design
   d. **If keywords do NOT match**: Skip this checklist entirely.
3. If NO standard file contains a `(design-checklist)` marker: Do nothing. No checklist is appended.

**Writing checklist to `tech_requirement.md`:**

If ANY checklists matched, append a `## Design Checklist` section to the **end** of `tech_requirement.md` (already created in Turn 1 Batch 3). Each matched checklist gets its own `###` subsection with a table:

```markdown
## Design Checklist

### <Tiêu đề standard> (source: <standard_file.md> §<section>)

| # | Item | Status | Note |
|---|------|--------|------|
| 1 | <checklist item> | 1 | <reason why used> |
| 2 | <checklist item> | 0 | <reason why not used> |

### <Tiêu đề standard 2> (source: <standard_file_2.md> §<section>)

| # | Item | Status | Note |
|---|------|--------|------|
| 1 | <checklist item> | 1 | <reason> |
```

Format rules:
- Status column: `0` = không dùng, `1` = dùng. NO other format (`[x]`, `[ ]`, `true`, `false`).
- Note column: brief reason explaining why item is used or not used.
- One `###` subsection per matched standard file.

> **Important**: The checklist content comes FROM the standard files, not from the agent. The agent only evaluates which items are applicable to the current design. Never invent checklist items.

**Notification: Append to `proposal.md` (Turn 3 update):**

If ANY checklists were matched and written to `tech_requirement.md`, the agent MUST also append the following section to the **end** of `proposal.md` (already created in Turn 2). This is the second write to `proposal.md` — same agent session, no race condition.

```markdown
## Design Checklist Detected

| # | Checklist Name | Source | Items |
|---|---------------|--------|-------|
| 1 | <checklist heading> | <standard_file.md> §<section> | <number of items> |

> Chi tiết checklist đã được ghi vào `tech_requirement.md`. Vui lòng kiểm tra và chỉnh sửa trước khi chạy `feat-apply`.
```

**Notification: Chat message after Turn 3:**

If ANY checklists were matched, the agent MUST display a prominent message in the chat output:

```
╔══════════════════════════════════════════════════════════════╗
║  YÊU CẦU KIỂM TRA VÀ CHỈNH SỬA CHECKLIST                 ║
║                                                              ║
║  Đã phát hiện N checklist từ standard files.                ║
║  Checklist đã được ghi vào tech_requirement.md              ║
║  Vui lòng kiểm tra và chỉnh sửa trước khi chạy feat-apply. ║
╚══════════════════════════════════════════════════════════════╝
```

Show progress: "Turn 3 complete: created design.md + N specs (parallel). Checklist: [appended M checklists to tech_requirement.md / no matching checklists found]."

---

### Turn 4: Create Tasks

> **Goal**: Read design+specs, get task instructions, and create tasks.md — all in ONE turn.

**Batch 1 — Read design + specs (parallel):**

Read ALL files just created in Turn 3:
- `design.md`
- ALL `specs/**/*.md`

**Batch 2 — Get task instructions:**

```bash
openspec instructions tasks --change "<name>" --json
```

**Batch 3 — Write tasks.md:**

Create `tasks.md` based on:
- Actual content of design and specs (from Batch 1)
- Template from openspec instructions (from Batch 2)
- **Planning Rules** from `rule_planing_feature.md`

Show progress: "Turn 4 complete: created tasks.md."

---

### Turn 5: Final Status

> **Goal**: Show final status and summary.

**Batch 1 — Final status check:**

```bash
openspec status --change "<name>"
```

**Output Summary:**

After completing all artifacts, summarize:
- Change name and location
- Phase filtering stats: "Read N/M files (propose-phase only)" or "Read all N files (no phase filter)"
- Input sources used (with type and reference)
- Planning rules applied (or note if rule file was missing)
- List of artifacts created with brief descriptions
- What's ready: "All artifacts created! Ready for implementation."
- Prompt: "Run `/opsx-apply` or ask me to implement to start working on the tasks."

---

**Artifact Creation Guidelines**

- Follow the `instruction` field from `openspec instructions` for each artifact type
- The schema defines what each artifact should contain — follow it
- Read dependency artifacts for context before creating new ones
- Use `template` as the structure for your output file — fill in its sections
- **IMPORTANT**: `context` and `rules` are constraints for YOU, not content for the file
  - Do NOT copy `<context>`, `<rules>`, `<project_context>` blocks into the artifact
  - These guide what you write, but should never appear in the output
- **Design artifacts**: MUST comply with openspec design template AND Design Rules from `rule_planing_feature.md`
- **Design Checklist**: After writing `design.md`, scan loaded standard files for `(design-checklist)` markers. If detection keywords match proposal context, append checklist to `tech_requirement.md` (NOT `design.md`) with `0/1` status (0 = không dùng, 1 = dùng) in table format. Multiple matched checklists = multiple `###` subsections under one `## Design Checklist`. After writing checklist, append `## Design Checklist Detected` section to `proposal.md` AND display chat notification box. Never fabricate checklist items -- content comes from standard files only.
- **Spec artifacts**: MUST comply with openspec spec template AND Spec Rules from `rule_planing_feature.md`
- **All artifacts**: MUST reference input sources for traceability (URD section, doc path, code module)
- **Parallel execution**: `design` and all `specs/<cap>/spec.md` MUST be created using parallel tool calls in Turn 3. Do NOT create them sequentially.

**Guardrails**

- **MUST** run Gate Check BEFORE any Turn. If `feat_overview.md` is missing, **STOP** and instruct the user to run `/feat-init`
- **MUST** read `metadata.yaml` and validate `jira` field in Gate Check. HALT if jira is empty `[]` and no `<force flow>` marker. Accept `<force flow>` with WARNING
- **MUST complete the workflow in exactly 5 turns** — do NOT split a turn's batches into separate turns
- **MUST merge reads+writes into sequential batches WITHIN each turn** — each turn can have multiple tool call batches but counts as ONE model invocation
- **MUST filter files by phase=propose** when Phase column exists in `feat_overview.md` Sections 2/3/4. Skip files with phase-only `apply`, `archive`, or `init`
- **MUST fallback to reading ALL Include=1 files** when `feat_overview.md` uses legacy 3-column format (no Phase column)
- **MUST use parallel tool calls** to read ALL context files in Turn 1 Batch 2 — do NOT read files sequentially
- **MUST** generate `tech_requirement.md` in Turn 1 Batch 3 AFTER reading all context files and BEFORE artifact generation
- **MUST** exclude planning-only files (`rule_planing_feature.md`, `requirement_standards.md`, `requirement_structures.md`) from `tech_requirement.md`
- **MUST** keep `tech_requirement.md` as small as possible (target ~2-3KB for standards section) — use condensed DO/DON'T bullet points. Do NOT block if file exceeds 5KB due to Design Checklist content
- MUST read and apply rules from `base_knowledge/common_rules/rule_planing_feature.md` — if the file is missing, warn user but continue with openspec defaults
- MUST cross-reference collected context when writing design and spec artifacts
- MUST read ACTUAL FILE CONTENTS of files listed in `feat_overview.md` — the overview is an input MAP, NOT a replacement for reading files
- If a file listed in overview does not exist, SKIP it with a WARNING — do NOT fail
- If context is critically unclear, ask the user — but prefer making reasonable decisions to keep momentum
- Verify each artifact file exists after writing before proceeding to next turn
