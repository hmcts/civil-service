const { testFilesHelper } = require('./e2e/plugins/failedAndNotExecutedTestFilesPlugin');

const functional = process.env.FUNCTIONAL;

const ccdPipelineTests = [
      './e2e/tests/ui_tests/*.js',
      './e2e/tests/ui_tests/damages/*_test.js',
      './e2e/tests/ui_tests/lrspec/*_test.js',
      './e2e/tests/ui_tests/damages/nightly/*_test.js',
      './e2e/tests/ui_tests/noticeofchange/*_test.js',
      './e2e/tests/ui_tests/manageContactInformation/*_test.js',
      './e2e/tests/ui_tests/settle_discontinue/*_test.js',
      './e2e/tests/ui_tests/sdo/*_test.js',
      './e2e/tests/ui_tests/carm/*_test.js',
      './e2e/tests/ui_tests/minti/*_test.js',
      './e2e/tests/ui_tests/refunds/*_test.js',
      './e2e/tests/ui_tests/default_judgement/*_test.js',
      './e2e/tests/ui_tests/JudgmentOnline/*_test.js',
      './e2e/tests/ui_tests/hearings/*_test.js',
      './e2e/tests/ui_tests/query_management/*_test.js',
      './e2e/tests/api_tests/lrspec_cui/*_test.js',
    ];

const civilServiceAndCamundaTests = [
  './e2e/tests/api_tests/*.js',
  './e2e/tests/api_tests/judgmentOnline/*_test.js',
  './e2e/tests/api_tests/mediation/*_test.js',
  './e2e/tests/api_tests/sdo_R2/*_test.js',
  './e2e/tests/api_tests/generalapplication/*_test.js',
  './e2e/tests/api_tests/defaultJudgments/*_test.js',
  './e2e/tests/api_tests/damages/*_test.js',
  './e2e/tests/api_tests/sdo/*_test.js',
  './e2e/tests/api_tests/hearings/*_test.js',
  './e2e/tests/api_tests/bulkclaim/*_test.js',
  './e2e/tests/api_tests/lrspec/*_test.js',
  './e2e/tests/api_tests/lrspec_cui/*_test.js',
  './e2e/tests/api_tests/settle-discontinue/*_test.js',
  './e2e/tests/api_tests/multiIntermediateTrack/*_test.js',
  './e2e/tests/api_tests/settle-discontinue/*_test.js',
  './e2e/tests/api_tests/automated_hearing_notice/*_test.js',
  './e2e/tests/api_tests/caseworkerEvents/*_test.js',
];

const getTests = () => {
  let prevFailedTestFiles = process.env.PREV_FAILED_TEST_FILES;
  let prevNotExecutedTestFiles = process.env.PREV_NOT_EXECUTED_TEST_FILES;

  if (prevFailedTestFiles !== undefined || prevNotExecutedTestFiles !== undefined) {
    prevFailedTestFiles = prevFailedTestFiles ? prevFailedTestFiles.split(',') : [];
    prevNotExecutedTestFiles = prevNotExecutedTestFiles ? prevNotExecutedTestFiles.split(',') : [];
    return [...prevFailedTestFiles, ...prevNotExecutedTestFiles];
  }
  if(process.env.WA_TESTS === 'true')
    return [...ccdPipelineTests, ...civilServiceAndCamundaTests]
  if(process.env.CCD_UI_TESTS === 'true')
    return ccdPipelineTests;
  else
    return civilServiceAndCamundaTests;
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
      waitForTimeout: parseInt(process.env.WAIT_FOR_TIMEOUT_MS || 90000),
      windowSize: '1280x960',
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
