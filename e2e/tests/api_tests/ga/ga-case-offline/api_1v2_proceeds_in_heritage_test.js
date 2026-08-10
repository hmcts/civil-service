const config = require('../../../../config.js');

const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
let civilCaseReference, gaCaseReference;

Feature('GA 1v2 Judge accepts strike out application API tests').tag('@civil-service-nightly @api-ga-case-offline');

Scenario('Judge makes decision 1V2 - Accept Strike out application', async ({api_ga}) => {

  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser,
    mpScenario, 'SoleTrader', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  console.log('Civil Case created for general application: ' + civilCaseReference);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.defendantResponseClaim(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplication(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse1v2(config.defendantSolicitorUser, config.secondDefendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Judge approves strike out application: ' + gaCaseReference + ' ***');

  await api_ga.judgeApprovesStrikeOutApplication(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge approves strike out application - GA Case Reference: ' + gaCaseReference + ' ***');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
