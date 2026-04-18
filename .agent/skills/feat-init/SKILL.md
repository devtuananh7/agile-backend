---
name: feat-init
description: Initialize a feature change with input inventory. Use when the user wants to prepare and gather all inputs (business docs, rules, standards, structures, satellite docs, knowledge, reference code) before running feat-propose.
license: VNPAY-DVNH
compatibility: Requires openspec CLI and optionally Confluence MCP server.
metadata:
  author: anhdt8
  version: "1.0"
---

Initialize a feature change — gather all input sources, inventory rules/standards/structures, and produce `feat_overview.md` for developer review before `feat-propose`.

I'll create:
- Change folder via openspec CLI
- `metadata.yaml` (change tracking)
- `feat_overview.md` (consolidated input inventory)

When reviewed and ready, run `/feat-propose` to generate artifacts.

---

**Input**: The user's request should include a feature name (kebab-case) OR a description of what they want to build.

The user may also provide **input sources** (all are optional except Jira — see below):

| # | Input Type | How to Identify | Default if not provided |
|---|-----------|----------------|------------------------|
| 1 | **Jira tasks** | User provides Jira ticket IDs or links | **MANDATORY** -- HALT if missing (unless `--force`) |
| 2 | **Confluence docs** | User provides page IDs or links | Empty list |
| 3 | **Rules** | User lists specific rule files from `base_knowledge/common_rules/` | Auto-scan entire directory |
| 4 | **Standards** | User lists specific standard files from `base_knowledge/standards/` | Auto-scan entire directory |
| 5 | **Structures** | User lists specific structure files from `base_knowledge/structures/` | Auto-scan entire directory |
| 6 | **Satellite docs** | User provides paths to documents under project root | Empty list |
| 7 | **Related knowledge** | User provides paths to previous feature archives or knowledge docs | Empty list |
| 8 | **Reference code** | User provides class/package paths as implementation examples | Empty list |

**Flags**:

| Flag | Effect |
|------|--------|
| `--force` | Skip mandatory Jira check. Writes `<force flow>` marker into `metadata.yaml` jira field. |

**Steps**

1. **Gather feature information**

   If the user has not provided clear input, ask:
   > "What feature do you want to build? Please provide:
   > - Feature name or description
   > - **Jira ticket IDs or links (MANDATORY -- or use `--force` to skip)**
   > - Confluence page links (optional)
   > - Any specific rules/standards/structures to apply (optional -- leave blank to auto-scan all)
   > - Satellite document paths (optional)
   > - Related knowledge from previous features (optional)
   > - Reference code classes/packages (optional)"

   From the description, derive a kebab-case name (e.g., "chuyển tiền liên ngân hàng" -> `interbank-transfer`).

   **IMPORTANT**: Do NOT proceed without understanding what the user wants to build.

   **1-pre. Jira Validation (MANDATORY)**

   After gathering input, check if user provided Jira ticket IDs or links:

   a. **If Jira provided**: Extract ticket IDs/links. Proceed to Step 2.
   b. **If `--force` flag detected**: No Jira required. Mark for `<force flow>` in metadata. Proceed to Step 2.
      - Announce: "[WARNING] Jira check skipped (--force). Feature will be marked as <force flow>."
   c. **If NO Jira AND NO `--force`**: HALT immediately.
      - Announce: "Jira ticket ID hoặc link là bắt buộc cho luồng feat-init. Vui lòng cung cấp Jira hoặc dùng flag `--force` để bỏ qua."
      - Do NOT proceed.

2. **Create the change directory**
   ```bash
   openspec new change "<name>"
   ```
   This creates a scaffolded change at `openspec/changes/<name>/` with `.openspec.yaml`.

   If a change with that name already exists, ask if user wants to continue it or create a new one.

3. **Generate `metadata.yaml`**

   Create `openspec/changes/<name>/metadata.yaml` with change tracking information:

   ```yaml
   id: "<PROJECT_ID>"  # Format: UPPERCASE(rootProject.name) + YYMMDD + 5 random alphanumeric (mixed case)
   name: "<change-name>"
   type: "new-feature"
   created: "YYYY-MM-DDTHH:mm:ss+07:00"  # Exact timestamp when the command was invoked
   summary: "<Tóm tắt yêu cầu bằng tiếng Việt>"
   service:
     - "<service-1>"  # Services affected (from user input or codebase analysis)
     - "<service-2>"
   path:
     - "<endpoint-1>"  # API endpoints affected/new (if identifiable)
     - "<endpoint-2>"
   confluence:
     - id: "<page-id>"
       name: "<page-title>"  # From user input, empty list if not provided
   jira:
     - id: "<ticket-id>"
       name: "<ticket-title>"  # From user input, empty list if not provided
   ```

   **Rules:**
   - `id` MUST be generated as: `UPPERCASE(rootProject.name)` + `YYMMDD` + 5 random alphanumeric characters (mixed uppercase/lowercase letters and digits). Read `rootProject.name` from `settings.gradle` at project root. Example: `VCBDIGIBIZ260323A1b2C`
   - `created` MUST use the exact current timestamp (with timezone) at the moment of invocation
   - `summary` MUST be written in Vietnamese
   - `confluence` from user-provided inputs -- use empty list `[]` if not provided
   - `jira`: populate from user-provided Jira IDs/links. **If `--force` was used**, set jira to: `[{id: "<force flow>", name: "Jira check skipped by --force flag"}]`
   - Generate this file BEFORE creating `feat_overview.md`

   Show progress: "Created metadata.yaml"

4. **Collect input inventory**

   For each of the 7 input sections, collect data using **explicit input**, **summary files**, or **auto-scan**.

   ### 4-pre. Detect Summary Files (MANDATORY first step)

   Before collecting Rules/Standards/Structures, check if summary files exist:
   - `base_knowledge/standards/summary_standards.md` (contains Standards Index + Rules Index)
   - `base_knowledge/structures/summary_structures.md` (contains Structures Index)

   **If BOTH summary files exist** → use **Summary Mode** for sections 4b, 4c, 4d:
   - Read both summary files (2 files, ~3KB total)
   - Extract file names, Summary text, and Phase from the index tables
   - Output tables with 5 columns: `(File, Include, Phase, Summary, Notes)`
   - Announce: "Detected summary files — using Summary Mode with phase tagging."

   **If summary files do NOT exist** → use **Legacy Mode** (auto-scan):
   - Fallback to list_dir behavior (3-column tables, no Phase/Summary)
   - Announce: "Summary files not found — using Legacy Mode (list_dir). Run `/summary --knowledge` to enable phase tagging."

   ### 4a. Business References (Jira + Confluence)
   - Use links/IDs provided by user
   - If Confluence pages were provided, use Confluence MCP server to retrieve page titles
   - If no links provided → section will have empty table with header only

   ### 4b. Rules
   - **If user listed specific rules** → use only those files (with Phase from summary if available)
   - **Summary Mode** → read Rules Index from `standards/summary_standards.md`:
     - Extract all entries from the `## Rules Index` table
     - For each entry: use File, Summary, Phase from the table
     - Exclude files with `Phase: init` (e.g., requirement files — not needed for feat workflow)
   - **Legacy Mode** (no summary) → auto-scan `base_knowledge/common_rules/` directory:
     - List all `.md` files (excluding README.md)
   - Output: table with file name, Include (`1` = include, `0` = exclude), and Notes. 
     - **Intelligent Filtering**: If the AI has read the URD or has detailed feature context, automatically analyze and assign `0` to rules that are NOT relevant to the business context, providing a brief explanation in the `Notes`. Keep `1` ONLY for rules that truly match. Default to `1` if there is no context.

   ### 4c. Standards
   - **If user listed specific standards** → use only those files (with Phase from summary if available)
   - **Summary Mode** → read Standards Index from `standards/summary_standards.md`:
     - Extract all entries from the `## Standards Index` table
     - For each entry: use File, Summary, Phase from the table
     - Exclude files with `Phase: init` (e.g., requirement_standards.md)
   - **Legacy Mode** (no summary) → auto-scan `base_knowledge/standards/` directory:
     - List all `.md` files (excluding README.md, requirement_standards.md)
     - Import **file names only** — do NOT read file contents (save tokens)
   - Output: see format in Step 5 template. All files included by default.

   ### 4d. Architecture Structures
   - **If user listed specific structures** → use only those files (with Phase from summary if available)
   - **Summary Mode** → read Structures Index from `structures/summary_structures.md`:
     - Extract all entries from the `## Structures Index` table
     - For each entry: use File, Summary, Phase from the table
     - Exclude files with `Phase: init` (e.g., requirement_structures.md)
   - **Legacy Mode** (no summary) → auto-scan `base_knowledge/structures/` directory:
     - List all `.md` files (excluding README.md, requirement_structures.md)
     - Import **file names only** — do NOT read file contents (save tokens)
   - Output: see format in Step 5 template. All files included by default.

   ### 4e. Satellite Documents
   - **User Paths**: Use paths provided by user (documents under project root: URD satellites, API specs, etc.).
   - **Auto-scan `converted/`**: Automatically check `base_knowledge/converted/` for `.md` files.
     - If files exist, list them (do not read contents).
     - Combine these with any user-provided paths in the inventory. Set Type as "Converted Document" for these files.
     - **Move Files**: Create the directory `openspec/changes/<name>/satellite/`. Move all files from `base_knowledge/converted/` into this new directory. Notify the user: "Đã di chuyển N tài liệu vệ tinh vào satellite/".
   - If no paths provided and `converted/` is empty → section will have empty table with header only.

   ### 4f. Related Knowledge
   - Use paths provided by user (previous feature archives, knowledge docs)
   - Also auto-scan `openspec/changes/archive/` for relevant archived features
   - If no paths and no relevant archives → section will have empty table with header only

   ### 4g. Reference Codebase
   - Use class/package paths provided by user
   - If paths provided, verify they exist and note their purpose (controller, service, etc.)
   - If no paths provided → section will have empty table with header only

5. **Generate `feat_overview.md`**

   Write `feat_overview.md` to: `openspec/changes/<name>/feat_overview.md`

   Template:

   ```markdown
   # Feature Overview: <Feature Name>

   > Tài liệu khởi tạo tính năng — tổng hợp input cho bước `feat-propose`.
   > Được tạo bởi `feat-init` vào <timestamp>.

   ## 1. Business References

   | # | Type | Link/ID | Description |
   |---|------|---------|-------------|
   | 1 | Jira | [link or ticket-id] | [ticket title or description] |
   | 2 | Confluence | [link or page-id] | [page title] |

   ## 2. Rules

   > Source: `base_knowledge/common_rules/`
   > Mode: [Explicit / Summary / Auto-scan]
   > Include: `1` = will be read, `0` = excluded. Phase: which workflow phases need this file.

   <!-- Summary Mode (5 columns — when summary files exist): -->
   | # | File | Include | Phase | Summary | Notes |
   |---|------|---------|-------|---------|-------|
   | 1 | [file_name.md] | `1` | [propose/apply/archive] | [summary from summary_standards.md] | — |

   <!-- Legacy Mode (3 columns — when summary files do NOT exist): -->
   <!-- | # | File | Include | Notes | -->
   <!-- |---|------|---------|-------| -->
   <!-- | 1 | [file_name.md] | `1` | — | -->

   ## 3. Standards

   > Source: `base_knowledge/standards/`
   > Mode: [Explicit / Summary / Auto-scan]
   > Include: `1` = will be read, `0` = excluded. Phase: which workflow phases need this file.

   <!-- Summary Mode (5 columns): -->
   | # | File | Include | Phase | Summary | Notes |
   |---|------|---------|-------|---------|-------|
   | 1 | [file_name.md] | `1` | [propose/apply/archive] | [summary from summary_standards.md] | — |

   <!-- Legacy Mode (3 columns): -->
   <!-- | # | File | Include | Notes | -->
   <!-- |---|------|---------|-------| -->
   <!-- | 1 | [file_name.md] | `1` | — | -->

   > Note: Only files that actually exist will be listed.

   ## 4. Architecture Structures

   > Source: `base_knowledge/structures/`
   > Mode: [Explicit / Summary / Auto-scan]
   > Include: `1` = will be read, `0` = excluded. Phase: which workflow phases need this file.

   <!-- Summary Mode (5 columns): -->
   | # | File | Include | Phase | Summary | Notes |
   |---|------|---------|-------|---------|-------|
   | 1 | [file_name.md] | `1` | [propose/apply/archive] | [summary from summary_structures.md] | — |

   <!-- Legacy Mode (3 columns): -->
   <!-- | # | File | Include | Notes | -->
   <!-- |---|------|---------|-------| -->
   <!-- | 1 | [file_name.md] | `1` | — | -->

   > Note: Only files that actually exist will be listed.

   ## 5. Satellite Documents

   > Documents under project root (URD satellites, API specs, partner docs, etc.)

   | # | Path | Type | Description |
   |---|------|------|-------------|
   | 1 | `docs/urd/<feature>/...` | URD Satellite | [description] |

   ## 6. Related Knowledge

   > Knowledge from previous features, archived changes, or shared documentation.

   | # | Path/Source | Type | Relevance |
   |---|-----------|------|-----------|
   | 1 | `openspec/changes/archive/<date>-<name>/` | Archived Change | [why relevant] |

   ## 7. Reference Codebase

   > Existing code implementations to use as examples/patterns.

   | # | Class/Package Path | Layer | Purpose |
   |---|-------------------|-------|---------|
   | 1 | `com.vnpay.omni.<service>.<layer>.<feature>/` | Controller/Service | [what pattern to follow] |

   ## Summary

   - **Total input sources**: [count]
   - **Rules**: [N] files
   - **Standards**: [N] files
   - **Structures**: [N] files
   - **Satellite docs**: [N] files
   - **Reference code**: [N] classes/packages
   - **Status**: Ready for `feat-propose` / Needs more input
   ```

   Show progress: "Created feat_overview.md"

6. **Show summary and wait for review**

   Display:
   ```
   ## feat-init Complete

   **Feature:** <feature-name>
   **Location:** openspec/changes/<name>/
   **Files created:**
   - metadata.yaml [OK]
   - feat_overview.md [OK]

   ### Input Inventory Summary
   - Business References: [N] links
   - Rules: [N] files ([Explicit/Auto-scan])
   - Standards: [N] files ([Explicit/Auto-scan])
   - Structures: [N] files ([Explicit/Auto-scan])
   - Satellite Docs: [N] files
   - Related Knowledge: [N] sources
   - Reference Code: [N] classes/packages

   Please review `feat_overview.md` and adjust if needed.
   When ready, run `/feat-propose` to generate all artifacts.
   ```

**Output**

After completing all steps:
- Change name and location
- List of files created (metadata.yaml, feat_overview.md)
- Input inventory summary with counts per section
- Prompt: "Review `feat_overview.md` and run `/feat-propose` when ready."

**Guardrails**

- **MUST** validate Jira input in Step 1-pre BEFORE creating change directory. If no Jira and no `--force` flag, HALT immediately
- **MUST** write `<force flow>` marker into `metadata.yaml` jira field when `--force` is used
- **MUST** create `metadata.yaml` BEFORE `feat_overview.md`
- **MUST** auto-scan `base_knowledge/` directories when user does not provide explicit input for rules/standards/structures -- list **file names only**, do NOT read file contents. 
- **MUST** check for summary files (`standards/summary_standards.md`, `structures/summary_structures.md`) in Step 4-pre BEFORE scanning directories
- **Summary Mode**: When summary files exist, read them (2 files, ~3KB total) to get file names, summaries, and phase tags. Output 5-column tables `(File, Include, Phase, Summary, Notes)`. Do NOT list_dir or read individual file contents.
- **Legacy Mode**: When summary files do NOT exist, fallback to auto-scan `base_knowledge/` directories -- list **file names only**, do NOT read file contents (save tokens). Output 3-column tables `(File, Include, Notes)`.
- In both modes: all files listed with Include = `1` by default -- developer changes Include to `0` for unneeded files. Actual content reading is deferred to `feat-propose`/`feat-apply`
- **MUST** exclude files with `Phase: init` from the output tables (these are config files for initial-project, not for feat workflow)
- **MUST** verify file existence before adding satellite docs or reference code to inventory
- **MUST NOT** generate openspec artifacts (proposal, design, specs, tasks) -- only `metadata.yaml` and `feat_overview.md`
- **MUST NOT** modify any existing files -- only create new files in the change directory
- If Confluence pages provided, use MCP server to retrieve page titles for traceability
- If change already exists, ask user if they want to continue or create new
- Sections 2-3-4 MUST automatically assign Include = `0` and explain in the Notes column for files clearly irrelevant to the business context if URD context is available. Otherwise, default to Include = `1` and Notes = (blank).
- Sections 2-3-4 MUST list files found by scan/summary with Include = `1`. Do NOT hardcode or fabricate content -- Summary column is from summary files, Notes column is for the developer to fill in optionally
