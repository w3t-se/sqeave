#!/usr/bin/env node
const fs = require("fs");
const path = require("path");
const { execSync } = require("child_process");

// Parse arguments
const args = process.argv.slice(2);
const dirIndex = args.indexOf("--dir");
const folderName = dirIndex !== -1 && args[dirIndex + 1] ? args[dirIndex + 1] : "sqeave-app";

// Resolve target directory
const templateDir = path.join(__dirname, "template");
const targetDir = path.resolve(process.cwd(), folderName);

console.log(`Creating a new Sqeave project in '${folderName}'...`);

// Create the target folder if it doesn't exist
if (!fs.existsSync(targetDir)) {
  fs.mkdirSync(targetDir, { recursive: true });
}

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

// Change working directory to the target directory
process.chdir(targetDir);

// Optionally install dependencies
console.log("Installing dependencies...");
execSync("pnpm install", { stdio: "inherit" });

console.log("Project created successfully!");
console.log("Run 'pnpx vite' to start the dev server at http://localhost:5173!");
