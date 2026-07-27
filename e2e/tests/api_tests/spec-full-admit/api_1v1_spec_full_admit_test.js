

const config = require('../../../config.js');

Feature('1v1 spec full admit api journey').tag('@civil-service-nightly @api-spec-full-admit');

Scenario('1v1 spec full admit', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'FULL_ADMISSION');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_ADMISSION', 'ONE_V_ONE',
    'All_FINAL_ORDERS_ISSUED');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});

