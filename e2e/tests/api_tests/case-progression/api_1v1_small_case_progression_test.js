const config = require('../../../config.js');

const mpScenario = 'ONE_V_ONE';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;

const claimAmountJudge = '11000';

Feature('1v1 small track case progression api journeys').tag('@civil-service-nightly @api-case-progression');

Scenario.skip('1v1 full defence unspecified - judge draws small claims WITH sum of damages - hearing scheduled', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmountJudge);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, null, 'FAST_CLAIM');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
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
});

Scenario.skip('1v1 full defence unspecified - judge draws small claims WITHOUT sum of damages - hearing scheduled', async ({ api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmountJudge);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, null, 'FAST_CLAIM');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  await api.createSDO(judgeUser, 'CREATE_SMALL_NO_SUM');
  await api.evidenceUploadApplicant(config.applicantSolicitorUser);
  await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
  await api.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'SMALL_CLAIMS');
  await api.amendHearingDueDate(config.systemupdate);
  await api.hearingFeePaid(config.hearingCenterAdminWithRegionId1);
  if (['demo'].includes(config.runningEnv)) {
    await api.triggerBundle(config.systemupdate);
  }
});

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
