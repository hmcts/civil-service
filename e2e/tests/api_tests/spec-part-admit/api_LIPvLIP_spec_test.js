/**
 * CUI/LiP Test Ownership:
 * civil-ccd-definition: Owns workflow and API-level integration tests.
 * civil-citizen-ui: Owns citizen-rendered UI and routing tests.
 */



const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

const claimType = 'SmallClaims';
let caseId;

Feature('LIP v LIP spec part admit api journeys').tag('@civil-service-nightly @api-spec-part-admit');

Before(async () => {
  await createAccount(config.applicantCitizenUser.email, config.applicantCitizenUser.password);
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LiP v LiP Part admit defendant and claimant response - claimant rejects installment plan - CARM enabled', async ({api_spec_cui}) => {
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, claimType, true);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'SmallClaimPartAdmit', true);
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, 'IN_MEDIATION', true, 'partadmit');
});

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.applicantCitizenUser.email);
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});