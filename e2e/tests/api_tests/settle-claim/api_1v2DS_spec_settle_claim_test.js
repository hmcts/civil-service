

const config = require('../../../config.js');

Feature('1v2 settle claim spec api journey').tag('@civil-service-nightly @api-settle-claim');

Scenario('1v2 settle claim spec', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_APPLICANT_INTENTION');
  await api_spec.settleClaim(config.applicantSolicitorUser, 'NO');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
