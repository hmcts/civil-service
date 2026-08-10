const config = require('../../../config');

Feature('Unspec automated hearing notice schedulers');

const judgeUser = config.judgeUserWithRegionId1;

let caseId;

BeforeSuite(async ({hearings}) => {
  await hearings.setupStaticMocks();
});

Scenario('01 Prepare unspec claim up to sdo', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', '11000');
  caseId = await api.getCaseId();
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, 'ONE_V_ONE', null, 'FAST_CLAIM');
  await api.claimantResponse(config.applicantSolicitorUser, 'ONE_V_ONE', 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  await api.createSDO(judgeUser, 'CREATE_FAST');
}).retry(3);

Scenario('02 Generate Unspec Disposal hearing notice', async ({hearings}) => {
  const hearingId = await hearings.createUnspecDisposalHearing(caseId);
  await hearings.triggerUnspecAutomatedHearingNoticeScheduler(hearingId);
}).retry(3);

Scenario('03 Generate Unspec Trial hearing notice', async ({hearings}) => {
    const hearingId = await hearings.createUnspecTrialHearing(caseId);
    await hearings.triggerUnspecAutomatedHearingNoticeScheduler(hearingId);
}).retry(3);

Scenario('04 Generate Unspec Dispute Resolution hearing notice', async ({hearings}) => {
  const hearingId = await hearings.createUnspecDisputeResolutionHearing(caseId);
  await hearings.triggerUnspecAutomatedHearingNoticeScheduler(hearingId);
}).retry(3);

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
