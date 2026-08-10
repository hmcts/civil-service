const config = require('../../../config');

Feature('Spec automated hearing notice scheduler - duplicate detection').tag('@civil-service-pr @api-hearings');

let caseId;

const minus1d = d => new Date(new Date(d).getTime() - 86400000).toISOString();
const plus1d = d => new Date(new Date(d).getTime() + 86400000).toISOString();
const changedSchedule = () => ({hearingStartDateTime: '2030-01-01T09:00:00Z', hearingEndDateTime: '2030-01-01T16:00:00Z', hearingVenueId: '000000'});
const serviceData = s => ({
  hearingNoticeGenerated: true,
  hearingLocation: s.hearingVenueId,
  days: [{hearingStartDateTime: s.hearingStartDateTime, hearingEndDateTime: s.hearingEndDateTime}]
});

BeforeSuite(async ({hearings}) => {
  await hearings.setupStaticMocks();
});

Scenario('01 Create Spec claim with SDO', async ({api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, false);
  caseId = await api_spec_small.getCaseId();
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE');
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', false);
  await api_spec_small.createSDO(config.judgeUser2WithRegionId2, 'CREATE_SMALL', true);
}).retry(3);

Scenario('02 Generate hearing notice for a single HMC response', async ({hearings}) => {
  const h = await hearings.createSpecDisposalHearingV(caseId, 1);
  await hearings.setPartiesNotifiedResponses([]);
  await hearings.triggerSpecAutomatedHearingNoticeScheduler(h.hearingId);
}).retry(3);

Scenario('03 Skip hearing notice when the current version is already notified', async ({hearings}) => {
  const h = await hearings.createSpecTrialHearingV(caseId, 1);
  const sched = hearings.listedHearingSchedule();
  await hearings.setPartiesNotifiedResponses([
    {requestVersion: 1, responseReceivedDateTime: h.receivedDateTime, serviceData: serviceData(sched)}
  ]);
  await hearings.triggerSpecSchedulerExpectingNoNotice(h.hearingId);
}).retry(3);

Scenario('04 Notify only the current version when multiple HMC responses exist', async ({hearings}) => {
  const h = await hearings.createSpecDisposalHearingV(caseId, 2);
  await hearings.setPartiesNotifiedResponses([
    {requestVersion: 1, responseReceivedDateTime: plus1d(h.receivedDateTime), serviceData: serviceData(changedSchedule())},
    {requestVersion: 2, responseReceivedDateTime: minus1d(h.receivedDateTime), serviceData: serviceData(changedSchedule())}
  ]);
  await hearings.triggerSpecAutomatedHearingNoticeScheduler(h.hearingId);
}).retry(3);

Scenario('05 Generate hearing notice for a re-listed hearing version', async ({hearings}) => {
  const h = await hearings.createSpecTrialHearingV(caseId, 2);
  await hearings.setPartiesNotifiedResponses([
    {requestVersion: 1, responseReceivedDateTime: minus1d(h.receivedDateTime), serviceData: serviceData(changedSchedule())}
  ]);
  await hearings.triggerSpecAutomatedHearingNoticeScheduler(h.hearingId);
}).retry(3);

Scenario('06 Acknowledge an unchanged hearing without generating a notice', async ({hearings}) => {
  const h = await hearings.createSpecDisposalHearingV(caseId, 1);
  const sched = hearings.listedHearingSchedule();
  await hearings.setPartiesNotifiedResponses([
    {requestVersion: 1, responseReceivedDateTime: minus1d(h.receivedDateTime), serviceData: serviceData(sched)}
  ]);
  await hearings.triggerSpecSchedulerExpectingNoNotice(h.hearingId);
}).retry(3);

Scenario('07 Avoid a duplicate hearing notice when the scheduler re-runs', async ({hearings}) => {
  const h = await hearings.createSpecDisposalHearingV(caseId, 1);
  const sched = hearings.listedHearingSchedule();
  await hearings.setPartiesNotifiedResponses([]);
  await hearings.triggerSpecAutomatedHearingNoticeScheduler(h.hearingId);
  await hearings.setPartiesNotifiedResponses([
    {requestVersion: 1, responseReceivedDateTime: h.receivedDateTime, serviceData: serviceData(sched)}
  ]);
  await hearings.triggerSpecSchedulerExpectingNoNotice(h.hearingId);
}).retry(3);

Scenario('08 Handle partial HMC response data and still generate the notice', async ({hearings}) => {
  const h = await hearings.createSpecDisposalHearingV(caseId, 1);
  await hearings.setPartiesNotifiedResponses([
    {responseReceivedDateTime: null, serviceData: serviceData(changedSchedule())}
  ]);
  await hearings.triggerSpecAutomatedHearingNoticeScheduler(h.hearingId);
}).retry(3);

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
