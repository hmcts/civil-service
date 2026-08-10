const { waitForGACamundaEventsFinishedBusinessProcess } = require('../../../../api/testingSupport');
const { getAppTypes } = require('../../../../pages/generalApplication/generalApplicationTypes');
const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_ONE';
const awaitingPaymentStatus = states.AWAITING_APPLICATION_PAYMENT.name;
const judgeDecisionStatus = states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name;
const judgeDismissOrderStatus = states.APPLICATION_DISMISSED.name;
const claimantType = 'Company';

let civilCaseReference, gaCaseReference, user;

Feature('GA CCD 1v1 - General Application Journey').tag('@ui-ga-make-decision');

Scenario('GA for 1v1 Specified Claim- Dismissal order journey', async ({ I, api_ga }) => {
  civilCaseReference = await api_ga.createSpecifiedClaim(config.applicantSolicitorUser, mpScenario, claimantType);
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

  user = config.judgeUser2WithRegionId4;
  await I.login(user);

  await I.judgeMakeDecision(
    'makeAnOrder',
    'dismissTheApplication',
    'no',
    gaCaseReference,
    'Dismissal_order',
    'noneOrder'
  );
  await waitForGACamundaEventsFinishedBusinessProcess(
    gaCaseReference,
    states.APPLICATION_DISMISSED.id,
    config.applicantSolicitorUser
  );
  await I.judgeCloseAndReturnToCaseDetails();
  await I.verifyJudgesSummaryPage('Dismissal order', 'no', 'Claimant');
  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Dismissal order');
  await I.dontSee('Go');
  await I.dontSee('Next step');
  console.log('Judges Dismissed this order: ' + gaCaseReference);

  await I.login(config.applicantSolicitorUser);
  await I.navigateToTab(civilCaseReference, 'Applications');
  await I.see(judgeDismissOrderStatus);
  await I.verifyUploadedClaimDocument(civilCaseReference, 'Dismissal order document');

  await I.verifyCaseFileOrderDocument(civilCaseReference, 'Dismissal order document');
  await I.verifyCaseFileAppDocument(civilCaseReference, 'Applicant Evidence');

  await api_ga.verifyGAState(
    config.defendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    states.APPLICATION_DISMISSED.id
  );
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.defendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    'Y'
  );
}).retry(0);

// AfterSuite(async ({ api_ga }) => {
//   await api_ga.cleanUp();
// });
