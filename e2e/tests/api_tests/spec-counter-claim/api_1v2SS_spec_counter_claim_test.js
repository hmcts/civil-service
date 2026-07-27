

const config = require('../../../config.js');

Feature('1v2SS spec counter claim api journey').tag('@civil-service-nightly @api-spec-counterclaim');

Scenario('1v2SS small claim counter claim', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'COUNTER_CLAIM', 'ONE_V_TWO');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
