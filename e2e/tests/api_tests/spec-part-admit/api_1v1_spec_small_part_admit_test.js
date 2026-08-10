const config = require('../../../config.js');

Feature('1v1 spec api small claims journey').tag('@civil-service-nightly @api-spec-part-admit');

Scenario('1v1 spec api small claims', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION');
  await api_spec_small.claimantResponse(config.applicantSolicitorUser);
});

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
});
