const config = require('../../../../config.js');

let civilCaseReference, gaCaseReference;

Feature('GA SPEC Claim 1v2 Defendant Response Case Close API tests').tag('@civil-service-nightly @api-ga-case-offline');

Scenario('Case offline APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION', async ({api_ga}) => {
  civilCaseReference = await api_ga.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Make a General Application with state APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');
  gaCaseReference
    = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Case offline: ' + civilCaseReference + ' ***');
  await api_ga.defendantResponseSpecClaim(config.defendantSolicitorUser, 'COUNTER_CLAIM', 'ONE_V_TWO');
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'PROCEEDS_IN_HERITAGE');
}).retry(1);

Scenario('Case offline ORDER_MADE', async ({api_ga}) => {
  civilCaseReference = await api_ga.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
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
  await api_ga.defendantResponseSpecClaim(config.defendantSolicitorUser, 'COUNTER_CLAIM', 'ONE_V_TWO');
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'ORDER_MADE');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
