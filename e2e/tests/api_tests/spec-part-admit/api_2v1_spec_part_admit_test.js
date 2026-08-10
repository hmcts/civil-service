

const config = require('../../../config.js');

Feature('2v1 spec part admission api journey').tag('@civil-service-nightly @api-spec-part-admit');

Scenario('2v1 spec part admission', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION', 'TWO_V_ONE');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'PART_ADMISSION', 'TWO_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
