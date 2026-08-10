const config = require('../../../config.js');

const claimAmountPenniesIntermediate = '9900000';
const claimAmountIntermediate = '99000';
const judgeUser = config.judgeUserWithRegionId1;
let caseId;

Feature('Query Management - Non Hearing E2E journey').tag('@civil-ccd-nightly @ui-qm');

Scenario('01 Claimant LR raises a query', async ({ api_spec, I }) => {
  const mpScenario = 'ONE_V_ONE';
  caseId = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'JUDICIAL_REFERRAL', false, true);
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
  await I.login(config.applicantSolicitorUser);
  await I.raiseNewNonHearingQuery(caseId);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetails(caseId);
}).retry(2);

Scenario('02 Defendant LR raises a query', async ({ I }) => {
  await I.login(config.defendantSolicitorUser);
  await I.raiseNewNonHearingQuery(caseId);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetails(caseId);
}).retry(2);

Scenario('03 CaseWorker can access and also responds back to a query', async ({ I }) => {
  await I.login(config.ctscAdminUser);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetailsAsCaseWorker(caseId);
}).retry(2);

Scenario('04 Judge can access to a query', async ({ I }) => {
  await I.login(config.judgeUserWithRegionId1);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyQueriesDetails(caseId);
}).retry(2);

Scenario('05 Take claim offline', async ({ I }) => {
  await I.login(config.adminUser);
  await I.caseProceedsInCaseman(caseId);
}).retry(2);

Scenario('06 Offline case - Claimant cant raise a query', async ({ I }) => {
  await I.login(config.applicantSolicitorUser);
  await I.raiseNewQueryInOfflineState(caseId);
  await I.waitForText('Enter query details');
  await I.waitForText('Errors');
  await I.waitForText('If your case is offline, you cannot raise a query.');
}).retry(2);

AfterSuite(async ({ api_spec }) => {
  await api_spec.cleanUp();
});
