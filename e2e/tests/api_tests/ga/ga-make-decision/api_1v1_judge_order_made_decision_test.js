const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
const genAppType = 'STAY_THE_CLAIM';
let civilCaseReference, gaCaseReference, state;

Feature('GA 1v1 Judge make decision order made API tests').tag('@civil-service-nightly @api-ga-make-decision');

Scenario('Judge makes decision 1V1 - Order Made', async ({api_ga}) => {

  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithStayClaim(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');
  console.log('*** Start Judge makes decision order made and : ' + gaCaseReference + ' ***');

state = await api_ga.judgeMakesDecisionOrderMadeStayClaimAppln(config.judgeUser2WithRegionId2, gaCaseReference);
console.log('*** End Judge makes decision order made - GA Case Reference: ' + gaCaseReference + ' ***');
}).retry(1);

Scenario('Judge Revisit 1V1 unspec - Order Made End Date Scheduler', async ({api_ga}) => {

  console.log('*** Triggering Judge Revisit Order Made Scheduler ***');
  await api_ga.judgeRevisitScheduler(gaCaseReference, state, genAppType);
  console.log('*** End of Judge Revisit Order Made Scheduler ***');

}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
