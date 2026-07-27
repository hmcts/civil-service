const fs = require('fs');
const path = require('path');

const repoRoot = path.resolve(__dirname, '..', '..', '..', '..');
const README_PATH = path.join(repoRoot, 'README.md');

function replaceSection(content, startMarker, endMarker, replacement) {
  const startIndex = content.indexOf(startMarker);
  const endIndex = content.indexOf(endMarker);
  if (startIndex === -1 || endIndex === -1 || endIndex < startIndex) {
    throw new Error(`Markers ${startMarker} / ${endMarker} not found in README.md`);
  }
  const before = content.slice(0, startIndex + startMarker.length);
  const after = content.slice(endIndex);
  return `${before}\n\n${replacement}\n\n${after}`;
}

function updateReadmeSection({ jsonPath, defaultJsonPath, startMarker, endMarker, generateMarkdownTable }) {
  if (typeof generateMarkdownTable !== 'function') {
    throw new Error('generateMarkdownTable function is required');
  }
  const resolvedJsonPath = jsonPath
    ? path.resolve(process.cwd(), jsonPath)
    : defaultJsonPath;
  const table = generateMarkdownTable(resolvedJsonPath);
  let readme = fs.readFileSync(README_PATH, 'utf8');
  readme = replaceSection(readme, startMarker, endMarker, table);
  fs.writeFileSync(README_PATH, readme);
}

module.exports = { updateReadmeSection };
