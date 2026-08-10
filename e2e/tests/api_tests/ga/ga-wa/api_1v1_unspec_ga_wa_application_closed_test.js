const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
let civilCaseReference, gaCaseReference;

Feature(' GA - WA Application Closed');

Scenario.skip('1v1 Unspec GA-WA Application closed test', async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(
  config.applicantSolicitorUser, mpScenario, 'Company');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);
  console.log('*** General Application case created ***' + gaCaseReference);
  // We need to fix the below steps
/*  await api_ga.amendclaimDismissedDeadline(config.systemupdate);
  await api_ga.caseDismisalScheduler(civilCaseReference, gaCaseReference, systemUpdate);*/
}).retry(0);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
