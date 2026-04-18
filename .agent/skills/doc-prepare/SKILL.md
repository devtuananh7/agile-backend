---
name: doc-prepare
description: Pipeline to convert raw input documents (DOCX, PDF, XLSX, JSON, OpenAPI) to clean Markdown for agent consumption, with optional quality check and beautification.
---

# `doc-prepare` Skill

This skill executes the document preparation pipeline.

## Input Constraints
- The user must place input documents in `base_knowledge/temp/`.
- Supported formats: `.doc`, `.docx`, `.xls`, `.xlsx`, `.json`, `.pdf`
- Output directory is `base_knowledge/converted/`.

## Processing Steps

Follow these steps exactly to convert the raw documents to Markdown.

### Step 1: Scan Temp Directory
1. Check the contents of `base_knowledge/temp/`.
2. Filter for supported files (`.doc`, `.docx`, `.xls`, `.xlsx`, `.json`, `.pdf`).
3. If no supported files are found, notify the user: "Không tìm thấy tài liệu nào trong base_knowledge/temp/. Vui lòng tải file vào thư mục này." and **STOP**.
4. Warn the user if unsupported files (e.g., .pptx) are found, and ignore them.

### Step 2: Convert Files
1. Create `base_knowledge/converted/` if it doesn't exist.
2. For *each* supported file in `base_knowledge/temp/`, run the OpenSpec CLI conversion command:
   ```bash
   openspec convert "base_knowledge/temp/<filename>" -o "base_knowledge/converted/<basename>.md"
   ```
   *Note: Preserve the original file name, just change the extension to `.md`.*

### Step 3: Branch based on `--light` flag
Check the command invocation. Was this skill called with the `--light` (or `-l`) flag?

**If `--light` flag is PRESENT:**
1. You are running in **Light Mode**. Skip Steps 4 and 5.
2. Proceed directly to **Step 6 (Cleanup)**.
3. Then proceed to **Step 7 (Review Gate)**, showing only the list of converted files.

**If `--light` flag is ABSENT:**
1. You are running in **Full Mode**.
2. Proceed to **Step 4 (Quality Check)**.

### Step 4: Quality Check (Full Mode Only)
For each successfully converted `.md` file in `base_knowledge/converted/`, evaluate it against these 4 Quality Criteria:

1.  **Heading structure**: Does the file have ≥3 proper Markdown headings (`#`, `##`, `###`) creating a logical hierarchy?
2.  **Table format**: If tables exist, are they formatted correctly using proper standard Markdown table syntax (`| --- |`)?
3.  **Clean encoding**: Is the content free of junk characters (like `□`, `▯`, `\ufffd`, mojibake) and raw HTML tags?
4.  **Parseable sections**: Does the document have clear section boundaries rather than walls of unbroken text?

**Evaluation Result:**
- If the file passes ALL 4 criteria: Mark as `CLEAN` (`quality: clean-as-is`).
- If the file fails ANY of the 4 criteria: Mark as `NEEDS BEAUTIFY` (`quality: auto-beautified`) and proceed to **Step 5 (Beautify)** for this file.

*If marked CLEAN, proceed to Step 5 only to add the Summary Header, do NOT modify the core content.*

### Step 5: Beautify and Add Summary Header (Full Mode Only)
Iterate through the results from Step 4.

**For files marked `NEEDS BEAUTIFY`:**
1. Read the full content of the file.
2. Rewrite the file to fix structural issues (repair headings, fix broken tables, remove junk characters, logical reorganization).
3. Do not lose any semantic meaning during the rewrite.

**For ALL files (both `CLEAN` and `NEEDS BEAUTIFY`):**
Add the following standardized YAML frontmatter and Summary block to the very top of the file:

```markdown
---
source: <original_filename.ext>
converted: <YYYY-MM-DD HH:mm>
quality: <auto-beautified OR clean-as-is>
---

# Summary

> <Generate a 3-5 sentence summary of the core content>
> - Chủ đề chính: <Main topic>
> - Loại tài liệu: <Document type, e.g., URD, API Spec, Hợp đồng, etc.>
> - Số sections: <Number of main sections>

---

<Content begins here...>
```

Overwrite the file in `base_knowledge/converted/<basename>.md` with this updated content.

### Step 6: Cleanup
Regardless of the mode (`--light` or full), the `base_knowledge/temp/` directory must be cleaned up to ensure no state carries over to future runs.

1. Recursively delete the temp directory contents and the directory itself:
   ```bash
   rm -rf base_knowledge/temp/
   ```
2. Inform the user "Đã xóa thư mục temp."

### Step 7: Review Gate
This step is MANDATORY to halt the workflow and ensure the user takes responsibility for verifying the outcome.

1. List all the `.md` files present in `base_knowledge/converted/`.
2. Display the following exact banner in your response to the user:

```
╔══════════════════════════════════════════════════════════════╗
║  YÊU CẦU NGƯỜI DÙNG REVIEW LẠI TÀI LIỆU                  ║
║  TRƯỚC KHI THỰC HIỆN CÁC BƯỚC TIẾP THEO                   ║
╚══════════════════════════════════════════════════════════════╝
```
3. Stop the workflow completely. Do not trigger `feat-init` or any other action. Wait for the user's next command.
