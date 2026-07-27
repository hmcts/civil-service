

const config = require('../../../config.js');

Feature('1v2SS spec full defence api journeys').tag('@civil-service-nightly @api-spec-full-defence');

Scenario('1v2SS small claim full defence, defendant response', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
});

Scenario('1v2SS small claim different response full defence', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'DIFF_FULL_DEFENCE', 'ONE_V_TWO');
});

Scenario('1v2SS small claim full defence, claimant response', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO',
    'JUDICIAL_REFERRAL');
}).tag('@civil-service-master @civil-service-pr @civil-camunda-master @civil-camunda-pr');

Scenario('1v2SS small claim full defence, claimant response not proceed', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'NOT_PROCEED', 'ONE_V_TWO',
    'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
