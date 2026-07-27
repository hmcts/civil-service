const config = require('../../../config.js');
const {createAccount} = require('../../../api/idamHelper');
const {checkToggleEnabled} = require('../../../api/testingSupport');
const {assert} = require('chai');

// 1v1 spec default judgment with the judgment buffer (LR claimant vs LiP defendant). Covers the buffer state
// machine, the activeJudgment fields, the request->grant interest freeze and the scheduler exclusions. RPA and
// CJES are asserted at the process/data layer (suppressed during the buffer, the grant process runs at issue);
// their external SendGrid email / CJES POST delivery is covered by the civil-service unit tests.

let caseid;
let judgmentBufferEnabled;

// Scheduler-expiry scenarios need Elasticsearch (the scheduler searches ES) so they run in nightly (AAT) only.
// The non-scheduler core (request -> buffer state, event options, registry guard) is tagged @ui-judgment-buffer
// so it also runs in the PR preview, which does not have Elasticsearch enabled.
Feature('1v1 spec default judgment - Judgment Buffer (LR claimant vs LiP defendant)')
  .tag('@civil-ccd-nightly @ui-dj');

BeforeSuite(async () => {
  judgmentBufferEnabled = await checkToggleEnabled('judgment-buffer');
  if (!judgmentBufferEnabled) return;
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('01 Create LRvLiP spec claim, LR claimant requests DJ - case parks in JUDGMENT_REQUESTED with buffer confirmation (DTSCCI-5327 AC1, DTSCCI-5101 AC1)', async ({I, api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  caseid = await api_spec_cui.getCaseId();
  await api_spec_cui.amendRespondent1ResponseDeadline(config.systemupdate);
  await api_spec_cui.verifyCaseState('AWAITING_RESPONDENT_ACKNOWLEDGEMENT'); // pre-DJ precondition state

  await I.login(config.applicantSolicitorUser);
  await I.initiateDJSpec(caseid, 'ONE_V_ONE', 'UNSPEC', 'Default judgment requested');

  await api_spec_cui.verifyCaseState('JUDGMENT_REQUESTED');
  await api_spec_cui.verifyBufferStateInitialFields();
}).retry(2).tag('@ui-judgment-buffer');

Scenario('02 Claimant LR retains event options in JUDGMENT_REQUESTED (DTSCCI-5327 AC2)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.verifyEventsAvailable(config.applicantSolicitorUser, 'JUDGMENT_REQUESTED');
  await api_spec_cui.verifyEventsAvailable(config.adminUser, 'JUDGMENT_REQUESTED');
}).retry(2).tag('@ui-judgment-buffer');

Scenario('03 LiP defendant submits defence during buffer - pending DJ cancelled, case moves to AWAITING_APPLICANT_INTENTION (DTSCCI-5101 AC1+AC2)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseid, 'SmallClaims', false, '');
  await api_spec_cui.verifyCaseState('AWAITING_APPLICANT_INTENTION');
  await api_spec_cui.verifyActiveJudgmentCancelled();
  await api_spec_cui.verifyJudgmentNeverRegistered(); // defence-cancel: DJ never registered -> RPA/CJES never fired
  await api_spec_cui.amendJoDJCreatedDate(144);
  await api_spec_cui.runScheduler('JudgementBuffer');
  await api_spec_cui.verifyBufferNotIssuedAfterScheduler('AWAITING_APPLICANT_INTENTION');
}).retry(2);

Scenario('04 Defence-during-buffer triggers defendant-response notifications to both parties (DTSCCI-5101 AC6)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.verifyDefendantResponseNotificationsSent(caseid);
}).retry(2);

Scenario('05 Scheduler issues buffered DJ once 48h+ buffer expires - full end-to-end (DTSCCI-5436 positive)', async ({I, api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  const buffCaseId = await api_spec_cui.getCaseId();
  await api_spec_cui.amendRespondent1ResponseDeadline(config.systemupdate);

  await I.login(config.applicantSolicitorUser);
  await I.initiateDJSpec(buffCaseId, 'ONE_V_ONE', 'UNSPEC', 'Default judgment requested');
  await api_spec_cui.verifyCaseState('JUDGMENT_REQUESTED');
  await api_spec_cui.waitForBusinessProcessFinished();
  await api_spec_cui.verifyBufferStateInitialFields();

  const schedulers = await api_spec_cui.listSchedulers();
  assert.include(schedulers, 'JudgementBuffer',
    `JudgementBuffer scheduler not registered. Got: ${JSON.stringify(schedulers)}`);

  await api_spec_cui.amendJoDJCreatedDate(144);
  await new Promise(resolve => setTimeout(resolve, 5000));

  await api_spec_cui.runScheduler('JudgementBuffer');
  await api_spec_cui.verifyJudgmentBufferIssued();
  await api_spec_cui.verifyDjGrantNotificationsSent(buffCaseId);
}).retry(2);

Scenario('06 Scheduler does NOT issue DJ when buffer not yet expired (<48h) (DTSCCI-5436 negative - time gate)', async ({I, api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  const withinBufferCaseId = await api_spec_cui.getCaseId();
  await api_spec_cui.amendRespondent1ResponseDeadline(config.systemupdate);

  await I.login(config.applicantSolicitorUser);
  await I.initiateDJSpec(withinBufferCaseId, 'ONE_V_ONE', 'UNSPEC', 'Default judgment requested');
  await api_spec_cui.verifyCaseState('JUDGMENT_REQUESTED');
  await api_spec_cui.waitForBusinessProcessFinished();

  await api_spec_cui.amendJoDJCreatedDate(40);
  await new Promise(resolve => setTimeout(resolve, 5000));

  await api_spec_cui.runScheduler('JudgementBuffer');
  await api_spec_cui.verifyJudgmentBufferUnchanged();
}).retry(2);

Scenario('07 Scheduler endpoint rejects unknown scheduler name with 404 (DTSCCI-5436 negative - registry guard)', async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.runSchedulerExpectingNotFound('NotARealScheduler');
}).retry(2).tag('@ui-judgment-buffer');

Scenario('08 CTSC takes case offline during buffer - case proceeds offline to PROCEEDS_IN_HERITAGE_SYSTEM (DTSCCI-3661 AC4)', async ({I, api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  const offlineCaseId = await api_spec_cui.getCaseId();
  await api_spec_cui.amendRespondent1ResponseDeadline(config.systemupdate);

  await I.login(config.applicantSolicitorUser);
  await I.initiateDJSpec(offlineCaseId, 'ONE_V_ONE', 'UNSPEC', 'Default judgment requested');
  await api_spec_cui.verifyCaseState('JUDGMENT_REQUESTED');
  await api_spec_cui.waitForBusinessProcessFinished();

  await I.login(config.adminUser);
  await I.caseProceedsInCaseman(offlineCaseId);
  await api_spec_cui.waitForBusinessProcessFinished();

  await api_spec_cui.verifyCaseState('PROCEEDS_IN_HERITAGE_SYSTEM');
  await api_spec_cui.reportState('offline-branch'); // observe park-vs-clear of the pending judgment when offline
  await api_spec_cui.verifyJudgmentNeverRegistered(); // DJ never issued/registered offline -> RPA/CJES never fired
  await api_spec_cui.amendJoDJCreatedDate(144);
  await api_spec_cui.runScheduler('JudgementBuffer');
  await api_spec_cui.verifyBufferNotIssuedAfterScheduler('PROCEEDS_IN_HERITAGE_SYSTEM');
  await api_spec_cui.verifyJudgmentNeverRegistered(); // still never registered after the scheduler skips it
}).retry(2);

Scenario('09 DJ with instalment payment plan - instalmentDetails captured at request and preserved at grant (DTSCCI-5061 field coverage)', async ({I, api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  const instCaseId = await api_spec_cui.getCaseId();
  await api_spec_cui.amendRespondent1ResponseDeadline(config.systemupdate);

  await I.login(config.applicantSolicitorUser);
  await I.initiateDJSpec(instCaseId, 'ONE_V_ONE', 'UNSPEC', 'Default judgment requested', 'repaymentPlan');
  await api_spec_cui.verifyCaseState('JUDGMENT_REQUESTED');
  await api_spec_cui.waitForBusinessProcessFinished();
  await api_spec_cui.verifyBufferStateInitialFields();
  await api_spec_cui.verifyInstalmentDetails('MONTHLY');

  await api_spec_cui.amendJoDJCreatedDate(144);
  await api_spec_cui.runScheduler('JudgementBuffer');
  await api_spec_cui.verifyJudgmentBufferIssued();
  await api_spec_cui.verifyInstalmentDetails('MONTHLY');
}).retry(2);

Scenario('10 Discontinued during buffer - case never issued by scheduler (DTSCCI-3663 AC1 exclusion)', async ({I, api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  const discCaseId = await api_spec_cui.getCaseId();
  await api_spec_cui.amendRespondent1ResponseDeadline(config.systemupdate);

  await I.login(config.applicantSolicitorUser);
  await I.initiateDJSpec(discCaseId, 'ONE_V_ONE', 'UNSPEC', 'Default judgment requested');
  await api_spec_cui.verifyCaseState('JUDGMENT_REQUESTED');
  await api_spec_cui.waitForBusinessProcessFinished();

  await api_spec_cui.discontinueClaim(config.applicantSolicitorUser, 'ONE_V_ONE_NO_P_NEEDED');
  await api_spec_cui.verifyCaseState('CASE_DISCONTINUED');

  await api_spec_cui.amendJoDJCreatedDate(144);
  await api_spec_cui.runScheduler('JudgementBuffer');
  await api_spec_cui.verifyBufferNotIssuedAfterScheduler('CASE_DISCONTINUED');
}).retry(2);

AfterSuite(async ({api_spec_cui}) => {
  if (!judgmentBufferEnabled) return;
  await api_spec_cui.cleanUp();
});
