Convert raw input documents (DOCX, PDF, XLSX, JSON, OpenAPI) to clean Markdown for agent consumption.

**Input**: User places raw document files in `base_knowledge/temp/`.

**Output**: Clean Markdown files in `base_knowledge/converted/`.

**Flags**:
- `--auto-run` / `-y`: Optional. If present, auto-run commands without asking.
- `--light` / `-l`: Optional. If present, only convert the files to Markdown. Skip the quality check and beautification step. Use this for very large files or when the output format isn't critical.

---

**Processing Steps:**

1.  **Read the Skill Directory**
    Load the detailed instructions and processing logic from the skill directory. The Skill directory is at `.agent/skills/doc-prepare/SKILL.md`.

2.  **Execute the Skill**
    Follow the processing steps defined in the `doc-prepare` skill to scan the `temp` directory, convert files, optionally perform quality checks and beautification, and finally clean up the `temp` directory.
