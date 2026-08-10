#!/usr/bin/env node

const path = require('path');
const { generateDocs } = require('./e2e-data-gen');

const outputPath = path.join('e2e/e2e-documentation/results/e2e-data', 'e2e-ui-data.json');

const count = generateDocs({
  suiteType: 'ui',
  targetDir: 'e2e/tests/ui_tests',
  outputFile: outputPath
});

console.log(`Wrote ${count} UI scenarios to ${outputPath}`);
