#!/usr/bin/env node

const path = require('path');
const { updateConfluencePage } = require('../support/update-page');
const { generateConfluenceTable } = require('./e2e-table-gen');

const jsonPathArg = process.argv[2];
const defaultPath = path.join(__dirname, '..', '..', 'results', 'e2e-data', 'e2e-ui-data.json');
const jsonPath = jsonPathArg ? path.resolve(process.cwd(), jsonPathArg) : defaultPath;

updateConfluencePage({ jsonPath, targetHeadingText: 'UI Tests', generateConfluenceTable });
