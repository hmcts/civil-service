const config = require('../../../config.js');
const { createAccount, deleteAccount } = require('../../../api/idamHelper');
const claimType = 'INTERMEDIATE';
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId2;
let caseId, taskId, takeCaseOfflineTaskeExpectedTask;
if (config.runWAApiTest) {
  takeCaseOfflineTaskeExpectedTask = require('../../../../wa/tasks/takeCaseOfflineTask.js');
}

Feature('CCD 1v1 LR v LiP API test spec intermediate  track @api-multi-intermediate-spec @api-prod @api-nonprod');

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
  await deleteAccount(config.defendantCitizenUser2.email);
});
