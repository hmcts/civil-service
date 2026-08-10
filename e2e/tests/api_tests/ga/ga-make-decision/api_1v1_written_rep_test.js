const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
let civilCaseReference, gaCaseReference;

Feature('GA 1v1 Judge Make Order Written Rep API tests');
// This test will be made run on nightly as part of this ticket CIV-14206

Scenario('Judge makes decision 1V1 - WRITTEN_REPRESENTATIONS- Respondent upload Directions Document', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(
    config.applicantSolicitorUser, mpScenario, 'Company');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplication(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Judge Make Order on GA Case Reference - WRITTEN_REPRESENTATIONS: ' + gaCaseReference + ' ***');

  await api_ga.judgeMakesDecisionWrittenRep(config.judgeUser2WithRegionId4, gaCaseReference);
  console.log('*** End Judge Make Order GA Case Reference - WRITTEN_REPRESENTATIONS: ' + gaCaseReference + ' ***');

  console.log('*** Start Judge Make Decision on GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponseToWrittenRepresentations(config.applicantSolicitorUser, gaCaseReference);
  console.log('*** End Judge Make Decision GA Case Reference: ' + gaCaseReference + ' ***');
  let doc = 'gaAddl';
  await api_ga.assertDocumentVisibilityToUser(config.applicantSolicitorUser, 'Claimant', civilCaseReference, gaCaseReference, doc);
});

Scenario('Judge uncloaked the without notice application: Judge revisit makes decision 1V1 - WRITTEN_REPRESENTATIONS- Respondent upload Directions Document', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(
    config.applicantSolicitorUser, mpScenario, 'Company');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start Judge Make Order on GA Case Reference - WRITTEN_REPRESENTATIONS: ' + gaCaseReference + ' ***');

  await api_ga.judgeRevisitMakesDecisionWrittenRepUncloakedAppln(config.judgeUser2WithRegionId4, gaCaseReference);
  console.log('*** End Judge Make Order GA Case Reference - WRITTEN_REPRESENTATIONS: ' + gaCaseReference + ' ***');

});

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
