

const config = require('../../../config.js');

Feature('2v1 spec counter claim api journey').tag('@civil-service-nightly @api-spec-counterclaim');

Scenario('2v1 small claim counter claim', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'COUNTER_CLAIM', 'TWO_V_ONE');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
