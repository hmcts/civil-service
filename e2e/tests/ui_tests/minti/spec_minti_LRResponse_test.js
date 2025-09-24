const config = require('../../../config.js');
const { createAccount, deleteAccount } = require('../../../api/idamHelper');
const intermediateTrackClaimAmount = '99000';
const claimAmountMulti = '200001';

Feature('Minti tracks - LR responses @e2e-minti').tag('@e2e-nightly-prod');

Scenario('LR vs LIP Multi track - LR response', async ({api_spec_cui, I}) => {
  const mpScenario = 'ONE_V_ONE';
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
  let caseId = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, mpScenario, 'MULTI');
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'MULTI');
  await I.login(config.applicantSolicitorUser);
  await I.respondToDefenceMinti(caseId, mpScenario, claimAmountMulti);
}).retry(1);

Scenario('LR vs LIP Int track - LR response', async ({api_spec_cui, I}) => {
  const mpScenario = 'ONE_V_ONE';
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
  let caseId = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', 'INTERMEDIATE');
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'INTERMEDIATE');
  await I.login(config.applicantSolicitorUser);
  await I.respondToDefenceMinti(caseId, mpScenario, intermediateTrackClaimAmount);
}).retry(1);

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteAccount(config.defendantCitizenUser2.email);
});
