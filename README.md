# Fashion AI Library

A lightweight AI-powered web app for fashion garment classification and inspiration image search. The MVP lets a user upload garment inspiration images, save source context, run mock or real OpenAI Vision classification, search with SQLite FTS5, filter by extracted metadata, and add manual designer annotations.

## Tech Stack

- Frontend: Next.js 16, React 19, Tailwind CSS
- Backend: Java 17, Spring Boot 3.3, Maven
- Database: SQLite with FTS5
- Image storage: local `backend/uploads/`
- AI: OpenAI Responses API with strict JSON schema output
- Tests: JUnit for parser, search, filters, and annotations

Production note: local uploads are intentionally simple for the assessment. In production, image storage would move to S3, Cloudflare R2, or another object store, with signed upload/download URLs.

## Repository Layout

```text
backend/   Spring Boot API, SQLite schema initialization, AI classification, search
frontend/  Next.js browser workflow
PRD.md     Product requirements and tradeoff notes
plan.md    Stepwise implementation and validation plan
```

## Prerequisites

- Java 17+
- Node.js 24+ and npm 11+
- An OpenAI API key with available quota for real classification

## Backend Setup

```powershell
cd backend
Copy-Item .env.example .env
```

Edit `backend/.env`:

```env
OPENAI_API_KEY=your_key_here
OPENAI_MODEL=gpt-4o-mini
```

Run the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Useful checks:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8080/api/health/db
```

Expected health response:

```json
{"status":"ok"}
```

## Frontend Setup

```powershell
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000`.

The frontend defaults to `http://localhost:8080` for the backend. To override:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## Main Workflow

1. Upload a fashion inspiration image.
2. Add source context such as designer, date, country, and city.
3. Run mock classification for offline validation, or OpenAI classification when `OPENAI_API_KEY` is configured.
4. Search generated descriptions, AI metadata, and manual annotations.
5. Filter dynamically by stored metadata such as garment type, material, color, country, year, and designer.
6. Add manual tags, notes, and observations separately from AI metadata.

## API Summary

- `GET /api/health`
- `GET /api/health/db`
- `POST /api/images/upload`
- `GET /api/images`
- `GET /api/images/{id}`
- `POST /api/images/{id}/classifications/mock`
- `POST /api/images/{id}/classifications/openai`
- `POST /api/images/{id}/annotations`
- `GET /api/filters`
- `GET /uploads/{filename}`

## OpenAI Classification

The real classifier uses the OpenAI Responses API with image input and strict structured output. The backend sends the uploaded image as a data URL and requires a JSON object with:

- `description`
- `garment_type`
- `style`
- `material`
- `color_palette`
- `pattern`
- `season`
- `occasion`
- `consumer_profile`
- `trend_notes`
- `location_context`
- `confidence_notes`

The model output is parsed and validated by the same parser used by the mock classifier. If parsing fails, the classification is rejected instead of storing partial or malformed metadata.

## Search And Filters

SQLite FTS5 indexes:

- AI description
- flattened AI metadata
- manual annotation text

Dynamic filters are generated from stored image context and classification metadata, so the frontend never relies on hardcoded filter options.

## Testing

Run backend tests:

```powershell
cd backend
.\mvnw.cmd test
```

Run frontend checks:

```powershell
cd frontend
npm run typecheck
npm run build
```

Current backend coverage focuses on:

- classification JSON parsing and validation
- FTS5 search indexing
- dynamic filter generation
- manual annotation persistence and search indexing

## Evaluation Summary

Functional scope completed:

- Spring Boot backend skeleton and health checks
- SQLite schema initialization and FTS5 verification
- local image upload and static serving
- mock classification parser and validator
- FTS5 search across AI and manual text
- dynamic filters from real stored data
- manual annotations
- Next.js upload, classify, search, filter, and annotation flows
- OpenAI Vision endpoint with strict JSON schema output

Model quality approach:

- Uses a constrained schema to reduce malformed model responses.
- Keeps uncertain visual inference in `confidence_notes`.
- Normalizes array metadata for predictable filtering and search.
- Retains raw model JSON for audit/debugging.

Product tradeoffs:

- The mock classification path keeps the demo usable without an API key.
- Manual annotations are stored separately from AI metadata so user expertise is not mixed with inferred labels.
- SQLite + FTS5 is enough for a one-day MVP and easy to review, while leaving room to replace search later.

## Known Limitations

- The current UI is intentionally compact and not fully polished for mobile-heavy usage.
- OpenAI classification depends on API key validity, model access, and available quota.
- The local filesystem upload strategy is not production durable.
- There is no authentication or multi-user separation.
- Search ranking is basic FTS5 BM25 without semantic embeddings.
- The schema validates shape, but visual classification quality still needs a human-reviewed evaluation set.
- Frontend tests are not yet added; current automated tests are backend-focused.

## Next Steps

- Add a small labeled evaluation set and compare model output against expected garment attributes.
- Add frontend component or Playwright workflow tests.
- Add retry and clearer user-facing states for OpenAI quota/rate-limit errors.
- Move images to S3/R2 and store object keys instead of local paths.
- Add auth and workspace/project separation.
- Add embedding search for more flexible inspiration discovery.
