 
const config = require('../../../config.js');
let civilCaseReference, gaCaseReference;
const mpScenario = 'ONE_V_ONE';
const claimAmountJudge = '11000';

Feature('Smoke test - API 1v1 spec create claim and create general application').tag('@civil-ccd-smoke');

Scenario('1v1 spec create claim and create general application', async ({I, api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', claimAmountJudge);
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.defendantSolicitorUser, civilCaseReference);
  console.log('Without Notice General Application Initiated by Defendant2 : ' + gaCaseReference);
  console.log('*** Start Judge makes decision order made: ' + gaCaseReference + ' ***');
  await api_ga.judgeMakesDecisionOrderMade(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge makes decision order made - GA Case Reference: ' + gaCaseReference + ' ***');

  await I.login(config.defendantSolicitorUser);
  await I.navigateToTab(civilCaseReference, 'Applications');
  await I.see('Order Made');
});

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
