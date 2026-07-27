#!/usr/bin/env node

const path = require('path');
const { generateFunctionalGroupDocs } = require('./ft-groups-data-gen');

const outputPath = path.join('e2e/e2e-documentation/results/ft-groups-data', 'ft-groups-ui-data.json');

const count = generateFunctionalGroupDocs({
  suiteType: 'ui',
  targetDir: 'e2e/tests/ui_tests',
  outputFile: outputPath
});

console.log(`Wrote ${count} UI functional test groups to ${outputPath}`);
