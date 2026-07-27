

const config = require('../../../config.js');

let validJudgmentOnlineSetAsideTakeCaseOfflineTask;

if (config.runWAApiTest) {
  validJudgmentOnlineSetAsideTakeCaseOfflineTask = require('../../../../wa/tasks/judgmentOnlineSetAsideTakeCaseOffline.js');
}

let caseNumber;

Feature('1v1 Spec Set Aside Judgment Following Receiving Defence').tag('@civil-ccd-nightly @ui-jo');

Scenario('01 Create 1v1 spec claim, request default judgment', async ({I, api_spec, LRspec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  caseNumber = await api_spec.getCaseId();
  await LRspec.setCaseId(caseNumber);
  await api_spec.amendRespondent1ResponseDeadline(config.systemupdate);
  await I.login(config.applicantSolicitorUser);
  await I.initiateDJSpec(caseNumber, 'ONE_V_ONE', 'SPEC');
}).retry(2);

Scenario('02 Set A side Judgment - Order following defence received', async ({LRspec}) => {
  await LRspec.login(config.hearingCenterAdminWithRegionId2);
  await LRspec.requestSetAsideJudgmentFollowingDefenceReceived();
}).retry(2);

Scenario('03 Set Aside - Take Case Offline', async ({LRspec, api, WA}) => {
  await LRspec.login(config.hearingCenterAdminWithRegionId2);
  let taskId;
  if (config.runWAApiTest) {
    const judgmentOnlineSetAsideTakeCaseOfflineTask = await api.retrieveTaskDetails(config.hearingCenterAdminWithRegionId2, caseNumber, config.waTaskIds.judgmentOnlineSetAsideTakeCaseOffline);
    console.log('judgmentOnlineSetAsideTakeCaseOfflineTask...' , judgmentOnlineSetAsideTakeCaseOfflineTask);
    WA.validateTaskInfo(judgmentOnlineSetAsideTakeCaseOfflineTask, validJudgmentOnlineSetAsideTakeCaseOfflineTask);
    taskId = judgmentOnlineSetAsideTakeCaseOfflineTask['id'];
    api.assignTaskToUser(config.hearingCenterAdminWithRegionId2, taskId);
  }
  await LRspec.caseProceedsInCaseman();
  if (config.runWAApiTest) {
    api.completeTaskByUser(config.hearingCenterAdminWithRegionId2, taskId);
  }
}).retry(2);

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
