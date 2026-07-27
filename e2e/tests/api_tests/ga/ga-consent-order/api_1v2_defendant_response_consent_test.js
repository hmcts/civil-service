const config = require('../../../../config.js');

const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
let civilCaseReference, gaCaseReference;

Feature('GA 1v2 Defendants response consent order API tests').tag('@civil-service-nightly');

Scenario('Defendants response 1V2', async ({api_ga}) => {

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
    gaCaseReference = await api_ga.initiateConsentGeneralApplication(config.defendantSolicitorUser, civilCaseReference, ['STAY_THE_CLAIM']);

    console.log('*** Verify Collections creation in Civil Claim ***');
    await api_ga.verifyCivilClaimGACollections(config.defendantSolicitorUser, civilCaseReference, gaCaseReference);

    console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
    await api_ga.respondentConsentResponse1v2(config.applicantSolicitorUser, config.secondDefendantSolicitorUser, gaCaseReference, false);
    console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');
    console.log('*** NBC Admin Region4 Refer to Judge Process Start ***');

  await api_ga.nbcAdminReferToJudge(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  console.log('*** NBC Admin Region4 Refer to Judge Process End ***');

}).retry(1);

AfterSuite(async ({api_ga}) => {
    await api_ga.cleanUp();
});
