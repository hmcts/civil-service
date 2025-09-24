const config = require('../../../config.js');
//const {paymentUpdate} = require('../../../api/apiRequest');
const parties = require('../../../helpers/party');
const {assignCaseRoleToUser, addUserCaseMapping, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const {PARTY_FLAGS} = require('../../../fixtures/caseFlags');
const {waitForFinishedBusinessProcess, checkToggleEnabled, checkCaseFlagsEnabled} = require('../../../api/testingSupport');
//const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';

// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;

let caseNumber, validFastTrackDirectionsTask;

if (config.runWAApiTest) {
  validFastTrackDirectionsTask = require('../../../../wa/tasks/fastTrackDirectionsTask.js');
}

if (config.runWAApiTest) {
  validFastTrackDirectionsTask = require('../../../../wa/tasks/fastTrackDirectionsTask.js');
}

Feature('1v2 Different Solicitors fast track - Claim Journey @e2e-unspec-fast @e2e-nightly-prod @e2e-unspec-1v2DS @master-e2e-ft');

Scenario('Claimant solicitor raises a claim against 2 defendants who have different solicitors', async ({I, api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  caseNumber = await api.getCaseId();
  await I.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
}).retry(2);

Scenario('1v2 Diff   - Assign roles to defendants', async () => {
  await assignCaseRoleToUser(caseNumber, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
  await assignCaseRoleToUser(caseNumber,  'RESPONDENTSOLICITORTWO', config.secondDefendantSolicitorUser);
}).retry(2);

Scenario('Defendant 1 solicitor acknowledges claim', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.acknowledgeClaim('fullDefence');
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Acknowledge claim'));
}).retry(2);

Scenario('Defendant 2 solicitor acknowledges claim', async ({I}) => {
  await I.login(config.secondDefendantSolicitorUser);
  await I.acknowledgeClaim(null, 'fullDefence');
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Acknowledge claim'));
}).retry(2);

Scenario('Defendant 1 solicitor requests deadline extension', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.informAgreedExtensionDate();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  // I.see(caseEventMessage('Inform agreed extension date'));
}).retry(2);

Scenario('Defendant 1 solicitor adds defendant litigation friend', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.addDefendantLitigationFriend();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Add litigation friend'));
}).retry(2);

Scenario('Defendant 1 solicitor rejects claim for defendant 1', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaim({
    defendant1Response: 'fullDefence',
    claimValue: 11000});
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Respond to claim'));
}).retry(2);

Scenario('Defendant 2 solicitor rejects claim for defendant 2', async ({I}) => {
  await I.login(config.secondDefendantSolicitorUser);
  await I.respondToClaim({
    party: parties.RESPONDENT_SOLICITOR_2,
    defendant2Response: 'fullDefence',
    claimValue: 11000});
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Respond to claim'));
}).retry(2);

Scenario('Claimant solicitor responds to defence', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.respondToDefence('ONE_V_TWO_TWO_LEGAL_REP', 11000);
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('View and respond to defence'));
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario.skip('Add case flags', async ({I}) => {
  if(await checkCaseFlagsEnabled()) {
    const caseFlags = [{
      partyName: 'Example applicant1 company', roleOnCase: 'Claimant 1',
      details: [PARTY_FLAGS.vulnerableUser.value]
    }, {
      partyName: 'John Smith', roleOnCase: 'Defendant solicitor 1 witness',
      details: [PARTY_FLAGS.unacceptableBehaviour.value]
    }
    ];

    await I.login(config.hearingCenterAdminWithRegionId1);
    await I.createCaseFlags(caseFlags);
    // await I.validateCaseFlags(caseFlags);
  }
}).retry(2);

Scenario.skip('Defendant 2 solicitor adds unavailable dates', async ({I}) => {
  if (await checkToggleEnabled('update-contact-details')) {
    await I.login(config.secondDefendantSolicitorUser);
    await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseNumber);
    await I.waitForText('Summary');
    await I.addUnavailableDates(caseNumber);
  }
}).retry(2);

Scenario('Stay the case', async ({I}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await I.stayCase();
    await waitForFinishedBusinessProcess(caseNumber);
  }
}).retry(2);

Scenario('Request update on the stay case - Manage stay', async ({I}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await I.manageStay('REQ_UPDATE');
    await waitForFinishedBusinessProcess(caseNumber);
  }
}).retry(2);

Scenario('Lift the stay case - Manage stay', async ({I}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await I.manageStay('LIFT_STAY', 'JUDICIAL_REFERRAL');
    await waitForFinishedBusinessProcess(caseNumber);
  }
}).retry(2);

Scenario('Judge triggers SDO', async ({I, api, WA}) => {
  await I.login(config.judgeUserWithRegionId1);
  let taskId;
  if (config.runWAApiTest) {
    const fastTrackDirections = await api.retrieveTaskDetails(config.judgeUserWithRegionId1, caseNumber, config.waTaskIds.fastTrackDirections);
    console.log('fastTrackDirections...' , fastTrackDirections);
    WA.validateTaskInfo(fastTrackDirections, validFastTrackDirectionsTask);
    taskId = fastTrackDirections['id'];
    api.assignTaskToUser(config.judgeUserWithRegionId1, taskId);
  }
  await I.initiateSDO(null, null, 'fastTrack', null);
  if (config.runWAApiTest) {
    api.completeTaskByUser(config.judgeUserWithRegionId1, taskId);
  }
}).retry(2);

Scenario('Claimant solicitor uploads evidence', async ({I}) => {
    await I.login(config.applicantSolicitorUser);
    await I.evidenceUpload(caseNumber, false);
}).retry(2);

Scenario.skip('Defendant solicitor uploads evidence', async ({I}) => {
    await I.login(config.defendantSolicitorUser);
    await I.evidenceUpload(caseNumber, true);
}).retry(2);

Scenario('Create a Hearing Request', async ({I}) => {
  if (['demo'].includes(config.runningEnv)) {
    await I.login(config.hearingCenterAdminWithRegionId1);
    await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseNumber);
    await I.requestNewHearing();
    await I.updateHearing();
    await I.cancelHearing();
  }
}).retry(2);

Scenario('Transfer online case', async ({I}) => {
    await I.login(config.hearingCenterAdminWithRegionId1);
    await I.transferOnlineCase();
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
