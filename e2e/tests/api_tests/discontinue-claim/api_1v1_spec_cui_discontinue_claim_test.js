/**
 * CUI/LiP Test Ownership:
 * civil-ccd-definition: Owns workflow and API-level integration tests.
 * civil-citizen-ui: Owns citizen-rendered UI and routing tests.
 */



const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

const claimType = 'SmallClaims';
let caseId;

Feature('1vLIP spec api claim discontinuance journey').tag('@civil-service-nightly @api-discontinue-claim');

Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('Discontinue claim 1v1 LR v LiP defendant and claimant response - claim created from exui - CARM not enabled', async ({api_spec_cui}) => {
    let mpScenario = 'ONE_V_ONE_NO_P_NEEDED';
    caseId = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', claimType, false);
    await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, claimType, false);
    await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'No', 'JUDICIAL_REFERRAL', false);
    await api_spec_cui.discontinueClaim(config.applicantSolicitorUser, mpScenario);
});

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
