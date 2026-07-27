 
const config = require('../../../config.js');
const mpScenario = 'ONE_V_ONE';

let civilCaseReference;

Feature('Smoke test - API 1v1 spec create claim and create general application').tag('@civil-service-smoke');

Scenario('API 1v1 spec create claim and create general application', async ({api_ga}) => {
  civilCaseReference = await api_ga.createSpecifiedClaim(config.applicantSolicitorUser, mpScenario);
  await api_ga.initiateGeneralApplication(config.applicantSolicitorUser, civilCaseReference);
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});