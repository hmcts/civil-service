const config = require('../../../config.js');
const legalAdvUser = config.tribunalCaseworkerWithRegionId4;
const {unAssignAllUsers, addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper.js');
let caseNumber;
let validDecisionOnReconsiderationRequestTask;

if (config.runWAApiTest) {
  validDecisionOnReconsiderationRequestTask = require('../../../../wa/tasks/decisionOnReconsiderationRequestTask.js');
}

Feature('Request for reconsideration uphold the previous order made - 1v1 - spec').tag('@civil-ccd-nightly @ui-rfr');

Scenario('01 1v1 spec request for reconsideration to uphold the previous order made', async ({api_spec_small, LRspec}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', true);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true);
  caseNumber = await api_spec_small.getCaseId();
  await LRspec.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
  await api_spec_small.createSDO(legalAdvUser, 'CREATE_SMALL_NO_SUM');
}).retry(2);

Scenario('02 Request for Reconsideration to uphold the previous order made', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.requestForReconsiderationForUI();
}).retry(2);

Scenario('03 Decision on Reconsideration Request to uphold the previous order made', async ({LRspec, api, WA}) => {
  await LRspec.login(config.judgeUserWithRegionId1);
  let taskId;
  if (config.runWAApiTest) {
    const decisionOnReconsiderationRequest = await api.retrieveTaskDetails(config.judgeUserWithRegionId1, caseNumber, config.waTaskIds.decisionOnReconsiderationRequestTask);
    await WA.validateTaskInfo(decisionOnReconsiderationRequest, validDecisionOnReconsiderationRequestTask);
    taskId = decisionOnReconsiderationRequest['id'];
    await api.assignTaskToUser(config.judgeUserWithRegionId1, taskId);
  }
  await LRspec.decisionForReconsiderationYesOption();
  if (config.runWAApiTest) {
    api.completeTaskByUser(config.judgeUserWithRegionId1, taskId);
  }
}).retry(2);

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
  await unAssignAllUsers();
});