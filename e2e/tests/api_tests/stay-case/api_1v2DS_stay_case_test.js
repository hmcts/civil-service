const config = require('../../../config.js');

const mintiEnabled = true;
const claimAmountMulti = '200001';

Feature('1v2DS spec stay case api journey').tag('@civil-service-nightly @api-stay-case');

Scenario('1v2DS Stay Case Judicial Referral', async ({api}) => {
  const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
  const judgeUser = config.judgeUserWithRegionId1;
  const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmountMulti, mintiEnabled);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL', 'FOR_SDO', 'MULTI_CLAIM');
  await api.stayCase(config.hearingCenterAdminWithRegionId1);
  await api.manageStay(config.hearingCenterAdminWithRegionId1, true);
  //commenting this for multi claim as send and reply is not enabled yet
  // await api.sendMessage(config.ctscAdminUser);
  // await api.replyMessage(config.judgeUserWithRegionId1);
  // await api.replyMessage(config.ctscAdminUser);
  await api.manageStay(config.hearingCenterAdminWithRegionId1, false, true);
  await api.createFinalOrder(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'MULTI');
  await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
  await api.dismissCase(config.hearingCenterAdminWithRegionId1);
}).retry(1);

AfterSuite(async ({api}) => {
  await api.cleanUp();
});


