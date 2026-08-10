const config = require('../../../config.js');
const { createAccount, deleteCitizenAccount } = require('../../../api/idamHelper');
const claimType = 'INTERMEDIATE';
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId2;
let caseId, taskId, takeCaseOfflineTaskeExpectedTask;
if (config.runWAApiTest) {
  takeCaseOfflineTaskeExpectedTask = require('../../../../wa/tasks/takeCaseOfflineTask.js');
}

Feature('Spec 1v1 LR v LiP api intermediate track journey')
  .tag('@civil-service-master @civil-service-pr @civil-camunda-master @civil-camunda-pr @civil-service-nightly @api-intermediate-track');

Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LR v LiP intermediate track', async ({ api_spec_cui, WA }) => {
  caseId = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', claimType);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, claimType);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'No', 'AWAITING_APPLICANT_INTENTION', false, claimType);
  if (config.runWAApiTest) {
    const transferCaseOffline = await api_spec_cui.retrieveTaskDetails(hearingCenterAdminToBeUsed, caseId, config.waTaskIds.transferCaseOffline);
    console.log('transferCaseOffline...', transferCaseOffline);
    WA.validateTaskInfo(transferCaseOffline, takeCaseOfflineTaskeExpectedTask);
    taskId = transferCaseOffline['id'];
    api_spec_cui.assignTaskToUser(hearingCenterAdminToBeUsed, taskId);
  }
});

AfterSuite(async ({ api_spec_cui }) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
