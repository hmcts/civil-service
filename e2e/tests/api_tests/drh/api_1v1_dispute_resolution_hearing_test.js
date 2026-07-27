const config = require('../../../config.js');

const mpScenario = 'ONE_V_ONE';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;

const claimAmountJudge = '11000';
let fastTrackDirectionsTask, taskId;
let legalAdvisorSmallClaimsTrackDirectionsTask, scheduleAHearingTask;
let transferOfflineSdoTask;
if (config.runWAApiTest) {
  fastTrackDirectionsTask = require('../../../../wa/tasks/fastTrackDirectionsTask.js');
  legalAdvisorSmallClaimsTrackDirectionsTask = require('../../../../wa/tasks/legalAdvisorSmallClaimsTrackDirectionsTask.js');
  transferOfflineSdoTask = require('../../../../wa/tasks/transferOfflineSdo.js');
  scheduleAHearingTask = require('../../../../wa/tasks/scheduleAHearing.js');
}

Feature('1v1 sdo api journeys').tag('@civil-service-nightly @api-drh @civil-wa-master @civil-wa-pr @civil-wa-nightly');

Scenario.skip('1v1 full defence unspecified - judge draws small claims DRH - hearing scheduled', async ({ api, WA}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmountJudge);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, null, 'FAST_CLAIM');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  const caseId = await api.getCaseId();
  if (config.runWAApiTest) {
    const task = await api.retrieveTaskDetails(judgeUser, caseId, config.waTaskIds.fastTrackDirections);
    WA.validateTaskInfo(task, fastTrackDirectionsTask);
    taskId = task['id'];
  }
  await api.createSDO(judgeUser, 'CREATE_SMALL_DRH');
  if (config.runWAApiTest) {
    api.completeTaskByUser(judgeUser, taskId);
  }
  await api.evidenceUploadApplicant(config.applicantSolicitorUser, mpScenario, 'DRH');
  await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario, 'DRH');
  if (config.runWAApiTest) {
    const hearingTask = await api.retrieveTaskDetails(hearingCenterAdminToBeUsed, caseId, config.waTaskIds.scheduleAHearing);
    WA.validateTaskInfo(hearingTask, scheduleAHearingTask);
    taskId = hearingTask['id'];
  }
  await api.scheduleHearing(hearingCenterAdminToBeUsed, 'SMALL_CLAIMS');
  if (config.runWAApiTest) {
    api.completeTaskByUser(hearingCenterAdminToBeUsed, taskId);
  }
  await api.amendHearingDueDate(config.systemupdate);
  await api.hearingFeePaidDRH(hearingCenterAdminToBeUsed);
  await api.createFinalOrderJO(judgeUser, 'FREE_FORM_ORDER');
});


AfterSuite(async ({api}) => {
  await api.cleanUp();
});
