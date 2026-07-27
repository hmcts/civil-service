const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
const genAppType = 'UNLESS_ORDER';
let civilCaseReference, gaCaseReference, state;

Feature('GA 1v1 Judge make decision unless order API tests').tag('@civil-service-nightly @api-ga-make-decision');

Scenario('Judge makes decision 1V1 - unless order', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithUnlessOrder(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Judge makes decision unless order: ' + gaCaseReference + ' ***');

  state = await api_ga.judgeMakesDecisionOrderMadeUnlessOrderAppln(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge makes decision unless order - GA Case Reference: ' + gaCaseReference + ' ***');
}).retry(1);

Scenario('Judge Revisit 1V1 - unless order End Date Scheduler', async ({api_ga}) => {

  console.log('*** Triggering Judge Revisit unless order Scheduler ***');
  await api_ga.judgeRevisitScheduler(gaCaseReference, state, genAppType);
  console.log('*** End of Judge Revisit unless order Scheduler ***');

}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
