const config = require('../../../config.js');

Feature('1v1 spec api manage contact information journeys').tag('@civil-service-nightly @api-mci');

Scenario('1v1 spec api manage contact information', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE');
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No');
  await api_spec_small.manageContactInformation(config.adminUser, true);
});

Scenario('1v1 spec api manage contact information - CARM enabled', async ({I, api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, true);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', false, true);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', true);
  await api_spec_small.manageContactInformation(config.adminUser, true);
});

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
});
