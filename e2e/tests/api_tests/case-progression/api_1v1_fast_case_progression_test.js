const config = require('../../../config.js');

const mpScenario = 'ONE_V_ONE';
const judgeUser = config.judgeUserWithRegionId1;
const claimAmountJudge = '11000';

Feature('1v1 fast track case progression api journeys')
  .tag('@civil-service-nightly @api-case-progression');

Scenario.skip('1v1 full defence unspecified - judge draws fast track WITH sum of damages - hearing scheduled', async ({ api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmountJudge);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, null, 'FAST_CLAIM');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  await api.createSDO(judgeUser, 'CREATE_FAST');
  await api.evidenceUploadApplicant(config.applicantSolicitorUser);
  await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
  await api.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'FAST_TRACK_TRIAL');
  await api.amendHearingDueDate(config.systemupdate);
  await api.hearingFeePaid(config.hearingCenterAdminWithRegionId1);
  if (['demo'].includes(config.runningEnv)) {
    await api.triggerBundle(config.systemupdate);
  }
  await api.createFinalOrder(config.judgeUserWithRegionId1, 'ASSISTED_ORDER');
}).tag('@civil-service-master @civil-service-pr @civil-camunda-master @civil-camunda-pr');

Scenario.skip('1v1 full defence unspecified - judge draws fast track WITHOUT sum of damages - hearing scheduled', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmountJudge);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, null, 'FAST_CLAIM');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  await api.createSDO(judgeUser, 'CREATE_FAST_NO_SUM');
  await api.evidenceUploadApplicant(config.applicantSolicitorUser);
  await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
  await api.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'FAST_TRACK_TRIAL');
  await api.amendHearingDueDate(config.systemupdate);
  await api.hearingFeeUnpaid(config.hearingCenterAdminWithRegionId1);
});

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
