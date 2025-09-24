
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

Feature('CCD 1v1 API test spec intermediate  track @api-multi-intermediate-spec');

async function prepareClaim(api_spec, mpScenario) {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
}

Scenario('1v1 FULL_DEFENCE Intermediate claim Specified @api-prod', async ({api_spec}) => {
  const mpScenario = 'ONE_V_ONE';
  await prepareClaim(api_spec, mpScenario);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'JUDICIAL_REFERRAL', false, true);
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
  await api_spec.evidenceUploadApplicant(config.applicantSolicitorUser, mpScenario);
  await api_spec.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL', true);
});

Scenario('1v1 FULL_ADMISSION Intermediate claim Specified', async ({api_spec}) => {
  const mpScenario = 'ONE_V_ONE';
  await prepareClaim(api_spec, mpScenario);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_ADMISSION', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true);
});

Scenario('1v1 PART_ADMISSION Intermediate claim Specified', async ({api_spec}) => {
  const mpScenario = 'ONE_V_ONE';
  await prepareClaim(api_spec, mpScenario);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'PART_ADMISSION', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true);
});

Scenario('1v1 COUNTER_CLAIM Intermediate claim Specified', async ({api_spec}) => {
  const mpScenario = 'ONE_V_ONE';
  await prepareClaim(api_spec, mpScenario);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'COUNTER_CLAIM', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
});

Scenario('1v2 full defence Intermediate claim Specified Different Solicitor', async ({api_spec}) => {
  const mpScenario = 'ONE_V_TWO';
  await prepareClaim(api_spec, mpScenario);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL', 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT', false, true, claimAmountIntermediate);
  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL', 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'JUDICIAL_REFERRAL', false, true);
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
  await api_spec.evidenceUploadApplicant(config.applicantSolicitorUser, mpScenario);
  await api_spec.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL', true);

});

Scenario.skip('1v2  full defence Intermediate claim Specified same solicitor @wa-task @api-nonprod', async ({api_spec, WA}) => {
  const mpScenario = 'ONE_V_TWO_SAME_SOL';
  await prepareClaim(api_spec, mpScenario);
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
