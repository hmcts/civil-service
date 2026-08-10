

const config = require('../../../config.js');
const mpScenario = 'ONE_V_TWO_ONE_LEGAL_REP';

Feature('1v2 same solicitor unspec full defence api journey').tag('@civil-service-nightly @api-unspec-full-defence');

Scenario('1v2 same solicitor unspec full defence', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.addCaseNote(config.adminUser);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.amendPartyDetails(config.adminUser);
  await api.acknowledgeClaim(config.defendantSolicitorUser, mpScenario);
  await api.informAgreedExtension(config.defendantSolicitorUser, mpScenario);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
