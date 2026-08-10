const {waitForGACamundaEventsFinishedBusinessProcess} = require('../../../../api/testingSupport');
const {getAppTypes} = require('../../../../pages/generalApplication/generalApplicationTypes');
const states = require('../../../../fixtures/ga-events/ga-ccd/state');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_TWO_ONE_LEGAL_REP';
const respondentStatus = states.AWAITING_RESPONDENT_RESPONSE.name;
const judgeDecisionStatus = states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name;
const writtenRepStatus = states.AWAITING_WRITTEN_REPRESENTATIONS.name;
const awaitingPaymentStatus = states.AWAITING_APPLICATION_PAYMENT.name;
let civilCaseReference, gaCaseReference, user;

Feature('GA CCD 1v2 Same Solicitor - General Application Journey').tag('@ui-ga-make-decision');

BeforeSuite(async ({api_ga}) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser,
    mpScenario, 'SoleTrader', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, mpScenario, true);
  console.log('Civil Case created for general application: ' + civilCaseReference);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
});

Scenario('GA for 1v2 Same Solicitor - respond to application - Sequential written representations journey',
  async ({ I, api_ga }) => {
  await I.login(config.applicantSolicitorUser);
  await I.navigateToCaseDetails(civilCaseReference);
  await I.createGeneralApplication(
    getAppTypes().slice(0, 1),
    civilCaseReference, '' +
    'no', 'no', 'yes', 'no', 'no', 'no',
    'disabledAccess');
  console.log('General Application created: ' + civilCaseReference);
  gaCaseReference = await api_ga.getGACaseReference(config.applicantSolicitorUser, civilCaseReference);
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, states.AWAITING_APPLICATION_PAYMENT.id, config.applicantSolicitorUser);
  await I.clickAndVerifyTab(civilCaseReference, 'Applications', getAppTypes().slice(0, 1), 1);
  await I.see(awaitingPaymentStatus);
  await I.payAndVerifyGAStatus(civilCaseReference, gaCaseReference,
    states.AWAITING_RESPONDENT_RESPONSE.id, config.applicantSolicitorUser, respondentStatus);

  console.log('Defendant 1 solicitor responding:' + gaCaseReference);
  await I.login(config.defendantSolicitorUser);
  await I.respondToApplication(gaCaseReference, 'yes', 'yes', 'yes', 'yes', 'no',
    'signLanguageInterpreter', getAppTypes().slice(0, 1));
  console.log('Defendant 1 Solicitor responded to application: ' + gaCaseReference);
  await I.respCloseAndReturnToCaseDetails();
  await I.verifyResponseSummaryPage();
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference,
    states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.id, config.defendantSolicitorUser);
  await I.navigateToTab(civilCaseReference, 'Applications');
  await I.see(judgeDecisionStatus);

  console.log('Judge Making decision:' + gaCaseReference);

  user = config.judgeUser2WithRegionId4;
  await I.login(user);
  await I.judgeWrittenRepresentationsDecision('orderForWrittenRepresentations',
    'sequentialRep', gaCaseReference, 'yes', 'Order_Written_Representation_Sequential', 'noneOrder');
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, states.AWAITING_WRITTEN_REPRESENTATIONS.id, config.applicantSolicitorUser);
  await I.judgeCloseAndReturnToCaseDetails();
  await I.verifyJudgesSummaryPage('Sequential representations', 'yes', 'Claimant');
  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Written representation sequential');
  console.log('Judges made an order for Sequential written representations on case: ' + gaCaseReference);

  await I.login(config.applicantSolicitorUser);
  await I.navigateToTab(civilCaseReference, 'Applications');
  await I.see(writtenRepStatus);
  await I.respondToJudgesWrittenRep(gaCaseReference, 'Written Representation Documents');
  console.log('Responded to Judges written representations on case: ' + gaCaseReference);

  await I.verifyCaseFileAppDocument(civilCaseReference, 'Sequential order document');
});

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
