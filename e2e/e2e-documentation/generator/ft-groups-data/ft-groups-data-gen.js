#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const {
  repoRoot,
  walk,
  toPosix,
  fileIsDependent,
  collectScenarios,
  isFunctionalTag
} = require('../support/data-gen-utils');

const functionalGroupDisplayNames = {
  'bulk-claim': 'Bulk Claim',
  'case-flags': 'Case Flags',
  'case-offline': 'Case Offline',
  'case-progression': 'Case Progression',
  'cos': 'Certificate of Service',
  'discontinue-claim': 'Discontinue Claim',
  'dj': 'Default Judgment',
  'drh': 'Dispute Resolution Hearing',
  'flight-delay': 'Flight Delay',
  'hearings': 'Hearings',
  'intermediate-track': 'Intermediate Track',
  'jo': 'Judgment Online',
  'mci': 'Manage Contact Information',
  'mediation': 'Mediation',
  'multi-track': 'Multi Track',
  'nihl': 'Noise Induced Hearing Loss',
  'noc': 'Notice of Change',
  'not-suitable-sdo': 'Not Suitable For SDO',
  'qm': 'Query Management',
  'rfr': 'Request For Reconsideration',
  'settle-claim': 'Settle Claim',
  'spec-counter-claim': 'Specified Counter Claim',
  'spec-full-admit': 'Specified Full Admit',
  'spec-full-defence': 'Specified Full Defence',
  'spec-part-admit': 'Specified Part Admit',
  'stay-case': 'Stay Case',
  'unspec-full-defence': 'Unspecified Full Defence',
  'wa-task': 'WA Task'
};


function formatDependentFeature(scenarios) {
  if (!scenarios.length) {
    return null;
  }
  const tags = Array.from(new Set(scenarios.flatMap(s => s.tags)));
  const featureName = scenarios[0].featureName;
  const filePath = scenarios[0].filePath;
  const beforeSuite = scenarios.find(s => (s.beforeSuiteSteps || []).length)?.beforeSuiteSteps || [];
  const flattenedSteps = [...beforeSuite];

  scenarios.forEach(scenario => {
    const steps = [
      ...(scenario.beforeSteps || []),
      ...(scenario.collectedSteps || [])
    ];
    steps.forEach(step => {
      flattenedSteps.push(step);
    });
  });

  return {
    testName: featureName || path.basename(filePath),
    filePath,
    tags,
    steps: flattenedSteps
  };
}

function formatIndependentScenario(scenario) {
  const tags = Array.from(scenario.tagsSet || []);
  const steps = [
    ...(scenario.beforeSteps || []),
    ...(scenario.collectedSteps || [])
  ];
  return {
    testName: scenario.testName,
    filePath: scenario.filePath,
    tags,
    steps
  };
}

function buildTestRecords({ suiteType, targetDir }) {
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
        results.push(formatIndependentScenario(s));
      });
    }
  });

  results.sort((a, b) => {
    if (a.filePath !== b.filePath) {
      return a.filePath.localeCompare(b.filePath);
    }
    return a.testName.localeCompare(b.testName);
  });

  return results;
}

function buildGroupPath(tagType, filePath) {
  const baseDir = tagType === 'ui' ? 'e2e/tests/ui_tests' : 'e2e/tests/api_tests';
  const posixPath = toPosix(filePath);
  if (posixPath.startsWith(`${baseDir}/`)) {
    const remainder = posixPath.slice(baseDir.length + 1);
    const firstSegment = remainder.split('/')[0];
    if (firstSegment) {
      return `${baseDir}/${firstSegment}`;
    }
  }
  return baseDir;
}

function addOrderedUnique(target, values) {
  const seen = new Set(target);
  values.forEach(value => {
    if (!seen.has(value)) {
      seen.add(value);
      target.push(value);
    }
  });
}

function deriveGroupsFromTests(testRecords) {
  const groups = new Map();

  testRecords.forEach(record => {
    const functionalTags = (record.tags || []).filter(isFunctionalTag);
    functionalTags.forEach(tag => {
      const match = /^@(ui|api)-(.+)$/.exec(tag);
      if (!match) {
        return;
      }
      const [, tagType, groupName] = match;
      const groupPath = buildGroupPath(tagType, record.filePath);
      const pipeline = [
        'civil-ccd-definition: PR',
        'civil-service: PR',
        'civil-camunda-bpmn-definition: PR',
        'civil-wa-task-configuration: PR',
      ];
      const isGaFolder = groupPath.endsWith('/ga');
      const filteredPipeline = isGaFolder
        ? pipeline.filter(p => ![
          'civil-camunda-bpmn-definition: PR',
          'civil-wa-task-configuration: PR',
        ].includes(p))
        : pipeline;
      const key = tag;
      if (!groups.has(key)) {
        groups.set(key, {
          functionalTestGroup: functionalGroupDisplayNames[groupName] || groupName,
          tag,
          githubLabelName: tagType === 'ui'
            ? `pr_ft_ui-${groupName}`
            : `pr_ft_api-${groupName}`,
          pipeline: filteredPipeline,
          folderPaths: new Set(),
          steps: [],
          testNames: []
        });
      }
      const entry = groups.get(key);
      entry.folderPaths.add(groupPath);
      addOrderedUnique(entry.steps, record.steps || []);
      addOrderedUnique(entry.testNames, [record.testName]);
    });
  });

  return Array.from(groups.values())
    .map(group => ({
      functionalTestGroup: group.functionalTestGroup,
      folderPath: Array.from(group.folderPaths).sort().join(', '),
      githubLabelName: group.githubLabelName,
      pipeline: group.pipeline,
      tag: group.tag,
      steps: group.steps,
      testNames: group.testNames
    }))
    .sort((a, b) => {
      if (a.functionalTestGroup !== b.functionalTestGroup) {
        return a.functionalTestGroup.localeCompare(b.functionalTestGroup);
      }
      return a.tag.localeCompare(b.tag);
    });
}

function generateFunctionalGroupDocs({ suiteType, targetDir, outputFile }) {
  if (!suiteType || !targetDir) {
    throw new Error('suiteType and targetDir are required');
  }
  const tests = buildTestRecords({ suiteType, targetDir });
  const groups = deriveGroupsFromTests(tests);

  const outputPath = path.join(repoRoot, outputFile);
  fs.mkdirSync(path.dirname(outputPath), { recursive: true });
  fs.writeFileSync(outputPath, JSON.stringify(groups, null, 2));
  return groups.length;
}

module.exports = {
  generateFunctionalGroupDocs
};
