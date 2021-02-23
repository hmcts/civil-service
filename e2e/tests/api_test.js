const config = require('../config.js');

Feature('CCD API tests @api-tests');

Scenario('Create claim', async (api) => {
  await api.createClaimWithRepresentedRespondent(config.solicitorUser);
});

Scenario('Amend claim documents', async (api) => {
  await api.amendClaimDocuments();
});

Scenario('Notify claim', async (api) => {
  await api.notifyClaim();
});

Scenario('Acknowledge service', async (api) => {
  await api.acknowledgeService();
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

Scenario('Create claim and move it to caseman', async (api) => {
  await api.createClaimWithRepresentedRespondent(config.solicitorUser);
  await api.caseProceedsInCaseman();
});
