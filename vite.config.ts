import { defineConfig } from "vite";
import solid from "vite-plugin-solid";
import squint from "@w3t-ab/vite-plugin-squint";
import path from "node:path";
import tailwindcss from "tailwindcss";

export default defineConfig({
    plugins: [solid(),
	     squint({ scan: true }),],

    test: {
	globals: true,
	environment: 'jsdom',
	transformMode: {
	    web: [/\.jsx?$/],
	},
	setupFiles: ["./vitest.setup.ts"],
    },
  server: {
    watch: {
      ignored: ['!**/dist/**']
    }
  },
   css: {
    postcss: {
      plugins: [tailwindcss({})],
    },
  },
  build: {
    emptyOutDir: false,
    target: "esnext",
    minify: true,
    sourcemap: true,

    lib: {
      entry: {
        main: path.resolve("dist/main/export/index.mjs"),
        devtools: path.resolve("dist/devtools/index.mjs"),
        components: path.resolve("dist/components/index.mjs")
      },
      formats: ["es"]
    },

      rollupOptions: {
	  input: {
	      main: "dist/main/export/index.mjs",
	      devtools: "dist/devtools/index.mjs",
	      components: "dist/components/index.mjs"
	  },
      external: [
        "solid-js",
        "solid-js/web",
        "solid-js/store",
        "solid-transition-group",
        "@corvu/resizable"
      ],
      output: {
        dir: "dist/bundle",
        entryFileNames: "[name].mjs",
        chunkFileNames: "chunks/[name]-[hash].mjs"
      }
    }
  }
});
