/* global process */

exports.config = {
  tests: './e2e/tests/*_test.js',
  output: './output',
  helpers: {
    Puppeteer: {
      show: process.env.SHOW_BROWSER_WINDOW || false,
      windowSize: '1200x900',
      waitForTimeout: 20000,
      chrome: {
        ignoreHTTPSErrors: true,
        args: process.env.PROXY_SERVER ? [`--proxy-server=${process.env.PROXY_SERVER}`,] : [],
      },
    },
  },
  include: {
    I: './e2e/steps_file.js',
    loginPage: './e2e/pages/login.page.js'
  },
  mocha: {
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
          mochaFile: 'test-results/result.xml',
        },
      },
      'mochawesome': {
        stdout: '-',
        options: {
          reportDir: './output',
          inlineAssets: true,
          json: false,
        },
      },
    }
  }
}
