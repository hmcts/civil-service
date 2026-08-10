

const config = require('../../../config.js');

Feature('1v1 spec counter claim api journey').tag('@civil-service-nightly @api-spec-counterclaim');

Scenario('1v1 counter claim', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'COUNTER_CLAIM');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});

