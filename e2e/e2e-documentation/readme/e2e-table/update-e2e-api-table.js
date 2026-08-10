#!/usr/bin/env node

const path = require('path');
const { updateReadmeSection } = require('../support/update-readme');
const { generateMarkdownTable } = require('./e2e-table-gen');

const repoRoot = path.resolve(__dirname, '..', '..', '..', '..');
const defaultJsonPath = path.join(repoRoot, 'e2e/e2e-documentation/results/e2e-data/e2e-api-data.json');
const markers = {
  start: '<!-- API_TESTS_TABLE_START -->',
  end: '<!-- API_TESTS_TABLE_END -->'
};

const jsonArg = process.argv[2];

updateReadmeSection({
  jsonPath: jsonArg,
  defaultJsonPath,
  startMarker: markers.start,
  endMarker: markers.end,
  generateMarkdownTable
});

console.log('README.md updated with latest API test table');
