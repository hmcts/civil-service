const config = require('../../../config.js');
let caseId, validSummaryJudgmentDirectionsTask, validScheduleAHearingTask;

if (config.runWAApiTest) {
  validSummaryJudgmentDirectionsTask = require('../../../../wa/tasks/summaryJudgmentDirectionsTask.js');
  validScheduleAHearingTask = require('../../../../wa/tasks/scheduleADisposalHearing.js');
}
const mpScenarioOtherRemedy = 'ONE_V_ONE_OTHER_REMEDY';

Feature('1v1 Unspec Other Remedy default judgment').tag('@civil-ccd-nightly @ui-other-remedy-dj');

Scenario('01 Create 1v1 other remedy claim, notify claim, notify claim details, request default judgement', async ({I, api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenarioOtherRemedy, '11000');
  caseId = await api.getCaseId();

  //below amend claim documents only needed as assertion was failing on notify claims
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.amendRespondent1ResponseDeadline(config.systemupdate);
  await I.login(config.applicantSolicitorUser);
  await I.initiateDJUnspec(caseId, mpScenarioOtherRemedy);
}).retry(2);

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
