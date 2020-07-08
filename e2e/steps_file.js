/* global process */

// in this file you can append custom step methods to 'I' object

const output = require('codeceptjs').output;

const config = require('./config.js');
const loginPage = require('./pages/login.page');
const createCasePage = require('./pages/createClaim/createCase.page');
const solicitorReferencesPage = require('./pages/createClaim/solicitorReferences.page');
const chooseCourtPage = require('./pages/createClaim/chooseCourt.page');
const claimantDetailsPage = require('./pages/createClaim/claimantDetails.page');
const claimValuePage = require('./pages/createClaim/claimValue.page');

const statementOfTruth = require('./fragments/statementOfTruth');

const baseUrl = process.env.URL || 'http://localhost:3333';
const signedInSelector = 'exui-header';

module.exports = function() {
  return actor({
    // Define custom steps here, use 'this' to access default methods of I.
    // It is recommended to place a general 'login' function here.
    async login(user) {
      await this.retryUntilExists(async () => {
        this.amOnPage(baseUrl);

        if (await this.hasSelector(signedInSelector)) {
          this.click('Sign out');
        }

        loginPage.signIn(user);
      }, signedInSelector);
    },

    grabCaseNumber: async function () {
      let caseNumber = await this.grabTextFrom('ccd-case-header > h1');

      caseNumber = caseNumber.split('-').join('');
      return caseNumber;
    },

    async createCase() {
      this.click('Create case');
      this.waitForElement(`#cc-jurisdiction > option[value="${config.definition.jurisdiction}"]`);
      await this.retryUntilExists(() => createCasePage.selectCaseType(), 'ccd-markdown');
      await this.clickContinue();
      await solicitorReferencesPage.enterReferences();
      await chooseCourtPage.enterCourt();
      await claimantDetailsPage.enterClaimant(config.address);
      await claimValuePage.enterClaimValue();
      await statementOfTruth.enterNameAndRole();
      await this.retryUntilExists(() => this.click('Issue claim'), 'ccd-markdown');
      this.see('Your claim has been issued');
      await this.retryUntilExists(() =>
        this.click('Close and Return to case details'), locate('ccd-case-header > h1'));
    },

    async clickContinue() {
      await this.click('Continue');
    },

    /**
     * Retries defined action util element described by the locator is present. If element is not present
     * after 4 tries (run + 3 retries) this step throws an error.
     *
     * Warning: action logic should avoid framework steps that stop test execution upon step failure as it will
     *          stop test execution even if there are retries still available. Catching step error does not help.
     *
     * @param action - an action that will be retried until either condition is met or max number of retries is reached
     * @param locator - locator for an element that is expected to be present upon successful execution of an action
     * @param maxNumberOfTries - maximum number to retry the function for before failing
     * @returns {Promise<void>} - promise holding no result if resolved or error if rejected
     */
    async retryUntilExists(action, locator, maxNumberOfTries = 6) {
      for (let tryNumber = 1; tryNumber <= maxNumberOfTries; tryNumber++) {
        output.log(`retryUntilExists(${locator}): starting try #${tryNumber}`);
        if (tryNumber > 1 && await this.hasSelector(locator)) {
          output.log(`retryUntilExists(${locator}): element found before try #${tryNumber} was executed`);
          break;
        }
        await action();
        if (await this.waitForSelector(locator) != null) {
          output.log(`retryUntilExists(${locator}): element found after try #${tryNumber} was executed`);
          break;
        } else {
          output.print(`retryUntilExists(${locator}): element not found after try #${tryNumber} was executed`);
        }
        if (tryNumber === maxNumberOfTries) {
          throw new Error(`Maximum number of tries (${maxNumberOfTries}) has been reached in search for ${locator}`);
        }
      }
    },
  });
};
