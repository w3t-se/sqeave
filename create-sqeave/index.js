#!/usr/bin/env node
const fs = require("fs");
const path = require("path");
const { execSync } = require("child_process");

const templateDir = path.join(__dirname, "template");
const targetDir = process.cwd();

console.log("Creating a new Sqeave project...");

// Copy all files from the template to the target directory
fs.readdirSync(templateDir).forEach((file) => {
  const sourcePath = path.join(templateDir, file);
  const targetPath = path.join(targetDir, file);

  if (fs.lstatSync(sourcePath).isDirectory()) {
    fs.mkdirSync(targetPath, { recursive: true });
    fs.cpSync(sourcePath, targetPath, { recursive: true });
  } else {
    fs.copyFileSync(sourcePath, targetPath);
  }
});

// Optionally install dependencies
console.log("Installing dependencies...");
execSync("pnpm install", { stdio: "inherit" });

console.log("Project created successfully!");
console.log("Run 'pnpx vite' to start the dev server at http://localhost:5173!");
