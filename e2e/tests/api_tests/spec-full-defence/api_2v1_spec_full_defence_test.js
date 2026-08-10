

const config = require('../../../config.js');

Feature('2v1 spec full defence api journeys').tag('@civil-service-nightly @api-spec-full-defence');

Scenario('2v1 small claim different response full defence', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'DIFF_FULL_DEFENCE', 'TWO_V_ONE');
});

// skipping until DTSCCI-329 is resolved
Scenario.skip('2v1 small claim different response no full defence', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'DIFF_NOT_FULL_DEFENCE', 'TWO_V_ONE');
});

Scenario('2v1 small claim full defence and defendant response @api-spec-full-defence', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
});

Scenario('2v1 small claim full defence and not proceed @api-spec-full-defence', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'NOT_PROCEED', 'TWO_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
