const config = require('../../../config.js');
const {addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
let caseNumber;
let validValidateDiscontinueTask;

if (config.runWAApiTest) {
  validValidateDiscontinueTask = require('../../../../wa/tasks/validateDiscontinueTask.js');
}

Feature('Discontinue This Claim - Full discontinuance  - 1v1 - spec').tag('@civil-ccd-nightly @ui-discontinue-claim');

Scenario('01 1v1 spec Discontinue This Claim - Full discontinuance', async ({api_spec_small, LRspec}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', true);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true);
  caseNumber = await api_spec_small.getCaseId();
  await LRspec.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(2);

Scenario('02 Discontinue This Claim', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.requestForDiscontinueThisClaimForUI();
}).retry(2);

//Skipped until DTSCCI-2718 and DTSCCI-2720 are fixed
Scenario.skip('03 Validate Discontinuance', async ({LRspec, api, WA}) => {
  await LRspec.login(config.ctscAdminUser);
  let taskId;
  if (config.runWAApiTest) {
    const validateDiscontinue = await api.retrieveTaskDetails(config.ctscAdminUser, caseNumber, config.waTaskIds.validateDiscontinueTask);
    console.log('validateDiscontinue...' , validateDiscontinue);
    WA.validateTaskInfo(validateDiscontinue, validValidateDiscontinueTask);
    taskId = validateDiscontinue['id'];
    api.assignTaskToUser(config.ctscAdminUser, taskId);
  }
  await LRspec.requestForValidateDiscontinuanceForUI();
  if (config.runWAApiTest) {
    api.completeTaskByUser(config.ctscAdminUser, taskId);
  }
}).retry(2);

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
  await unAssignAllUsers();
});
