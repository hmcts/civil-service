const fs = require('fs/promises');
const event = require('codeceptjs').event;
const lockfile = require('proper-lockfile');

const tempFailedTestsPath = 'test-results/functional/tempFailedTests.json';
const tempPassedTestsPath = 'test-results/functional/tempPassedTests.json';
const tempToBeExecutedTestFilesPath = 'test-results/functional/tempToBeExecutedTestFiles.json';
const testFilesReportPath = 'test-results/functional/testFilesReport.json';

function normaliseFilePath(filePath) {
  const index = filePath.indexOf('/e2e/');
  if (index !== -1) {
    return `.${filePath.substring(index)}`;
  } else {
    return filePath;
  }
}

async function writeToBeExecutedTestFiles(testFiles) {
  const toBeExecutedTestFiles = JSON.parse(await fs.readFile(tempToBeExecutedTestFilesPath, 'utf-8'));
  if (toBeExecutedTestFiles.length !== testFiles.length) {
    const normalisedTestFiles = testFiles.map((testFile) => normaliseFilePath(testFile));
    await fs.writeFile(tempToBeExecutedTestFilesPath, JSON.stringify(normalisedTestFiles, null, 2));
  }
}

async function addTestToFile(testToAdd, testsPath) {
  const release = await lockfile.lock(testsPath, { retries: { retries: 10, factor: 1.5 } });
  try {
    const normalisedTestFile = normaliseFilePath(testToAdd.file);
    const tests = JSON.parse(await fs.readFile(testsPath, 'utf-8'));

    const exists = tests.some(
      (test) => test.testId === testToAdd.uuid && test.testFile === normalisedTestFile
    );
    if (!exists) {
      tests.push({ testId: testToAdd.uuid, testFile: normalisedTestFile });
    }

    await fs.writeFile(testsPath, JSON.stringify(tests, null, 2));
  } finally {
    await release();
  }
}


async function removeTestFromFile(testToRemove, testsPath) {
  const release = await lockfile.lock(testsPath, { retries: { retries: 10, factor: 1.5 } });
  try {
    const normalisedTestFile = normaliseFilePath(testToRemove.file);
    let tests = JSON.parse(await fs.readFile(testsPath, 'utf-8'));

    tests = tests.filter(
      (passedTest) => !(passedTest.testId === testToRemove.uuid && passedTest.testFile === normalisedTestFile)
    );

    await fs.writeFile(testsPath, JSON.stringify(tests, null, 2));
  } finally {
    await release();
  }
}

module.exports = function () {
  event.dispatcher.on(event.all.before, async function (result) {
    await writeToBeExecutedTestFiles(result.testFiles);
  });

  event.dispatcher.on(event.test.failed, async function (test) {
    // await removeTestFromFile(test, tempPassedTestsPath);
    await addTestToFile(test, tempFailedTestsPath);
  });

  event.dispatcher.on(event.test.passed, async function (test) {
    await removeTestFromFile(test, tempFailedTestsPath);
    await addTestToFile(test, tempPassedTestsPath);
  });
};

module.exports.testFilesHelper = {
  createTempFailedTestsFile: async () => {
    await fs.writeFile(tempFailedTestsPath, JSON.stringify([], null, 2));
  },
  createTempPassedTestsFile: async () => {
    await fs.writeFile(tempPassedTestsPath, JSON.stringify([], null, 2));
  },
  createTempToBeExecutedTestsFile: async () => {
    await fs.writeFile(tempToBeExecutedTestFilesPath, JSON.stringify([], null, 2));
  },
  createTestFilesReport: async () => {
    const failedTests = JSON.parse(await fs.readFile(tempFailedTestsPath, 'utf-8'));
    const passedTests = JSON.parse(await fs.readFile(tempPassedTestsPath, 'utf-8'));
    const toBeExecutedTestFiles = JSON.parse(await fs.readFile(tempToBeExecutedTestFilesPath, 'utf-8'));

    const failedTestFiles = [...new Set(failedTests.map((failedTest) => failedTest.testFile))];

    let passedTestFiles = [...new Set(passedTests.map((passedTest) => passedTest.testFile))];
    passedTestFiles = passedTestFiles.filter((passedTestFile) => !failedTestFiles.includes(passedTestFile));

    const executedTestFiles = new Set([...failedTestFiles, ...passedTestFiles]);
    const notExecutedTestFiles = toBeExecutedTestFiles.filter(
      (toBeExecutedTestFile) => !executedTestFiles.has(toBeExecutedTestFile)
    );

    const testFilesReport = {
      failedTestFiles,
      passedTestFiles,
      notExecutedTestFiles,
      createdAt: new Date().toISOString(),
      gitCommitId: process.env.GIT_COMMIT ?? null,
      ftGroups: process.env.PR_FT_GROUPS?.split(',') ?? null,
    };

    await fs.writeFile(testFilesReportPath, JSON.stringify(testFilesReport, null, 2));
  },
  deleteTempFailedTestsFile: async () => {
    await fs.unlink(tempFailedTestsPath);
  },
  deleteTempPassedTestsFile: async () => {
    await fs.unlink(tempPassedTestsPath);
  },
  deleteTempToBeExecutedTestFiles: async () => {
    await fs.unlink(tempToBeExecutedTestFilesPath);
  },
};
