/**
 * CUI/LiP Test Ownership:
 * civil-ccd-definition: Owns workflow and API-level integration tests.
 * civil-citizen-ui: Owns citizen-rendered UI and routing tests.
 */

const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

const claimType = 'SmallClaims';
let caseId;
let carmEnabled = false;

Feature('LIP v LIP spec api part admit journey')
  .tag('@civil-ccd-master @civil-ccd-pr @civil-service-nightly @api-spec-part-admit');

Before(async () => {
  await createAccount(config.applicantCitizenUser.email, config.applicantCitizenUser.password);
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('LiP v LiP defendant response with part admit pay by installments', async ({api_spec_cui}) => {
    caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser,'SmallClaims',false,'INDIVIDUAL');
    await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId,claimType,carmEnabled,'PA_INSTALLMENTS_INDIVIDUAL');
    await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, 'JUDICIAL_REFERRAL', carmEnabled,'PA_REJECT_NO_MEDIATION');
  });

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.applicantCitizenUser.email);
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
