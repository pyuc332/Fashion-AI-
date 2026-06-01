import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./app/**/*.{ts,tsx}", "./components/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#151515",
        mist: "#f6f4ef",
        clay: "#a05d43",
      },
    },
  },
  plugins: [],
};

export default config;
