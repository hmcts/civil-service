

const config = require('../../../config.js');

Feature('2v1 spec api fast track journeys').tag('@civil-service-nightly @api-spec-counterclaim');

Scenario('2v1 fast claim counter claim @api-spec-counterclaim', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'COUNTER_CLAIM', 'TWO_V_ONE');
});

AfterSuite(async  ({api_spec_fast}) => {
  await api_spec_fast.cleanUp();
});
