const config = require('../../../config.js');
const { deleteCitizenAccount, createAccount } = require('../../../api/idamHelper');
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId2;
let caseId, taskId, takeCaseOfflineTaskeExpectedTask;
if (config.runWAApiTest) {
  takeCaseOfflineTaskeExpectedTask = require('../../../../wa/tasks/takeCaseOfflineTask.js');
}
const claimType = 'MULTI';

Feature('1v1 LR v LiP spec multi track api journey').tag('@civil-service-nightly @api-multi-track');

Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LR v LiP multi track', async ({ api_spec_cui, WA}) => {
  const mpScenario = 'ONE_V_ONE';
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
  caseId = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimType);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, claimType);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'No', 'AWAITING_APPLICANT_INTENTION', false, claimType);
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
