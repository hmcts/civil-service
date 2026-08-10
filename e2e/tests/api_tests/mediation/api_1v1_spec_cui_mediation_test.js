/**
 * CUI/LiP Test Ownership:
 * civil-ccd-definition: Owns workflow and API-level integration tests.
 * civil-citizen-ui: Owns citizen-rendered UI and routing tests.
 */

const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

const claimType = 'SmallClaims';
let carmEnabled = false;
let claimRef;

let mediationAdmin = config.nbcUserWithRegionId1;

// set config.localMediationTests to true to run locally
Feature('Unsuccessful mediation for spec small claim with unrepresented defendant')
  .tag('@civil-service-nightly @api-mediation');

Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('CARM enabled', async ({api_spec_cui}) => {
  carmEnabled = true;
  claimRef = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, '', claimType, carmEnabled);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, claimRef, claimType, carmEnabled);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'Yes', 'IN_MEDIATION', carmEnabled);
  await api_spec_cui.mediationUnsuccessful(mediationAdmin, carmEnabled);
}).tag('@civil-service-master @civil-service-pr @civil-camunda-master @civil-camunda-pr');

Scenario('CARM not enabled', async ({api_spec_cui}) => {
  carmEnabled = false;
  claimRef = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, '', claimType, carmEnabled);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, claimRef, claimType, carmEnabled);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'Yes', 'IN_MEDIATION', carmEnabled);
  await api_spec_cui.mediationUnsuccessful(mediationAdmin, carmEnabled);
});

AfterSuite(async ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});

