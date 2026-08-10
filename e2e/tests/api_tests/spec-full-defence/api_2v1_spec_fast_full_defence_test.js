

const config = require('../../../config.js');

Feature('2v1 spec fast track full defence api journeys').tag('@civil-service-nightly @api-spec-full-defence');

Scenario('2v1 fast claim different response no full defence', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'DIFF_NOT_FULL_DEFENCE', 'TWO_V_ONE');
});

Scenario.skip('2v1 fast claim full defence and claimant response', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE');
  await api_spec_fast.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
}).tag('@api-spec-full-defence');

Scenario('2v1 fast claim full defence and not proceed', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE');
  await api_spec_fast.claimantResponse(config.applicantSolicitorUser, 'NOT_PROCEED', 'TWO_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
});

Scenario('2v1 fast claim different response full defence', async ({I, api_spec_fast}) => {
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec_fast.defendantResponse(config.defendantSolicitorUser, 'DIFF_FULL_DEFENCE', 'TWO_V_ONE');
});

AfterSuite(async  ({api_spec_fast}) => {
  await api_spec_fast.cleanUp();
});
