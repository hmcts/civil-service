const { waitForGACamundaEventsFinishedBusinessProcess } = require('../../../../api/testingSupport');
const { getAppTypes } = require('../../../../pages/generalApplication/generalApplicationTypes');
const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_ONE';
const awaitingPaymentStatus = states.AWAITING_APPLICATION_PAYMENT.name;
const respondentStatus = states.AWAITING_RESPONDENT_RESPONSE.name;
const judgeApproveOrderStatus = states.ORDER_MADE.name;

let civilCaseReference, gaCaseReference, user;

Feature('1v1 Judge makes a decision').tag('@ui-ga-make-decision');

Scenario('GA for 1v1 - Makes a decision', async ({ I, api_ga }) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Case created for general application: ' + civilCaseReference);
  await I.login(config.applicantSolicitorUser);
  await I.createGeneralApplication(
    getAppTypes().slice(3, 4),
    civilCaseReference,
    '' + 'yes',
    'no',
    'no',
    'no',
    'no',
    'no',
    'disabledAccess'
  );
  console.log('1v1 General Application created: ' + civilCaseReference);
  gaCaseReference = await api_ga.getGACaseReference(config.applicantSolicitorUser, civilCaseReference);
  await waitForGACamundaEventsFinishedBusinessProcess(
    gaCaseReference,
    states.AWAITING_APPLICATION_PAYMENT.id,
    config.applicantSolicitorUser
  );
  await I.clickAndVerifyTab(civilCaseReference, 'Applications', getAppTypes().slice(3, 4), 1);
  await I.see(awaitingPaymentStatus);

  user = config.judgeUser2WithRegionId2;
  await I.login(user);

  await I.verifyCaseFileAppDocument(civilCaseReference, 'Supporting evidence');
  await I.login(config.defendantSolicitorUser);
  await I.verifyCaseFileAppDocument(civilCaseReference, 'No document');

  await I.payAndVerifyGAStatus(
    civilCaseReference,
    gaCaseReference,
    states.AWAITING_RESPONDENT_RESPONSE.id,
    config.applicantSolicitorUser,
    respondentStatus
  );

  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);

  await I.login(user);
  await I.judgeMakeDecision(
    'makeAnOrder',
    'approveOrEditTheOrder',
    'no',
    gaCaseReference,
    'General_order',
    'courtOwnInitiativeOrder'
  );
  await waitForGACamundaEventsFinishedBusinessProcess(
    gaCaseReference,
    states.ORDER_MADE.id,
    config.applicantSolicitorUser
  );
  await I.judgeCloseAndReturnToCaseDetails();
  await I.verifyJudgesSummaryPage('Approve order', 'no', 'Claimant');
  await I.verifyUploadedApplicationDocument(gaCaseReference, 'General order');
  console.log('Judges made a decision on case: ' + gaCaseReference);
  await I.login(config.applicantSolicitorUser);
  await I.navigateToTab(civilCaseReference, 'Applications');
  await I.see(judgeApproveOrderStatus);
  await I.verifyUploadedClaimDocument(civilCaseReference, 'General order document');
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.defendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    'Y'
  );

  await I.verifyCaseFileOrderDocument(civilCaseReference, 'General order document');
  await I.verifyCaseFileAppDocument(civilCaseReference, 'Applicant Evidence');
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
});
