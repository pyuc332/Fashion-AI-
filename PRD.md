# Fashion Garment Classification & Inspiration Search PRD

## 1. Overview

Build a lightweight AI-powered web app that helps fashion designers organize, search, and reuse inspiration imagery captured in the field.

The product should allow a designer to upload garment inspiration photos, classify each image with a multimodal AI model, store both structured metadata and natural-language descriptions, search across the image library, filter dynamically by discovered attributes, and add manual designer annotations over time.

This is a one-day proof of concept. The goal is to demonstrate an end-to-end product workflow, thoughtful AI integration, clean engineering structure, and clear communication of trade-offs.

## 2. Target User

Fashion designers who regularly capture inspiration images in the field and need a fast, intuitive way to organize, revisit, and reuse visual references.

Primary user needs:

- Save inspiration images quickly.
- Understand what is in each image without manually tagging everything.
- Search by visual/fashion language such as "embroidered neckline", "linen resortwear", or "artisan market".
- Filter by garment attributes, context, location, time, and designer.
- Add personal observations that remain distinct from AI-generated metadata.

## 3. Product Goals

- Provide a working upload-to-search workflow.
- Use AI to generate useful garment descriptions and structured fashion metadata.
- Support dynamic filtering from stored data rather than hardcoded filter values.
- Support full-text search across AI descriptions, AI metadata, and manual annotations.
- Clearly distinguish AI-generated output from designer-authored annotations.
- Include a README that explains setup, architecture, evaluation, assumptions, limitations, and next steps.

## 4. Non-Goals

The MVP will not include:

- Authentication or multi-user permissions.
- Production object storage such as S3 or Cloudflare R2.
- Batch upload.
- Async background job processing.
- Embedding-based semantic search.
- Human-in-the-loop model correction workflow beyond manual annotations.
- Production deployment automation.
- Mobile-native app support.

These are intentionally excluded to keep the assessment scoped to a one-day implementation.

## 5. MVP Scope

### 5.1 Image Upload

Users can upload a single image through the web UI.

Upload metadata may include:

- Designer name.
- Capture date.
- Continent.
- Country.
- City.

The backend saves the image to local storage under an `uploads/` directory and stores the image record in SQLite.

### 5.2 AI Classification

Users can classify an uploaded image using an OpenAI vision-capable model.

The classifier returns:

- A rich natural-language description.
- Structured garment metadata.
- Confidence or uncertainty notes when fields are inferred visually.

Expected structured fields:

- Garment type.
- Style.
- Material.
- Color palette.
- Pattern.
- Season.
- Occasion.
- Consumer profile.
- Trend notes.
- Location context.

The backend validates and normalizes the model output before persistence.

### 5.3 Image Library

Users can view uploaded images in a visual grid.

Each image card should show:

- Thumbnail.
- Garment type.
- Color palette.
- Pattern or style.
- Designer or location when available.

Users can open an image detail view to inspect:

- Full image.
- AI-generated description.
- AI-generated structured metadata.
- Manual tags, notes, and observations.

### 5.4 Search

Users can search the image library with natural text queries.

Search should cover:

- AI-generated image descriptions.
- Flattened AI metadata.
- Manual annotation tags, notes, and observations.

SQLite FTS5 will be used for MVP full-text search.

Example queries:

- `embroidered neckline`
- `artisan market`
- `linen summer dress`
- `streetwear jacket`
- `Kyoto neutral palette`

### 5.5 Dynamic Filtering

The UI displays filter options generated from stored image data.

Supported filters:

- Garment type.
- Style.
- Material.
- Color palette.
- Pattern.
- Season.
- Occasion.
- Consumer profile.
- Trend notes.
- Continent.
- Country.
- City.
- Year.
- Month.
- Designer.

Filter values should come from the current database contents rather than hardcoded constants.

### 5.6 Designer Annotations

Users can add manual annotations to an image:

- Tags.
- Notes.
- Observations.

Manual annotations must be visually distinguished from AI-generated metadata in the UI.

Manual annotations should be included in full-text search.

## 6. Recommended Tech Stack

### Frontend

- Next.js.
- Tailwind CSS.

Frontend responsibilities:

- Upload experience.
- Image grid.
- Detail view.
- Search input.
- Dynamic filters.
- Annotation editing.

### Backend

- Java Spring Boot.

Backend responsibilities:

- Image upload handling.
- Local file storage.
- Static serving of uploaded images.
- OpenAI Vision API orchestration.
- JSON schema validation and metadata normalization.
- SQLite persistence.
- SQLite FTS5 search indexing.
- Dynamic filter generation.
- Annotation APIs.

### Database

- SQLite.
- SQLite FTS5 virtual table for search.

### Image Storage

- Local `uploads/` directory for the proof of concept.
- Production recommendation: migrate to S3, Cloudflare R2, or equivalent object storage.

### AI

- OpenAI vision-capable model.
- Strict JSON schema output.
- Store both raw model response and normalized metadata.

### Tests

- JUnit tests for backend logic.

Minimum test coverage:

- Metadata parsing and validation.
- Dynamic filter generation.
- Annotation search indexing.

## 7. Architecture

```text
Next.js + Tailwind frontend
  |
  | HTTP / JSON
  v
Java Spring Boot backend
  |
  +-- OpenAI Vision API
  |
  +-- SQLite
  |    +-- image records
  |    +-- classifications
  |    +-- annotations
  |    +-- FTS5 search index
  |
  +-- local uploads/
```

The frontend is intentionally kept thin. Product experience lives in Next.js, while AI orchestration, persistence, metadata normalization, search, and tests live in the Java backend.

## 8. Data Model

### images

Stores uploaded image records and user-provided context.

Fields:

- `id`
- `filename`
- `image_url`
- `designer`
- `captured_at`
- `continent`
- `country`
- `city`
- `created_at`
- `updated_at`

### classifications

Stores AI-generated output.

Fields:

- `id`
- `image_id`
- `description`
- `garment_type_json`
- `style_json`
- `material_json`
- `color_palette_json`
- `pattern_json`
- `season_json`
- `occasion_json`
- `consumer_profile_json`
- `trend_notes_json`
- `location_context_json`
- `confidence_notes`
- `raw_model_json`
- `model_name`
- `created_at`
- `updated_at`

Array-like metadata is stored as JSON text for MVP simplicity.

### annotations

Stores designer-authored annotations.

Fields:

- `id`
- `image_id`
- `tags_json`
- `notes`
- `observations`
- `created_at`
- `updated_at`

### image_search_fts

SQLite FTS5 virtual table.

Indexed content:

- `image_id`
- `description`
- `ai_metadata_text`
- `manual_annotation_text`

The FTS index is updated when classifications or annotations are created or changed.

## 9. API Requirements

### Upload Image

`POST /api/images/upload`

Accepts multipart form data:

- `file`
- `designer`
- `capturedAt`
- `continent`
- `country`
- `city`

Returns:

- Image record.

### Classify Image

`POST /api/images/{imageId}/classify`

Runs AI classification for an uploaded image.

Returns:

- Normalized classification.
- Updated image record.

### List Images

`GET /api/images`

Query parameters:

- `q`
- `garmentType`
- `style`
- `material`
- `color`
- `pattern`
- `season`
- `occasion`
- `consumerProfile`
- `trend`
- `continent`
- `country`
- `city`
- `year`
- `month`
- `designer`

Returns:

- Matching image cards with metadata.

### Get Image Detail

`GET /api/images/{imageId}`

Returns:

- Image record.
- Classification.
- Manual annotations.

### Get Dynamic Filters

`GET /api/filters`

Returns available filter values derived from stored data.

### Create or Update Annotation

`POST /api/images/{imageId}/annotations`

Body:

- `tags`
- `notes`
- `observations`

Returns:

- Saved annotation.

## 10. AI Classification Contract

The backend should request strict JSON output from the model.

Expected output shape:

```json
{
  "description": "A concise but rich fashion-oriented description of the image.",
  "garment_type": ["dress"],
  "style": ["bohemian", "resort"],
  "material": ["linen"],
  "color_palette": ["cream", "indigo"],
  "pattern": ["embroidered"],
  "season": ["spring", "summer"],
  "occasion": ["casual", "market"],
  "consumer_profile": ["fashion-conscious traveler"],
  "trend_notes": ["artisan detailing", "natural fibers"],
  "location_context": {
    "continent": "Asia",
    "country": "Japan",
    "city": "Kyoto"
  },
  "confidence_notes": "Material and consumer profile are inferred visually and may be uncertain."
}
```

Classification assumptions:

- Material is visually inferred and may be wrong.
- Consumer profile and trend notes are interpretive.
- Location context should prefer user-provided upload context when available.
- The raw model response should be stored for auditability.

## 11. UX Requirements

### Upload View

Required:

- File picker.
- Optional context fields.
- Upload action.
- Classify action.
- Loading and error states.

### Library View

Required:

- Search bar.
- Dynamic filter controls.
- Image grid.
- Empty state.
- Error state.

### Detail View

Required:

- Large image preview.
- AI-generated section.
- Manual annotation section.
- Editable tags, notes, and observations.

AI-generated and manual content should be clearly labeled.

## 12. Evaluation Plan

The README should include a lightweight evaluation summary.

Suggested evaluation approach:

1. Select 8-12 sample garment or fashion inspiration images.
2. Create a small expected-label sheet for garment type, dominant colors, visible pattern, and search terms.
3. Run classification on each image.
4. Record qualitative and lightweight quantitative results.

Suggested evaluation dimensions:

- Garment type accuracy.
- Color palette usefulness.
- Pattern detection quality.
- Description usefulness for designers.
- Search relevance.
- Annotation search behavior.

Example summary format:

| Dimension | Result | Notes |
| --- | --- | --- |
| Garment type | 8/10 useful | Strong on common categories; weaker on layered outfits. |
| Color palette | 9/10 useful | Captures dominant colors well. |
| Pattern | 7/10 useful | Subtle textures may be missed. |
| Search relevance | 8/10 useful | FTS works well for explicit terms and annotations. |

## 13. Testing Plan

### Backend Unit Tests

Required JUnit coverage:

- Valid AI JSON parses into normalized classification metadata.
- Invalid or partial AI JSON fails gracefully or falls back safely.
- Dynamic filters are generated from stored classifications and annotations.
- Annotation text is included in the FTS index.
- Search returns images based on manual annotations.

### Manual QA

Minimum manual test flow:

1. Start backend and frontend.
2. Upload an image with context.
3. Classify the image.
4. Confirm the image appears in the grid.
5. Search for a term from the AI description.
6. Filter by garment type or color.
7. Add manual annotation.
8. Search for a term from the manual annotation.

## 14. Assumptions

- The app is a proof of concept for a single local user.
- Uploaded images are fashion or garment related.
- The backend has access to an OpenAI API key.
- SQLite FTS5 is available through the selected SQLite JDBC driver.
- Local file storage is acceptable for the assessment.
- Designer identity is stored as plain metadata rather than an authenticated user account.

## 15. Risks and Mitigations

### Risk: AI output is inconsistent

Mitigation:

- Use strict JSON schema output.
- Validate model responses before saving.
- Store raw response for debugging.
- Include confidence notes.

### Risk: FTS5 setup consumes time

Mitigation:

- Validate FTS5 support early.
- Keep the FTS index simple and flattened.
- If blocked, document a temporary `LIKE` fallback, but the target implementation remains FTS5.

### Risk: Two-service setup is heavier than a single-stack app

Mitigation:

- Keep the frontend thin.
- Keep backend APIs simple.
- Provide clear setup instructions in README.

### Risk: Model quality is hard to prove in one day

Mitigation:

- Include a small transparent evaluation set.
- Report qualitative and quantitative observations.
- Be explicit about uncertain fields.

## 16. One-Day Implementation Plan

### Phase 1: Project Setup

- Create Spring Boot backend.
- Create Next.js frontend.
- Add README skeleton and `.env.example`.
- Verify SQLite connection and FTS5 support.

### Phase 2: Upload and Storage

- Implement image upload API.
- Save files to local `uploads/`.
- Serve uploaded images as static assets.
- Persist image records.

### Phase 3: Classification

- Implement OpenAI Vision client.
- Add strict JSON schema prompt.
- Parse and validate AI response.
- Persist classification and raw response.
- Update FTS index.

### Phase 4: Search and Filters

- Implement image list endpoint.
- Implement FTS search.
- Implement dynamic filter generation.
- Support combined search and filter queries.

### Phase 5: Frontend

- Build upload UI.
- Build image grid.
- Build search and dynamic filters.
- Build image detail view.
- Build annotation editor.

### Phase 6: Tests and Documentation

- Add JUnit tests for parsing, filters, and annotation search.
- Add sample evaluation notes.
- Complete README setup and architecture documentation.
- Document limitations and next steps.

## 17. Future Improvements

- Replace local image storage with S3 or Cloudflare R2.
- Add async classification jobs and status polling.
- Add authentication and designer workspaces.
- Add embedding-based semantic search.
- Add image similarity search.
- Add correction workflow for AI metadata.
- Add batch upload.
- Add production deployment configuration.
- Add richer evaluation set with labeled images.
- Add observability for model latency, cost, and failure rates.

## 18. Definition of Done

The MVP is complete when:

- A user can upload an image.
- The backend can classify the image using an OpenAI vision model.
- AI description and structured metadata are stored.
- The image appears in a searchable visual grid.
- Filters are generated dynamically from stored data.
- A user can add manual annotations.
- Manual annotations are searchable and visibly distinct from AI output.
- JUnit tests cover core backend logic.
- README includes setup, architecture, evaluation, assumptions, limitations, and next steps.
