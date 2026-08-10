const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');

const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const hnStateStatus = states.HEARING_SCHEDULED.id;
let civilCaseReference, gaCaseReference;

Feature('GA 1v2 Judge makes order application after hearing API tests').tag('@civil-service-nightly @api-ga-final-order');

Scenario('Without Notice Hearing notice journey', async ({api_ga}) => {

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
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.defendantSolicitorUser, civilCaseReference);

  console.log('*** Start Judge makes order application after hearing on GA Case Reference: ' + gaCaseReference + ' ***');
  const doc = 'hearingNotice';

  await api_ga.judgeListApplicationForHearing(config.judgeUser2WithRegionId2, gaCaseReference);
  console.log('*** End Judge makes order application after hearing GA Case Reference: ' + gaCaseReference + ' ***');

  await api_ga.hearingCenterAdminScheduleHearing(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  await api_ga.assertGaDocumentVisibilityToUser(config.judgeUser2WithRegionId2, civilCaseReference, gaCaseReference, doc);
  await api_ga.verifyGAState(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, hnStateStatus);
  await api_ga.assertGaDocumentVisibilityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, doc);
  await api_ga.assertGaDocumentVisibilityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, doc);


  await api_ga.judgeMakeFinalOrder(config.judgeUser2WithRegionId2, gaCaseReference, 'FREE_FORM_ORDER', false);
  const finalDoc = 'generalOrder';
  await api_ga.assertGaDocumentVisibilityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, finalDoc);
  await api_ga.assertGaDocumentVisibilityToUser(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, finalDoc);


  await api_ga.hearingCenterAdminScheduleHearing(config.hearingCenterAdminWithRegionId2, gaCaseReference);
  await api_ga.verifyGAState(config.defendantSolicitorUser, civilCaseReference, gaCaseReference, 'HEARING_SCHEDULED');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
