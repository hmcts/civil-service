

const config = require('../../../config.js');

Feature('1v1 settle claim spec api journey').tag('@civil-service-nightly @api-settle-claim');

Scenario('1v1 settle claim spec', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.informAgreedExtensionDate(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE',
    'All_FINAL_ORDERS_ISSUED');
  await api_spec.settleClaim(config.applicantSolicitorUser, 'NO');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});

