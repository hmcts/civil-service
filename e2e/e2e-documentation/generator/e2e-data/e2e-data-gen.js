#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const {
  repoRoot,
  pipelineTagMap,
  walk,
  fileIsDependent,
  collectScenarios,
  isFunctionalTag
} = require('../support/data-gen-utils');

function boolToYesNo(value) {
  return value ? 'yes' : 'no';
}

function formatDisplayPath(posixPath) {
  return posixPath.split('/').join(' -> ');
}

function deriveTagMetadata(tags) {
  const pipelines = new Set();
  const functionalTags = [];
  const functionalGroups = new Set();

  tags.forEach(tag => {
    if (pipelineTagMap[tag]) {
      pipelineTagMap[tag].forEach(p => pipelines.add(p));
    } else if (isFunctionalTag(tag)) {
      functionalTags.push(tag);
      const match = /^@(ui|api)-(.+)$/.exec(tag);
      if (match) {
        const [, tagType, rawGroup] = match;
        const labelPrefix = tagType === 'ui' ? 'pr_ft_ui-' : 'pr_ft_api-';
        functionalGroups.add(`${labelPrefix}${rawGroup}`);
      }
    }
  });

  return {
    tags,
    pipelines: Array.from(pipelines),
    functionalTestGroupTags: functionalTags,
    functionalTestGroups: Array.from(functionalGroups)
  };
}

function formatDependentFeature(scenarios) {
  if (!scenarios.length) {
    return null;
  }
  const tags = Array.from(new Set(scenarios.flatMap(s => s.tags)));
  const tagMeta = deriveTagMetadata(tags);
  const featureName = scenarios[0].featureName;
  const filePath = scenarios[0].filePath;
  const displayPath = formatDisplayPath(filePath);
  const beforeSuite = scenarios.find(s => (s.beforeSuiteSteps || []).length)?.beforeSuiteSteps || [];
  const flattenedSteps = [...beforeSuite];
  const featureSkipped = scenarios.some(s => s.featureSkipped);
  const decorateStep = (step, scenarioSkipped) => {
    if (!scenarioSkipped) {
      return step;
    }
    return step.endsWith(' (skipped)') ? step : `${step} (skipped)`;
  };

  scenarios.forEach(scenario => {
    const steps = [
      ...(scenario.beforeSteps || []),
      ...(scenario.collectedSteps || [])
    ];
    steps.forEach(step => {
      flattenedSteps.push(decorateStep(step, scenario.skipped));
    });
  });
  const dependentSkipped = featureSkipped || scenarios.every(s => s.skipped);

  return {
    testName: featureName || path.basename(filePath),
    featureName,
    filePath: displayPath,
    independentScenario: boolToYesNo(false),
    ...tagMeta,
    steps: flattenedSteps,
    skipped: boolToYesNo(dependentSkipped)
  };
}

function formatIndependentScenario(scenario) {
  const tags = Array.from(scenario.tagsSet || []);
  const tagMeta = deriveTagMetadata(tags);
  const steps = [
    ...(scenario.beforeSteps || []),
    ...(scenario.collectedSteps || [])
  ];
  const decoratedSteps = steps.map(step => {
    if (!scenario.skipped) {
      return step;
    }
    return step.endsWith(' (skipped)') ? step : `${step} (skipped)`;
  });
  return {
    testName: scenario.testName,
    featureName: scenario.featureName,
    filePath: formatDisplayPath(scenario.filePath),
    independentScenario: boolToYesNo(true),
    ...tagMeta,
    steps: decoratedSteps,
    skipped: boolToYesNo(Boolean(scenario.skipped))
  };
}

function generateDocs({ suiteType, targetDir, outputFile }) {
  const absoluteDir = path.join(repoRoot, targetDir);
  const files = walk(absoluteDir).filter(
    file => /_tests?\.js$/i.test(file)
  );
  const results = [];

  files.forEach(file => {
    const scenarios = collectScenarios(file, suiteType);
    if (fileIsDependent(file, suiteType)) {
      const record = formatDependentFeature(scenarios);
      if (record) {
        results.push(record);
      }
    } else {
      scenarios.forEach(s => {
        const record = formatIndependentScenario(s);
        results.push(record);
      });
    }
  });

  results.sort((a, b) => {
    if (a.filePath !== b.filePath) {
      return a.filePath.localeCompare(b.filePath);
    }
    return a.testName.localeCompare(b.testName);
  });

  const outputPath = path.join(repoRoot, outputFile);
  fs.mkdirSync(path.dirname(outputPath), { recursive: true });
  fs.writeFileSync(outputPath, JSON.stringify(results, null, 2));
  return results.length;
}

module.exports = {
  generateDocs
};
