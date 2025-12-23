import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    include: ["dist/src/test/w3t-ab/sqeave/*.mjs"],
    environment: "jsdom",
  },
});