const config = require('../../../config.js');

Feature('1v1 unspec api case offline journey').tag('@civil-service-nightly @api-case-offline');

Scenario('Create claim and move it to caseman', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api.moveCaseToCaseman(config.adminUser);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});