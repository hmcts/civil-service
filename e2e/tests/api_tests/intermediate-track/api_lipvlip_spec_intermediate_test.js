const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

let caseId;

Feature('1v1 LIP v LIP and LR v LIP spec api journeys').tag('@civil-service-nightly @api-intermediate-track');

Before(async () => {
  await createAccount(config.applicantCitizenUser.email, config.applicantCitizenUser.password);
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LiP v LiP defendant and claimant response - CARM enabled - Minti Enabled', async ({api_spec_cui}) => {
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'INTERMEDIATE', true, '', true);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'INTERMEDIATE', true);
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, 'AWAITING_APPLICANT_INTENTION', true);
});

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.applicantCitizenUser.email);
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});