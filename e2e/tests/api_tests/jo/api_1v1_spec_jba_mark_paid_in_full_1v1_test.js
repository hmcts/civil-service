const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

const claimType = 'SmallClaims';
let caseId;
let carmEnabled = false;

Feature('Spec 1v1 judgment by admission mark paid in full api test')
  .tag('@civil-service-master @civil-service-pr @civil-camunda-master @civil-camunda-pr @civil-service-nightly');

Before(async () => {
  await createAccount(config.applicantCitizenUser.email, config.applicantCitizenUser.password);
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LR v LR defendant response with full admit pay by set date judgment by admission mark paid in full', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, false);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION_JBA');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FA_ACCEPT_CCJ', 'ONE_V_ONE', 'All_FINAL_ORDERS_ISSUED', false, false);
  await api_spec.markJudgmentPaid(config.applicantSolicitorUser);
});

Scenario('1v1 LiP v LiP defendant response with part admit pay by installments judgment by admission mark paid in full', async ({api_spec_cui}) => {
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, claimType, false, 'INDIVIDUAL');
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, claimType, carmEnabled, 'PA_INSTALLMENTS_INDIVIDUAL');
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, 'All_FINAL_ORDERS_ISSUED', carmEnabled, 'PA_ACCEPT_CCJ');
  await api_spec_cui.judgmentPaidInFullCui(config.applicantCitizenUser, caseId, true);
}).tag('@api-jo');

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.applicantCitizenUser.email);
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
