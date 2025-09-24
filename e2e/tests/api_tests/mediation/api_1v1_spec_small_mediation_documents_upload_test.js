
const {date} = require('../../../api/dataHelper');
const config = require('../../../config.js');

let mediationAdminRegion1 = config.localMediationTests ? config.nbcUserLocal : config.nbcUserWithRegionId1;
let mediationAdminRegion4 = config.localMediationTests ? config.nbcUserLocal : config.nbcUserWithRegionId4;
let mediationAdminRegion2 = config.localMediationTests ? config.nbcUserLocal : config.nbcUserWithRegionId2;

// Fix all these tests and run against preview and aat
//BUG - CIV-15903
Feature('Spec small claims mediation API test');

async function prepareClaim1v1(api_spec_small, carmEnabled) {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, carmEnabled);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', false, carmEnabled);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', carmEnabled);
}

async function prepareClaim1v2SameSol(api_spec, carmEnabled) {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL', carmEnabled);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'JUDICIAL_REFERRAL', carmEnabled);
}

async function prepareClaim1v2DiffSol(api_spec, carmEnabled) {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO', carmEnabled);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_RESPONDENT_ACKNOWLEDGEMENT', carmEnabled);
  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_APPLICANT_INTENTION', carmEnabled);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', 'JUDICIAL_REFERRAL', carmEnabled);
}

async function prepareClaim2v1(api_spec, carmEnabled) {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE', carmEnabled);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'TWO_V_ONE', 'JUDICIAL_REFERRAL', carmEnabled);
}

Scenario.skip('1v1 claimant and defendant upload mediation documents - CARM not enabled', async ({api_spec_small}) => {
  await prepareClaim1v1(api_spec_small, false);
  await api_spec_small.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec_small.uploadMediationDocuments(config.defendantSolicitorUser);
  await api_spec_small.createSDO(config.judgeUser2WithRegionId4, 'CREATE_SMALL', false);
});

Scenario.skip('1v1 claimant and defendant part admit states paid- claimant not received payment - upload mediation documents - CARM enabled', async ({api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, true);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION_STATES_PAID', 'ONE_V_ONE', false, true);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', true, 'PART_ADMIT_STATES_PAID_NOT_RECEIVED');
  await api_spec_small.mediationUnsuccessful(mediationAdminRegion4, true);
  await api_spec_small.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec_small.uploadMediationDocuments(config.defendantSolicitorUser);
}).tag('@api-prod @api-nonprod');

Scenario.skip('1v1 claimant and defendant part admit states paid- claimant received payment rejects PA - upload mediation documents - CARM enabled', async ({api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, true);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION_STATES_PAID', 'ONE_V_ONE', false, true);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', true, 'PART_ADMIT_STATES_PAID_CLAIMANT_RECEIVED_REJECTSPA');
  await api_spec_small.mediationUnsuccessful(mediationAdminRegion4, true);
  await api_spec_small.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec_small.uploadMediationDocuments(config.defendantSolicitorUser);
}).tag('@api-prod @api-nonprod');

Scenario.skip('1v1 claimant and defendant part admit reject upload mediation documents - CARM enabled', async ({api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, true);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION_NOT_PAID', 'ONE_V_ONE', false, true);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', true, 'PART_ADMIT_REJECT');
  await api_spec_small.mediationUnsuccessful(mediationAdminRegion4, true);
  await api_spec_small.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec_small.uploadMediationDocuments(config.defendantSolicitorUser);
  await api_spec_small.createSDO(config.judgeUser2WithRegionId4, 'CREATE_SMALL', false);
}).tag('@api-prod @api-nonprod');

Scenario.skip('1v2 same solicitor claimant and defendant upload mediation documents - CARM not enabled', async ({api_spec}) => {
  await prepareClaim1v2SameSol(api_spec, false);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser );
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
});

Scenario.skip('1v1 claimant and defendant upload mediation documents - CARM enabled', async ({api_spec_small}) => {
  await prepareClaim1v1(api_spec_small, true);
  await api_spec_small.mediationUnsuccessful(mediationAdminRegion2, true);
  await api_spec_small.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec_small.uploadMediationDocuments(config.defendantSolicitorUser);
  await api_spec_small.createSDO(config.judgeUser2WithRegionId2, 'CREATE_SMALL', true);
}).tag('@api-prod @api-nonprod');

Scenario.skip('1v2 same solicitor claimant and defendant upload mediation documents - CARM enabled', async ({api_spec}) => {
  await prepareClaim1v2SameSol(api_spec, true);
  await api_spec.mediationUnsuccessful(mediationAdminRegion1, true);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser );
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
});

Scenario.skip('1v2 different solicitor claimant and defendant upload mediation documents - CARM not enabled', async ({api_spec}) => {
  await prepareClaim1v2DiffSol(api_spec, false);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.secondDefendantSolicitorUser);
});

Scenario.skip('1v2 different solicitor claimant and defendant upload mediation documents - CARM enabled', async ({api_spec}) => {
  await prepareClaim1v2DiffSol(api_spec, true);
  await api_spec.mediationUnsuccessful(mediationAdminRegion1, true);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.secondDefendantSolicitorUser);
}).tag('@api-prod @api-nonprod');

Scenario.skip('2v1 claimant and defendant upload mediation documents - CARM not enabled', async ({api_spec}) => {
  await prepareClaim2v1(api_spec, false);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
});

Scenario.skip('2v1 claimant and defendant upload mediation documents - CARM enabled', async ({api_spec}) => {
  await prepareClaim2v1(api_spec, true);
  await api_spec.mediationUnsuccessful(mediationAdminRegion1, true);
  await api_spec.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec.uploadMediationDocuments(config.defendantSolicitorUser);
});

AfterSuite(async ({api_spec_small, api_spec}) => {
  await api_spec_small.cleanUp();
  await api_spec.cleanUp();
});
