#!/usr/bin/env node

const path = require('path');
const { updateReadmeSection } = require('../support/update-readme');
const { generateMarkdownTable } = require('./ft-groups-table-gen');

const repoRoot = path.resolve(__dirname, '..', '..', '..', '..');
const defaultJsonPath = path.join(repoRoot, 'e2e/e2e-documentation/results/ft-groups-data/ft-groups-api-data.json');
const markers = {
  start: '<!-- FT_GROUPS_API_TABLE_START -->',
  end: '<!-- FT_GROUPS_API_TABLE_END -->'
};

const jsonArg = process.argv[2];

updateReadmeSection({
  jsonPath: jsonArg,
  defaultJsonPath,
  startMarker: markers.start,
  endMarker: markers.end,
  generateMarkdownTable
});

console.log('README.md updated with latest functional test group API table');
