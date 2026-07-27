const { waitForGACamundaEventsFinishedBusinessProcess } = require('../../../../api/testingSupport');
const states = require('../../../../fixtures/ga-events/ga-ccd/state');
const listForHearingStatus = states.LISTING_FOR_A_HEARING.name;
const hnStatus = states.HEARING_SCHEDULED.name;
const mpScenario = 'ONE_V_ONE';
let civilCaseReference, gaCaseReference;
const config = require('../../../../config.js');
Feature('Before SDO 1v1 - GA CP - Hearing Notice document').tag('@civil-ccd-nightly @ui-ga-hearing-notice');

BeforeSuite(async ({ api_ga }) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(
    config.applicantSolicitorUser,
    mpScenario,
    'SoleTrader',
    '11000'
  );
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  console.log('Civil Case created for general application: ' + civilCaseReference);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
});

Scenario('Claimant and Defendant Hearing notice - With notice journey', async ({ I, api_ga }) => {
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplication(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Judge List the application for hearing on GA Case Reference: ' + gaCaseReference + ' ***');

  await api_ga.judgeListApplicationForHearing(config.judgeUser2WithRegionId2, gaCaseReference);

  console.log('Hearing Notice creation');
  await I.login(config.hearingCenterAdminWithRegionId2);
  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(listForHearingStatus);
  await I.navigateToHearingNoticePage(gaCaseReference);
  await I.fillHearingNotice(gaCaseReference, 'claimAndDef', 'basildon', 'VIDEO');
  await waitForGACamundaEventsFinishedBusinessProcess(
    gaCaseReference,
    states.HEARING_SCHEDULED.id,
    config.hearingCenterAdminWithRegionId2
  );
  console.log('Hearing Notice created for: ' + gaCaseReference);
  await I.click('Close and Return to case details');
  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Hearing Notice');
  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(hnStatus);
  await I.verifyUploadedClaimDocument(civilCaseReference, 'Hearing Notice');

  await I.login(config.applicantSolicitorUser);
  await I.verifyUploadedClaimDocument(civilCaseReference, 'Hearing Notice');
  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Hearing Notice');

  await I.login(config.defendantSolicitorUser);
  await I.verifyUploadedClaimDocument(civilCaseReference, 'Hearing Notice');
  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Hearing Notice');

  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.hearingCenterAdminWithRegionId2,
    civilCaseReference,
    gaCaseReference,
    'Y'
  );
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.judgeUser2WithRegionId2,
    civilCaseReference,
    gaCaseReference,
    'Y'
  );

  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.applicantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    'Y'
  );
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.defendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    'Y'
  );
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
});
