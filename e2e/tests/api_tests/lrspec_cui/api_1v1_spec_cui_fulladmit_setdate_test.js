const config = require('../../../config.js');
const { createAccount, deleteAccount } = require('../../../api/idamHelper');

const claimType = 'SmallClaims';
let carmEnabled = false;
let caseId;

Feature('CCD 1v1 API test @api-spec-cui @non-prod-e2e-ft');
Before(async () => {
    await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LiP v LiP defendant response with full admit pay by set date', async ({ api_spec_cui }) => {
    await respondWithFAPayBySetDate(api_spec_cui);
});

async function respondWithFAPayBySetDate(api_spec_cui) {
    caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser,'SmallClaims',false,'INDIVIDUAL');
    await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, claimType, carmEnabled, 'FA_SETDATE_INDIVIDUAL');
    await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, 'PROCEEDS_IN_HERITAGE_SYSTEM', carmEnabled,'FA_ACCEPT_CCJ');
}

AfterSuite(async ({ api_spec_cui }) => {
    await api_spec_cui.cleanUp();
    await deleteAccount(config.defendantCitizenUser2.email);
});
