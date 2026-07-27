

const config = require('../../../config.js');

Feature('2v1 spec full admit api journey').tag('@civil-service-nightly @api-spec-full-admit');

Scenario('2v1 spec full admit', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION', 'TWO_V_ONE');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_ADMISSION', 'TWO_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
