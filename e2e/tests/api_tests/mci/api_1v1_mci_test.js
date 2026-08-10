const config = require('../../../config.js');

const mpScenario = 'ONE_V_ONE';

Feature('1v1 manage contact information api journey').tag('@civil-service-nightly @api-mci');

Scenario('1v1 manage contact information', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO');
  // await api.manageDefendant1Details(config.adminUser);
  await api.manageDefendant1LROrgDetails(config.defendantSolicitorUser);
}).retry(1);

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});