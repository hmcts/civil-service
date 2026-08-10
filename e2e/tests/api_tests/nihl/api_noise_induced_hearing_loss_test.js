const config = require('../../../config.js');

const mpScenario1v1 = 'ONE_V_ONE';
const mpScenario1v1Nihl = 'ONE_V_ONE_NIHL';
const claimAmount = '11000';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
// To use on local because the idam images are different
//const judgeUser = config.judgeUserWithRegionId1Local;
//const hearingCenterAdminToBeUsed = config.hearingCenterAdminLocal;

Feature('Noise Induced Hearing Loss API test - fast claim - unspec').tag('@civil-service-nightly @api-nihl');

Scenario('1v1 unspec create SDO for Noise Induced Hearing Loss', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario1v1Nihl, claimAmount);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario1v1, null, 'FAST_CLAIM_NIHL');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario1v1, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  await api.createSDO(judgeUser, 'CREATE_FAST_NIHL');
  await api.evidenceUploadApplicant(config.applicantSolicitorUser);
  await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario1v1);
  await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
  await api.amendHearingDueDate(config.systemupdate);
  await api.hearingFeePaid(hearingCenterAdminToBeUsed);
  if (['demo'].includes(config.runningEnv)) {
      await api.triggerBundle(config.systemupdate);
    }
  await api.createFinalOrder(judgeUser, 'ASSISTED_ORDER');
});

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
