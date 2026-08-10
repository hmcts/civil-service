const config = require('../../../config.js');

Feature('1v1 spec small claims full admit api journey').tag('@civil-service-nightly @api-spec-full-admit');

Scenario('1v1 spec small claims full admit', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION');
  await api_spec_small.claimantResponse(config.applicantSolicitorUser);
});

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
});
