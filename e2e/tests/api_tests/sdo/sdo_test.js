const config = require('../../../config.js');

const mpScenario = 'ONE_V_ONE';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;

const legalAdvUser = config.tribunalCaseworkerWithRegionId4;
// to use on local because the idam images are different
// const judgeUser = config.judgeUserWithRegionId1Local;
// const legalAdvUser = config.tribunalCaseworkerWithRegionId1Local;
const claimAmountJudge = '11000';
const claimAmountAdvisor = '100';
let fastTrackDirectionsTask, taskId;
let legalAdvisorSmallClaimsTrackDirectionsTask, scheduleAHearingTask;
let transferOfflineSdoTask;
if (config.runWAApiTest) {
  fastTrackDirectionsTask = require('../../../../wa/tasks/fastTrackDirectionsTask.js');
  legalAdvisorSmallClaimsTrackDirectionsTask = require('../../../../wa/tasks/legalAdvisorSmallClaimsTrackDirectionsTask.js');
  transferOfflineSdoTask = require('../../../../wa/tasks/transferOfflineSdo.js');
  scheduleAHearingTask = require('../../../../wa/tasks/scheduleAHearing.js');
}

Feature('CCD 1v1 API test');

async function prepareClaim(api, claimAmount) {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmount);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, null, 'FAST_CLAIM');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
}

async function prepareSpecSmallClaim(api_spec_small) {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE');
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, 'true', 'No');
}

Scenario('1v1 full defence unspecified - judge draws small claims WITH sum of damages - hearing scheduled', async ({api}) => {
  // sdo requires judicial_referral, which is not past preview
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim(api, claimAmountJudge);
    await api.createSDO(judgeUser, 'CREATE_SMALL');
    await api.evidenceUploadApplicant(config.applicantSolicitorUser);
    await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
    await api.scheduleHearing(hearingCenterAdminToBeUsed, 'SMALL_CLAIMS');
    await api.amendHearingDueDate(config.systemupdate);
    await api.hearingFeePaid(hearingCenterAdminToBeUsed);
    if (['demo'].includes(config.runningEnv)) {
      await api.triggerBundle(config.systemupdate);
    }
    await api.createFinalOrder(judgeUser, 'FREE_FORM_ORDER');
  }
});

Scenario('1v1 full defence unspecified - judge draws fast track WITH sum of damages - hearing scheduled @api-sdo @api-prod', async ({ api}) => {
  // sdo requires judicial_referral, which is not past preview
    await prepareClaim(api, claimAmountJudge);
    await api.createSDO(judgeUser, 'CREATE_FAST');
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await api.evidenceUploadApplicant(config.applicantSolicitorUser);
    await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
    await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
    await api.amendHearingDueDate(config.systemupdate);
    await api.hearingFeePaid(hearingCenterAdminToBeUsed);
    if (['demo'].includes(config.runningEnv)) {
      await api.triggerBundle(config.systemupdate);
    }
    await api.createFinalOrder(judgeUser, 'ASSISTED_ORDER');
  }
});

Scenario('1v1 full defence unspecified - judge draws small claims WITHOUT sum of damages - hearing scheduled', async ({ api}) => {
  // sdo requires judicial_referral, which is not past preview
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim(api, claimAmountJudge);
    await api.createSDO(judgeUser, 'CREATE_SMALL_NO_SUM');
    await api.evidenceUploadApplicant(config.applicantSolicitorUser);
    await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
    await api.scheduleHearing(hearingCenterAdminToBeUsed, 'SMALL_CLAIMS');
    await api.amendHearingDueDate(config.systemupdate);
    await api.hearingFeePaid(hearingCenterAdminToBeUsed);
    if (['demo'].includes(config.runningEnv)) {
      await api.triggerBundle(config.systemupdate);
    }
  }
});


Scenario('1v1 full defence unspecified - judge draws fast track WITHOUT sum of damages - hearing scheduled', async ({api}) => {
  // sdo requires judicial_referral, which is not past preview
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim(api, claimAmountJudge);
    await api.createSDO(judgeUser, 'CREATE_FAST_NO_SUM');
    await api.evidenceUploadApplicant(config.applicantSolicitorUser);
    await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
    await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
    await api.amendHearingDueDate(config.systemupdate);
    await api.hearingFeeUnpaid(hearingCenterAdminToBeUsed);
  }
});

AfterSuite(async ({api}) => {
  await api.cleanUp();
});

Feature('CCD 1v1 API test @e2e-nightly');

Scenario('1v1 full defence unspecified - legal advisor draws small claims WITHOUT sum of damages - hearing scheduled', async ({api}) => {
  // sdo requires judicial_referral, which is not past preview
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim(api, claimAmountAdvisor);
    await api.createSDO(legalAdvUser, 'CREATE_SMALL_NO_SUM');
    await api.evidenceUploadApplicant(config.applicantSolicitorUser);
    await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
    await api.scheduleHearing(hearingCenterAdminToBeUsed, 'SMALL_CLAIMS');
    await api.amendHearingDueDate(config.systemupdate);
    await api.hearingFeePaid(hearingCenterAdminToBeUsed);
    if (['demo'].includes(config.runningEnv)) {
      await api.triggerBundle(config.systemupdate);
    }
  }
});

Scenario('1v1 full defence unspecified - legal advisor draws small claims WITH sum of damages - hearing scheduled', async ({api}) => {
  // sdo requires judicial_referral, which is not past preview
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim(api, claimAmountAdvisor);
    await api.createSDO(legalAdvUser, 'CREATE_SMALL');
    await api.evidenceUploadApplicant(config.applicantSolicitorUser);
    await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
    await api.scheduleHearing(hearingCenterAdminToBeUsed, 'SMALL_CLAIMS');
    await api.amendHearingDueDate(config.systemupdate);
    await api.hearingFeePaid(hearingCenterAdminToBeUsed);
    if (['demo'].includes(config.runningEnv)) {
      await api.triggerBundle(config.systemupdate);
    }
  }
});

Scenario('1v1 full defence unspecified - legal advisor draws fast track WITH sum of damages - hearing scheduled', async ({api}) => {
  // sdo requires judicial_referral, which is not past preview
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim(api, claimAmountAdvisor);
    await api.createSDO(legalAdvUser, 'CREATE_FAST');
    await api.evidenceUploadApplicant(config.applicantSolicitorUser);
    await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
    await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
    await api.amendHearingDueDate(config.systemupdate);
    await api.hearingFeePaid(hearingCenterAdminToBeUsed);
  }
});

Scenario('1v1 full defence unspecified - legal advisor draws fast track WITHOUT sum of damages - hearing scheduled', async ({api}) => {
  // sdo requires judicial_referral, which is not past preview
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim(api, claimAmountAdvisor);
    await api.createSDO(legalAdvUser, 'CREATE_FAST_NO_SUM');
    await api.evidenceUploadApplicant(config.applicantSolicitorUser);
    await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
    await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
    await api.amendHearingDueDate(config.systemupdate);
    await api.hearingFeePaid(hearingCenterAdminToBeUsed);
    if (['demo'].includes(config.runningEnv)) {
      await api.triggerBundle(config.systemupdate);
    }
  }
});

Scenario('1v1 full defence unspecified - judge draws small claims DRH - hearing scheduled @wa-task', async ({ api, WA}) => {
  await prepareClaim(api, claimAmountJudge);
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
});

Scenario('1v1 full defence specified - legal advisor draws disposal order - hearing scheduled @wa-r4 @sdo-spec @api-sdo', async ({api_spec_small, WA}) => {
  // sdo requires judicial_referral, which is not past preview
  await prepareSpecSmallClaim(api_spec_small);
  if (config.runWAApiTest) {
    const caseId = await api_spec_small.getCaseId();
    const task = await api_spec_small.retrieveTaskDetails(legalAdvUser, caseId, config.waTaskIds.legalAdvisorDirections);
    WA.validateTaskInfo(task, legalAdvisorSmallClaimsTrackDirectionsTask);
    taskId = task['id'];
  }
  if (config.runWAApiTest) {
    api_spec_small.completeTaskByUser(legalAdvUser, taskId);
  }
});

Scenario('1v1 full defence unspecified - judge declares SDO unsuitable - hearing scheduled', async ({api, WA}) => {
  // sdo requires judicial_referral, which is not past preview
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim(api, claimAmountJudge);
    await api.createSDO(judgeUser, 'UNSUITABLE_FOR_SDO');
    if (config.runWAApiTest) {
      const caseId = await api.getCaseId();
      const task = await api.retrieveTaskDetails(hearingCenterAdminToBeUsed, caseId, config.waTaskIds.notSuitableSdo);
      WA.validateTaskInfo(task, transferOfflineSdoTask);
    }
  }
});

// skip while ccd-data-store says legalAdvUser has no permission to run this event
Scenario.skip('1v1 full defence unspecified - legal advisor declares SDO unsuitable - hearing scheduled', async ({api}) => {
  // sdo requires judicial_referral, which is not past preview
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim(api, claimAmountAdvisor);
    await api.createSDO(legalAdvUser, 'UNSUITABLE_FOR_SDO');
  }
});

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
