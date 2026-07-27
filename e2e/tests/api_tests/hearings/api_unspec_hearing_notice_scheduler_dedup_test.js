const config = require('../../../config');

Feature('Unspec automated hearing notice scheduler - duplicate detection').tag('@civil-service-pr @api-hearings');

const judgeUser = config.judgeUserWithRegionId1;
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

Scenario('01 Create Unspec claim with SDO', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', '11000');
  caseId = await api.getCaseId();
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, 'ONE_V_ONE', null, 'FAST_CLAIM');
  await api.claimantResponse(config.applicantSolicitorUser, 'ONE_V_ONE', 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  await api.createSDO(judgeUser, 'CREATE_FAST');
}).retry(3);

Scenario('02 Generate hearing notice for a single HMC response', async ({hearings}) => {
  const h = await hearings.createUnspecDisposalHearingV(caseId, 1);
  await hearings.setPartiesNotifiedResponses([]);
  await hearings.triggerUnspecAutomatedHearingNoticeScheduler(h.hearingId);
}).retry(3);

Scenario('03 Skip hearing notice when the current version is already notified', async ({hearings}) => {
  const h = await hearings.createUnspecTrialHearingV(caseId, 1);
  const sched = hearings.listedHearingSchedule();
  await hearings.setPartiesNotifiedResponses([
    {requestVersion: 1, responseReceivedDateTime: h.receivedDateTime, serviceData: serviceData(sched)}
  ]);
  await hearings.triggerUnspecSchedulerExpectingNoNotice(h.hearingId);
}).retry(3);

Scenario('04 Notify only the current version when multiple HMC responses exist', async ({hearings}) => {
  const h = await hearings.createUnspecDisposalHearingV(caseId, 2);
  await hearings.setPartiesNotifiedResponses([
    {requestVersion: 1, responseReceivedDateTime: plus1d(h.receivedDateTime), serviceData: serviceData(changedSchedule())},
    {requestVersion: 2, responseReceivedDateTime: minus1d(h.receivedDateTime), serviceData: serviceData(changedSchedule())}
  ]);
  await hearings.triggerUnspecAutomatedHearingNoticeScheduler(h.hearingId);
}).retry(3);

Scenario('05 Generate hearing notice for a re-listed hearing version', async ({hearings}) => {
  const h = await hearings.createUnspecTrialHearingV(caseId, 2);
  await hearings.setPartiesNotifiedResponses([
    {requestVersion: 1, responseReceivedDateTime: minus1d(h.receivedDateTime), serviceData: serviceData(changedSchedule())}
  ]);
  await hearings.triggerUnspecAutomatedHearingNoticeScheduler(h.hearingId);
}).retry(3);

Scenario('06 Acknowledge an unchanged hearing without generating a notice', async ({hearings}) => {
  const h = await hearings.createUnspecDisposalHearingV(caseId, 1);
  const sched = hearings.listedHearingSchedule();
  await hearings.setPartiesNotifiedResponses([
    {requestVersion: 1, responseReceivedDateTime: minus1d(h.receivedDateTime), serviceData: serviceData(sched)}
  ]);
  await hearings.triggerUnspecSchedulerExpectingNoNotice(h.hearingId);
}).retry(3);

Scenario('07 Avoid a duplicate hearing notice when the scheduler re-runs', async ({hearings}) => {
  const h = await hearings.createUnspecDisposalHearingV(caseId, 1);
  const sched = hearings.listedHearingSchedule();
  await hearings.setPartiesNotifiedResponses([]);
  await hearings.triggerUnspecAutomatedHearingNoticeScheduler(h.hearingId);
  await hearings.setPartiesNotifiedResponses([
    {requestVersion: 1, responseReceivedDateTime: h.receivedDateTime, serviceData: serviceData(sched)}
  ]);
  await hearings.triggerUnspecSchedulerExpectingNoNotice(h.hearingId);
}).retry(3);

Scenario('08 Handle partial HMC response data and still generate the notice', async ({hearings}) => {
  const h = await hearings.createUnspecDisposalHearingV(caseId, 1);
  await hearings.setPartiesNotifiedResponses([
    {responseReceivedDateTime: null, serviceData: serviceData(changedSchedule())}
  ]);
  await hearings.triggerUnspecAutomatedHearingNoticeScheduler(h.hearingId);
}).retry(3);

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
