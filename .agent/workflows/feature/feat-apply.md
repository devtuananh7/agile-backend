---
description: Implement tasks from an OpenSpec feature change with extended tracking (todo-uncover, new-apis, delta-spec) and strict project standards enforcement.
---

Implement tasks from an OpenSpec change, extending the standard opsx-apply process with additional tracking documentation and mandatory compliance with project coding standards.

**Input**: Optionally specify a change name (e.g., `/feat-apply add-auth`). If omitted, check if it can be inferred from conversation context. If vague or ambiguous you MUST prompt for available changes.

**Steps**

1. **Invoke feat-apply skill**
   Read and rigorously follow the instructions in `.agent/skills/feat-apply/SKILL.md`.

This skill performs the standard apply loop (fetch apply instructions, read context, run tasks), but with three major extensions:

**A. Gate Check + 2-Turn Parallel Pipeline**
Before writing any code, the agent MUST:
1. **Gate Check**: Verify `feat_overview.md` exists. If missing → HALT, request `/feat-init`.
2. **Turn 1**: Read `feat_overview.md`, parse Section 2/3/4, build file list.
3. **Turn 2**: Read ALL files (4 core docs + listed files) using **parallel tool calls in a single batch**. Do NOT read files sequentially.

**B. Mandatory Core Documents (HALT if missing)**
- **Tech Stack**: `base_knowledge/structures/overview_system.md`
- **Coding Conventions**: `base_knowledge/standards/coding_standard.md`
- **Logging Standard**: `base_knowledge/standards/logging_standard.md`
- **Error/Exception Handling**: `base_knowledge/standards/error_handling_standard.md`
- **Supplementary files**: From `feat_overview.md` Section 2/3/4 (WARNING if missing)

> **CRITICAL**: If any of the 4 core documents are missing, the workflow MUST HALT. If `feat_overview.md` is missing, run `/feat-init` first.

**C. Extended Tracking Artifacts**
After implementation, generate in the change directory (`openspec/changes/<name>/`):
- `todo-uncover.md`: Statistics and lists of TODOs/FIXMEs uncovered.
- `new-apis.md`: Documentation of new endpoints (path, request, response, purpose).
- `delta-spec.md`: Analysis of the impact scope and delta changes compared to previous system behavior.
