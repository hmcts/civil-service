const config = require('../../../config.js');
//const {paymentUpdate} = require('../../../api/apiRequest');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
//const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';

// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;

let caseNumber, validFastTrackDirectionsTask;

if (config.runWAApiTest) {
  validFastTrackDirectionsTask = require('../../../../wa/tasks/fastTrackDirectionsTask.js');
}

Feature('1v2DS fast track case progression journey')
  .tag('@civil-ccd-master @civil-ccd-pr @civil-ccd-nightly @ui-case-progression');

Scenario('01 Prepare 1v2DS unspec fast track claim up to case progression', async ({I, api}) => {
  caseNumber = await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.addCaseNote(config.adminUser);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  // Skipping this step as it is failing with partyIDs at the moment.
  // await api.acknowledgeClaim(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  I.setCaseId(caseNumber);
});

Scenario('02 Judge triggers SDO', async ({I, api, WA}) => {
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

Scenario('03 Claimant solicitor uploads evidence', async ({I}) => {
    await I.login(config.applicantSolicitorUser);
    await I.evidenceUpload(caseNumber, false);
}).retry(2);

Scenario.skip('04 Defendant solicitor uploads evidence', async ({I}) => {
    await I.login(config.defendantSolicitorUser);
    await I.evidenceUpload(caseNumber, true);
}).retry(2);

Scenario('05 Transfer online case', async ({I}) => {
    await I.login(config.hearingCenterAdminWithRegionId1);
    await I.transferOnlineCase();
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});