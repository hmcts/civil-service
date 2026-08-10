

const config = require('../../../config.js');

Feature('2v1 spec fast track full admission api journeys').tag('@civil-service-nightly @api-spec-full-admit');

Scenario('2v1 spec fast track full admission', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION', 'TWO_V_ONE');
  await api_spec_fast.claimantResponse(config.applicantSolicitorUser, 'FULL_ADMISSION', 'TWO_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async  ({api_spec_fast}) => {
  await api_spec_fast.cleanUp();
});
