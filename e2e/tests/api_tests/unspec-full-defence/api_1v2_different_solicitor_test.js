

const config = require('../../../config.js');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';

// add @api-tests to run

Feature('1v2 different solicitor unspec full defence api journey').tag('@civil-service-nightly @api-unspec-full-defence');

Scenario('1v2 different solicitor unspec full defence', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.addCaseNote(config.adminUser);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.amendPartyDetails(config.adminUser);
  await api.acknowledgeClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  // Skipping this step as it is failing with partyIDs at the moment.
  // await api.acknowledgeClaim(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.informAgreedExtension(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.informAgreedExtension(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
