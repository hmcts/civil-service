const config = require('../config.js');

Feature('CCD API tests @api-tests');

Scenario('Create claim', async (api) => {
  await api.createClaimWithRepresentedRespondent(config.solicitorUser);
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

Scenario('Create claim where respondent is litigant in person', async (api) => {
  await api.createClaimWithRespondentLitigantInPerson(config.solicitorUser);
});
