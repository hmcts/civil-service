

const config = require('../../../config.js');

Feature('1v2SS spec api fast track journeys').tag('@civil-service-nightly @api-spec-full-admit');

Scenario('1v2SS fast claim fast claim full admit, defendant and claimant response @api-spec-full-admit', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION', 'ONE_V_TWO');
  await api_spec_fast.claimantResponse(config.applicantSolicitorUser, 'FULL_ADMISSION', 'ONE_V_TWO',
    'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async  ({api_spec_fast}) => {
  await api_spec_fast.cleanUp();
});
