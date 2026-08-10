const config = require('../../../config.js');
const { PUBLIC_QUERY } = require('../../../fixtures/queryTypes');

const claimAmountPenniesIntermediate = '9900000';
const claimAmountIntermediate = '99000';
const judgeUser = config.judgeUserWithRegionId1;
let caseId;

Feature('Query Management - Raise, Respond and Follow up Queries').tag('@civil-ccd-nightly @ui-qm');

Scenario('01 Claimant Follow up a query', async ({ api_spec, I, qmSteps }) => {
  const mpScenario = 'ONE_V_ONE';
  caseId = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'JUDICIAL_REFERRAL', false, true);
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
  let query;
  query = await qmSteps.raiseLRQuery(caseId, config.applicantSolicitorUser, PUBLIC_QUERY, false);
  await qmSteps.respondToQuery(caseId, config.ctscAdminUser, query, PUBLIC_QUERY);
  await I.login(config.applicantSolicitorUser);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.raiseFollowUpQuestionAndVerify(caseId, true);
}).retry(2);

Scenario('02 CaseWorker can access and also responds back to a query', async ({ I }) => {
  await I.login(config.ctscAdminUser);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyFollowUpQuestionAsCaseWorker(caseId, true);
}).retry(2);

Scenario('03 Judge can access to a query', async ({ I }) => {
  await I.login(config.judgeUserWithRegionId1);
  await I.navigateToCaseDetails(caseId);
  await I.waitForText('Summary');
  await I.verifyFollowUpQuestionAsJudge(caseId, true);
}).retry(2);

AfterSuite(async ({ api_spec }) => {
  await api_spec.cleanUp();
});
