const config = require('../../../../config.js');

const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
let civilCaseReference, gaCaseReference;

Feature('GA Claim 1v2 Notify Claim Case Close API tests').tag('@civil-service-nightly');

Scenario('Case offline 1V2 notify_claim_details AWAITING_ADDITIONAL_INFORMATION', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(
    config.applicantSolicitorUser, mpScenario, 'Company');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);

  gaCaseReference
    = await api_ga.initiateGeneralApplicationWithState(config.applicantSolicitorUser, civilCaseReference, 'AWAITING_RESPONDENT_RESPONSE');
  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse1v2(config.defendantSolicitorUser, config.secondDefendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');


  await api_ga.judgeMakesDecisionAdditionalInformation(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** Start Respondent respond to Judge Additional information on GA Case Reference: '
    + gaCaseReference + ' ***');
  await api_ga.respondentResponseToJudgeAdditionalInfo(config.applicantSolicitorUser, gaCaseReference);
  console.log('*** End Respondent respond to Judge Additional information on GA Case Reference: '
    + gaCaseReference + ' ***');

  console.log('Case offline');

  await api_ga.partialNotifyClaimDetails(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'PROCEEDS_IN_HERITAGE');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
