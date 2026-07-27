

const config = require('../../../config.js');

Feature('1v2SS spec api fast track journeys').tag('@civil-service-nightly @api-spec-full-defence');

Scenario('1v2SS different response no full defence', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'DIFF_NOT_FULL_DEFENCE', 'ONE_V_TWO');
});

Scenario('1v2SS full defence and claimant response', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_spec_fast.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO',
    'AWAITING_APPLICANT_INTENTION');
});

Scenario('1v2SS fast claim full defence and not proceed', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_spec_fast.claimantResponse(config.applicantSolicitorUser, 'NOT_PROCEED', 'ONE_V_TWO',
    'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async  ({api_spec_fast}) => {
  await api_spec_fast.cleanUp();
});
