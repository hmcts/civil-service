#!/usr/bin/env node

const path = require('path');
const { generateFunctionalGroupDocs } = require('./ft-groups-data-gen');

const outputPath = path.join('e2e/e2e-documentation/results/ft-groups-data', 'ft-groups-api-data.json');

const count = generateFunctionalGroupDocs({
  suiteType: 'api',
  targetDir: 'e2e/tests/api_tests',
  outputFile: outputPath
});

console.log(`Wrote ${count} API functional test groups to ${outputPath}`);
