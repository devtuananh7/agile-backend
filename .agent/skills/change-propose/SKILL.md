---
name: change-propose
description: Propose a Change Request (CR) based on reasoning existing features and documentation mapping. Use when the user wants to analyze the current system deeply before proposing changes.
license: VNPAY-DVNH
compatibility: Requires project codebase with existing controllers, services, and documentation.
metadata:
  author: anhdt8
  version: "1.0"
---

Propose a Change Request (CR) by deeply analyzing the current system.

I'll create a CR with artifacts:
- `cr-proposal.md` (Why & Impact — why this CR is needed, impact on the system)
- `cr-spec.md` (Scope — what must change: API contracts, business rules, edge cases, security)
- `cr-design.md` (How — how to modify code, based on Code Mapping)
- `cr-tasks.md` (Specific implementation checklist)
- `current-code-logic.md` (Current code logic — detailed processing flow, business logic, integrations)
- `compare-logic.md` (Logic comparison — current code vs documented/new logic)

---

**Input**: The user's request should include a CR name (kebab-case) OR a description of the feature to change/add (e.g., `update-payment-flow`).

**Steps**

## 1. Gather CR Information

- If the user has not provided clear input, ask:
  > "What feature do you want to change or add? Describe briefly."
- Convert the description into a kebab-case CR name (e.g., "update payment flow" → `update-payment-flow`).
- Determine the CR creation date in `YYYY-MM-DD` format.

**IMPORTANT**: Do NOT proceed without understanding what the user wants to change.

## 2. Reasoning on Existing Features (Codebase Analysis)

Analyze the current codebase related to the mentioned feature:

a. **Scan Controller layer**:
   - Find `@RestController` classes related to the feature.
   - Identify path mappings (`@PostMapping`, `@GetMapping`, etc.).
   - List request/response models.

b. **Scan Service layer**:
   - Identify service classes handling business logic.
   - Trace dependency injection (`@Autowired`).
   - Trace call flow: Controller → Service → Repository/External.

c. **Scan Model/Entity layer**:
   - Identify DTOs, entities, request/response objects.
   - Check database mappings (`@Entity`, `@Table`, `@Column`).

d. **Scan Infrastructure**:
   - Cache services, external API calls, messaging.
   - Config values (`configService.getConfig(...)`).

e. **Write Current State Summary**:
   Summarize the current state including:
   - List of related components (file path + role).
   - Main processing flow (diagram form if possible).
   - Config keys in use.
   - External dependencies.

   **IMPORTANT**: The detailed analysis from this step MUST be written into `current-code-logic.md` (see Step 5e). The summary in `cr-proposal.md` should reference this file — do not duplicate the full detail.

## 3. Reasoning & Mapping Documentation (Doc to Code Mapping)

a. **Find related documentation**:
   - Scan `openspec/changes/` (both active and archive) for related specs/designs.
   - Read related Knowledge Items (KIs) from the knowledge base.
   - Read Confluence documentation via MCP if available.

b. **Map Code ↔ Doc**:
   Create a cross-reference table:

   | Documentation (Section/Ref) | Current Code (File/Method) | Status |
   |---|---|---|
   | Section X.X - Description | `ServiceClass.method()` | [V] Matched / [!] Divergent / [X] Missing |

c. **Identify out-of-date points**:
   - List code logic that has changed but documentation has not been updated.
   - List documentation that describes features not yet implemented in code.

## 4. Gap Analysis & Change Requirements

Compare "Current State" with "New CR Requirements":

a. **No Change** (Keep as-is):
   - Logic/modules not affected by the CR.

b. **Modify**:
   - API changes (path, request/response).
   - Business logic changes.
   - Database schema changes.
   - Configuration changes.

c. **New**:
   - New modules/services to create.
   - New entities/DTOs.
   - New endpoints.

d. **Remove**:
   - Deprecated logic to be removed.

Present in this format:

```
┌─────────────────────────────────────────────────┐
│              GAP ANALYSIS SUMMARY               │
├─────────────────────────────────────────────────┤
│ [V] No Change: [count] components                 │
│ [EDIT]  Modify:    [count] components                │
│ [NEW] New:       [count] components                │
│ [DEL]  Remove:    [count] components                │
└─────────────────────────────────────────────────┘
```

## 5. Generate Artifacts (Create CR Documents)

a. **Create CR directory**:
   ```
   openspec/changes/<YYYY-MM-DD>-<cr-name>/
   ```

b. **Generate `metadata.yaml`**:

   Create `openspec/changes/<YYYY-MM-DD>-<cr-name>/metadata.yaml`:

   ```yaml
   id: "<PROJECT_ID>"  # Format: UPPERCASE(rootProject.name) + YYMMDD + 5 random alphanumeric (mixed case)
   name: "<cr-name>"
   type: "change-request"
   created: "YYYY-MM-DDTHH:mm:ss+07:00"  # Exact timestamp when the command was invoked
   summary: "<Tóm tắt yêu cầu thay đổi bằng tiếng Việt>"
   service:
     - "<service-1>"  # Services affected (from codebase analysis in Step 2)
     - "<service-2>"
   path:
     - "<endpoint-1>"  # API endpoints affected (from controller scan in Step 2)
     - "<endpoint-2>"
   confluence:
     - id: "<page-id>"
       name: "<page-title>"  # From documentation if provided, empty list if not
   jira:
     - id: "<ticket-id>"
       name: "<ticket-title>"  # From Jira reference if provided, empty list if not
   ```

   **Rules:**
   - `id` MUST be generated as: `UPPERCASE(rootProject.name)` + `YYMMDD` + 5 random alphanumeric characters (mixed uppercase/lowercase letters and digits). Read `rootProject.name` from `settings.gradle` at project root. Example: `VCBDIGIBIZ260323A1b2C`
   - `created` MUST use the exact current timestamp (with timezone)
   - `summary` MUST be written in Vietnamese
   - `service` and `path` derived from Step 2 (codebase analysis)
   - `confluence` and `jira` from user-provided inputs — use empty list `[]` if not provided

c. **Generate `cr-proposal.md`**:
   This file focuses on **Why** and **Impact**.

   Template:
   ```markdown
   # CR: <CR Name>

   ## Background
   [Brief description of why this CR is needed]

   ## Objective
   [What problem does this CR solve / what feature does it add]

   ## Impact Analysis

   ### Backward Compatibility
   [Does this CR break existing logic?]

   ### Dependencies
   [Which modules/services are affected?]

   ### Risk Assessment
   [Risks when deploying?]

   ## Current State Summary
   [Summary of current code state from Step 2]

   ## Doc-to-Code Mapping
   [Mapping table from Step 3]

   ## Gap Analysis
   [Analysis results from Step 4]
   ```

d. **Generate `cr-spec.md`**:
   This file defines the **Scope** — what exactly must change (API contracts, business logic, constraints, edge cases). Generated AFTER `cr-proposal.md` and BEFORE `cr-design.md`.

   Template:
   ```markdown
   # Spec: <CR Name>

   ## Scope Overview
   [Brief description of what this CR changes — boundaries of the change]

   ## API Contract Changes

   ### [Endpoint 1: METHOD /path]
   **Change type**: Modify / New / Remove

   **Request Changes**:
   ```json
   {
     "existingField": "unchanged",
     "newField": "added — description and constraints"
   }
   ```

   **Response Changes**:
   ```json
   {
     "existingField": "unchanged",
     "newField": "added — description"
   }
   ```

   **Headers**: [Any new/changed headers]

   ### [Endpoint 2: METHOD /path]
   [Same structure]

   ## Business Logic & Constraints

   ### [Logic Change 1: Title]
   - **Current behavior**: [How it works now — reference `current-code-logic.md`]
   - **New behavior**: [How it should work after CR]
   - **Validation rules**: [New/changed validation steps]
   - **Business constraints**: [Limits, conditions, blocking rules]

   ### [Logic Change 2: Title]
   [Same structure]

   ## Data Model Changes

   | Entity/DTO | Field | Change | Type | Constraints | Notes |
   |-----------|-------|--------|------|-------------|-------|
   | `RequestDTO` | `newField` | ADD | String | Required, max 50 chars | [purpose] |
   | `Entity` | `column` | MODIFY | Integer → Long | Not null | [reason] |

   ## Edge Cases & Error Handling

   | # | Scenario | Expected Behavior | Error Code | HTTP Status | Message |
   |---|---------|-------------------|-----------|-------------|----------|
   | 1 | [edge case description] | [how system should respond] | `ERR_XXX` | 400/422/500 | [user-facing message] |
   | 2 | ... | ... | ... | ... | ... |

   ## Security & Authorization
   - **Affected audience**: [CA/SME/Enterprise/Internal/System]
   - **Permission changes**: [New roles/permissions required, or unchanged]
   - **Data sensitivity**: [Any new sensitive data handling?]

   ## Configuration Requirements

   | Config Key | Default | Description | Required By |
   |-----------|---------|-------------|-------------|
   | `NEW_KEY` | `value` | [purpose] | [which logic uses it] |

   ## Out of Scope
   [Explicitly state what this CR does NOT change — prevent scope creep]
   ```

   **cr-spec.md Creation Rules**:
   - MUST reference `current-code-logic.md` for current behavior descriptions
   - MUST list ALL API contract changes with request/response JSON examples
   - MUST list ALL edge cases with error code mappings
   - MUST include an "Out of Scope" section to prevent scope creep
   - Focus on WHAT changes, NOT how to implement — design details go in `cr-design.md`

   Show progress: "Created cr-spec.md"

e. **Generate `cr-design.md`**:
   This file focuses on **How** — how to modify the code.

   Template:
   ```markdown
   # Design: <CR Name>

   ## Design Overview
   [Describe the technical approach]

   ## File Changes

   [List in tree format — comply with Planning Rule]

   ```
   service/src/main/java/...
   ├── controller/          [EXISTING]
   │   └── XxxController.java   [MODIFY]
   ├── service/             [EXISTING]
   │   └── XxxService.java      [MODIFY]
   ├── model/               [NEW PACKAGE]
   │   ├── request/
   │   │   └── NewRequest.java   [NEW]
   │   └── response/
   │       └── NewResponse.java  [NEW]
   ```

   ## Logic Implementation Mapping

   [1-to-1 mapping of logic ↔ business doc — comply with Planning Rule]

   | Logic/Method Name | Detailed Behavior Description | Which Doc Section? (Ref ID) | Note (Constraints) |
   |---|---|---|---|
   | ... | ... | ... | ... |

   ## API Changes
   [If any: method, path, request/response schema changes]

   ## Database Changes
   [If any: ALTER TABLE, new tables, migration scripts]

   ## Configuration Changes
   [New/changed config keys]

   | Key | Default | Description | Environment |
   |---|---|---|---|

   ## Error Codes
   [New error codes — comply with Error Handling Rule]

   | Error Code | HTTP Status | Message Template | Trigger |
   |---|---|---|---|
   ```

f. **Generate `cr-tasks.md`**:
   Specific implementation checklist.

   Template:
   ```markdown
   # Tasks: <CR Name>

   ## Pre-requisites
   - [ ] Review and approve cr-proposal.md
   - [ ] Review and approve cr-design.md

   ## Implementation
   - [ ] Task 1: [Specific description]
   - [ ] Task 2: [Specific description]
   - [ ] ...

   ## Testing
   - [ ] Unit tests for new logic
   - [ ] Integration tests
   - [ ] Manual testing

   ## Documentation
   - [ ] Update API doc (push to APIDog if needed)
   - [ ] Update internal documentation

   ## Deployment
   - [ ] Config changes applied
   - [ ] Database migration executed
   ```

g. **Generate `current-code-logic.md`**:
   Detailed current code logic extracted from Step 2 analysis. This file is the **single source of truth** for how the feature currently works in code.

   Template:
   ```markdown
   # Current Code Logic: <CR Name>

   ## Related Components

   | File Path | Role | Layer |
   |-----------|------|-------|
   | `path/to/Controller.java` | REST endpoint | Controller |
   | `path/to/Service.java` | Business logic | Service |
   | `path/to/Repository.java` | Data access | Repository |

   ## Processing Flow

   [Detailed step-by-step flow with method calls, conditions, and data transformations]
   [Use Mermaid sequence/flowchart diagrams]

   ```mermaid
   sequenceDiagram
       participant Client
       participant Controller
       participant Service
       participant Repository
       participant ExternalAPI
       [Fill in actual flow]
   ```

   ## Business Logic Details

   ### [Method/Feature Name 1]
   - **Entry point**: `ClassName.methodName()`
   - **Input**: [Parameters and their types]
   - **Validation**: [Validation rules applied]
   - **Processing**: [Step-by-step logic]
   - **Output**: [Return value/response]
   - **Error handling**: [Exceptions thrown, error codes used]

   ### [Method/Feature Name 2]
   [Same structure as above]

   ## Configuration Keys

   All config keys retrieved via `commonService.getConfig(...)` or `configService.getConfig(...)`:

   | Config Key | Default Value | Used In (File/Method) | Purpose |
   |-----------|---------------|----------------------|----------|
   | `KEY_NAME` | `"default"` | `Service.method()` | [Purpose] |

   ## Message Codes

   All message codes used via `CommonService.getMessage(...)` or `Constants.MessageCode.*`:

   | Message Code | Constant | Used In (File/Method) | Description |
   |-------------|----------|----------------------|-------------|
   | `Constants.MessageCode.ERROR_112` | `ERROR_112` | `Service.method()` | [Description] |

   ## Error / Response Codes

   All response codes from `Constants.ResCode.*`:

   | Error Code | Constant | Trigger Condition | Used In (File/Method) | Description |
   |-----------|----------|------------------|----------------------|-------------|
   | `Constants.ResCode.ERROR_112` | `ERROR_112` | Balance insufficient | `Service.method()` | [Description] |

   ## External Integrations

   | System | API/Method | Request Format | Response Format | Timeout |
   |--------|-----------|---------------|-----------------|----------|
   | Core Banking ESB | [endpoint] | [format] | [format] | [timeout] |
   ```

h. **Generate `compare-logic.md`**:
   Comparison between current code logic and documented/new logic. This file is generated AFTER `current-code-logic.md` and uses data from Steps 3-4.

   - If **no related documentation exists**: state "No related documentation found for comparison. This file compares current code logic against CR requirements only."
   - If **documentation exists**: compare each logic point between current code and documented behavior.

   Template:
   ```markdown
   # Logic Comparison: <CR Name>

   ## Comparison Summary

   | Status | Count | Description |
   |--------|-------|-------------|
   | [V] Match | [N] | Code matches documentation/requirements |
   | [!] Divergent | [N] | Code differs from documentation |
   | [NEW] New (in CR) | [N] | New logic required by this CR |
   | [X] Missing in code | [N] | Documented but not implemented |
   | [DEL] To remove | [N] | Current code to deprecate/remove |

   ## Detailed Comparison

   | # | Logic Point | Current Code Logic | Doc/New Logic | Status | Note |
   |---|------------|-------------------|---------------|--------|------|
   | 1 | [description] | [what code does now — ref `current-code-logic.md`] | [what doc says / what CR requires] | [V]/[!]/[NEW]/[X] | [explanation] |
   | 2 | ... | ... | ... | ... | ... |

   ## Divergence Details

   ### Divergence #1: [Title]
   - **Current code**: [detailed current behavior]
   - **Documentation says**: [what the doc/spec says]
   - **Impact**: [what happens if we align code to doc]
   - **Recommendation**: [align to doc / keep code / discuss]

   ## New Logic Details

   ### New Logic #1: [Title]
   - **Requirement**: [what the CR requires]
   - **Affected components**: [which files/methods need to change]
   - **Suggested approach**: [brief implementation hint]

   ## Impact of Changes
   [Summary of which current logic paths will be affected when adopting the new/documented logic]
   ```

## 6. Show Final Status

After creating all artifacts, print a summary.

---

**Output On Success**

Print to screen:
- CR name and directory path.
- Mapping results (number of matched / divergent / missing points).
- Gap Analysis Summary (no change / modify / new / remove).
- List of CR documents created (6 artifacts) with brief descriptions.
- Prompt: "Run `/opsx:apply` to start implementing."

---

**Artifact Creation Guidelines**

- Comply with **Planning Rule**: file changes must be in tree format, 1-to-1 logic mapping with business docs.
- Comply with **Error Handling Rule**: error codes use `Constants.ResCode`, messages via `CommonService.getMessage()`.
- Comply with **Security Rule**: do not expose sensitive data, validate input, encrypt when needed.
- Use **ASCII diagrams** when describing processing flows.
- Use **Markdown tables** when comparing or listing items.
- Each artifact must be **self-contained** — readable on its own.

**Guardrails**

- **MUST** analyze the actual codebase before writing the proposal — do not guess.
- **MUST** create all 6 artifacts: `cr-proposal.md`, `cr-spec.md`, `cr-design.md`, `cr-tasks.md`, `current-code-logic.md`, `compare-logic.md`.
- **MUST** generate `cr-spec.md` AFTER `cr-proposal.md` and BEFORE `cr-design.md`.
- **MUST** map code ↔ doc — if no related documentation is found, explicitly state "No related documentation found".
- **MUST** list ALL config keys (`getConfig(...)`) in `current-code-logic.md` with key name, default value, usage location, and purpose.
- **MUST** list ALL message codes (`getMessage(...)`, `Constants.MessageCode.*`) in `current-code-logic.md` with constant, usage location, and description.
- **MUST** list ALL error/response codes (`Constants.ResCode.*`) in `current-code-logic.md` with constant, trigger condition, and description.
- **DO NOT** implement code — only create CR documents.
- **DO NOT** skip gap analysis — this is the most important part.
- **DO NOT** omit config keys, message codes, or error codes — these are critical for understanding the current system.
- If context is unclear, ask the user — but prefer making reasonable decisions to maintain momentum.
- If the CR directory already exists, ask the user if they want to continue or create a new one.
- Verify each artifact file exists after writing before proceeding to the next.
