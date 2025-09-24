const config = require('../../../config.js');
const {createAccount, deleteAccount} = require('../../../api/idamHelper');

const claimType = 'SmallClaims';
let carmEnabled = false;
let claimRef;

let mediationAdmin = config.nbcUserWithRegionId1;

async function prepareClaim(api_spec_cui, carmEnabled) {
  claimRef = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, '', claimType, carmEnabled);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, claimRef, claimType, carmEnabled);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'Yes', 'IN_MEDIATION', carmEnabled);
}

// set config.localMediationTests to true to run locally
Feature('Unsuccessful mediation for spec small claim with unrepresented defendant @api-cui-carm @api-nightly-prod');

Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('CARM enabled', async ({api_spec_cui}) => {
  carmEnabled = true;
  await prepareClaim(api_spec_cui, carmEnabled);
  await api_spec_cui.mediationUnsuccessful(mediationAdmin, carmEnabled);
}).tag('@api-prod');

Scenario('CARM not enabled', async ({api_spec_cui}) => {
  carmEnabled = false;
  await prepareClaim(api_spec_cui, carmEnabled);
  await api_spec_cui.mediationUnsuccessful(mediationAdmin, carmEnabled);
});

AfterSuite(async ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteAccount(config.defendantCitizenUser2.email);
});


