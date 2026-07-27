

const config = require('../../../config.js');

Feature('1v1 spec case flags api journey').tag('@civil-service-nightly @api-case-flags');

Scenario('1v1 spec case flags', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
  await api_spec.createCaseFlags(config.hearingCenterAdminWithRegionId1);
  await api_spec.manageCaseFlags(config.hearingCenterAdminWithRegionId1);
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});

