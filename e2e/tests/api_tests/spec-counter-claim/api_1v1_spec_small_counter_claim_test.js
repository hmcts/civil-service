const config = require('../../../config.js');

Feature('1v1 spec small claim counter claim api journey').tag('@civil-service-nightly @api-spec-counterclaim');

Scenario('1v1 spec small claim counter claim', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'COUNTER_CLAIM');
  // counter claim defense brings the case offline
});

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
});
