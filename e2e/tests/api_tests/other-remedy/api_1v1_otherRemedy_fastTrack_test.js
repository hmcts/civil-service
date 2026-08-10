const config = require('../../../config.js');
const mpScenarioOtherRemedy = 'ONE_V_ONE_OTHER_REMEDY';
const mpScenario = 'ONE_V_ONE';

Feature('1v1 unspec full defence api journey for Other Remedy claim type fast track').tag('@civil-service-nightly @api-other-remedy');

Scenario('1v1 unspec full defence', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenarioOtherRemedy, '22000');
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO');
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
