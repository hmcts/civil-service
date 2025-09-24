const config = require('../../../config.js');
const multiTrackClaimAmount = '200001';
const mintiEnabled = true;
const track = 'MULTI_CLAIM';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
let caseId, taskId, intermediateTrackOrderMadeReviewCaseExpectedTask, multiTrackDirectionsExpectedTask;

if (config.runWAApiTest) {
  multiTrackDirectionsExpectedTask = require('../../../../wa/tasks/multiTrackDirectionsTask.js');
  intermediateTrackOrderMadeReviewCaseExpectedTask = require('../../../../wa/tasks/intermediateTrackOrderMadeReviewCaseTask.js');
}
Feature('CCD API test unspec multi track @api-multi-intermediate-unspec');

async function prepareClaim(api, mpScenario, claimAmount, WA) {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmount, mintiEnabled);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await defendantResponse(api, mpScenario);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', track);
  caseId = await api.getCaseId();
  if (config.runWAApiTest) {
    const multiTrackDirections = await api.retrieveTaskDetails(config.judgeUserWithRegionId1, caseId, config.waTaskIds.multiTrackDirections);
    console.log('multiTrackDirections...', multiTrackDirections);
    WA.validateTaskInfo(multiTrackDirections, multiTrackDirectionsExpectedTask);
    taskId = multiTrackDirections['id'];
    api.assignTaskToUser(config.judgeUserWithRegionId1, taskId);
  }
  await api.createFinalOrder(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'MULTI');
  await api.evidenceUploadApplicant(config.applicantSolicitorUser, mpScenario);
  if (config.runWAApiTest) {
    const multiTrackOrderMakeReview = await api.retrieveTaskDetails(config.hearingCenterAdminWithRegionId1, caseId, config.waTaskIds.multiTrackOrderMadeReview);
    console.log('multiTrackOrderMakeReview...', multiTrackOrderMakeReview);
    WA.validateTaskInfo(multiTrackOrderMakeReview, intermediateTrackOrderMadeReviewCaseExpectedTask);
    taskId = multiTrackOrderMakeReview['id'];
    api.assignTaskToUser(config.hearingCenterAdminWithRegionId1, taskId);
  }
  await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL', true);
}

Scenario('1v1 Create Unspecified Multi Track claim @api-prod', async ({api}) => {
  const mpScenario = 'ONE_V_ONE';
  await prepareClaim(api, mpScenario, multiTrackClaimAmount);
});

Scenario('1v2 Different Solicitors Create Unspecified Multi Track claim ', async ({ api }) => {
  const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
  await prepareClaim(api, mpScenario, multiTrackClaimAmount);
});

Scenario('1v2 Same Solicitor Create Unspecified Multi Track claim', async ({ api }) => {
  const mpScenario = 'ONE_V_TWO_ONE_LEGAL_REP';
  await prepareClaim(api, mpScenario, multiTrackClaimAmount);
});

Scenario('2v1 Create Unspecified Multi Track claim @api-nonprod', async ({ api, WA }) => {
  const mpScenario = 'TWO_V_ONE';
  await prepareClaim(api, mpScenario, multiTrackClaimAmount, WA);
});

async function defendantResponse(api, mpScenario) {
  if (mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP') {
    await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
    await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  } else {
    await api.defendantResponse(config.defendantSolicitorUser, mpScenario);
  }
}

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
