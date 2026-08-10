

const config = require('../../../config.js');

Feature('1v2SS spec part admit api journey').tag('@civil-service-nightly @api-spec-part-admit');

Scenario('1v2SS spec part admit', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION', 'ONE_V_TWO');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'PART_ADMISSION', 'ONE_V_TWO',
    'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
