# SRS Generation Rules

This document defines the standard template and rules for generating Software Requirements Specification (SRS) documents. Based on IEEE 830 standard with 6 sections. Developers can customize section requirements by changing `[REQUIRED]` / `[OPTIONAL]` markers.

---

## SRS Writing Principles

- **Be Specific**: Use clear language to avoid ambiguity
- **Make it Testable**: Requirements must be verifiable by testers
- **Define "What," Not "How"**: Focus on behavior, not implementation design
- **Traceability**: Ensure every requirement connects back to a business need
- **Business Language**: Write for tester and client audience — avoid deep technical terminology (class names, package paths)

---

## SRS Template Sections

### Section 1: Introduction [REQUIRED]

```markdown
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
```

**Rules:**
- Purpose MUST clearly state the document's intent and target readers
- Project Scope MUST be derived from `proposal.md` (Why + What Changes)
- Definitions table MUST include all domain-specific terms used in the document

---

### Section 2: Overall Description [REQUIRED]

```markdown
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
```

**Rules:**
- Product Functions MUST be derived from **actual code** or **spec artifacts**, NOT from assumptions
- User Classes MUST cover all actors interacting with the feature

---

### Section 3: System Features (Functional Requirements) [REQUIRED]

```markdown
## 3. System Features (Functional Requirements)

### 3.N <Feature Name>

#### 3.N.1 Description and Priority
[Describe the feature and its importance — HIGH/MEDIUM/LOW]

#### 3.N.2 Stimulus/Response Sequences
[What triggers the feature and how it responds — based on actual API endpoints and flows]

#### 3.N.3 Functional Requirements
[Detailed, testable requirements: "The system shall..."]
- FR-001: [requirement derived from actual implementation]
- FR-002: [requirement derived from actual implementation]

#### 3.N.4 Test Scenarios
| # | Scenario | Preconditions | Steps | Expected Result |
|---|----------|--------------|-------|----------------|
| TC-001 | [scenario name] | [setup needed] | [steps] | [expected outcome] |
```

**Rules:**
- This is the **core section** — MUST be the most detailed
- MUST repeat `3.N` block for each distinct feature/capability
- ALL functional requirements MUST be derived from **actual code or artifacts**, NOT from assumptions
- Each feature MUST include **Test Scenarios** (3.N.4) — no exceptions
- Use `delta-spec.md` (if available) to adjust requirements that changed during implementation
- Priority MUST reflect business impact (HIGH = core flow, MEDIUM = supporting, LOW = nice-to-have)

---

### Section 4: External Interface Requirements [OPTIONAL]

```markdown
## 4. External Interface Requirements

### 4.1 User Interfaces
[Screen layout, GUI standards, or API consumer interfaces]

### 4.2 Hardware Interfaces
[Supported devices and protocols, if applicable]

### 4.3 Software Interfaces
[Databases, OS, external APIs, internal services — derived from actual integrations]

### 4.4 Communications Interfaces
[Network protocols, message formats, if applicable]
```

**Rules:**
- Use `new-apis.md` (if available) to populate API interface details
- Software Interfaces MUST list all external system integrations discovered in code/design
- Skip Hardware/Communications sub-sections if not applicable (do NOT leave empty placeholders)

---

### Section 5: Non-functional Requirements (NFRs) [OPTIONAL]

```markdown
## 5. Non-functional Requirements (NFRs)

### 5.1 Performance Requirements
[Speed, response times, throughput — derived from actual implementation constraints]

### 5.2 Safety/Security Requirements
[Data protection, compliance standards, authentication/authorization requirements]

### 5.3 Quality Attributes
[Availability, maintainability, usability requirements]
```

**Rules:**
- Performance requirements SHOULD include measurable targets when available
- Security requirements MUST reflect actual authentication/authorization mechanisms in code

---

### Section 6: Other Requirements [OPTIONAL]

```markdown
## 6. Other Requirements

### 6.1 Known Limitations
[Document any known limitations, workarounds, or deferred items]

### 6.2 Appendices
[Additional supporting information, references, regulatory constraints]
```

**Rules:**
- Use `todo-uncover.md` (if available) to populate Known Limitations
- Include any TODOs, FIXMEs, or uncovered edge cases discovered during implementation

---

## Source-to-Section Mapping

When tracking artifacts from `feat-apply` are available, map them to SRS sections:

| Tracking Artifact | Maps To SRS Section |
|-------------------|---------------------|
| `todo-uncover.md` | Section 6 — Known Limitations |
| `new-apis.md` | Section 4 — External Interface Requirements |
| `delta-spec.md` | Section 3 — Functional Requirements (adjustments) |
| `tech_requirement.md` | Section 2 — Constraints + Section 5 — NFRs |
| `metadata.yaml` | Section 1 — Scope, Definitions |
| `feat_overview.md` | Section 1 — Purpose, Scope |

---

## Customization Guide

To customize SRS generation for your project:

1. **Change section requirements**: Toggle `[REQUIRED]` ↔ `[OPTIONAL]` on any section header
2. **Add sub-sections**: Add new sub-sections under any section — the generator will populate them
3. **Remove sub-sections**: Delete sub-sections you don't need (within `[OPTIONAL]` sections only)
4. **Add project-specific rules**: Append rules under any section's `**Rules:**` block
