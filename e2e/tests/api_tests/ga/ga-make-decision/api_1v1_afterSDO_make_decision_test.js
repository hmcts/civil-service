const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
const claimAmountJudge = '11000';
let civilCaseReference, gaCaseReference;

Feature('Unspec 1v1 - General Application after SDO Journey').tag('@civil-service-nightly @api-ga-make-decision');

Scenario('Claimant create GA - JUDICIAL_REFERRAL state', async ({ api_ga, I }) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', claimAmountJudge);
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  console.log('Civil Case created for general application: ' + civilCaseReference);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start Judge makes decision order made: ' + gaCaseReference + ' ***');
  await api_ga.judgeMakesDecisionOrderMade(config.judgeUser2WithRegionId2, gaCaseReference);

  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  await api_ga.verifyGALocation(config.applicantSolicitorUser, gaCaseReference, civilCaseReference);
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
