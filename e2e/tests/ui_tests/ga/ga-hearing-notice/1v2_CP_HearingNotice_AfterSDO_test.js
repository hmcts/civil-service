const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const {waitForGACamundaEventsFinishedBusinessProcess} = require('../../../../api/testingSupport');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
let civilCaseReference, gaCaseReference;

Feature('After SDO 1v2 - GA CP - Hearing Notice document')
  .tag('@civil-ccd-master @civil-ccd-pr @civil-ccd-nightly @ui-ga-hearing-notice');

Scenario('Claimant Hearing notice - Without notice journey', async ({ api_ga, I }) => {
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
  await I.wait(10);
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.secondDefendantSolicitorUser, civilCaseReference);

  console.log('*** Start Judge List the application for hearing on GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.judgeListApplicationForHearingInPerson(config.judgeUser2WithRegionId2, gaCaseReference);
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, states.LISTING_FOR_A_HEARING.id);
  await api_ga.verifyGAState(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, states.LISTING_FOR_A_HEARING.id);

  console.log('Hearing Notice creation');
  await I.login(config.hearingCenterAdminWithRegionId2);

  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(states.LISTING_FOR_A_HEARING.name);
  await I.navigateToHearingNoticePage(gaCaseReference);
  await I.fillHearingNotice(gaCaseReference, 'claimant', 'default', 'IN_PERSON');
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, states.HEARING_SCHEDULED.id, config.hearingCenterAdminWithRegionId2);
  console.log('After SDO Hearing Notice created for: ' + gaCaseReference);
  await I.click('Close and Return to case details');

  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Hearing Notice');
  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(states.HEARING_SCHEDULED.name);
  await I.verifyUploadedClaimDocument(civilCaseReference, 'After SDO - Hearing Notice');

  await I.login(config.secondDefendantSolicitorUser);
  await I.verifyUploadedClaimDocument(civilCaseReference, 'After SDO - Hearing Notice');
  await I.verifyCaseFileAppDocument(civilCaseReference, 'Hearing Notice');

  await I.login(config.defendantSolicitorUser);
  await I.verifyCaseFileAppDocument(civilCaseReference, 'Hearing Notice');
  await I.dontSee('Applications', 'div.mat-tab-label-content');

  await I.login(config.applicantSolicitorUser);
  await I.verifyCaseFileAppDocument(civilCaseReference, 'Hearing Notice');
  await I.dontSee('Applications', 'div.mat-tab-label-content');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
