const config = require('../../../config.js');
const mpScenario = 'ONE_V_TWO_ONE_LEGAL_REP';
let caseId;

Feature('1v2 Same Solicitor - Manage Contact Information').tag('@civil-ccd-nightly @ui-mci');

Scenario('01 Create claim to claimant response', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.addDefendantLitigationFriend(config.defendantSolicitorUser, mpScenario);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
  caseId = await api.getCaseId();
}).retry(1);

Scenario('02 Manage Contact Information For Admin', async ({I}) => {
  await I.login(config.adminUser);
  await I.manageWitnessesForDefendant(caseId);
}).retry(1);

Scenario('03 Manage Contact Information For Claimant Solicitor', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.manageOrganisationIndividualsForClaimant(caseId);
}).retry(1);

Scenario('04 Manage Contact Information For Defendant parties', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.manageLitigationFriendForDefendant(caseId);
  await I.manageDefendant(caseId);
}).retry(1);

AfterSuite(async ({api}) => {
  await api.cleanUp();
});
