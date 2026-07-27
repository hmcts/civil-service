 
const config = require('../../../config.js');
const mpScenario = 'ONE_V_ONE';

let civilCaseReference;

Feature('Smoke test - 1v1 spec create claim');

Scenario('1v1 spec create claim', async ({I, api_spec}) => {
  civilCaseReference = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await I.login(config.applicantSolicitorUser);
  await I.navigateToCaseDetails(civilCaseReference);
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});