const config = require('../../../config.js');
const {addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
let caseNumber;
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';

let validValidateDiscontinueTask;

if (config.runWAApiTest) {
  validValidateDiscontinueTask = require('../../../../wa/tasks/validateDiscontinueTask.js');
}

Feature('Discontinue This Claim - Hearing Schedule - Full discontinuance  - 1v2 - spec').tag('@u-nightly-prod @ui-discontinue-claim');

Scenario('01 1v2 full defence unspecified - judge draws fast track WITHOUT sum of damages - hearing scheduled', async ({api, LRspec}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaim(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  await api.createSDO(judgeUser, 'CREATE_FAST_NO_SUM');
  await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
  caseNumber = await api.getCaseId();
  await LRspec.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
});

Scenario('02 Discontinue This Claim', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.requestForDiscontinueThisClaimForUI1v2();
}).retry(2);

//Skipped in AAT and Demo until DTSCCI-2718 and DTSCCI-2719 are fixed
Scenario('03 Validate Discontinuance', async ({LRspec, api, WA}) => {
  if(['preview'].includes(config.runningEnv)) {
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
  }
}).retry(2);

//Skipped until DTSCCI-2718 and DTSCCI-2719 are fixed
Scenario('04 Claim Discontinued - Remove Hearing', async ({LRspec}) => {
   if(['preview'].includes(config.runningEnv)) {
    await LRspec.login(config.hearingCenterAdminWithRegionId1);
    await LRspec.addCaseNote();
   }
}).retry(2);

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
  await unAssignAllUsers();
});
