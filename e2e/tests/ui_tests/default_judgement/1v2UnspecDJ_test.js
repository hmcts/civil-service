

const config = require('../../../config.js');

Feature('1v2 Unspec defaultJudgement @e2e-dj-unspec');

let validSummaryJudgmentDirectionsTask;

if (config.runWAApiTest) {
  validSummaryJudgmentDirectionsTask = require('../../../../wa/tasks/summaryJudgmentDirectionsTask.js');
}

Scenario('DefaultJudgement @create-claim @e2e-nightly-prod', async ({I, api, WA}) => {

  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_TWO_LEGAL_REP');
  let caseid = await api.getCaseId();

  //below amend claim documents only needed as assertion was failing on notify claims
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.amendRespondent1ResponseDeadline(config.systemupdate);
  await api.amendRespondent2ResponseDeadline(config.systemupdate);
  await I.login(config.applicantSolicitorUser);
  await I.initiateDJUnspec(caseid, 'ONE_V_TWO');
  await I.login(config.judgeUserWithRegionId1);
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseid);
  await I.waitForText('Summary');
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseid + '/trigger/EVIDENCE_UPLOAD_JUDGE/EVIDENCE_UPLOAD_JUDGE');
    await I.waitForText('How do you want to add a case note?');
    await I.judgeAddsCaseNotes();
    await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseid);
    await I.waitForText('Summary');
  }

  let taskId;
  if (config.runWAApiTest) {
    const summaryJudgmentDirectionsTask = await api.retrieveTaskDetails(config.judgeUserWithRegionId1, caseid, config.waTaskIds.judgeUnspecDJTask);
    console.log('summaryJudgmentDirectionsTask...' , summaryJudgmentDirectionsTask);
    WA.validateTaskInfo(summaryJudgmentDirectionsTask, validSummaryJudgmentDirectionsTask);
    taskId = summaryJudgmentDirectionsTask['id'];
    api.assignTaskToUser(config.judgeUserWithRegionId1, taskId);
  }

  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseid + '/trigger/STANDARD_DIRECTION_ORDER_DJ/STANDARD_DIRECTION_ORDER_DJ');
  await I.judgePerformDJDirectionOrder();

  if (config.runWAApiTest) {
    api.completeTaskByUser(config.judgeUserWithRegionId1, taskId);
  }

  await I.login(config.hearingCenterAdminWithRegionId1);
  await I.staffPerformDJCaseTransferCaseOffline(caseid);
}).retry(2);

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});

