
const {date} = require('../../../api/dataHelper');
const config = require('../../../config.js');

let mediationAdminRegion1 = config.localMediationTests ? config.nbcUserLocal : config.nbcUserWithRegionId1;

// Fix all these tests and run against preview and aat
//BUG - CIV-15903
Feature('Spec small claims mediation api journey').tag('api-nightly-prod');

Scenario.skip('1v2 same solicitor claimant and defendant upload mediation documents - CARM not enabled', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL', false);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'JUDICIAL_REFERRAL', false);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser );
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
});

Scenario.skip('1v2 same solicitor claimant and defendant upload mediation documents - CARM enabled', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL', true);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'JUDICIAL_REFERRAL', true);
  await api_spec.mediationUnsuccessful(mediationAdminRegion1, true);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser );
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
});

Scenario.skip('1v2 different solicitor claimant and defendant upload mediation documents - CARM not enabled', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO', false);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_RESPONDENT_ACKNOWLEDGEMENT', false);
  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_APPLICANT_INTENTION', false);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', 'JUDICIAL_REFERRAL', false);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.secondDefendantSolicitorUser);
});

Scenario.skip('1v2 different solicitor claimant and defendant upload mediation documents - CARM enabled', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO', true);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_RESPONDENT_ACKNOWLEDGEMENT', true);
  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_APPLICANT_INTENTION', true);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', 'JUDICIAL_REFERRAL', true);
  await api_spec.mediationUnsuccessful(mediationAdminRegion1, true);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.secondDefendantSolicitorUser);
}).tag('@api-mediation');

AfterSuite(async ({api_spec_small, api_spec}) => {
  await api_spec_small.cleanUp();
  await api_spec.cleanUp();
});
