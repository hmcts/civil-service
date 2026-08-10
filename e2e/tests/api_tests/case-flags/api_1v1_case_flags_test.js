const config = require('../../../config.js');
const mpScenario = 'ONE_V_ONE';

Feature('1v1 case flags api journey').tag('@civil-service-nightly @api-case-flags');

Scenario('1v1 case flags', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO');
  await api.createCaseFlags(config.hearingCenterAdminWithRegionId1);
  await api.manageCaseFlags(config.hearingCenterAdminWithRegionId1);
}).retry(1);

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
