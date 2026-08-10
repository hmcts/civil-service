const config = require('../../../config.js');
const claimAmountPenniesMulti = '20000001';
const claimAmountMulti = '200001';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
let civilCaseReference;

Feature('1v2SS spec multi track journey').tag('@civil-ccd-nightly @ui-multi-track');

Scenario('1v2SS spec multi track', async ({ api_spec, I }) => {
  const mpScenario = 'ONE_V_TWO_SAME_SOL';
  civilCaseReference = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesMulti);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountMulti);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'JUDICIAL_REFERRAL', false, true);
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'MULTI');
  await api_spec.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
  await api_spec.amendHearingDueDate(config.systemupdate);
  await api_spec.hearingFeePaid(hearingCenterAdminToBeUsed);
  await I.login(config.applicantSolicitorUser);
  await I.evidenceUpload(civilCaseReference, false, true);
  await I.login(config.defendantSolicitorUser);
  await I.evidenceUpload(civilCaseReference, true, true, true, 'ONE_V_TWO_ONE_LEGAL_REP');
  await I.login(config.applicantSolicitorUser);
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + civilCaseReference);
  await I.waitForText('Summary');
  await I.verifyBundleDetails(civilCaseReference);
}).retry(2);

AfterSuite(async ({ api_spec }) => {
  await api_spec.cleanUp();
});
