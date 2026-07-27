const fs = require('fs');
const path = require('path');

const repoRoot = path.resolve(__dirname, '..', '..', '..', '..');
const codeceptConfig = require(path.join(repoRoot, 'e2e/config.js'));
const dependentApiList = require('./dependent-api-features');
const dependentUiList = require('./dependent-ui-features');

const dependentApiFiles = new Set(
  dependentApiList.map(p => p.replace(/\\/g, '/'))
);
const dependentUiFiles = new Set(
  dependentUiList.map(p => p.replace(/\\/g, '/'))
);

const pipelineTagMap = {
  '@civil-ccd-master': ['civil-ccd-definition: master'],
  '@civil-ccd-pr': ['civil-ccd-definition: PR'],
  '@civil-ccd-nightly': ['civil-ccd-definition: nightly'],
  '@civil-ccd-smoke': ['civil-ccd-definition: master (smoke)', 'civil-ccd-definition: PR (smoke)'],
  '@civil-service-master': ['civil-service: master'],
  '@civil-service-pr': ['civil-service: PR'],
  '@civil-service-nightly': ['civil-service: nightly'],
  '@civil-service-smoke': ['civil-service: master (smoke)', 'civil-service: PR (smoke)'],
  '@civil-camunda-master': ['civil-camunda-bpmn-definition: master'],
  '@civil-camunda-pr': ['civil-camunda-bpmn-definition: PR'],
  '@civil-camunda-smoke': ['civil-camunda-bpmn-definition: master (smoke)', 'civil-camunda-bpmn-definition: PR (smoke)'],
  '@civil-wa-master': ['civil-wa-task-configuration: master'],
  '@civil-wa-pr': ['civil-wa-task-configuration: PR'],
  '@civil-wa-nightly': ['civil-wa-task-configuration: nightly'],
  '@civil-wa-smoke': ['civil-wa-task-configuration: master (smoke)', 'civil-wa-task-configuration: PR (smoke)']
};

const pipelineTagSet = new Set(Object.keys(pipelineTagMap));

const actorStepObjects = [
  'I',
  'LRspec',
  'WA',
  'api',
  'api_spec',
  'api_spec_fast',
  'api_spec_small',
  'api_spec_cui',
  'noc',
  'hearings',
  'bulks',
  'qmSteps'
];

const ignoredStepMethods = new Set([
  'getCaseId',
  'login',
  'setCaseId',
  'signOut',
  'amOnPage',
  'waitForText',
  'wait',
  'navigateToCaseDetails',
  'see',
  'grabCaseNumber',
  'navigateToTab',
  'assertHasEvents',
  'retrieveTaskDetails',
  'validateTaskInfo',
  'completeTaskByUser'
]);

function walk(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  return entries.flatMap(entry => {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      return walk(fullPath);
    }
    return [fullPath];
  });
}

function toPosix(relativePath) {
  return relativePath.split(path.sep).join('/');
}

function isSmokeFile(filePath) {
  const base = path.basename(filePath).toLowerCase();
  return base.startsWith('smoke');
}

function fileIsDependent(filePath, suiteType) {
  const relative = toPosix(path.relative(repoRoot, filePath));
  if (suiteType === 'api') {
    return dependentApiFiles.has(relative);
  }
  return dependentUiFiles.has(relative);
}

function normaliseTag(token) {
  if (!token) {
    return null;
  }
  let trimmed = token.trim();
  if (!trimmed) {
    return null;
  }
  trimmed = trimmed.replace(/[;,]+$/, '');
  if (!trimmed.startsWith('@')) {
    if (trimmed.startsWith('e2e-') || trimmed.startsWith('api-') || trimmed.startsWith('civil-') || pipelineTagSet.has(`@${trimmed}`)) {
      trimmed = `@${trimmed}`;
    }
  }
  return trimmed;
}

function splitTags(str) {
  if (!str || typeof str !== 'string') {
    return [];
  }
  return str
    .split(/[\s,]+/)
    .map(normaliseTag)
    .filter(Boolean);
}

function extractNameAndInlineTags(rawName) {
  if (typeof rawName !== 'string') {
    return { name: '', tags: [] };
  }
  const inlineTags = [];
  const cleaned = rawName.replace(/@[\w-]+/g, match => {
    inlineTags.push(match);
    return '';
  });
  return {
    name: cleaned.replace(/\s+/g, ' ').trim(),
    tags: inlineTags
  };
}

function extractHelperSteps(fn) {
  if (typeof fn !== 'function') {
    return [];
  }
  const source = fn.toString();
  const commentRanges = [];
  const commentRegex = /\/\/.*|\/\*[\s\S]*?\*\//g;
  let commentMatch;
  while ((commentMatch = commentRegex.exec(source))) {
    commentRanges.push({
      start: commentMatch.index,
      end: commentMatch.index + commentMatch[0].length,
      text: commentMatch[0]
    });
  }

  const isInComment = index =>
    commentRanges.some(range => index >= range.start && index < range.end);

  const matches = [];
  const stepsRegex = /(\w+Steps)\.([A-Za-z0-9_]+)\s*\(/g;
  let match;
  while ((match = stepsRegex.exec(source))) {
    if (!ignoredStepMethods.has(match[2]) && !isInComment(match.index)) {
      matches.push({ name: `${match[1]}.${match[2]}`, index: match.index });
    }
  }

  const actorRegex = new RegExp(`\\b(${actorStepObjects.join('|')})\\.([A-Za-z0-9_]+)\\s*\\(`, 'g');
  while ((match = actorRegex.exec(source))) {
    if (!ignoredStepMethods.has(match[2]) && !isInComment(match.index)) {
      matches.push({ name: `${match[1]}.${match[2]}`, index: match.index });
    }
  }

  commentRanges.forEach(range => {
    const actorTarget = actorStepObjects.join('|');
    const commentActorRegex = new RegExp(
      `^\\s*(?:\\/\\/\\s*|\\/\\*\\s*|\\*\\s*)?(?:await\\s+)?(${actorTarget})\\.([A-Za-z0-9_]+)\\s*\\(`
    );
    const commentStepsRegex = /^\s*(?:\/\/\s*|\/\*\s*|\*\s*)?(?:await\s+)?(\w+Steps)\.([A-Za-z0-9_]+)\s*\(/;
    const lines = range.text.split('\n');
    let offset = 0;

    lines.forEach(line => {
      let commentStep = commentActorRegex.exec(line);
      if (!commentStep) {
        commentStep = commentStepsRegex.exec(line);
      }
      if (commentStep && !ignoredStepMethods.has(commentStep[2])) {
        matches.push({
          name: `${commentStep[1]}.${commentStep[2]} (skipped)`,
          index: range.start + offset + (commentStep.index || 0)
        });
      }
      offset += line.length + 1;
    });
  });

  matches.sort((a, b) => a.index - b.index);
  const ordered = [];
  const seen = new Set();
  matches.forEach(({ name }) => {
    if (!seen.has(name)) {
      seen.add(name);
      ordered.push(name);
    }
  });
  return ordered;
}

function createChain(target) {
  const chain = {};
  const passthrough = () => chain;
  chain.tag = tags => {
    splitTags(tags).forEach(tag => {
      if (!target.tagsSet.has(tag)) {
        target.tagsSet.add(tag);
        target.tags.push(tag);
      }
    });
    return chain;
  };
  chain.retry = passthrough;
  chain.retries = passthrough;
  chain.config = passthrough;
  chain.timeout = passthrough;
  chain.workers = passthrough;
  chain.meta = passthrough;
  chain.severity = passthrough;
  return chain;
}

function collectScenarios(filePath, suiteType) {
  const absolute = path.resolve(filePath);
  const relative = toPosix(path.relative(repoRoot, absolute));
  delete require.cache[require.resolve(absolute)];

  const scenarios = [];
  let currentFeature = null;
  let beforeHookSteps = [];
  let beforeSuiteSteps = [];

  const previousGlobals = {
    Feature: global.Feature,
    Scenario: global.Scenario,
    xScenario: global.xScenario,
    Before: global.Before,
    After: global.After,
    BeforeSuite: global.BeforeSuite,
    AfterSuite: global.AfterSuite,
    Data: global.Data,
    DataTable: global.DataTable,
    inject: global.inject,
    config: global.config,
    actor: global.actor
  };

  function restoreGlobals() {
    Object.entries(previousGlobals).forEach(([key, value]) => {
      if (value === undefined) {
        delete global[key];
      } else {
        global[key] = value;
      }
    });
  }

  function registerFeature(rawName, { skip = false } = {}) {
    const { name, tags } = extractNameAndInlineTags(rawName);
    const feature = {
      name: name || rawName,
      rawName,
      tags: [],
      tagsSet: new Set(),
      skip
    };
    tags.forEach(tag => {
      if (!feature.tagsSet.has(tag)) {
        feature.tagsSet.add(tag);
        feature.tags.push(tag);
      }
    });
    currentFeature = feature;
    return createChain(feature);
  }

  function scenarioFactory({ skip = false } = {}) {
    return function defineScenario(rawName, maybeOpts, maybeFn) {
      const { name, tags } = extractNameAndInlineTags(rawName);
      let fn = maybeFn;
      if (typeof maybeOpts === 'function') {
        fn = maybeOpts;
      }
      const featureSkipped = Boolean(currentFeature && currentFeature.skip);
      const scenario = {
        suiteType,
        filePath: relative,
        rawName,
        testName: name || rawName,
        featureName: currentFeature ? currentFeature.name : null,
        tags: [],
        tagsSet: new Set(currentFeature ? currentFeature.tags : []),
        collectedSteps: extractHelperSteps(fn),
        beforeSteps: beforeHookSteps.flat(),
        beforeSuiteSteps: beforeSuiteSteps.flat(),
        skipped: skip || featureSkipped,
        featureSkipped
      };
      splitTags(tags.join(' ')).forEach(tag => scenario.tagsSet.add(tag));
      scenario.tags = Array.from(scenario.tagsSet);
      scenarios.push(scenario);
      return createChain(scenario);
    };
  }

  const Feature = rawName => registerFeature(rawName);
  Feature.only = rawName => registerFeature(rawName);
  Feature.skip = rawName => registerFeature(rawName, { skip: true });

  const Scenario = scenarioFactory();
  Scenario.only = scenarioFactory();
  Scenario.skip = scenarioFactory({ skip: true });

  function registerBeforeHook(arg1, arg2) {
    const fn = typeof arg1 === 'function' ? arg1 : arg2;
    if (typeof fn === 'function') {
      beforeHookSteps.push(extractHelperSteps(fn));
    }
  }

  function registerBeforeSuiteHook(arg1, arg2) {
    const fn = typeof arg1 === 'function' ? arg1 : arg2;
    if (typeof fn === 'function') {
      beforeSuiteSteps.push(extractHelperSteps(fn));
    }
  }

  function noop() {}
  const Data = () => ({
    Scenario,
    xScenario: Scenario
  });

  global.Feature = Feature;
  global.Scenario = Scenario;
  global.xScenario = Scenario;
  global.Before = (arg1, arg2) => registerBeforeHook(arg1, arg2);
  global.After = noop;
  global.BeforeSuite = (arg1, arg2) => registerBeforeSuiteHook(arg1, arg2);
  global.AfterSuite = noop;
  global.Data = Data;
  global.DataTable = () => ({ Scenario });
  global.inject = () => ({ });
  global.config = codeceptConfig;
  global.actor = () => ({ });

  try {
    require(absolute);
  } catch (error) {
    restoreGlobals();
    throw error;
  }
  restoreGlobals();
  delete require.cache[require.resolve(absolute)];

  return scenarios;
}

function isFunctionalTag(tag) {
  if (!tag) {
    return false;
  }
  if (pipelineTagSet.has(tag)) {
    return false;
  }
  return tag.startsWith('@ui-') || tag.startsWith('@api-');
}

module.exports = {
  repoRoot,
  codeceptConfig,
  dependentApiFiles,
  dependentUiFiles,
  pipelineTagMap,
  pipelineTagSet,
  actorStepObjects,
  ignoredStepMethods,
  walk,
  toPosix,
  isSmokeFile,
  fileIsDependent,
  normaliseTag,
  splitTags,
  extractNameAndInlineTags,
  extractHelperSteps,
  createChain,
  collectScenarios,
  isFunctionalTag
};
