const config = require('../../../../config.js');

const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
let civilCaseReference, gaCaseReference;

Feature('GA 1v2 application collection for different solicitor API tests')
  .tag('@civil-service-nightly @api-ga-collections');


Scenario('GA 1v2  - Without Notice Application Collection After Judge Makes Decision List for Hearing', async ({api_ga}) => {

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
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);
  console.log('Without Notice General Application Initiated by Claimant : ' + gaCaseReference);

  console.log('*** Start Judge makes decision List for Hearing: ' + gaCaseReference + ' ***');

  await api_ga.judgeListApplicationForHearing(config.judgeUser2WithRegionId2, gaCaseReference);
  await api_ga.assertGaAppCollectionVisiblityToUser(config.judgeUser2WithRegionId2, civilCaseReference, gaCaseReference, 'Y');
  console.log('*** End Judge makes decision - GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start  GA Case Visibility in all Collections: ' + gaCaseReference + ' ***');
  await api_ga.assertGaAppCollectionVisiblityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'Y');
  await api_ga.assertGaAppCollectionVisiblityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, null);
  await api_ga.assertGaAppCollectionVisiblityToUser(config.secondDefendantSolicitorUser, civilCaseReference, gaCaseReference, null);

  console.log('*** End of Validating  GA Case Visibility in all Collections: ' + gaCaseReference + ' ***');

}).retry(1).tag('@civil-service-master @civil-service-pr');

Scenario('GA 1v2  - Without Notice Application Collection after Creation of GA Case Test', async ({api_ga}) => {

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
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);
  console.log('Without Notice General Application Initiated by Claimant : ' + gaCaseReference);

  console.log('*** Start  GA Case Visibility in all Collections: ' + gaCaseReference + ' ***');
  await api_ga.assertGaAppCollectionVisiblityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'Y');
  await api_ga.assertGaAppCollectionVisiblityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, null);
  await api_ga.assertGaAppCollectionVisiblityToUser(config.secondDefendantSolicitorUser, civilCaseReference, gaCaseReference, null);
  console.log('*** End of Validating  GA Case Visibility in all Collections: ' + gaCaseReference + ' ***');

}).retry(1);

Scenario('GA 1v2  - Without Notice Application Collection after Creation of GA Case initiated by Defendant2', async ({api_ga}) => {

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
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.secondDefendantSolicitorUser, civilCaseReference);
  console.log('Without Notice General Application Initiated by Defendant2 : ' + gaCaseReference);

  console.log('*** Start  GA Case Visibility in all Collections: ' + gaCaseReference + ' ***');
  await api_ga.assertGaAppCollectionVisiblityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, null);
  await api_ga.assertGaAppCollectionVisiblityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, null);
  await api_ga.assertGaAppCollectionVisiblityToUser(config.secondDefendantSolicitorUser, civilCaseReference, gaCaseReference, 'Y');
  console.log('*** End of Validating  GA Case Visibility in all Collections: ' + gaCaseReference + ' ***');
  console.log('*** Start Judge Make Decision on GA Case Reference: ' + gaCaseReference + ' ***');

  await api_ga.judgeMakesDecisionAdditionalInformation(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge Make Decision GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Respondent respond to Judge Additional information on GA Case Reference: '
              + gaCaseReference + ' ***');
  await api_ga.respondentResponseToJudgeAdditionalInfo(config.secondDefendantSolicitorUser, gaCaseReference);
  console.log('*** End Respondent respond to Judge Additional information on GA Case Reference: '
              + gaCaseReference + ' ***');
  let doc = 'gaAddl';
  await api_ga.assertNullGaDocumentVisibilityToUser(config.applicantSolicitorUser, civilCaseReference, doc);
  await api_ga.assertNullGaDocumentVisibilityToUser(config.defendantSolicitorUser, civilCaseReference, doc);

  await api_ga.assertDocumentVisibilityToUser(config.judgeUser2WithRegionId2, 'Staff', civilCaseReference, gaCaseReference, doc);
  await api_ga.assertDocumentVisibilityToUser(config.secondDefendantSolicitorUser, 'Claimant', civilCaseReference, gaCaseReference, doc);
}).retry(0);

Scenario('GA 1v2  - Without Notice Application Collection after Judge Makes Decision Order Made', async ({api_ga}) => {

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
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.secondDefendantSolicitorUser, civilCaseReference);
  console.log('Without Notice General Application Initiated by Defendant2 : ' + gaCaseReference);

  const doc = 'generalOrder';
  console.log('*** Start Judge makes decision order made: ' + gaCaseReference + ' ***');

  await api_ga.judgeMakesDecisionOrderMade(config.judgeUser2WithRegionId2, gaCaseReference);
  await api_ga.assertGaDocumentVisibilityToUser(config.judgeUser2WithRegionId2, civilCaseReference, gaCaseReference, doc);
  console.log('*** End Judge makes decision order made - GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start GA Case Visibility in all Collections: ' + gaCaseReference + ' ***');
  await api_ga.assertGaAppCollectionVisiblityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, null);
  await api_ga.assertGaDocumentVisibilityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, doc);
  await api_ga.assertGaAppCollectionVisiblityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, null);
  await api_ga.assertGaDocumentVisibilityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, doc);
  await api_ga.assertGaAppCollectionVisiblityToUser(config.secondDefendantSolicitorUser, civilCaseReference, gaCaseReference, 'Y');
  await api_ga.assertGaDocumentVisibilityToUser(config.secondDefendantSolicitorUser, civilCaseReference, gaCaseReference, doc);
  console.log('*** End of Validating  GA Case Visibility in all Collections: ' + gaCaseReference + ' ***');
}).retry(1);

// AfterSuite(async ({api_ga}) => {
//   await api_ga.cleanUp();
// });
