
const {date} = require('../../../api/dataHelper');
const config = require('../../../config.js');

let mediationAdminRegion1 = config.localMediationTests ? config.nbcUserLocal : config.nbcUserWithRegionId1;

// Fix all these tests and run against preview and aat
//BUG - CIV-15903
Feature('Spec small claims mediation api journey').tag('api-nightly-prod');

Scenario.skip('2v1 claimant and defendant upload mediation documents - CARM not enabled', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE', false);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE', 'JUDICIAL_REFERRAL', false);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
});

Scenario.skip('2v1 claimant and defendant upload mediation documents - CARM enabled', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE', true);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE', 'JUDICIAL_REFERRAL', true);
  await api_spec.mediationUnsuccessful(mediationAdminRegion1, true);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
});

AfterSuite(async ({api_spec_small, api_spec}) => {
  await api_spec_small.cleanUp();
  await api_spec.cleanUp();
});
