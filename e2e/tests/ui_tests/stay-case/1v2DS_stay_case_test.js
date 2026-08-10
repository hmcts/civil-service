const config = require('../../../config.js');
//const {paymentUpdate} = require('../../../api/apiRequest');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const {waitForFinishedBusinessProcess} = require('../../../api/testingSupport');
//const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';

// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;

let caseNumber, validFastTrackDirectionsTask;

if (config.runWAApiTest) {
  validFastTrackDirectionsTask = require('../../../../wa/tasks/fastTrackDirectionsTask.js');
}

Feature('1v2 Different Solicitors fast track - Claim Journey').tag('@ui-stay-case');

Scenario('01 1v2DS Prepare claim up to case progression', async ({I, api}) => {
  caseNumber = await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaim(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  // Skipping this step as it is failing with partyIDs at the moment.
  // await api.acknowledgeClaim(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  I.setCaseId(caseNumber);
}).retry(1);

Scenario.skip('02 Defendant 2 solicitor adds unavailable dates', async ({I}) => {
    await I.login(config.secondDefendantSolicitorUser);
    await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseNumber);
    await I.waitForText('Summary');
    await I.addUnavailableDates(caseNumber);
}).retry(2);

Scenario('03 Stay the case', async ({I}) => {
  await I.stayCase();
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('04 Request update on the stay case - Manage stay', async ({I}) => {
  await I.manageStay('REQ_UPDATE');
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('05 Lift the stay case - Manage stay', async ({I}) => {
  await I.manageStay('LIFT_STAY', 'JUDICIAL_REFERRAL');
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
