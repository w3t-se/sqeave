import { defineConfig } from "vitest/config";
import solid from "vite-plugin-solid";

export default defineConfig({
  plugins: [solid()],
  test: {
    include: ["dist/test/{*.{test,spec}.mjs,*.{test,spec}.jsx}"],
    environment: "jsdom",
      resolve: {
    conditions: ["development", "browser"],
  },
  },
});