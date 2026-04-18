---
name: feat-archive
description: Archive a completed feature change with code-vs-documentation sync check and extended tracking artifact preservation.
license: VNPAY-DVNH
compatibility: Requires openspec CLI.
metadata:
  author: anhdt8
  version: "1.0"
---

Archive a completed feature change. Extends `opsx-archive` with **code-vs-documentation sync check** and **feat-apply tracking artifact preservation**.

**Input**: Optionally specify a change name. If omitted, infer from conversation context or prompt.

**Flags:**
- `--full`: Include SRS generation during archive. When present, Step 5 generates SRS automatically (no prompt). When absent, Step 5 is skipped entirely.

**Steps**

## 1. Identify the Change to Archive

If no change name provided, scan `openspec/changes/` for active (non-archived) changes.

- Present the list and let the user select.
- If only one active change exists, confirm with the user before proceeding.

**IMPORTANT**: Do NOT guess or auto-select a change. Always let the user confirm.

## 2. Check Artifact Completion Status

Run `openspec status --change "<name>" --json` to check artifact completion.

Parse the JSON to understand:
- `schemaName`: The workflow being used
- `artifacts`: List of artifacts with their status (`done` or other)

**If any artifacts are not `done`:**
- Display warning listing incomplete artifacts
- Prompt user for confirmation to continue
- Proceed if user confirms

## 3. Check Task Completion Status

Read the tasks file (`tasks.md`) to check for incomplete tasks.

Count tasks marked with `- [ ]` (incomplete) vs `- [x]` (complete).

**If incomplete tasks found:**
- Display warning showing count: `"Warning: X of Y tasks are still incomplete."`
- Ask user for confirmation to continue.
- Proceed only if user confirms.

**If no tasks file exists:** Proceed without task-related warning.

## 4. Code-vs-Documentation Sync Check 

This is the core extension. Before archiving, verify that the documentation accurately reflects what was actually implemented.

### 4a. Identify Changed Files

Use `git diff` or manual file scanning to find modified/new files related to this change:
- Focus on Controller, Service, Model/Entity, and Config layers.
- Extract actual implementation details: endpoints, methods, entities, DTOs, configs, error codes.

### 4b. Read All Change Documents

Read the existing change artifacts:
- `proposal.md` — What was proposed
- `design.md` — How it was designed
- `specs/*/spec.md` — Detailed requirements
- `tasks.md` — Implementation plan

### 4c. Compare Code vs. Documentation

For each significant implementation detail, check if the documentation describes it accurately:

| Check Item | What to Compare |
|-----------|----------------|
| Endpoints | Documented paths/methods vs actual `@RequestMapping` annotations |
| Service methods | Documented method signatures vs actual method implementations |
| Entities/DTOs | Documented fields vs actual field definitions |
| Business logic | Documented flows vs actual control flow |
| Error handling | Documented error codes vs actual `Constants.ResCode` usage |
| Config keys | Documented config usage vs actual `getConfig()` calls |

### 4d. Handle Results

**If ALL MATCH (no deviations):**
```
[OK] Documentation is in sync with source code. No corrections needed.
```
Proceed to Step 5.

> **Note**: During archive phase, auto-correcting L1 artifacts (design.md, specs) is intentional. This is **reverse-sync** (code -> docs) to ensure archived documentation reflects reality. This is NOT a violation of L1 authority during implementation — L1 immutability only applies during `feat-apply`.

**If MISMATCHES FOUND:**

a. **Auto-correct each mismatched document** — Edit the documentation to reflect the actual implementation. Only add/modify sections, do NOT remove existing correct content.

b. **Add warning markers** at each corrected location:
```markdown
> [!WARNING]
> **Auto-corrected during archive (YYYY-MM-DD)**
> Original documentation stated: [original description]
> Actual implementation: [what was really implemented]
```

c. **Generate sync report:**
```
[!] Documentation Sync Report

Corrections applied:
| Document | Section | Original | Corrected To |
|----------|---------|----------|-------------|
| design.md | Endpoint /v1/init | POST with 3 params | POST with 5 params |
| specs/auth/spec.md | Requirement: Token Validation | Missing edge case | Added timeout scenario |

Total: N corrections across M documents.
All documents have been updated to match the source code.
```

d. **Prompt user:** "Documentation has been auto-corrected to match implementation. Review the changes and confirm to proceed with archive."

## 5. Generate SRS Document

**AFTER** Code-vs-Documentation Sync Check (Step 4) and **BEFORE** collecting tracking artifacts.

**Check for `--full` flag in user input:**

- **If `--full` is NOT present**: Skip SRS generation entirely. Record "SRS: Skipped (use `--full` to include, or `/srs-generator` standalone)" and proceed to Step 6.
- **If `--full` IS present**: Generate SRS automatically — **do NOT prompt** the user.

Generate `srs.md` at `openspec/changes/<name>/srs.md` using the following inputs:
- **Code analysis results** from Step 4a (actual implementation: endpoints, services, entities, DTOs, configs, error codes)
- **Change artifacts**: proposal.md, design.md, specs/\*/spec.md, tasks.md
- **Original codebase** for context on existing functionality
- **SRS template** from `base_knowledge/common_rules/rule_generate_srs.md` (if exists, otherwise use inline template below)

The SRS MUST be written for **tester and client audience** — use business language, avoid deep technical terminology (class names, package paths). Each System Feature MUST include test scenarios.

**SRS Writing Principles:**
- **Be Specific**: Use clear language to avoid ambiguity
- **Make it Testable**: Requirements must be verifiable by testers
- **Define "What," Not "How"**: Focus on behavior, not implementation design
- **Traceability**: Ensure every requirement connects back to a business need

Fallback Template (used only if `rule_generate_srs.md` is not found):
```markdown
# SRS: <Feature Name>

## 1. Introduction

### 1.1 Purpose
[Define the purpose of this SRS document and its audience]

### 1.2 Document Conventions
[Define standards used in this document (e.g., formatting, terminology)]

### 1.3 Intended Audience
[Identify who will read this document: testers, clients, business analysts, etc.]

### 1.4 Project Scope
[Describe the product/feature, including benefits, objectives, and goals]

### 1.5 Definitions, Acronyms, and Abbreviations
| Term | Definition |
|------|------------|
| [term] | [definition] |

## 2. Overall Description

### 2.1 Product Perspective
[How this feature fits into the larger system or acts as a standalone component]

### 2.2 Product Functions
[Summary of the main functions the feature performs — derived from actual implementation]

### 2.3 User Classes and Characteristics
[Identify user types (end users, admins, external systems) and their technical expertise]

### 2.4 Operating Environment
[Hardware platforms, operating systems, deployment environment]

### 2.5 Design and Implementation Constraints
[Technical restrictions: required languages, security policies, infrastructure limits]

### 2.6 Assumptions and Dependencies
[Factors that, if changed, would affect the requirements]

## 3. System Features (Functional Requirements)

### 3.1 <Feature Name>

#### 3.1.1 Description and Priority
[Describe the feature and its importance — HIGH/MEDIUM/LOW]

#### 3.1.2 Stimulus/Response Sequences
[What triggers the feature and how it responds — based on actual API endpoints and flows]

#### 3.1.3 Functional Requirements
[Detailed, testable requirements: "The system shall..."]
- FR-001: [requirement derived from actual implementation]
- FR-002: [requirement derived from actual implementation]

#### 3.1.4 Test Scenarios
| # | Scenario | Preconditions | Steps | Expected Result |
|---|----------|--------------|-------|----------------|
| TC-001 | [scenario name] | [setup needed] | [steps] | [expected outcome] |

[Repeat 3.x for each feature/capability implemented]

## 4. External Interface Requirements

### 4.1 User Interfaces
[Screen layout, GUI standards, or API consumer interfaces]

### 4.2 Hardware Interfaces
[Supported devices and protocols, if applicable]

### 4.3 Software Interfaces
[Databases, OS, external APIs, internal services — derived from actual integrations]

### 4.4 Communications Interfaces
[Network protocols, message formats, if applicable]

## 5. Non-functional Requirements (NFRs)

### 5.1 Performance Requirements
[Speed, response times, throughput — derived from actual implementation constraints]

### 5.2 Safety/Security Requirements
[Data protection, compliance standards, authentication/authorization requirements]

### 5.3 Quality Attributes
[Availability, maintainability, usability requirements]

## 6. Other Requirements
[Appendices, database requirements, legal/regulatory constraints, known limitations]
```

**SRS Generation Rules:**
- MUST derive ALL functional requirements from **actual code** (Step 4a results), NOT from assumptions
- MUST include test scenarios for each System Feature (Section 3.x.4)
- MUST cross-reference with proposal.md and specs for traceability
- MUST use business language accessible to testers and clients
- This is a **custom artifact** (not managed by openspec CLI) — write it directly to the change directory

Show progress: "Created srs.md (--full mode)"

## 6. Collect feat-apply Tracking Artifacts 

Check for tracking artifacts generated by `feat-apply` and `feat-propose` in `openspec/changes/<name>/`:

| File | Required | Description |
|------|----------|-------------|
| `metadata.yaml` | Expected | Change metadata (name, services, endpoints, timestamps, references) |
| `srs.md` | Expected | SRS document (generated in Step 5) |
| `tech_requirement.md` | Optional | Condensed technical rules for code generation (generated by feat-propose) |
| `todo-uncover.md` | Optional | TODOs, FIXMEs, uncovered edge cases |
| `new-apis.md` | Optional | New API endpoint documentation |
| `delta-spec.md` | Optional | Behavioral delta analysis |
| `standards-deviation.md` | Optional | L1-vs-L2 deviation report (generated by feat-apply when design conflicts with standards) |
| `satellite/` | Optional | Converted input documents (from doc-prepare) |

- List which tracking artifacts exist.
- These files will automatically travel with the change directory during archive.
- `metadata.yaml` should always be present (generated during `feat-init`).
- If none exist, note in the summary: "No tracking artifacts found."

## 7. Assess Delta Spec Sync State

Check for delta specs at `openspec/changes/<name>/specs/`. If none exist, proceed without sync prompt.

**If delta specs exist:**
- Compare each delta spec with its corresponding main spec at `openspec/specs/<capability>/spec.md`
- Determine what changes would be applied (adds, modifications, removals, renames)
- Show a combined summary before prompting

**Prompt options:**
- If changes needed: "Sync now (recommended)", "Archive without syncing"
- If already synced: "Archive now", "Cancel"

If user chooses sync, perform agent-driven intelligent merge of delta specs into main specs.

## 8. Perform the Archive

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

## 9. Display Archive Summary

Show a comprehensive archive completion report:

**Output On Success:**
```
## Archive Complete

**Change:** <change-name>
**Archived to:** openspec/changes/archive/YYYY-MM-DD-<name>/
**Specs:** Synced to main specs / Sync skipped / No delta specs

### Documentation Sync Check
[OK] Documentation is in sync with source code (or: N corrections applied)

### SRS Document
Generated (--full mode) (or: Skipped — run `/srs-generator` to generate standalone)

### Task Status
All X tasks complete. (or: Archived with Y incomplete tasks)

### feat-apply Tracking Artifacts
- srs.md [OK] (or: skipped)
- tech_requirement.md [OK] (or: not found)
- todo-uncover.md [OK] (or: not found)
- new-apis.md [OK] (or: not found)
- delta-spec.md [OK] (or: not found)
- standards-deviation.md [OK] (or: not found -- no deviations)
- satellite/ [OK - N files] (or: not found)

### Warnings (if any)
- [list of warnings if applicable]
```

**Output With Corrections:**
```
## Archive Complete (with corrections)

**Change:** <change-name>
**Archived to:** openspec/changes/archive/YYYY-MM-DD-<name>/

**Documentation Corrections:**
- N documents were auto-corrected to match source code
- All corrections marked with [!WARNING] blocks
- Review recommended: design.md, specs/auth/spec.md
```

---

**Guardrails**
- **MUST** perform code-vs-documentation sync check (Step 4) before archiving — this is the core value of this workflow.
- **MUST** generate SRS based on **actual code**, NOT based on assumptions or predictions (when `--full` is used).
- **MUST** write SRS for **tester/client audience**, using business language and including test scenarios (when `--full` is used).
- **MUST** only edit documentation to match code — NEVER suggest editing code to match documentation during archive.
- **MUST** add `> [!WARNING]` markers on every auto-corrected section with date and before/after details.
- **MUST** prompt user after corrections are applied, before proceeding to archive.
- **MUST** skip SRS generation (Step 5) when `--full` flag is NOT present — do NOT prompt.
- **MUST** auto-generate SRS when `--full` flag IS present — do NOT prompt, generate directly.
- Do NOT block archive on warnings — inform, confirm, and proceed.
- Do NOT auto-select a change — always let the user confirm.
- Do NOT modify existing correct content in documents — only correct inaccurate sections.
- Preserve all existing files when moving to archive (the entire directory moves, including tracking artifacts).
- Verify the archive directory exists after the move before reporting success.
