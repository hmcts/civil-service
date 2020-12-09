/* global process */

// in this file you can append custom step methods to 'I' object

const output = require('codeceptjs').output;

const config = require('./config.js');
const parties = require('./helpers/party.js');
const loginPage = require('./pages/login.page');
const caseViewPage = require('./pages/caseView.page');
const createCasePage = require('./pages/createClaim/createCase.page');
const solicitorReferencesPage = require('./pages/createClaim/solicitorReferences.page');
const chooseCourtPage = require('./pages/createClaim/chooseCourt.page');
const claimantLitigationDetails = require('./pages/createClaim/claimantLitigationDetails.page');
const claimTypePage = require('./pages/createClaim/claimType.page');
const respondentRepresentedPage = require('./pages/createClaim/isRespondentRepresented.page');
const personalInjuryTypePage = require('./pages/createClaim/personalInjuryType.page');
const uploadParticularsOfClaim = require('./pages/createClaim/uploadParticularsOfClaim.page');
const claimValuePage = require('./pages/createClaim/claimValue.page');
const pbaNumberPage = require('./pages/createClaim/pbaNumber.page');

const servedDocumentsPage = require('./pages/confirmService/servedDocuments.page');
const uploadDocumentsPage = require('./pages/confirmService/uploadDocuments.page');
const serviceMethodPage = require('./pages/confirmService/serviceMethod.page');
const serviceLocationPage = require('./pages/confirmService/serviceLocation.page');
const serviceDatePage = require('./pages/confirmService/serviceDate.page');

const responseIntentionPage = require('./pages/acknowledgeSerivce/responseIntention.page');

const proposeDeadline = require('./pages/requestExtension/proposeDeadline.page');
const extensionAlreadyAgreed = require('./pages/requestExtension/extensionAlreadyAgreed.page');

const respondToExtensionPage = require('./pages/respondExtension/respond.page');
const counterExtensionPage = require('./pages/respondExtension/counter.page');
const rejectionReasonPage = require('./pages/respondExtension/reason.page');

const responseTypePage = require('./pages/respondToClaim/responseType.page');
const uploadResponsePage = require('./pages/respondToClaim/uploadResponseDocument.page');

const proceedPage = require('./pages/respondToDefence/proceed.page');
const uploadResponseDocumentPage = require('./pages/respondToDefence/uploadResponseDocument.page');

const defendantLitigationFriendPage = require('./pages/addDefendantLitigationFriend/defendantLitigationDetails.page');

const statementOfTruth = require('./fragments/statementOfTruth');
const party = require('./fragments/party');
const event = require('./fragments/event');
const respondentDetails = require('./fragments/respondentDetails.page');
const confirmDetailsPage = require('./fragments/confirmDetails.page');

// DQ fragments
const fileDirectionsQuestionnairePage = require('./fragments/dq/fileDirectionsQuestionnaire.page');
const disclosureOfElectronicDocumentsPage = require('./fragments/dq/disclosureOfElectrionicDocuments.page');
const disclosureOfNonElectronicDocumentsPage = require('./fragments/dq/disclosureOfNonElectrionicDocuments.page');
const expertsPage = require('./fragments/dq/experts.page');
const witnessPage = require('./fragments/dq/witnesses.page');
const hearingPage = require('./fragments/dq/hearing.page');
const draftDirectionsPage = require('./fragments/dq/draftDirections.page');
const requestedCourtPage = require('./fragments/dq/requestedCourt.page');
const hearingSupportRequirementsPage = require('./fragments/dq/hearingSupportRequirements.page');
const furtherInformationPage = require('./fragments/dq/furtherInformation.page');
const welshLanguageRequirementsPage = require('./fragments/dq/language.page');

const address = require('./fixtures/address.js');

const baseUrl = process.env.URL || 'http://localhost:3333';

const SIGNED_IN_SELECTOR = 'exui-header';
const CASE_LIST = 'exui-case-list';
const TYPE_LOCATOR = '#wb-case-type > option';
const STATE_LOCATOR = '#wb-case-state > option';
const CASE_NUMBER_INPUT_LOCATOR = 'input[type$="number"]';
const CASE_HEADER = 'ccd-case-header > h1';

const TEST_FILE_PATH = './e2e/fixtures/examplePDF.pdf';

let caseId;

module.exports = function () {
  return actor({
    // Define custom steps here, use 'this' to access default methods of I.
    // It is recommended to place a general 'login' function here.
    async login(user) {
      await this.retryUntilExists(async () => {
        this.amOnPage(baseUrl);

        if (await this.hasSelector(SIGNED_IN_SELECTOR)) {
          this.click('Sign out');
        }

        loginPage.signIn(user);
      }, SIGNED_IN_SELECTOR);
    },

    async goToCase(caseId) {
        this.click('Case list');

        this.waitForElement(TYPE_LOCATOR);
        this.selectOption('case-type', 'Unspecified Claims');

        this.waitForElement(STATE_LOCATOR);
        this.selectOption('state', 'Any');

        this.waitForElement(CASE_NUMBER_INPUT_LOCATOR);
        this.fillField(CASE_NUMBER_INPUT_LOCATOR, caseId);
        this.click('Apply');

        const caseLinkLocator = `a[href$="/cases/case-details/${caseId}"]`;
        this.waitForElement(caseLinkLocator);
        this.click(caseLinkLocator);
        this.waitForElement(CASE_HEADER);
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
      await party.enterParty('applicant1', address);
      await claimantLitigationDetails.enterLitigantFriendWithDifferentAddressToApplicant(address, TEST_FILE_PATH);
      await party.enterParty('respondent1', address);
      await respondentRepresentedPage.enterRespondentRepresented();
      await claimTypePage.selectClaimType();
      await personalInjuryTypePage.selectPersonalInjuryType();
      await uploadParticularsOfClaim.upload(TEST_FILE_PATH);
      await claimValuePage.enterClaimValue();
      await pbaNumberPage.selectPbaNumber();
      await statementOfTruth.enterNameAndRole('claim');
      await event.submit('Issue claim', 'Your claim has been issued');
      await event.returnToCaseDetails();

      caseId = (await this.grabCaseNumber()).split('-').join('').substring(1);
    },

    async confirmService() {
      await caseViewPage.startEvent('Confirm service', caseId);
      await servedDocumentsPage.enterServedDocuments();
      await uploadDocumentsPage.uploadServedDocuments(TEST_FILE_PATH);
      await serviceMethodPage.selectPostMethod();
      await serviceLocationPage.selectUsualResidence();
      await serviceDatePage.enterServiceDate();
      await statementOfTruth.enterNameAndRole('service');
      await event.submit('Confirm service', 'You\'ve confirmed service');
      await event.returnToCaseDetails();
    },

    async acknowledgeService() {
      await caseViewPage.startEvent('Acknowledge service', caseId);
      await respondentDetails.verifyDetails();
      await confirmDetailsPage.confirmReference();
      await responseIntentionPage.selectResponseIntention();
      await event.submit('Acknowledge service', 'You\'ve acknowledged service');
      await event.returnToCaseDetails();
    },

    async addDefendantLitigationFriend() {
      await caseViewPage.startEvent('Add litigation friend', caseId);
      await defendantLitigationFriendPage.enterLitigantFriendWithDifferentAddressToDefendant(address, TEST_FILE_PATH);
      this.waitForText('Submit');
      this.click('Submit');
      this.waitForElement(CASE_HEADER);
    },

    async requestExtension() {
      await caseViewPage.startEvent('Request extension', caseId);
      await proposeDeadline.enterExtensionProposedDeadline();
      await extensionAlreadyAgreed.selectAlreadyAgreed();
      await event.submit('Ask for extension', 'You asked for extra time to respond');
      await event.returnToCaseDetails();
    },

    async respondToExtension() {
      await caseViewPage.startEvent('Respond to extension request', caseId);
      await respondToExtensionPage.selectDoNotAccept();
      await counterExtensionPage.enterCounterDate();
      await rejectionReasonPage.enterResponse();
      await event.submit('Respond to request', 'You\'ve responded to the request for more time');
      await event.returnToCaseDetails();
    },

    async respondToClaim() {
      await caseViewPage.startEvent('Respond to claim', caseId);
      await responseTypePage.selectFullDefence();
      await uploadResponsePage.uploadResponseDocuments(TEST_FILE_PATH);
      await respondentDetails.verifyDetails();
      await confirmDetailsPage.confirmReference();
      await fileDirectionsQuestionnairePage.fileDirectionsQuestionnaire(parties.RESPONDENT_SOLICITOR_1);
      await disclosureOfElectronicDocumentsPage.enterDisclosureOfElectronicDocuments(parties.RESPONDENT_SOLICITOR_1);
      await disclosureOfNonElectronicDocumentsPage.enterDirectionsProposedForDisclosure(parties.RESPONDENT_SOLICITOR_1);
      await expertsPage.enterExpertInformation(parties.RESPONDENT_SOLICITOR_1);
      await witnessPage.enterWitnessInformation(parties.RESPONDENT_SOLICITOR_1);
      await hearingPage.enterHearingInformation(parties.RESPONDENT_SOLICITOR_1);
      await draftDirectionsPage.enterDraftDirections(parties.RESPONDENT_SOLICITOR_1);
      await requestedCourtPage.selectSpecificCourtForHearing(parties.RESPONDENT_SOLICITOR_1);
      await hearingSupportRequirementsPage.selectRequirements(parties.RESPONDENT_SOLICITOR_1);
      await furtherInformationPage.enterFurtherInformation(parties.RESPONDENT_SOLICITOR_1);
      await welshLanguageRequirementsPage.enterWelshLanguageRequirements(parties.RESPONDENT_SOLICITOR_1);
      await statementOfTruth.enterNameAndRole(parties.RESPONDENT_SOLICITOR_1 + 'DQ');
      await event.submit('Submit response', 'You\'ve submitted your response');
      await event.returnToCaseDetails();
    },

    async respondToDefence() {
      await caseViewPage.startEvent('View and respond to defence', caseId);
      await proceedPage.proceedWithClaim();
      await uploadResponseDocumentPage.uploadResponseDocuments(TEST_FILE_PATH);
      await fileDirectionsQuestionnairePage.fileDirectionsQuestionnaire(parties.APPLICANT_SOLICITOR_1);
      await disclosureOfElectronicDocumentsPage.enterDisclosureOfElectronicDocuments(parties.APPLICANT_SOLICITOR_1);
      await disclosureOfNonElectronicDocumentsPage.enterDirectionsProposedForDisclosure(parties.APPLICANT_SOLICITOR_1);
      await expertsPage.enterExpertInformation(parties.APPLICANT_SOLICITOR_1);
      await witnessPage.enterWitnessInformation(parties.APPLICANT_SOLICITOR_1);
      await hearingPage.enterHearingInformation(parties.APPLICANT_SOLICITOR_1);
      await draftDirectionsPage.enterDraftDirections(parties.APPLICANT_SOLICITOR_1);
      await hearingSupportRequirementsPage.selectRequirements(parties.APPLICANT_SOLICITOR_1);
      await furtherInformationPage.enterFurtherInformation(parties.APPLICANT_SOLICITOR_1);
      await welshLanguageRequirementsPage.enterWelshLanguageRequirements(parties.APPLICANT_SOLICITOR_1);
      await statementOfTruth.enterNameAndRole(parties.APPLICANT_SOLICITOR_1 + 'DQ');
      await event.submit('Submit your response', 'You\'ve decided to proceed with the claim');
      await this.click('Close and Return to case details');
      this.waitForElement(CASE_LIST);
    },

    async clickContinue() {
      await this.click('Continue');
    },

    async addAnotherElementToCollection() {
      const numberOfElements = await this.grabNumberOfVisibleElements('.collection-title');
      this.click('Add new');
      this.waitNumberOfVisibleElements('.collection-title', numberOfElements + 1);
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
