require('dotenv').config({path: '.env.tests.local'});
const { testFilesHelper } = require('./e2e/plugins/failedAndNotExecutedTestFilesPlugin');

const functional = process.env.FUNCTIONAL;

const getTests = () => {
  let prevFailedTestFiles = process.env.PREV_FAILED_TEST_FILES;
  let prevNotExecutedTestFiles = process.env.PREV_NOT_EXECUTED_TEST_FILES;

  if (prevFailedTestFiles !== undefined || prevNotExecutedTestFiles !== undefined) {
    prevFailedTestFiles = prevFailedTestFiles ? prevFailedTestFiles.split(',') : [];
    prevNotExecutedTestFiles = prevNotExecutedTestFiles ? prevNotExecutedTestFiles.split(',') : [];
    return [...prevFailedTestFiles, ...prevNotExecutedTestFiles];
  }
  return [
    './e2e/tests/api_tests/{*,**/*}_test.js',
    './e2e/tests/ui_tests/{*,**/*}_test.js',
  ];
};

exports.config = {
  bootstrapAll: async () => {
    if (functional) {
      await testFilesHelper.createTempFailedTestsFile();
      await testFilesHelper.createTempPassedTestsFile();
      await testFilesHelper.createTempToBeExecutedTestsFile();
    }
  },
  teardownAll: async () => {
    if (functional) {
      await testFilesHelper.createTestFilesReport();
      await testFilesHelper.deleteTempFailedTestsFile();
      await testFilesHelper.deleteTempPassedTestsFile();
      await testFilesHelper.deleteTempToBeExecutedTestFiles();
    }
  },
  tests: getTests(),
  output: process.env.REPORT_DIR || 'test-results/functional',

  helpers: {
    Playwright: {
      url: process.env.URL || 'http://localhost:3333',
      show: process.env.SHOW_BROWSER_WINDOW === 'true' || false,
      waitForTimeout: parseInt(process.env.WAIT_FOR_TIMEOUT_MS || 60000), // 60 seconds (reduced from 90 seconds)
      windowSize: '1440x960',
      browser: 'chromium',
      timeout: 20000,
      waitForAction: 500,
      bypassCSP: true,
      ignoreHTTPSErrors: true,
      video: true,
      contextOptions: {
        recordVideo: {
          dir: 'failed-videos',
        },
      },
    },
    BrowserHelpers: {
      require: './e2e/helpers/browser_helper.js',
    },
    PlaywrightHelper: {
      require: "./e2e/helpers/PlaywrightHelper.js",
    },
    GenerateReportHelper: {
      require: './e2e/helpers/generate_report_helper.js',
    },
  },
  include: {
    I: './e2e/steps_file.js',
    LRspec: './e2e/steps_file_LRspec.js',
    WA: './e2e/steps_file_WA.js',
    api: './e2e/api/steps.js',
    api_spec: './e2e/api/steps_LRspec.js',
    api_spec_fast: './e2e/api/steps_LRspecFast.js',
    api_spec_small: './e2e/api/steps_LRspecSmall.js',
    api_spec_cui: './e2e/api/steps_LRspecCui.js',
    api_ga: './e2e/api/steps_ga.js',
    noc: './e2e/api/steps_noc.js',
    hearings: './e2e/api/steps_hearings.js',
    bulks: './e2e/api/steps_Bulk.js',
    qmSteps: './e2e/api/steps_qm.js',
  },
  plugins: {
    autoDelay: {
      enabled: true,
      methods: ['click', 'fillField', 'checkOption', 'selectOption', 'attach'],
    },
    retryFailedStep: {
      enabled: true,
    },
    screenshotOnFail: {
      enabled: true,
      fullPageScreenshots: true,
    },
    failedAndNotExecutedTestFilesPlugin: {
      enabled: functional,
      require: './e2e/plugins/failedAndNotExecutedTestFilesPlugin',
    },
    allure: {
      enabled: true,
      require: "allure-codeceptjs",
      resultsDir: "test-results/functional/allure-results",
    },
  },
  mocha: {
    bail: true,
    reporterOptions: {
      'codeceptjs-cli-reporter': {
        stdout: '-',
        options: {
          steps: false,
        },
      },
      'mocha-junit-reporter': {
        stdout: '-',
        options: {
          mochaFile: process.env.REPORT_FILE || 'test-results/functional/result.xml',
        },
      },
      mochawesome: {
        stdout: '-',
        options: {
          reportDir: process.env.REPORT_DIR || 'test-results/functional',
          reportFilename: `${process.env.MOCHAWESOME_REPORTFILENAME + '-' + new Date().getTime()}`,
          inlineAssets: true,
          overwrite: false,
          json: false,
        },
      },
    },
  },
};