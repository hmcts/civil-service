const config = require('../../config.js');

const mpScenario = 'ONE_V_ONE';

Feature('Smoke tests @smoke-tests-unspec');

Scenario('Create unspec claim to make sure ccd and bpmn are working fine', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
