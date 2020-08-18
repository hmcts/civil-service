/* global process */

// in this file you can append custom step methods to 'I' object

const output = require('codeceptjs').output;

const config = require('./config.js');
const loginPage = require('./pages/login.page');
const caseViewPage = require('./pages/caseView.page');
const createCasePage = require('./pages/createClaim/createCase.page');
const solicitorReferencesPage = require('./pages/createClaim/solicitorReferences.page');
const chooseCourtPage = require('./pages/createClaim/chooseCourt.page');
const claimTypePage = require('./pages/createClaim/claimType.page');
const claimValuePage = require('./pages/createClaim/claimValue.page');

const servedDocumentsPage = require('./pages/confirmService/servedDocuments.page');
const uploadDocumentsPage = require('./pages/confirmService/uploadDocuments.page');
const serviceMethodPage = require('./pages/confirmService/serviceMethod.page');
const serviceLocationPage = require('./pages/confirmService/serviceLocation.page');
const serviceDatePage = require('./pages/confirmService/serviceDate.page');

const confirmNameAndAddressPage = require('./pages/acknowledgeSerivce/confirmNameAndAddress.page');
const confirmDetailsPage = require('./pages/acknowledgeSerivce/confirmDetails.page');
const responseIntentionPage = require('./pages/acknowledgeSerivce/responseIntention.page');

const proposeDeadline = require('./pages/requestExtension/proposeDeadline.page');
const extensionAlreadyAgreed = require('./pages/requestExtension/extensionAlreadyAgreed.page');

const respondToExtensionPage = require('./pages/respondExtension/respond.page');
const counterExtensionPage = require('./pages/respondExtension/counter.page');
const rejectionReasonPage = require('./pages/respondExtension/reason.page');

const statementOfTruth = require('./fragments/statementOfTruth');
const party = require('./fragments/party');

const baseUrl = process.env.URL || 'http://localhost:3333';
const signedInSelector = 'exui-header';
const CASE_HEADER = 'ccd-case-header > h1';

module.exports = function () {
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
      this.waitForElement(CASE_HEADER);

      return await this.grabTextFrom(CASE_HEADER);
    },

    async createCase() {
      this.click('Create case');
      this.waitForElement(`#cc-jurisdiction > option[value="${config.definition.jurisdiction}"]`);
      await this.retryUntilExists(() => createCasePage.selectCaseType(), 'ccd-markdown');
      await this.clickContinue();
      await solicitorReferencesPage.enterReferences();
      await chooseCourtPage.enterCourt();
      await party.enterParty('claimant', config.address);
      await party.enterParty('respondent', config.address);
      await claimTypePage.selectClaimType();
      await claimValuePage.enterClaimValue();
      await statementOfTruth.enterNameAndRole('claim');
      this.waitForText('Issue claim');
      await this.retryUntilExists(() => this.click('Issue claim'), 'ccd-markdown');
      this.see('Your claim has been issued');
      this.click('Close and Return to case details');
    },

    async confirmService() {
      await caseViewPage.startEvent('Confirm service');
      await servedDocumentsPage.enterServedDocuments();
      await uploadDocumentsPage.uploadServedDocuments(config.testFile);
      await serviceMethodPage.selectPostMethod();
      await serviceLocationPage.selectUsualResidence();
      await serviceDatePage.enterServiceDate();
      await statementOfTruth.enterNameAndRole('service');

      this.waitForText('Confirm service');
      await this.retryUntilExists(() => this.click('Confirm service'), 'ccd-markdown');
      this.see('You\'ve confirmed service');
      this.click('Close and Return to case details');
      this.waitForElement(CASE_HEADER);
    },

    async acknowledgeService() {
      await caseViewPage.startEvent('Acknowledge service');
      await confirmNameAndAddressPage.verifyDetails();
      await confirmDetailsPage.confirmReference();
      await responseIntentionPage.selectResponseIntention();

      this.waitForText('Acknowledge service');
      await this.retryUntilExists(() => this.click('Acknowledge service'), 'ccd-markdown');
      this.see('You\'ve acknowledged service');
      this.click('Close and Return to case details');
      this.waitForElement(CASE_HEADER);
    },

    async requestExtension() {
      await caseViewPage.startEvent('Request extension');
      await proposeDeadline.enterExtensionProposedDeadline();
      await extensionAlreadyAgreed.selectAlreadyAgreed();

      this.waitForText('Ask for extension');
      await this.retryUntilExists(() => this.click('Ask for extension'), 'ccd-markdown');
      this.see('You asked for extra time to respond');
      this.click('Close and Return to case details');
      this.waitForElement(CASE_HEADER);
    },

    async respondToExtension() {
      await caseViewPage.startEvent('Respond to extension request');
      await respondToExtensionPage.selectDoNotAccept();
      await counterExtensionPage.enterCounterDate();
      await rejectionReasonPage.enterResponse();

      this.waitForText('Respond to request');
      await this.retryUntilExists(() => this.click('Respond to request'), 'ccd-markdown');
      this.see('You\'ve responded to the request for more time');
      this.click('Close and Return to case details');
      this.waitForElement(CASE_HEADER);
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
