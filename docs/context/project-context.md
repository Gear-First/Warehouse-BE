# Project Context (based on docs/)

This document summarizes the project context by analyzing the docs directory and capturing its structure. It links the written decisions/use-cases/policies to the current code layout and highlights the next implementation plan. See also: docs/context/minute-of-functional-spec.md

## docs directory structure

```
docs
├── README.md
├── adr
│   ├── ADR-01-Domain-First-Layered-Architecture.md
│   ├── ADR-02-Uniformed-Responses-Exceptions.md
│   ├── ADR-03-Test-Code-Convention-GWT.md
│   ├── ADR-04-Coding-Convention.md
│   └── ADR-05-Use-Cases-are-Non-Authoritative.md
├── context
│   ├── minute-of-functional-spec.md
│   └── project-context.md
├── domain
│   ├── oop-crc-receiving.md
│   ├── oop-crc-shipping.md
│   └── parts-domain.md
├── policy
│   ├── receiving-inspection.md
│   ├── shipping-fulfillment.md
│   └── inventory-overview.md
├── standards
│   ├── coding-convention.md
│   ├── contributing-docs.md
│   ├── exception-and-response.md
│   ├── glossary.md
│   ├── package-structure.md
│   ├── query-strategy.md
│   └── testing-convention.md
└── uc
    ├── 01-receiving
    │   ├── UC-REC-001-list-not-done.md
    │   ├── UC-REC-002-list-done.md
    │   ├── UC-REC-003-get-note-detail.md
    │   ├── UC-REC-004-update-line.md
    │   └── UC-REC-005-complete-note.md
    ├── 02-shipping
    │   ├── UC-SHP-001-list-not-done.md
    │   ├── UC-SHP-002-list-done.md
    │   ├── UC-SHP-003-get-note-detail.md
    │   ├── UC-SHP-004-update-line.md
    │   └── UC-SHP-005-complete-note.md
    ├── 03-parts
    │   ├── UC-PCT-001-list-categories.md
    │   ├── UC-PCT-002-get-category-detail.md
    │   ├── UC-PCT-003-create-category.md
    │   ├── UC-PCT-004-update-category.md
    │   └── UC-PCT-005-delete-category.md
    └── 04-inventory
        └── UC-INV-001-list-onhand.md
```

## What each section means and how it maps to code

- adr (Architecture Decision Records)
  - ADR-01: Domain-first + layered flow. Code reflects this under `src/main/java/com/gearfirst/warehouse/api/{receiving|shipping}` with controller → service (interface) → service impl → repository → entity.
  - ADR-02: Uniform API responses and exceptions. Implemented in `common/exception` and `common/response`.
  - ADR-03/04: Testing + coding conventions. Tests under `src/test/java/...` follow GWT style; general code style matches conventions.
  - ADR-05: Use Cases (UC) are non‑authoritative; real source of truth is policy/CRC/standards/code/tests.

- context
  - minute-of-functional-spec.md: Unified specs for Receiving/Shipping/Parts; aligns field sets and validation rules. This PR updates this file; code changes follow in next PR.
  - project-context.md: This document.

- standards
  - package-structure.md: Domain-centric packages with common isolated. The project follows this: `api/shipping`, `api/receiving`, and `common/*` for shared concerns.
  - exception-and-response.md: Standardized `ApiResponse`, `SuccessStatus`, `ErrorStatus`. Corresponding classes exist under `src/main/java/com/gearfirst/warehouse/common/response` and `common/exception`.
  - coding-convention.md, testing-convention.md: Align with code and tests.
  - glossary.md: Unified vocabulary for states/quantities/identifiers.
  - query-strategy.md: Keep Spring Data now; Querydsl prepare-only; Parts Read as pilot candidate.
  - contributing-docs.md: Branching, review, Date metadata, ADR-05 principle.

- domain (CRC notes + parts)
  - oop-crc-receiving.md, oop-crc-shipping.md: Responsibilities of Receiving/Shipping entities and services. Code alignment is visible in `api/shipping/domain` types and service contracts.
  - parts-domain.md: ERD/fields for Part, PartCategory, CarModel, PartCarModel. Implementation to be introduced in next PR.

- policy
  - receiving-inspection.md, shipping-fulfillment.md: Operational policies for inspections and fulfillment that UCs should align with. In case of conflicts, policies override UC docs per ADR-05.

- uc (Use Cases)
  - 01-receiving and 02-shipping specify endpoints and I/O for list/detail/update/complete flows.
  - Example (UC-SHP-003): GET `/v1/shipping/{noteId}` returns shipping note detail with lines and statuses; implemented by `ShippingController` + `ShippingService` using domain enums `NoteStatus`, `LineStatus`.

## Quick map from UC to code (shipping)

- List not done: UC-SHP-001 → `ShippingController.getNotDoneNotes()` → `ShippingService.listNotDone()` → repository
- List done: UC-SHP-002 → `ShippingController.getDoneNotes()` → `ShippingService.listDone()` → repository
- Get note detail: UC-SHP-003 → `ShippingController.getNoteDetail(noteId)` → `ShippingService.getNoteDetail(noteId)`
- Update line: UC-SHP-004 → `ShippingController.updateLine(noteId, lineId, UpdateLineRequest)`
- Complete note: UC-SHP-005 → `ShippingController.complete(noteId)` → `ShippingService.complete(noteId)`

DTOs live in `api/shipping/dto/*Response.java` and map closely to UC response shapes. Domain state is captured in `api/shipping/domain/*`.

## Quick map from UC to code (receiving)

Receiving is implemented in a simplified or mock form for now:
- Controller: `api/receiving/ReceivingController`
- Store/Mock: `api/receiving/ReceivingMockStore`
- DTOs: `api/receiving/dto/*`
- Endpoints reflect the UC files UC-REC-001..005.

## Common infrastructure referenced by docs

- Swagger/OpenAPI: `common/swagger/SwaggerConfig.java`
- Exceptions: `common/exception/*` with `GlobalExceptionHandler`
- Uniform responses: `common/response/*` (`ApiResponse`, statuses)
- Config: `src/main/resources/application*.yml|properties`

## Implementation Plan (next PR)

- Parts Domain
  - Create JPA entities: PartCategory, Part, CarModel, PartCarModel (composite key)
  - CRUD APIs for PartCategory and Part (read/search includes carModelId, categoryId)
- Receiving Domain
  - Replace mock with JPA entities: ReceivingNote, ReceivingNoteLine (fields as per minutes)
  - Service rules: line update validation, note completion checks; inventory increase hook
- Shipping Domain
  - Align fields with minutes (handledBy/customer naming); ensure validation mirrors receiving
- Inventory Read Model (basic)
  - Introduce view/query to list current stock by warehouse/part (calculated or table, TBD)
- Migrations
  - Add schema via JPA/Hibernate auto DDL or Flyway (decision pending)
- Tests
  - Controller/service tests for new endpoints; keep GWT style

Note: This PR intentionally updates docs only. No code changes are included here per request.
