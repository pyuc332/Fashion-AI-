# Fashion AI Library Development Plan

This plan breaks the project into small implementation steps with a validation checkpoint after each step. The goal is to keep the one-day MVP verifiable as it grows.

## Step 0: Initialize Repository Structure

Create the root project structure:

```text
backend/
frontend/
PRD.md
plan.md
.gitignore
```

Validation:

- Confirm `backend/` and `frontend/` exist.
- Confirm `PRD.md` and `plan.md` exist.
- Run `git status --short` and verify only intentional files are untracked or changed.

## Step 1: Spring Boot Backend Skeleton

Create a Spring Boot backend with Maven, Java, and a simple health endpoint.

Backend defaults:

- Spring Boot 3.x.
- Maven.
- Java 17 for the current local environment. Java 21 remains a future upgrade option if available.
- Server port `8080`.
- Upload directory config key: `app.upload-dir=uploads`.
- OpenAI placeholder config key: `openai.api-key=${OPENAI_API_KEY:}`.

Initial endpoint:

```http
GET /api/health
```

Expected response:

```json
{"status":"ok"}
```

Validation:

- Run the backend with Maven.
- Open `GET http://localhost:8080/api/health`.
- Confirm it returns `{"status":"ok"}`.
- Run backend tests and confirm the default context test passes.

## Step 2: SQLite Connection and FTS5 Check

Add SQLite persistence and verify FTS5 support early.

Validation:

- Open `GET http://localhost:8080/api/health/db`.
- Confirm the response reports both `database: ok` and `fts5: ok`.
- Confirm startup creates the initial schema without errors.

## Step 3: Image Upload and Local Storage

Add single-image upload, local file storage, static image serving, and image list/detail APIs.

Validation:

- Upload one image through the API.
- Confirm the image is saved under `backend/uploads/`.
- Confirm `GET /uploads/{filename}` serves the image.
- Confirm `GET /api/images` returns the uploaded record.

## Step 4: Mock Classification Parser

Add classification DTOs, parser, validation, persistence, and a mock endpoint before connecting the real AI API.

Validation:

- Save a fixed mock AI JSON response for an uploaded image.
- Confirm `GET /api/images/{id}` returns normalized classification metadata.
- Run JUnit parser tests.

## Step 5: SQLite FTS5 Search

Index AI descriptions, flattened AI metadata, and manual annotation text in SQLite FTS5.

Validation:

- Search for a term from mock classification text.
- Confirm the expected image is returned.
- Run JUnit search tests.

## Step 6: Dynamic Filters

Generate filter options from stored image and classification data instead of hardcoded constants.

Validation:

- Insert multiple classifications.
- Confirm `GET /api/filters` returns deduplicated values from data.
- Confirm combined filters narrow image results.

## Step 7: Manual Annotations

Allow designers to add tags, notes, and observations that remain distinct from AI metadata.

Validation:

- Add tags, notes, and observations.
- Confirm annotation data appears separately from AI metadata.
- Confirm annotation terms are searchable.

## Step 8: Real OpenAI Vision Classification

Connect the backend classifier to an OpenAI vision-capable model with strict JSON output.

Validation:

- Configure `OPENAI_API_KEY`.
- Upload and classify a real image.
- Confirm strict JSON output is parsed, stored, and searchable.

## Step 9: Next.js Frontend Skeleton

Create the Next.js + Tailwind frontend and connect it to the backend API.

Validation:

- Run the npm dev server.
- Confirm the app loads at `http://localhost:3000`.
- Confirm it can fetch backend image records.

## Step 10: Frontend Upload and Classify Workflow

Implement the main browser workflow for uploading an image and triggering classification.

Validation:

- Upload an image from the browser.
- Classify it.
- Refresh and confirm persisted data remains visible.

## Step 11: Frontend Search and Filters

Add search input, dynamic filters, empty state, loading state, and clear-filter behavior.

Validation:

- Search AI text and manual annotation text.
- Apply and clear dynamic filters.
- Confirm empty state works.

## Step 12: Frontend Annotations

Add manual tag, note, and observation editing in the image detail view.

Validation:

- Add manual tags and notes in the detail view.
- Confirm saved annotations are searchable.

## Step 13: Tests, README, and Evaluation

Complete the backend test suite, setup documentation, architecture notes, evaluation summary, assumptions, limitations, and next steps.

Validation:

- Run backend tests.
- Follow README setup from a clean checkout.
- Include evaluation notes and known limitations.
