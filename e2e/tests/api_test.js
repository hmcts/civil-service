const config = require('../config.js');

Feature('CCD API tests @api-tests');

Scenario('Create claim', async (api) => {
  await api.createClaim(config.solicitorUser);
});

Scenario('Confirm service', async (api) => {
  await api.confirmService();
});

Scenario('Acknowledge service', async (api) => {
  await api.acknowledgeService();
});

Scenario('Request extension', async (api) => {
  await api.requestExtension();
});

Scenario('Respond extension', async (api) => {
  await api.respondExtension();
});

Scenario('Defendant response', async (api) => {
  await api.defendantResponse();
});

Scenario('Claimant response', async (api) => {
  await api.claimantResponse();
});
