const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
let civilCaseReference, gaCaseReference;

Feature('Before SDO 1v1 - GA CP - Hearing Notice document API tests').tag('@civil-service-nightly');

Scenario('Judge decides Free Form Order', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplication(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');
  console.log('*** Start Judge decides Free Form Order on GA Case Reference: ' + gaCaseReference + ' ***');

  await api_ga.judgeListApplicationForFreeFormOrder(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge decides Free Form Order: ' + gaCaseReference + ' ***');

  await api_ga.assertGaDocumentVisibilityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'generalOrder');


  await api_ga.hearingCenterAdminScheduleHearing(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  await api_ga.verifyGAState(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, 'HEARING_SCHEDULED');
}).retry(1);

Scenario('Defendant Hearing notice journey', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplication(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');
  console.log('*** Start Judge List the application for hearing on GA Case Reference: ' + gaCaseReference + ' ***');
  const doc = 'hearingNotice';

  await api_ga.judgeListApplicationForHearing(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge List the application for hearing GA Case Reference: ' + gaCaseReference + ' ***');

  await api_ga.hearingCenterAdminScheduleHearing(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  await api_ga.assertGaDocumentVisibilityToUser(config.judgeUser2WithRegionId4, civilCaseReference, gaCaseReference, doc);
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'HEARING_SCHEDULED');
  await api_ga.verifyGAState(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, 'HEARING_SCHEDULED');

  await api_ga.assertGaAppCollectionVisiblityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'Y');
  await api_ga.assertGaAppCollectionVisiblityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, 'Y');
  await api_ga.assertGaDocumentVisibilityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, doc);
  await api_ga.assertGaDocumentVisibilityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, doc);


  await api_ga.judgeMakeFinalOrder(config.judgeUser2WithRegionId2, gaCaseReference, 'ASSISTED_ORDER', true);

  await api_ga.hearingCenterAdminScheduleHearing(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  await api_ga.verifyGAState(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, 'HEARING_SCHEDULED');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
