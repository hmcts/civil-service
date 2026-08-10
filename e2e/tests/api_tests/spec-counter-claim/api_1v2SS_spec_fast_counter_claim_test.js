

const config = require('../../../config.js');

Feature('1v2SS spec fast track counter api journey').tag('@civil-service-nightly @api-spec-counterclaim');

Scenario('1v2SS fast claim counter claim', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'COUNTER_CLAIM', 'ONE_V_TWO');
});

AfterSuite(async  ({api_spec_fast}) => {
  await api_spec_fast.cleanUp();
});
