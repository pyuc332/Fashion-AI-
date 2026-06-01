import "./globals.css";

import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Fashion AI Library",
  description: "AI-assisted garment classification and inspiration search.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
