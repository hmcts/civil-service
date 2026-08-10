/**
 * CUI/LiP Test Ownership:
 * civil-ccd-definition: Owns workflow and API-level integration tests.
 * civil-citizen-ui: Owns citizen-rendered UI and routing tests.
 */

const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

let caseId;

Feature('LR v LIP spec full defence api journey').tag('@civil-service-nightly @api-spec-full-defence');

Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('LR v LIP spec full defence', async ({api_spec_cui}) => {
  caseId = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', 'SmallClaims', true);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'SmallClaims', true);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'No', 'IN_MEDIATION', true);
});

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});