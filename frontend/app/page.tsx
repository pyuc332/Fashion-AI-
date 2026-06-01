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

const apiBaseUrl =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export default function HomePage() {
  const [images, setImages] = useState<ImageRecord[]>([]);
  const [classifications, setClassifications] = useState<
    Record<string, ClassificationRecord>
  >({});
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [designer, setDesigner] = useState("Manual Test");
  const [capturedAt, setCapturedAt] = useState("2026-06-01");
  const [country, setCountry] = useState("United States");
  const [city, setCity] = useState("Los Angeles");
  const [isLoading, setIsLoading] = useState(true);
  const [isUploading, setIsUploading] = useState(false);
  const [classifyingId, setClassifyingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  async function loadImages() {
    try {
      setIsLoading(true);
      setError(null);
      const response = await fetch(`${apiBaseUrl}/api/images`);
      if (!response.ok) {
        throw new Error(`Backend returned ${response.status}`);
      }
      const data = (await response.json()) as ImageRecord[];
      setImages(data);
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

  useEffect(() => {
    loadImages();
  }, []);

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
      setNotice("Image uploaded. You can classify it from the card.");
    } catch (uploadError) {
      setError(
        uploadError instanceof Error ? uploadError.message : "Upload failed",
      );
    } finally {
      setIsUploading(false);
    }
  }

  async function classifyImage(imageId: string) {
    try {
      setClassifyingId(imageId);
      setError(null);
      setNotice(null);
      const response = await fetch(
        `${apiBaseUrl}/api/images/${imageId}/classifications/mock`,
        { method: "POST" },
      );
      if (!response.ok) {
        throw new Error(`Classification failed with ${response.status}`);
      }
      const classification = (await response.json()) as ClassificationRecord;
      setClassifications((current) => ({
        ...current,
        [imageId]: classification,
      }));
      setNotice("Mock classification saved and indexed for search.");
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

  const subtitle = useMemo(() => {
    if (isLoading) {
      return "Checking the backend image library...";
    }
    if (error) {
      return "The workflow is ready once the Spring Boot backend is running.";
    }
    return `${images.length} image${images.length === 1 ? "" : "s"} in the library`;
  }, [error, images.length, isLoading]);

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
            <h2 className="text-xl font-semibold">No images yet</h2>
            <p className="mx-auto mt-2 max-w-xl text-sm leading-6 text-ink/60">
              Upload your first image above. It will be saved by the Java
              backend and appear here immediately.
            </p>
          </div>
        )}

        {!isLoading && images.length > 0 && (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {images.map((image) => {
              const classification = classifications[image.id];
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
                        <p className="font-semibold">Mock AI classification</p>
                        <p className="mt-1 line-clamp-3 text-ink/70">
                          {classification.description}
                        </p>
                      </div>
                    ) : (
                      <button
                        type="button"
                        onClick={() => classifyImage(image.id)}
                        disabled={classifyingId === image.id}
                        className="w-full rounded border border-ink/15 px-3 py-2 text-sm font-semibold hover:bg-ink hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        {classifyingId === image.id
                          ? "Classifying..."
                          : "Run mock classification"}
                      </button>
                    )}
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
