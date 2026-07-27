
const config = require('../../../config.js');

const claimAmountPenniesIntermediate = '9900000';
const claimAmountIntermediate = '99000';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
let caseId, taskId, intermediateTrackDirectionsExpectedTask, multiTrackOrderMadeReviewCaseExpectedTask;
if (config.runWAApiTest) {
  intermediateTrackDirectionsExpectedTask = require('../../../../wa/tasks/itermediateTrackDirectionsTask.js');
  multiTrackOrderMadeReviewCaseExpectedTask = require('../../../../wa/tasks/multiTrackOrderMadeReviewCaseTask.js');

}

Feature('1v2SS spec intermediate track api journey').tag('@civil-service-nightly @civil-wa-master @civil-wa-pr @civil-wa-nightly');

Scenario.skip('1v2SS spec full defence intermediate claim', async ({api_spec, WA}) => {
  const mpScenario = 'ONE_V_TWO_SAME_SOL';
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO','AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'JUDICIAL_REFERRAL', false, true);
  caseId = await api_spec.getCaseId();
  if (config.runWAApiTest) {
    const intermediateTrackDirections = await api_spec.retrieveTaskDetails(config.judgeUserWithRegionId1, caseId, config.waTaskIds.intermediateTrackDirections);
    console.log('intermediateTrackDirections...' , intermediateTrackDirections);
    WA.validateTaskInfo(intermediateTrackDirections, intermediateTrackDirectionsExpectedTask);
    taskId = intermediateTrackDirections['id'];
    api_spec.assignTaskToUser(config.judgeUserWithRegionId1, taskId);
  }
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
  if (config.runWAApiTest) {
    api_spec.completeTaskByUser(config.judgeUserWithRegionId1, taskId);
  }
  await api_spec.evidenceUploadApplicant(config.applicantSolicitorUser, mpScenario);
  if (config.runWAApiTest) {
    const multiTrackOrderMakeReview = await api_spec.retrieveTaskDetails(config.hearingCenterAdminWithRegionId1, caseId, config.waTaskIds.multiTrackOrderMadeReview);
    console.log('multiTrackOrderMakeReview...' , multiTrackOrderMakeReview);
    WA.validateTaskInfo(multiTrackOrderMakeReview, multiTrackOrderMadeReviewCaseExpectedTask);
    taskId = multiTrackOrderMakeReview['id'];
    api_spec.assignTaskToUser(config.hearingCenterAdminWithRegionId1, taskId);
  }
  await api_spec.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL', true);
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});