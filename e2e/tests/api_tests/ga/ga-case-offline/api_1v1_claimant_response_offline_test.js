const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
let civilCaseReference, gaCaseReference;

Feature('GA Claim 1v1 Claimant Response Case Close API tests').tag('@civil-service-nightly @api-ga-case-offline');

Scenario('Case offline APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Make a General Application with state APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');
  gaCaseReference
    = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Case offline: ' + civilCaseReference + ' ***');
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseClaim(config.applicantSolicitorUser, 'NOT_PROCEED', 'ONE_V_TWO',
    'PROCEEDS_IN_HERITAGE_SYSTEM');
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'PROCEEDS_IN_HERITAGE');
}).retry(1);

Scenario('Case offline ORDER_MADE', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Make a General Application with state ORDER_MADE');
  gaCaseReference
    = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);
  console.log('*** Start Judge Make Decision Uncloak and Application Approved on GA Case Reference: '
    + gaCaseReference + ' ***');
    await api_ga.judgeMakesOrderDecisionUncloak(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge Make Decision Uncloak and Application Approved on GA Case Reference: '
    + gaCaseReference + ' ***');

  console.log('*** Case offline: ' + civilCaseReference + ' ***');
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseClaim(config.applicantSolicitorUser, 'NOT_PROCEED', 'ONE_V_TWO',
    'PROCEEDS_IN_HERITAGE_SYSTEM');
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'ORDER_MADE');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
