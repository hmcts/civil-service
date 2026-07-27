const config = require('../../../config.js');

Feature('1v1 spec stay case api journey').tag('@civil-service-nightly @api-stay-case');

Scenario('1v1 LR FAST TRACK prepare for conduct hearing stay case', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponse(config.applicantSolicitorUser);
  await api_spec.createSDO(config.judgeUserWithRegionId1);
  await api_spec.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'FAST_TRACK_TRIAL');
  await api_spec.amendHearingDueDate(config.systemupdate);
  await api_spec.hearingFeePaid(config.hearingCenterAdminWithRegionId1);
  await api_spec.stayCase(config.hearingCenterAdminWithRegionId1);
  await api_spec.manageStay(config.hearingCenterAdminWithRegionId1, true);
  await api_spec.sendMessage(config.ctscAdminUser);
  await api_spec.replyMessage(config.judgeUserWithRegionId1);
  await api_spec.replyMessage(config.ctscAdminUser);
  await api_spec.manageStay(config.hearingCenterAdminWithRegionId1, false);
  await api_spec.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'FAST_TRACK_TRIAL');
  await api_spec.dismissCase(config.hearingCenterAdminWithRegionId1);
}).retry(1);

AfterSuite(async ({api_spec}) => {
  await api_spec.cleanUp();
});