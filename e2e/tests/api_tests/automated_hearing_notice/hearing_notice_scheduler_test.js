const config = require('../../../config');

Feature('Automated hearing notice schedulers @api-nonprod @AHN');

const judgeUser = config.judgeUserWithRegionId1;

let caseId;

BeforeSuite(async ({hearings}) => {
  await hearings.setupStaticMocks();
});

Scenario('Create Unspec claim with sdo', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', '11000');
  caseId = await api.getCaseId();
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, 'ONE_V_ONE', null, 'FAST_CLAIM');
  await api.claimantResponse(config.applicantSolicitorUser, 'ONE_V_ONE', 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  await api.createSDO(judgeUser, 'CREATE_FAST');
}).retry(3);

Scenario('Generate Unspec Disposal hearing notice', async ({hearings}) => {
  const hearingId = await hearings.createUnspecDisposalHearing(caseId);
  await hearings.triggerUnspecAutomatedHearingNoticeScheduler(hearingId);
}).retry(3);

Scenario('Generate Unspec Trial hearing notice', async ({hearings}) => {
    const hearingId = await hearings.createUnspecTrialHearing(caseId);
    await hearings.triggerUnspecAutomatedHearingNoticeScheduler(hearingId);
}).retry(3);

Scenario('Generate Unspec Dispute Resolution hearing notice', async ({hearings}) => {
  const hearingId = await hearings.createUnspecDisputeResolutionHearing(caseId);
  await hearings.triggerUnspecAutomatedHearingNoticeScheduler(hearingId);
}).retry(3);

Scenario('Create Spec claim with SDO', async ({api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, false);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE');
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', false);
  await api_spec_small.createSDO(config.judgeUser2WithRegionId2, 'CREATE_SMALL', true);
}).retry(3);

Scenario('Generate Spec Disposal hearing notice', async ({hearings}) => {
  const hearingId = await hearings.createSpecDisposalHearing(caseId);
  await hearings.triggerSpecAutomatedHearingNoticeScheduler(hearingId);
}).retry(3);

Scenario('Generate Spec Trial hearing notice', async ({hearings}) => {
  const hearingId = await hearings.createSpecTrialHearing(caseId);
  await hearings.triggerSpecAutomatedHearingNoticeScheduler(hearingId);
}).retry(3);

Scenario('Generate Spec Dispute Resolution hearing notice', async ({hearings}) => {
  const hearingId = await hearings.createSpecDisputeResolutionHearing(caseId);
  await hearings.triggerSpecAutomatedHearingNoticeScheduler(hearingId);
}).retry(3);

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
