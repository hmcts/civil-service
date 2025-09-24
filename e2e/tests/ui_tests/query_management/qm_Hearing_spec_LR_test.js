const config = require('../../../config.js');

const claimAmountPenniesIntermediate = '9900000';
const claimAmountIntermediate = '99000';
const judgeUser = config.judgeUserWithRegionId1;
let caseId;

Feature('Query Management - Hearing E2E journey @non-prod-e2e-ft @qm-spec');

async function prepareClaim(api_spec) {
  caseId = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO', false, true, claimAmountPenniesIntermediate);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL', 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT', false, true, claimAmountIntermediate);
  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL', 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'JUDICIAL_REFERRAL', false, true);
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
}

Scenario('Claimant LR raises a query', async ({ api_spec, I }) => {
  await prepareClaim(api_spec);
  await I.login(config.applicantSolicitorUser);
  await I.raiseNewHearingQuery(caseId);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetails(true);
}).retry(2);

Scenario('Defendant LR raises a query', async ({ I }) => {
  await I.login(config.defendantSolicitorUser);
  await I.raiseNewHearingQuery(caseId);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetails(true);
}).retry(2);

Scenario('Hearing centre admin can access and also responds back to a query', async ({ I }) => {
  await I.login(config.hearingCenterAdminWithRegionId1);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetailsAsCaseWorker(true);
}).retry(2);

Scenario('Judge can access to a query', async ({ I }) => {
  await I.login(config.judgeUserWithRegionId1);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetails(true);
}).retry(2);

Scenario('Take claim offline', async ({ I }) => {
  await I.login(config.adminUser);
  await I.caseProceedsInCaseman(caseId);
}).retry(2);

Scenario('Offline case - Claimant cant raise a query', async ({ I }) => {
  await I.login(config.applicantSolicitorUser);
  await I.raiseNewQueryInOfflineState(caseId);
  await I.waitForText('Enter query details');
  await I.see('If your case is offline, you cannot raise a query.');
}).retry(2);

AfterSuite(async ({ api_spec }) => {
  await api_spec.cleanUp();
});
