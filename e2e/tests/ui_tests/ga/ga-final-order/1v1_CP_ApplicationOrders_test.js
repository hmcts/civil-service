const states = require('../../../../fixtures/ga-events/ga-ccd/state');
const { waitForGACamundaEventsFinishedBusinessProcess } = require('../../../../api/testingSupport');
const mpScenario = 'ONE_V_ONE';
const doc = 'hearingNotice';
let civilCaseReference, gaCaseReference, user;
const judgeApproveOrderStatus = states.ORDER_MADE.name;
const config = require('../../../../config.js');
Feature('Before SDO 1v1 - GA CP - Applications Orders').tag('@civil-ccd-nightly @ui-ga-final-order');

Scenario('1v1 - Free form order - With notice journey', async ({ I, api_ga }) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplication(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Judge List the application for hearing on GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.judgeListApplicationForHearing(config.judgeUser2WithRegionId2, gaCaseReference);

  console.log('Hearing Notice creation');
  await api_ga.hearingCenterAdminScheduleHearing(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  await api_ga.assertGaDocumentVisibilityToUser(
    config.judgeUser2WithRegionId2,
    civilCaseReference,
    gaCaseReference,
    doc
  );
  console.log('Hearing Notice created for: ' + gaCaseReference);

  console.log('Judge making Free form application order for: ' + gaCaseReference);
  user = config.judgeUser2WithRegionId2;

  await I.login(user);
  await I.judgeMakeAppOrder(gaCaseReference, 'freeFromOrder', 'withoutNoticeOrder');
  await I.judgeCloseAndReturnToCaseDetails();
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, states.ORDER_MADE.id, user);
  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Free From Order');
  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(judgeApproveOrderStatus);
  await I.verifyUploadedClaimDocument(civilCaseReference, 'Free From Order');
  await I.verifyCaseFileOrderDocument(civilCaseReference, 'General order document');
  await I.verifyCaseFileAppDocument(civilCaseReference, 'Hearing Notice');
}).retry(1);

Scenario('1v1 - Assisted order - Without Further Hearing', async ({ api_ga, I }) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplication(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start response to GA Case Reference: ' + gaCaseReference + ' ***');
  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);
  console.log('*** End Response to GA Case Reference: ' + gaCaseReference + ' ***');

  console.log('*** Start Judge List the application for hearing on GA Case Reference: ' + gaCaseReference + ' ***');
  const doc = 'hearingNotice';

  await api_ga.judgeListApplicationForHearing(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge List the application for hearing GA Case Reference: ' + gaCaseReference + ' ***');

  await api_ga.hearingCenterAdminScheduleHearing(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  await api_ga.assertGaDocumentVisibilityToUser(
    config.judgeUser2WithRegionId2,
    civilCaseReference,
    gaCaseReference,
    doc
  );

  await api_ga.judgeMakeFinalOrder(config.judgeUser2WithRegionId2, gaCaseReference, 'ASSISTED_ORDER', false);

  await I.login(config.judgeUser2WithRegionId2);

  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Assisted Order');
  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(judgeApproveOrderStatus);
  await I.verifyUploadedClaimDocument(civilCaseReference, 'Assisted Order');
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
});
