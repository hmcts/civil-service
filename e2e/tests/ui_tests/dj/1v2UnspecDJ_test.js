

const config = require('../../../config.js');

let validSummaryJudgmentDirectionsTask;

if (config.runWAApiTest) {
  validSummaryJudgmentDirectionsTask = require('../../../../wa/tasks/summaryJudgmentDirectionsTask.js');
}

let caseId;

Feature('1v2 unspec default judgement').tag('@civil-ccd-nightly @ui-dj');

Scenario('01 1v2 create 1v2DS unspec claim, notify claim, notity claim details, request default judgment', async ({I, api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_TWO_LEGAL_REP');
  caseId = await api.getCaseId();

  //below amend claim documents only needed as assertion was failing on notify claims
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.amendRespondent1ResponseDeadline(config.systemupdate);
  await api.amendRespondent2ResponseDeadline(config.systemupdate);
  await I.login(config.applicantSolicitorUser);
  await I.initiateDJUnspec(caseId, 'ONE_V_TWO');
}).retry(2);

Scenario('02 Judge uploads evidence (add case note)', async ({I}) => {
  await I.login(config.judgeUserWithRegionId1);
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
  await I.waitForText('Summary');
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId + '/trigger/EVIDENCE_UPLOAD_JUDGE/EVIDENCE_UPLOAD_JUDGE');
  await I.waitForText('How do you want to add a case note?');
  await I.judgeAddsCaseNotes();
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
  await I.waitForText('Summary');
});

Scenario('03 Judge performs directions order', async ({I, api, WA}) => {
  await I.login(config.judgeUserWithRegionId1);
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
  let taskId;
  if (config.runWAApiTest) {
    const summaryJudgmentDirectionsTask = await api.retrieveTaskDetails(config.judgeUserWithRegionId1, caseId, config.waTaskIds.judgeUnspecDJTask);
    console.log('summaryJudgmentDirectionsTask...' , summaryJudgmentDirectionsTask);
    WA.validateTaskInfo(summaryJudgmentDirectionsTask, validSummaryJudgmentDirectionsTask);
    taskId = summaryJudgmentDirectionsTask['id'];
    api.assignTaskToUser(config.judgeUserWithRegionId1, taskId);
  }

  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId + '/trigger/STANDARD_DIRECTION_ORDER_DJ/STANDARD_DIRECTION_ORDER_DJ');
  await I.judgePerformDJDirectionOrder();

  if (config.runWAApiTest) {
    api.completeTaskByUser(config.judgeUserWithRegionId1, taskId);
  }
});

Scenario('04 Hearing admin takes case offline', async ({I}) => {
  await I.login(config.hearingCenterAdminWithRegionId1);
  await I.staffPerformDJCaseTransferCaseOffline(caseId);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});

