const { waitForGACamundaEventsFinishedBusinessProcess } = require('../../../../api/testingSupport');
const { getAppTypes } = require('../../../../pages/generalApplication/generalApplicationTypes');
const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_ONE';
const awaitingPaymentStatus = states.AWAITING_APPLICATION_PAYMENT.name;
const judgeDecisionStatus = states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name;
const judgeDirectionsOrderStatus = states.AWAITING_DIRECTIONS_ORDER_DOCS.name;

let civilCaseReference, gaCaseReference, user;

Feature('GA 1v1 Judge Make Order Directions Order tests').tag('@ui-ga-directions-order');

Scenario('GA for 1v1 - Direction order journey', async ({ I, api_ga }) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Case created for general application: ' + civilCaseReference);
  await I.login(config.applicantSolicitorUser);
  await I.navigateToCaseDetails(civilCaseReference);
  await I.createGeneralApplication(
    getAppTypes().slice(0, 4),
    civilCaseReference,
    'no',
    'no',
    'no',
    'yes',
    'yes',
    'no',
    'signLanguageInterpreter'
  );
  console.log('General Application created: ' + civilCaseReference);
  gaCaseReference = await api_ga.getGACaseReference(config.applicantSolicitorUser, civilCaseReference);
  await waitForGACamundaEventsFinishedBusinessProcess(
    gaCaseReference,
    states.AWAITING_APPLICATION_PAYMENT.id,
    config.applicantSolicitorUser
  );
  await I.clickAndVerifyTab(civilCaseReference, 'Applications', getAppTypes().slice(0, 4), 1);
  await I.see(awaitingPaymentStatus);
  await I.payAndVerifyGAStatus(
    civilCaseReference,
    gaCaseReference,
    states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.id,
    config.applicantSolicitorUser,
    judgeDecisionStatus
  );

  user = config.judgeUser2WithRegionId2;
  await I.login(user);

  await I.judgeMakeDecision(
    'makeAnOrder',
    'giveDirections',
    'no',
    gaCaseReference,
    'Directions_order',
    'withoutNoticeOrder'
  );
  await waitForGACamundaEventsFinishedBusinessProcess(
    gaCaseReference,
    states.AWAITING_DIRECTIONS_ORDER_DOCS.id,
    config.applicantSolicitorUser
  );
  await I.judgeCloseAndReturnToCaseDetails();
  await I.verifyJudgesSummaryPage('Judges Directions', 'no', 'Claimant');
  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Directions order');
  console.log('Judges Directions Order Made on case: ' + gaCaseReference);
  await I.login(config.applicantSolicitorUser);
  await I.navigateToTab(civilCaseReference, 'Applications');
  await I.see(judgeDirectionsOrderStatus);
  await I.verifyUploadedClaimDocument(civilCaseReference, 'Directions order document');

  await I.verifyCaseFileOrderDocument(civilCaseReference, 'Directions order document');
  await I.verifyCaseFileAppDocument(civilCaseReference, 'Applicant Evidence');

  await I.respondToJudgesDirections(gaCaseReference);
  console.log('Responded to Judges directions on case: ' + gaCaseReference);
  await api_ga.verifyGAState(
    config.defendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    states.AWAITING_DIRECTIONS_ORDER_DOCS.id
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
