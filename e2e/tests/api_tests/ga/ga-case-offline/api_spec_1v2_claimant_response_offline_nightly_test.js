const config = require('../../../../config.js');

let civilCaseReference, gaCaseReference;

Feature('GA SPEC Claim 1v2 Claimant Response Case Close API tests').tag('@civil-service-nightly');

Scenario.skip('Case offline LISTING_FOR_A_HEARING', async ({api_ga}) => {
  civilCaseReference = await api_ga.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  gaCaseReference
    = await api_ga.initiateGeneralApplication(config.applicantSolicitorUser, civilCaseReference);
  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Judge List the application for hearing on GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.judgeListApplicationForHearing(config.judgeUser2WithRegionId2, gaCaseReference);

  console.log('*** End Judge List the application for hearing GA Case Reference: ' + gaCaseReference + ' ***');


  console.log('*** Case offline: ' + civilCaseReference + ' ***');
  await api_ga.defendantResponseSpecClaim(config.defendantSolicitorUser, 'PART_ADMISSION', 'ONE_V_TWO');
  await api_ga.claimantResponseClaimSpec(config.applicantSolicitorUser, 'PART_ADMISSION', 'ONE_V_TWO',
    'All_FINAL_ORDERS_ISSUED');
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'PROCEEDS_IN_HERITAGE');
}).retry(1);

Scenario.skip('Case offline APPLICATION_DISMISSED', async ({api_ga}) => {
  civilCaseReference = await api_ga.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application with state APPLICATION_DISMISSED');
  gaCaseReference
    = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);
  console.log('*** Start Judge Make Decision Application Dismiss on GA Case Reference: '
    + gaCaseReference + ' ***');
  await api_ga.judgeDismissApplication(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge Make Decision Application Dismiss on GA Case Reference: '
    + gaCaseReference + ' ***');

  console.log('*** Case offline: ' + civilCaseReference + ' ***');
  await api_ga.defendantResponseSpecClaim(config.defendantSolicitorUser, 'PART_ADMISSION', 'ONE_V_TWO');
  await api_ga.claimantResponseClaimSpec(config.applicantSolicitorUser, 'PART_ADMISSION', 'ONE_V_TWO',
    'AWAITING_APPLICANT_INTENTION');
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'APPLICATION_DISMISSED');
}).retry(1);

Scenario.skip('Case offline AWAITING_RESPONDENT_RESPONSE', async ({api_ga}) => {
  civilCaseReference = await api_ga.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference
    = await api_ga.initiateGeneralApplication(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Case offline: ' + civilCaseReference + ' ***');
  await api_ga.defendantResponseSpecClaim(config.defendantSolicitorUser, 'PART_ADMISSION', 'ONE_V_TWO');
  await api_ga.claimantResponseClaimSpec(config.applicantSolicitorUser, 'PART_ADMISSION', 'ONE_V_TWO',
    'AWAITING_APPLICANT_INTENTION');
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'PROCEEDS_IN_HERITAGE');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
