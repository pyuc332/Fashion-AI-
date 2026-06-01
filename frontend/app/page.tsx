"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";

type ImageRecord = {
  id: string;
  originalFilename: string;
  imageUrl: string;
  designer: string | null;
  capturedAt: string | null;
  country: string | null;
  city: string | null;
  createdAt: string;
};

type ClassificationRecord = {
  id: string;
  imageId: string;
  description: string;
  garmentTypeJson: string;
  colorPaletteJson: string;
  patternJson: string;
  modelName: string;
};

type AnnotationRecord = {
  id: string;
  imageId: string;
  tagsJson: string;
  notes: string | null;
  observations: string | null;
};

type ImageDetailResponse = {
  image: ImageRecord;
  classification: ClassificationRecord | null;
  annotation: AnnotationRecord | null;
};

type AnnotationDraft = {
  tags: string;
  notes: string;
  observations: string;
};

type FilterOptions = {
  garmentTypes: string[];
  styles: string[];
  materials: string[];
  colors: string[];
  patterns: string[];
  seasons: string[];
  occasions: string[];
  consumerProfiles: string[];
  trends: string[];
  continents: string[];
  countries: string[];
  cities: string[];
  years: string[];
  months: string[];
  designers: string[];
};

type SearchFilters = {
  q: string;
  garmentType: string;
  style: string;
  material: string;
  color: string;
  pattern: string;
  season: string;
  occasion: string;
  country: string;
  city: string;
  year: string;
  month: string;
  designer: string;
};

const apiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const emptyFilterOptions: FilterOptions = {
  garmentTypes: [],
  styles: [],
  materials: [],
  colors: [],
  patterns: [],
  seasons: [],
  occasions: [],
  consumerProfiles: [],
  trends: [],
  continents: [],
  countries: [],
  cities: [],
  years: [],
  months: [],
  designers: [],
};

const emptySearchFilters: SearchFilters = {
  q: "",
  garmentType: "",
  style: "",
  material: "",
  color: "",
  pattern: "",
  season: "",
  occasion: "",
  country: "",
  city: "",
  year: "",
  month: "",
  designer: "",
};

const emptyAnnotationDraft: AnnotationDraft = {
  tags: "",
  notes: "",
  observations: "",
};

function draftFromAnnotation(annotation: AnnotationRecord): AnnotationDraft {
  return {
    tags: parseTagsJson(annotation.tagsJson).join(", "),
    notes: annotation.notes ?? "",
    observations: annotation.observations ?? "",
  };
}

function parseTagsJson(tagsJson: string): string[] {
  try {
    const parsed = JSON.parse(tagsJson) as unknown;
    return Array.isArray(parsed)
      ? parsed.filter((tag): tag is string => typeof tag === "string")
      : [];
  } catch {
    return [];
  }
}

export default function HomePage() {
  const [images, setImages] = useState<ImageRecord[]>([]);
  const [filterOptions, setFilterOptions] =
    useState<FilterOptions>(emptyFilterOptions);
  const [filters, setFilters] = useState<SearchFilters>(emptySearchFilters);
  const [appliedFilters, setAppliedFilters] =
    useState<SearchFilters>(emptySearchFilters);
  const [classifications, setClassifications] = useState<
    Record<string, ClassificationRecord>
  >({});
  const [annotations, setAnnotations] = useState<Record<string, AnnotationRecord>>(
    {},
  );
  const [annotationDrafts, setAnnotationDrafts] = useState<
    Record<string, AnnotationDraft>
  >({});
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [designer, setDesigner] = useState("Manual Test");
  const [capturedAt, setCapturedAt] = useState("2026-06-01");
  const [country, setCountry] = useState("United States");
  const [city, setCity] = useState("Los Angeles");
  const [isLoading, setIsLoading] = useState(true);
  const [isUploading, setIsUploading] = useState(false);
  const [classifyingId, setClassifyingId] = useState<string | null>(null);
  const [savingAnnotationId, setSavingAnnotationId] = useState<string | null>(
    null,
  );
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  async function loadImages(nextFilters: SearchFilters = appliedFilters) {
    try {
      setIsLoading(true);
      setError(null);
      const params = new URLSearchParams();
      Object.entries(nextFilters).forEach(([key, value]) => {
        if (value.trim()) {
          params.set(key, value.trim());
        }
      });
      const query = params.toString();
      const response = await fetch(
        `${apiBaseUrl}/api/images${query ? `?${query}` : ""}`,
      );
      if (!response.ok) {
        throw new Error(`Backend returned ${response.status}`);
      }
      const data = (await response.json()) as ImageRecord[];
      setImages(data);
      hydrateImageDetails(data);
    } catch (loadError) {
      setError(
        loadError instanceof Error
          ? loadError.message
          : "Could not load image records",
      );
    } finally {
      setIsLoading(false);
    }
  }

  async function hydrateImageDetails(nextImages: ImageRecord[]) {
    if (nextImages.length === 0) {
      setClassifications({});
      setAnnotations({});
      setAnnotationDrafts({});
      return;
    }
    try {
      const details = await Promise.all(
        nextImages.map(async (image) => {
          const response = await fetch(`${apiBaseUrl}/api/images/${image.id}`);
          if (!response.ok) {
            throw new Error(`Detail request returned ${response.status}`);
          }
          return (await response.json()) as ImageDetailResponse;
        }),
      );
      const nextClassifications: Record<string, ClassificationRecord> = {};
      const nextAnnotations: Record<string, AnnotationRecord> = {};
      const nextDrafts: Record<string, AnnotationDraft> = {};

      details.forEach((detail) => {
        if (detail.classification) {
          nextClassifications[detail.image.id] = detail.classification;
        }
        if (detail.annotation) {
          nextAnnotations[detail.image.id] = detail.annotation;
          nextDrafts[detail.image.id] = draftFromAnnotation(detail.annotation);
        } else {
          nextDrafts[detail.image.id] = emptyAnnotationDraft;
        }
      });

      setClassifications(nextClassifications);
      setAnnotations(nextAnnotations);
      setAnnotationDrafts(nextDrafts);
    } catch (detailError) {
      setError(
        detailError instanceof Error
          ? detailError.message
          : "Could not load image details",
      );
    }
  }

  async function loadFilters() {
    try {
      const response = await fetch(`${apiBaseUrl}/api/filters`);
      if (!response.ok) {
        throw new Error(`Filter request returned ${response.status}`);
      }
      const data = (await response.json()) as FilterOptions;
      setFilterOptions(data);
    } catch (filterError) {
      setError(
        filterError instanceof Error
          ? filterError.message
          : "Could not load filters",
      );
    }
  }

  useEffect(() => {
    loadImages();
    loadFilters();
  }, []);

  function applySearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setAppliedFilters(filters);
    loadImages(filters);
  }

  function clearSearch() {
    setFilters(emptySearchFilters);
    setAppliedFilters(emptySearchFilters);
    loadImages(emptySearchFilters);
  }

  async function handleUpload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedFile) {
      setError("Choose an image before uploading.");
      return;
    }

    const formData = new FormData();
    formData.append("file", selectedFile);
    formData.append("designer", designer);
    formData.append("capturedAt", capturedAt);
    formData.append("continent", "North America");
    formData.append("country", country);
    formData.append("city", city);

    try {
      setIsUploading(true);
      setError(null);
      setNotice(null);
      const response = await fetch(`${apiBaseUrl}/api/images/upload`, {
        method: "POST",
        body: formData,
      });
      if (!response.ok) {
        throw new Error(`Upload failed with ${response.status}`);
      }
      const uploaded = (await response.json()) as ImageRecord;
      setSelectedFile(null);
      setImages((current) => [uploaded, ...current]);
      setAnnotationDrafts((current) => ({
        ...current,
        [uploaded.id]: emptyAnnotationDraft,
      }));
      loadFilters();
      setNotice("Image uploaded. You can classify it from the card.");
    } catch (uploadError) {
      setError(
        uploadError instanceof Error ? uploadError.message : "Upload failed",
      );
    } finally {
      setIsUploading(false);
    }
  }

  async function classifyImage(imageId: string, provider: "mock" | "openai") {
    try {
      setClassifyingId(imageId);
      setError(null);
      setNotice(null);
      const endpoint =
        provider === "openai"
          ? "classifications/openai"
          : "classifications/mock";
      const response = await fetch(
        `${apiBaseUrl}/api/images/${imageId}/${endpoint}`,
        { method: "POST" },
      );
      if (!response.ok) {
        throw new Error(
          provider === "openai"
            ? `OpenAI classification failed with ${response.status}. Check backend/.env and OPENAI_API_KEY.`
            : `Classification failed with ${response.status}`,
        );
      }
      const classification = (await response.json()) as ClassificationRecord;
      setClassifications((current) => ({
        ...current,
        [imageId]: classification,
      }));
      loadFilters();
      setNotice(
        provider === "openai"
          ? "OpenAI classification saved and indexed for search."
          : "Mock classification saved and indexed for search.",
      );
    } catch (classifyError) {
      setError(
        classifyError instanceof Error
          ? classifyError.message
          : "Classification failed",
      );
    } finally {
      setClassifyingId(null);
    }
  }

  async function saveAnnotation(imageId: string) {
    const draft = annotationDrafts[imageId] ?? emptyAnnotationDraft;
    try {
      setSavingAnnotationId(imageId);
      setError(null);
      setNotice(null);
      const response = await fetch(`${apiBaseUrl}/api/images/${imageId}/annotations`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          tags: draft.tags
            .split(",")
            .map((tag) => tag.trim())
            .filter(Boolean),
          notes: draft.notes,
          observations: draft.observations,
        }),
      });
      if (!response.ok) {
        throw new Error(`Annotation save failed with ${response.status}`);
      }
      const annotation = (await response.json()) as AnnotationRecord;
      setAnnotations((current) => ({
        ...current,
        [imageId]: annotation,
      }));
      setAnnotationDrafts((current) => ({
        ...current,
        [imageId]: draftFromAnnotation(annotation),
      }));
      setNotice("Designer annotation saved and added to search.");
    } catch (annotationError) {
      setError(
        annotationError instanceof Error
          ? annotationError.message
          : "Annotation save failed",
      );
    } finally {
      setSavingAnnotationId(null);
    }
  }

  const subtitle = useMemo(() => {
    if (isLoading) {
      return "Checking the backend image library...";
    }
    if (error) {
      return "The workflow is ready once the Spring Boot backend is running.";
    }
    return `${images.length} image${images.length === 1 ? "" : "s"} in the library`;
  }, [error, images.length, isLoading]);

  const activeFilterCount = useMemo(
    () =>
      Object.values(appliedFilters).filter((value) => value.trim().length > 0)
        .length,
    [appliedFilters],
  );

  return (
    <main className="min-h-screen bg-mist text-ink">
      <section className="mx-auto flex w-full max-w-6xl flex-col gap-8 px-6 py-8 md:px-8">
        <header className="flex flex-col gap-3 border-b border-ink/10 pb-6">
          <p className="text-sm font-semibold uppercase tracking-[0.18em] text-clay">
            Fashion AI Library
          </p>
          <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
            <div>
              <h1 className="text-3xl font-semibold md:text-5xl">
                Upload and classify inspiration images
              </h1>
              <p className="mt-3 max-w-2xl text-base leading-7 text-ink/70">
                Upload a garment image, save field context, and run the mock AI
                classification path before connecting the real OpenAI model.
              </p>
            </div>
            <div className="rounded border border-ink/10 bg-white px-4 py-3 text-sm text-ink/70 shadow-sm">
              Backend: <span className="font-medium text-ink">{apiBaseUrl}</span>
            </div>
          </div>
          <p className="text-sm text-ink/60">{subtitle}</p>
        </header>

        <form
          onSubmit={handleUpload}
          className="grid gap-4 rounded border border-ink/10 bg-white p-5 shadow-sm md:grid-cols-5"
        >
          <label className="flex flex-col gap-2 text-sm md:col-span-2">
            <span className="font-medium">Image</span>
            <input
              type="file"
              accept="image/*"
              onChange={(event) =>
                setSelectedFile(event.target.files?.[0] ?? null)
              }
              className="rounded border border-ink/15 px-3 py-2"
            />
          </label>
          <label className="flex flex-col gap-2 text-sm">
            <span className="font-medium">Designer</span>
            <input
              value={designer}
              onChange={(event) => setDesigner(event.target.value)}
              className="rounded border border-ink/15 px-3 py-2"
            />
          </label>
          <label className="flex flex-col gap-2 text-sm">
            <span className="font-medium">Date</span>
            <input
              type="date"
              value={capturedAt}
              onChange={(event) => setCapturedAt(event.target.value)}
              className="rounded border border-ink/15 px-3 py-2"
            />
          </label>
          <label className="flex flex-col gap-2 text-sm">
            <span className="font-medium">Country</span>
            <input
              value={country}
              onChange={(event) => setCountry(event.target.value)}
              className="rounded border border-ink/15 px-3 py-2"
            />
          </label>
          <label className="flex flex-col gap-2 text-sm">
            <span className="font-medium">City</span>
            <input
              value={city}
              onChange={(event) => setCity(event.target.value)}
              className="rounded border border-ink/15 px-3 py-2"
            />
          </label>
          <div className="flex items-end md:col-span-4">
            <button
              type="submit"
              disabled={isUploading}
              className="rounded bg-ink px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isUploading ? "Uploading..." : "Upload image"}
            </button>
          </div>
        </form>

        {notice && (
          <div className="rounded border border-green-200 bg-green-50 p-4 text-sm text-green-800">
            {notice}
          </div>
        )}

        {error && (
          <div className="rounded border border-red-200 bg-red-50 p-5 text-red-800">
            <p className="font-semibold">Workflow issue</p>
            <p className="mt-2 text-sm">{error}</p>
          </div>
        )}

        <form
          onSubmit={applySearch}
          className="rounded border border-ink/10 bg-white p-5 shadow-sm"
        >
          <div className="flex flex-col gap-3 md:flex-row md:items-end">
            <label className="flex flex-1 flex-col gap-2 text-sm">
              <span className="font-medium">Search descriptions and notes</span>
              <input
                value={filters.q}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    q: event.target.value,
                  }))
                }
                placeholder="embroidered neckline, artisan market, summer capsule"
                className="rounded border border-ink/15 px-3 py-2"
              />
            </label>
            <button
              type="submit"
              className="rounded bg-clay px-4 py-2 text-sm font-semibold text-white"
            >
              Apply search
            </button>
            <button
              type="button"
              onClick={clearSearch}
              className="rounded border border-ink/15 px-4 py-2 text-sm font-semibold"
            >
              Clear
            </button>
          </div>

          <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <FilterSelect
              label="Garment type"
              value={filters.garmentType}
              options={filterOptions.garmentTypes}
              onChange={(value) =>
                setFilters((current) => ({ ...current, garmentType: value }))
              }
            />
            <FilterSelect
              label="Style"
              value={filters.style}
              options={filterOptions.styles}
              onChange={(value) =>
                setFilters((current) => ({ ...current, style: value }))
              }
            />
            <FilterSelect
              label="Material"
              value={filters.material}
              options={filterOptions.materials}
              onChange={(value) =>
                setFilters((current) => ({ ...current, material: value }))
              }
            />
            <FilterSelect
              label="Color"
              value={filters.color}
              options={filterOptions.colors}
              onChange={(value) =>
                setFilters((current) => ({ ...current, color: value }))
              }
            />
            <FilterSelect
              label="Pattern"
              value={filters.pattern}
              options={filterOptions.patterns}
              onChange={(value) =>
                setFilters((current) => ({ ...current, pattern: value }))
              }
            />
            <FilterSelect
              label="Season"
              value={filters.season}
              options={filterOptions.seasons}
              onChange={(value) =>
                setFilters((current) => ({ ...current, season: value }))
              }
            />
            <FilterSelect
              label="Occasion"
              value={filters.occasion}
              options={filterOptions.occasions}
              onChange={(value) =>
                setFilters((current) => ({ ...current, occasion: value }))
              }
            />
            <FilterSelect
              label="Designer"
              value={filters.designer}
              options={filterOptions.designers}
              onChange={(value) =>
                setFilters((current) => ({ ...current, designer: value }))
              }
            />
            <FilterSelect
              label="Country"
              value={filters.country}
              options={filterOptions.countries}
              onChange={(value) =>
                setFilters((current) => ({ ...current, country: value }))
              }
            />
            <FilterSelect
              label="City"
              value={filters.city}
              options={filterOptions.cities}
              onChange={(value) =>
                setFilters((current) => ({ ...current, city: value }))
              }
            />
            <FilterSelect
              label="Year"
              value={filters.year}
              options={filterOptions.years}
              onChange={(value) =>
                setFilters((current) => ({ ...current, year: value }))
              }
            />
            <FilterSelect
              label="Month"
              value={filters.month}
              options={filterOptions.months}
              onChange={(value) =>
                setFilters((current) => ({ ...current, month: value }))
              }
            />
          </div>

          {activeFilterCount > 0 && (
            <p className="mt-3 text-sm text-ink/60">
              {activeFilterCount} active search/filter{" "}
              {activeFilterCount === 1 ? "condition" : "conditions"}.
            </p>
          )}
        </form>

        {isLoading && (
          <div className="rounded border border-ink/10 bg-white p-6 shadow-sm">
            <div className="h-4 w-48 animate-pulse rounded bg-ink/10" />
            <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {[0, 1, 2].map((item) => (
                <div key={item} className="h-56 animate-pulse rounded bg-ink/10" />
              ))}
            </div>
          </div>
        )}

        {!isLoading && images.length === 0 && (
          <div className="rounded border border-dashed border-ink/20 bg-white p-8 text-center shadow-sm">
            <h2 className="text-xl font-semibold">
              {activeFilterCount > 0 ? "No matching images" : "No images yet"}
            </h2>
            <p className="mx-auto mt-2 max-w-xl text-sm leading-6 text-ink/60">
              {activeFilterCount > 0
                ? "Try clearing search or choosing broader filter values."
                : "Upload your first image above. It will be saved by the Java backend and appear here immediately."}
            </p>
          </div>
        )}

        {!isLoading && images.length > 0 && (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {images.map((image) => {
              const classification = classifications[image.id];
              const annotation = annotations[image.id];
              const annotationDraft =
                annotationDrafts[image.id] ?? emptyAnnotationDraft;
              return (
                <article
                  key={image.id}
                  className="overflow-hidden rounded border border-ink/10 bg-white shadow-sm"
                >
                  <div className="aspect-[4/3] bg-ink/5">
                    <img
                      src={`${apiBaseUrl}${image.imageUrl}`}
                      alt={image.originalFilename}
                      className="h-full w-full object-cover"
                    />
                  </div>
                  <div className="space-y-3 p-4">
                    <div>
                      <h2 className="truncate text-base font-semibold">
                        {image.originalFilename}
                      </h2>
                      <p className="text-sm text-ink/60">
                        {image.designer ?? "Unknown designer"}
                      </p>
                      <p className="text-sm text-ink/60">
                        {[image.city, image.country].filter(Boolean).join(", ") ||
                          "No location"}
                      </p>
                    </div>

                    {classification ? (
                      <div className="rounded bg-mist p-3 text-sm">
                        <p className="font-semibold">
                          {classification.modelName.startsWith("mock-")
                            ? "Mock AI classification"
                            : "OpenAI classification"}
                        </p>
                        <p className="mt-1 text-xs font-medium text-clay">
                          {classification.modelName}
                        </p>
                        <p className="mt-1 line-clamp-3 text-ink/70">
                          {classification.description}
                        </p>
                        <button
                          type="button"
                          onClick={() => classifyImage(image.id, "openai")}
                          disabled={classifyingId === image.id}
                          className="mt-3 w-full rounded bg-clay px-3 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-50"
                        >
                          {classifyingId === image.id
                            ? "Classifying..."
                            : "Replace with OpenAI classification"}
                        </button>
                      </div>
                    ) : (
                      <div className="grid gap-2">
                        <button
                          type="button"
                          onClick={() => classifyImage(image.id, "openai")}
                          disabled={classifyingId === image.id}
                          className="w-full rounded bg-clay px-3 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-50"
                        >
                          {classifyingId === image.id
                            ? "Classifying..."
                            : "Run OpenAI classification"}
                        </button>
                        <button
                          type="button"
                          onClick={() => classifyImage(image.id, "mock")}
                          disabled={classifyingId === image.id}
                          className="w-full rounded border border-ink/15 px-3 py-2 text-sm font-semibold hover:bg-ink hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
                        >
                          Run mock classification
                        </button>
                      </div>
                    )}

                    <div className="space-y-3 rounded border border-ink/10 p-3">
                      <div className="flex items-center justify-between gap-3">
                        <p className="text-sm font-semibold">
                          Designer annotation
                        </p>
                        {annotation && (
                          <span className="rounded bg-clay/10 px-2 py-1 text-xs font-semibold text-clay">
                            Manual
                          </span>
                        )}
                      </div>

                      {annotation && (
                        <div className="space-y-2 rounded bg-mist p-3 text-sm text-ink/70">
                          {parseTagsJson(annotation.tagsJson).length > 0 && (
                            <div className="flex flex-wrap gap-2">
                              {parseTagsJson(annotation.tagsJson).map((tag) => (
                                <span
                                  key={tag}
                                  className="rounded-full bg-white px-2 py-1 text-xs font-medium"
                                >
                                  {tag}
                                </span>
                              ))}
                            </div>
                          )}
                          {annotation.notes && <p>{annotation.notes}</p>}
                          {annotation.observations && (
                            <p className="text-ink/60">
                              {annotation.observations}
                            </p>
                          )}
                        </div>
                      )}

                      <label className="flex flex-col gap-1 text-sm">
                        <span className="font-medium">Tags</span>
                        <input
                          value={annotationDraft.tags}
                          onChange={(event) =>
                            setAnnotationDrafts((current) => ({
                              ...current,
                              [image.id]: {
                                ...annotationDraft,
                                tags: event.target.value,
                              },
                            }))
                          }
                          placeholder="capsule reference, neckline detail"
                          className="rounded border border-ink/15 px-3 py-2"
                        />
                      </label>
                      <label className="flex flex-col gap-1 text-sm">
                        <span className="font-medium">Notes</span>
                        <textarea
                          value={annotationDraft.notes}
                          onChange={(event) =>
                            setAnnotationDrafts((current) => ({
                              ...current,
                              [image.id]: {
                                ...annotationDraft,
                                notes: event.target.value,
                              },
                            }))
                          }
                          rows={2}
                          placeholder="Interesting trim for a summer capsule."
                          className="resize-none rounded border border-ink/15 px-3 py-2"
                        />
                      </label>
                      <label className="flex flex-col gap-1 text-sm">
                        <span className="font-medium">Observations</span>
                        <textarea
                          value={annotationDraft.observations}
                          onChange={(event) =>
                            setAnnotationDrafts((current) => ({
                              ...current,
                              [image.id]: {
                                ...annotationDraft,
                                observations: event.target.value,
                              },
                            }))
                          }
                          rows={2}
                          placeholder="Adapt neckline detail for resort capsule."
                          className="resize-none rounded border border-ink/15 px-3 py-2"
                        />
                      </label>
                      <button
                        type="button"
                        onClick={() => saveAnnotation(image.id)}
                        disabled={savingAnnotationId === image.id}
                        className="w-full rounded bg-ink px-3 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        {savingAnnotationId === image.id
                          ? "Saving annotation..."
                          : "Save annotation"}
                      </button>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>
    </main>
  );
}

function FilterSelect({
  label,
  value,
  options,
  onChange,
}: {
  label: string;
  value: string;
  options: string[];
  onChange: (value: string) => void;
}) {
  return (
    <label className="flex flex-col gap-2 text-sm">
      <span className="font-medium">{label}</span>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="rounded border border-ink/15 bg-white px-3 py-2"
      >
        <option value="">Any</option>
        {options.map((option) => (
          <option key={option} value={option}>
            {option}
          </option>
        ))}
      </select>
    </label>
  );
}
