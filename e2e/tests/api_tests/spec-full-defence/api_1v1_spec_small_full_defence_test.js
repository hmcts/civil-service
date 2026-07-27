const config = require('../../../config.js');

Feature('1v1 spec api small claims journeys').tag('@civil-service-nightly @api-spec-full-defence');

Scenario('1v1 FULL_DEFENCE claimant and defendant response small claim', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE');
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No');
  await api_spec_small.manageContactInformation(config.adminUser, true);
});

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
});
