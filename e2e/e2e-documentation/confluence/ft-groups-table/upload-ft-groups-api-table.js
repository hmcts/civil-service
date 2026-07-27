#!/usr/bin/env node

const path = require('path');
const { updateConfluencePage } = require('../support/update-page');
const { generateConfluenceTable } = require('./ft-groups-table-gen');

const jsonPathArg = process.argv[2];
const defaultPath = path.join(__dirname, '..', '..', 'results', 'ft-groups-data', 'ft-groups-api-data.json');
const jsonPath = jsonPathArg ? path.resolve(process.cwd(), jsonPathArg) : defaultPath;

updateConfluencePage({ jsonPath, targetHeadingText: 'API Functional Test Groups', generateConfluenceTable });
