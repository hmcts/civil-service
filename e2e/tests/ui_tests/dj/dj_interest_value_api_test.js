const config = require('../../../config.js');
const {createAccount} = require('../../../api/idamHelper');
const {checkToggleEnabled} = require('../../../api/testingSupport');

// Pure-API interest-value coverage for DTSCCI-3923: the claim and DJ request are driven through the CCD API and
// the request-time interest in activeJudgment.orderedAmount is asserted to the penny.
let judgmentBufferEnabled;

// Non-scheduler scenarios (interest value at request + the eligibility rejection) carry @ui-judgment-buffer so
// they run in the PR preview (no Elasticsearch needed). The full-flow scenarios (API-15..22) need the scheduler
// to grant via Elasticsearch, so they run in nightly (AAT) only via the Feature-level @civil-ccd-nightly tag.
Feature('DJ interest value - pure API (DTSCCI-3923)').tag('@civil-ccd-nightly @api-judgment-interest');

BeforeSuite(async () => {
  judgmentBufferEnabled = await checkToggleEnabled('judgment-buffer');
  if (!judgmentBufferEnabled) return;
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

const interestValueScenario = (label, opts, claimPounds, ratePct, fromDate, expectZero = false) =>
  Scenario(label, async ({api_spec_cui}) => {
    if (!judgmentBufferEnabled) return;
    await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', undefined, false, opts);
    await api_spec_cui.getCaseId();
    await api_spec_cui.amendRespondent1ResponseDeadline(config.systemupdate);
    await api_spec_cui.requestDefaultJudgmentSpecViaApi(config.applicantSolicitorUser);
    await api_spec_cui.verifyCaseState('JUDGMENT_REQUESTED');
    await api_spec_cui.verifyBufferStateInitialFields();
    await api_spec_cui.verifyJudgmentInterestValue(claimPounds, ratePct, fromDate, expectZero);
  }).retry(1).tag('@ui-judgment-buffer @api-judgment-interest');

interestValueScenario(
  'API-12 8% same-rate to fixed submit date - exact orderedAmount 159339p',
  {withInterest: true, sameRateInterestType: 'SAME_RATE_INTEREST_8_PC', interestClaimFrom: 'FROM_A_SPECIFIC_DATE', interestFromSpecificDate: '2024-01-01', interestClaimUntil: 'UNTIL_CLAIM_SUBMIT_DATE'},
  1500, 8, '2024-01-01');

interestValueScenario(
  'API-13 5% different rate - exact orderedAmount 155943p (lower than 8%)',
  {withInterest: true, sameRateInterestType: 'SAME_RATE_INTEREST_DIFFERENT_RATE', differentRate: '5', interestClaimFrom: 'FROM_A_SPECIFIC_DATE', interestFromSpecificDate: '2024-01-01', interestClaimUntil: 'UNTIL_CLAIM_SUBMIT_DATE'},
  1500, 5, '2024-01-01');

interestValueScenario(
  'API-14 zero interest (from == submit date) - orderedAmount == claim 150000p',
  {withInterest: true, sameRateInterestType: 'SAME_RATE_INTEREST_8_PC', interestClaimFrom: 'FROM_A_SPECIFIC_DATE', interestFromSpecificDate: '2024-10-10', interestClaimUntil: 'UNTIL_CLAIM_SUBMIT_DATE'},
  1500, 8, '2024-10-10', true);

// Full flow to All Final Orders Issued (pure-API request + scheduler grant): interest frozen request->grant,
// with issueDate set at grant rather than at request.
Scenario('API-15 8% interest - full flow to All_FINAL_ORDERS_ISSUED: interest frozen request->grant, issueDate=grant day', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  const interest = {withInterest: true, sameRateInterestType: 'SAME_RATE_INTEREST_8_PC', interestClaimFrom: 'FROM_A_SPECIFIC_DATE', interestFromSpecificDate: '2024-01-01', interestClaimUntil: 'UNTIL_CLAIM_SUBMIT_DATE'};
  await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', undefined, false, interest);
  await api_spec_cui.getCaseId();
  await api_spec_cui.amendRespondent1ResponseDeadline(config.systemupdate);
  await api_spec_cui.requestDefaultJudgmentSpecViaApi(config.applicantSolicitorUser);
  await api_spec_cui.verifyCaseState('JUDGMENT_REQUESTED');
  await api_spec_cui.verifyBufferStateInitialFields();
  await api_spec_cui.verifyJudgmentInterestValue(1500, 8, '2024-01-01'); // frozen at PENDING_ISSUE

  await api_spec_cui.amendJoDJCreatedDate(144);
  await api_spec_cui.runScheduler('JudgementBuffer');
  await api_spec_cui.verifyJudgmentBufferIssued(); // All_FINAL_ORDERS_ISSUED + issueDate + RTL=YES/rtlState=R + freeze
  await api_spec_cui.verifyJudgmentInterestValue(1500, 8, '2024-01-01'); // STILL frozen at ISSUED
}).retry(2).tag('@api-judgment-interest');

// Subsequent judgment lifecycle (post-issue, pure API): drive the buffered DJ to ISSUED, then the next event -
// SET_ASIDE, CANCELLED or SATISFIED.
const fullFlowToIssued = async (api_spec_cui) => {
  const interest = {withInterest: true, sameRateInterestType: 'SAME_RATE_INTEREST_8_PC', interestClaimFrom: 'FROM_A_SPECIFIC_DATE', interestFromSpecificDate: '2024-01-01', interestClaimUntil: 'UNTIL_CLAIM_SUBMIT_DATE'};
  await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', undefined, false, interest);
  await api_spec_cui.getCaseId();
  await api_spec_cui.amendRespondent1ResponseDeadline(config.systemupdate);
  await api_spec_cui.verifyCaseState('AWAITING_RESPONDENT_ACKNOWLEDGEMENT'); // pre-DJ precondition state
  await api_spec_cui.requestDefaultJudgmentSpecViaApi(config.applicantSolicitorUser);
  await api_spec_cui.verifyBufferStateInitialFields(); // buffer fields + RPA/CJES suppression + freeze snapshot
  await api_spec_cui.amendJoDJCreatedDate(144);
  await api_spec_cui.runScheduler('JudgementBuffer');
  await api_spec_cui.verifyJudgmentBufferIssued();
  await api_spec_cui.verifyActiveJudgmentState('ISSUED');
};

Scenario('API-16 full flow -> SET ASIDE (judgment moves to historicJudgment, state SET_ASIDE)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await fullFlowToIssued(api_spec_cui);
  await api_spec_cui.setAsideJudgmentViaApi(config.adminUser); // JUDGE_ORDER -> SET_ASIDE
  await api_spec_cui.verifyAnyJudgmentState('SET_ASIDE');
}).retry(2).tag('@api-judgment-lifecycle');

Scenario('API-17 full flow -> PAID same month as issue (ISSUED -> CANCELLED)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await fullFlowToIssued(api_spec_cui); // issueDate = today
  await api_spec_cui.markJudgmentPaidViaApi(config.adminUser); // paid today -> within a month -> CANCELLED
  await api_spec_cui.verifyAnyJudgmentState('CANCELLED');
}).retry(2).tag('@api-judgment-lifecycle');

Scenario('API-18 full flow -> PAID over a month after issue (ISSUED -> SATISFIED)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await fullFlowToIssued(api_spec_cui);
  await api_spec_cui.amendJudgmentIssueDate(40); // issued 40d ago so a payment today is > a month later
  await api_spec_cui.markJudgmentPaidViaApi(config.adminUser); // paid today -> SATISFIED
  await api_spec_cui.verifyAnyJudgmentState('SATISFIED');
}).retry(2).tag('@api-judgment-lifecycle');

Scenario('API-19 full flow -> EDIT JUDGMENT (ISSUED -> MODIFIED)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await fullFlowToIssued(api_spec_cui);
  await api_spec_cui.editJudgmentViaApi(config.adminUser);
  await api_spec_cui.verifyAnyJudgmentState('MODIFIED');
}).retry(2).tag('@api-judgment-lifecycle');

// Exclusion: an active/extended response deadline blocks the DJ request at the eligibility check, so the case
// never enters the buffer.
Scenario('API-EXT extension/active response deadline blocks DJ request (no buffer entry)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', undefined, false, {withInterest: false});
  await api_spec_cui.getCaseId();
  await api_spec_cui.setResponseDeadlineExtension(config.systemupdate, 60); // live/extended response window
  await api_spec_cui.requestDefaultJudgmentSpecExpectingRejection(config.applicantSolicitorUser);
}).retry(1).tag('@ui-judgment-buffer @api-judgment-lifecycle');

// Exact CANCELLED/SATISFIED boundary (rule: DAYS.between(issueDate, paidDate) > issueDate.lengthOfMonth()).
Scenario('API-20 paid exactly at the issue-month boundary (CANCELLED, just inside)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await fullFlowToIssued(api_spec_cui);
  const expected = await api_spec_cui.amendJudgmentIssueDateBoundary(0); // daysBetween == monthLen -> CANCELLED
  await api_spec_cui.markJudgmentPaidViaApi(config.adminUser);
  await api_spec_cui.verifyAnyJudgmentState(expected);
}).retry(2).tag('@api-judgment-lifecycle');

Scenario('API-21 paid one day past the issue-month boundary (SATISFIED, just outside)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await fullFlowToIssued(api_spec_cui);
  const expected = await api_spec_cui.amendJudgmentIssueDateBoundary(1); // daysBetween == monthLen+1 -> SATISFIED
  await api_spec_cui.markJudgmentPaidViaApi(config.adminUser);
  await api_spec_cui.verifyAnyJudgmentState(expected);
}).retry(2).tag('@api-judgment-lifecycle');

// Post-issue RPA/CJES delivery proof (hybrid): grant process COMPLETED (RPA+CJES tasks ran) + the RPA/CJES payloads
// mapped from the real issued case are correct. External SendGrid/CJES receipt stays out-of-band (CS unit tests).
Scenario('API-22 post-issue RPA/CJES: grant process completed + payloads correct', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await fullFlowToIssued(api_spec_cui);
  await api_spec_cui.verifyPostIssueDelivery();
}).retry(2).tag('@api-judgment-lifecycle @api-rpa-cjes');

AfterSuite(async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.cleanUp();
});
