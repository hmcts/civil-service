const config = require('../../../config.js');

Feature('1v1 spec full defence api journey').tag('@civil-service-nightly @api-spec-full-defence');

Scenario('1v1 spec full defence', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.informAgreedExtensionDate(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});

