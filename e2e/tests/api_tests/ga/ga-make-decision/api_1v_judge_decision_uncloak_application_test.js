const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
let civilCaseReference, gaCaseReference;

Feature('GA 1v1 Uncloak application API').tag('@civil-service-nightly');

Scenario('Judge makes decision 1V1 - ORDER_MADE - Uncloak and Application Approved', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');

  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application without notice');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser,
    civilCaseReference);

  console.log('*** Start Judge Make Decision Uncloak and Application Approved on GA Case Reference: '
    + gaCaseReference + ' ***');
  const doc = 'generalOrder';
  await api_ga.judgeMakesOrderDecisionUncloak(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge Make Decision Uncloak and Application Approved on GA Case Reference: '
    + gaCaseReference + ' ***');

  await api_ga.assertGaDocumentVisibilityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, doc);
  await api_ga.assertGaDocumentVisibilityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, doc);
}).retry(1);

Scenario('Judge makes decision 1V1 - REQUEST_MORE_INFORMATION - Uncloak Application', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');

  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application without notice');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser,
    civilCaseReference);

  console.log('*** Start Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');
  await api_ga.judgeRequestMoreInformationUncloak(config.judgeUser2WithRegionId2, gaCaseReference, true, true);
  console.log('*** End Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');

  console.log('*** Start Callback for Additional Payment: ' + gaCaseReference + ' ***');
  await api_ga.additionalPaymentSuccess(config.applicantSolicitorUser, gaCaseReference, 'AWAITING_RESPONDENT_RESPONSE');
  console.log('*** End Judge Make Decision Application Dismiss on GA Case Reference: ' + gaCaseReference + ' ***');

}).retry(1);

Scenario('Judge makes decision 1V1 - REQUEST_MORE_INFORMATION - Payment fails', async ({api_ga}) => {

  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');

  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application without notice');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser,
    civilCaseReference);

  console.log('*** Start Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');
  await api_ga.judgeRequestMoreInformationUncloak(config.judgeUser2WithRegionId2, gaCaseReference, true, true);
  console.log('*** End Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');

  console.log('*** Start Callback for Additional Payment Failure: ' + gaCaseReference + ' ***');
  await api_ga.additionalPaymentFailure(config.applicantSolicitorUser, gaCaseReference);
  console.log('*** End Callback for Additional Payment Failure on GA Case Reference: ' + gaCaseReference + ' ***');

}).retry(1);

Scenario('Judge makes decision 1V1 - REQUEST_MORE_INFORMATION - Uncloak Application - Response - Directions Order', async ({api_ga}) => {

  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');

  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application without notice');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser,
    civilCaseReference);

  console.log('*** Start Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');
  await api_ga.judgeRequestMoreInformationUncloak(config.judgeUser2WithRegionId2, gaCaseReference, true, true);
  console.log('*** End Judge Request More Information and Uncloak Application on GA Case Reference: '
    + gaCaseReference + ' ***');

  console.log('*** Start Callback for Additional Payment: ' + gaCaseReference + ' ***');
  await api_ga.additionalPaymentSuccess(config.applicantSolicitorUser, gaCaseReference, 'AWAITING_RESPONDENT_RESPONSE');
  console.log('*** End Callback for Additional Payment on GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Respondent respond to Judge Additional information on GA Case Reference: '
    + gaCaseReference + ' ***');
  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Respondent respond to Judge Additional information on GA Case Reference: '
    + gaCaseReference + ' ***');

  console.log('*** Start Judge Make Decision on GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.judgeMakesDecisionDirectionsOrder(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge Make Decision GA Case Reference: ' + gaCaseReference + ' ***');

  const doc = 'directionOrder';
  await api_ga.assertGaDocumentVisibilityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, doc);
  await api_ga.assertGaDocumentVisibilityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, doc);
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
