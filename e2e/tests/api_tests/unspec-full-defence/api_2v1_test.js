

const config = require('../../../config.js');
const mpScenario = 'TWO_V_ONE';

Feature('2v1 unspec full defence api journey').tag('@civil-service-nightly @api-unspec-full-defence');

Scenario('2v1 unspec full defence', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.addCaseNote(config.adminUser);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.amendPartyDetails(config.adminUser);
  await api.acknowledgeClaim(config.defendantSolicitorUser, mpScenario);
  await api.informAgreedExtension(config.defendantSolicitorUser, mpScenario);
  await api.defendantResponse(config.defendantSolicitorUser, 'TWO_V_ONE');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
