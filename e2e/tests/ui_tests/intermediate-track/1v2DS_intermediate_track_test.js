const config = require('../../../config.js');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');

const mintiEnabled = true;
const claimAmountMulti = '200001';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
let civilCaseReference;

Feature('1v2DS unspec intermediate track journey').tag('@civil-ccd-nightly @ui-intermediate-track');

Scenario('1v2DS unspec intermediate track', async ({api, I}) => {
  const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
  civilCaseReference = await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmountMulti, mintiEnabled);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL', 'FOR_SDO', 'MULTI_CLAIM');
  await api.createFinalOrder(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'MULTI');
  await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
  await I.login(judgeUser);
  await I.initiateFinalOrder(civilCaseReference, 'Multi Track', 'Fix a date for CCMC');

  await I.login(config.secondDefendantSolicitorUser);
  await I.evidenceUpload(civilCaseReference, true, true);
}).retry(1);

AfterSuite(async  () => {
  await unAssignAllUsers();
});