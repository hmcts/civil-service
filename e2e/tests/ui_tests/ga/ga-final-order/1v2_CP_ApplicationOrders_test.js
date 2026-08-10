const states = require('../../../../fixtures/ga-events/ga-ccd/state');
const { waitForGACamundaEventsFinishedBusinessProcess } = require('../../../../api/testingSupport');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const doc = 'hearingNotice';
const listForHearingStatus = states.LISTING_FOR_A_HEARING.name;
let civilCaseReference, gaCaseReference, user;
const config = require('../../../../config.js');
Feature('Before SDO 1v2 - GA CP - Applications Orders')
  .tag('@civil-ccd-master @civil-ccd-pr @civil-ccd-nightly @ui-ga-final-order');

Scenario('1v2 - Assisted order - With Further Hearing', async ({ I, api_ga }) => {
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
  await api_ga.defendantResponseClaim(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(
    config.applicantSolicitorUser,
    civilCaseReference
  );

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

  console.log('Judge making Assisted order for: ' + gaCaseReference);

  user = config.judgeUser2WithRegionId2;
  await I.login(user);

  await I.judgeMakeAppOrder(gaCaseReference, 'assistedOrder', 'withoutNoticeOrder');
  await I.judgeCloseAndReturnToCaseDetails();
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, states.LISTING_FOR_A_HEARING.id, user);

  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Assisted Order');

  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(listForHearingStatus);

  await I.verifyUploadedClaimDocument(civilCaseReference, 'Assisted Order');

  await I.verifyCaseFileOrderDocument(civilCaseReference, 'General order document');
  await I.verifyCaseFileAppDocument(civilCaseReference, 'Hearing Notice');
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
});
