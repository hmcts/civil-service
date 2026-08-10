const config = require('../../../config.js');

let caseId;

Feature('1v2SS spec stay case api journey').tag('@civil-service-nightly @api-stay-case');

Scenario('1v2SS LR UNSPEC claim hearing readiness', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP');
  await api.notifyClaim(config.applicantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP');
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  caseId = await api.getCaseId();
  await api.amendRespondent1ResponseDeadline(config.systemupdate);
  await api.defaultJudgment(config.applicantSolicitorUser, 'TRIAL_HEARING');
  await api.sdoDefaultJudgment(config.judgeUserWithRegionId1, 'TRIAL_HEARING');
  await api.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'OTHER');
  await api.stayCase(config.hearingCenterAdminWithRegionId1);
  await api.manageStay(config.hearingCenterAdminWithRegionId1, true);
  await api.sendMessage(config.ctscAdminUser);
  await api.replyMessage(config.judgeUserWithRegionId1);
  await api.replyMessage(config.ctscAdminUser);
  await api.manageStay(config.hearingCenterAdminWithRegionId1, false);
  await api.dismissCase(config.hearingCenterAdminWithRegionId1);
}).retry(1);

AfterSuite(async ({api}) => {
  await api.cleanUp();
});