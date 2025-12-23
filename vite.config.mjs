// vite.config.ts
import { defineConfig } from "vite";
import solid from "vite-plugin-solid";

export default defineConfig({
  plugins: [solid()],
  build: {
  target: 'esnext',
  polyfillDynamicImport: false,
  },
  test: {
  globals: true,
  environment: 'jsdom',
  transformMode: {
  web: [/\.jsx?$/],
  },
  setupFiles: ["./vitest.setup.ts"],
  // solid needs to be inline to work around
  // a resolution issue in vitest
  // And solid-testing-library needs to be here so that the 'hydrate'
  // method will be provided
  deps: {
  inline: [/solid-js/, /solid-testing-library/],
  },
  },
  resolve: {
  conditions: ['development', 'browser'],
},
});
