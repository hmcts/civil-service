
const config = require('../../../config.js');
const mpScenario = 'ONE_V_ONE';

Feature('Smoke test - API 1v1 spec create claim and create general application').tag('@civil-camunda-smoke @civil-wa-smoke');

Scenario('API 1v1 spec create claim and create general application', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
}).retry(5);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
