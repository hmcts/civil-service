const config = require('../../../config.js');

const claimAmountPenniesIntermediate = '9900000';
const claimAmountIntermediate = '99000';
const judgeUser = config.judgeUserWithRegionId1;
let caseId;

Feature('Query Management - Non Hearing E2E journey @qm-spec @non-prod-e2e-ft @e2e-nightly-prod');

async function prepareClaim(api_spec, mpScenario) {
  caseId = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'JUDICIAL_REFERRAL', false, true);
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
}

Scenario('Claimant LR raises a query', async ({ api_spec, I }) => {
  const mpScenario = 'ONE_V_ONE';
  await prepareClaim(api_spec, mpScenario);
  await I.login(config.applicantSolicitorUser);
  await I.raiseNewNonHearingQuery(caseId);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetails();
}).retry(2);

Scenario('Defendant LR raises a query', async ({ I }) => {
  await I.login(config.defendantSolicitorUser);
  await I.raiseNewNonHearingQuery(caseId);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetails();
}).retry(2);

Scenario('CaseWorker can access and also responds back to a query', async ({ I }) => {
  await I.login(config.ctscAdminUser);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetailsAsCaseWorker();
}).retry(2);

Scenario('Judge can access to a query', async ({ I }) => {
  await I.login(config.judgeUserWithRegionId1);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetails();
}).retry(2);

Scenario('Take claim offline', async ({ I }) => {
  await I.login(config.adminUser);
  await I.caseProceedsInCaseman(caseId);
}).retry(2);

Scenario('Offline case - Claimant cant raise a query', async ({ I }) => {
  await I.login(config.applicantSolicitorUser);
  await I.raiseNewQueryInOfflineState(caseId);
  await I.waitForText('Enter query details');
  await I.waitForText('Errors');
  await I.see('If your case is offline, you cannot raise a query.');
}).retry(2);

AfterSuite(async ({ api_spec }) => {
  await api_spec.cleanUp();
});
