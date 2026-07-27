const states = require('../../../../fixtures/ga-events/ga-ccd/state');
const {waitForGACamundaEventsFinishedBusinessProcess} = require('../../../../api/testingSupport');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
let civilCaseReference, gaCaseReference, user;
const config = require('../../../../config.js');
Feature('Before SDO 1v2 - GA - Consent Orders').tag('@ui-ga-consent-order');

Scenario('NBC admin Approve Consent Order', async ({ I, api_ga }) => {
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
  gaCaseReference = await api_ga.initiateConsentGeneralApplication(config.secondDefendantSolicitorUser,
    civilCaseReference, ['STAY_THE_CLAIM'],false, false);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentConsentResponse1v2(config.applicantSolicitorUser,
    config.defendantSolicitorUser, gaCaseReference, true);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  if (config.runWAApiTest || ['demo'].includes(config.runningEnv)) {
    await api_ga.retrieveTaskDetails(config.hearingCenterAdminWithRegionId2, gaCaseReference, config.waTaskIds.nbcUserReviewGA);
  } else {
    console.log('WA flag is not enabled');
    return;
  }

  console.log('NBC admin Approves Consent order' + gaCaseReference);
  user = config.hearingCenterAdminWithRegionId2;
  await I.login(user);

  await I.approveConsentOrder(gaCaseReference);
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, states.ORDER_MADE.id, user);
  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Consent Order');

  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(states.ORDER_MADE.name);

  await I.verifyUploadedClaimDocument(civilCaseReference, 'Consent order document');

  await I.verifyCaseFileOrderDocument(civilCaseReference, 'Consent Order');
  await I.verifyCaseFileAppDocument(civilCaseReference, 'Consent Order');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
