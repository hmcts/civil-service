const { waitForGACamundaEventsFinishedBusinessProcess } = require('../../../../api/testingSupport');
const { getAppTypes } = require('../../../../pages/generalApplication/generalApplicationTypes');
const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_ONE';
const awaitingPaymentStatus = states.AWAITING_APPLICATION_PAYMENT.name;
const respondentStatus = states.AWAITING_RESPONDENT_RESPONSE.name;
const judgeDecisionStatus = states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name;
const additionalInfoStatus = states.AWAITING_ADDITIONAL_INFORMATION.name;

let civilCaseReference, gaCaseReference, user;

Feature('GA 1v1 Judge Make Decision Additional Information tests').tag('@ui-ga-add-info');

Scenario('GA for 1v1- respond to application - Request more information', async ({ I, api_ga }) => {
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
    getAppTypes().slice(0, 5),
    civilCaseReference,
    'no',
    'no',
    'yes',
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
  await I.clickAndVerifyTab(civilCaseReference, 'Applications', getAppTypes().slice(0, 5), 1);
  await I.see(awaitingPaymentStatus);
  await I.payAndVerifyGAStatus(
    civilCaseReference,
    gaCaseReference,
    states.AWAITING_RESPONDENT_RESPONSE.id,
    config.applicantSolicitorUser,
    respondentStatus
  );

  await I.login(config.defendantSolicitorUser);
  await I.respondToApplication(
    gaCaseReference,
    'yes',
    'yes',
    'yes',
    'yes',
    'signLanguageInterpreter',
    getAppTypes().slice(0, 5)
  );
  console.log('Org1 solicitor Responded to application: ' + gaCaseReference);
  await I.respCloseAndReturnToCaseDetails();
  await waitForGACamundaEventsFinishedBusinessProcess(
    gaCaseReference,
    states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.id,
    config.defendantSolicitorUser
  );
  await I.navigateToTab(civilCaseReference, 'Applications');
  await I.see(judgeDecisionStatus);

  user = config.judgeUser2WithRegionId2;
  await I.login(user);

  await I.judgeRequestMoreInfo(
    'requestMoreInfo',
    'requestMoreInformation',
    gaCaseReference,
    'yes',
    'Request_for_information'
  );

  await waitForGACamundaEventsFinishedBusinessProcess(
    gaCaseReference,
    states.AWAITING_ADDITIONAL_INFORMATION.id,
    config.defendantSolicitorUser
  );

  await I.judgeCloseAndReturnToCaseDetails();
  await I.verifyJudgesSummaryPage('Request more information', 'yes', 'Claimant');
  await I.verifyUploadedApplicationDocument(gaCaseReference, 'Request for information');
  console.log('Judges requested more information on case: ' + gaCaseReference);
  await I.login(config.applicantSolicitorUser);
  await I.navigateToTab(civilCaseReference, 'Applications');
  await I.see(additionalInfoStatus);
  await I.respondToJudgeAdditionalInfo(gaCaseReference);
  console.log('Responded to Judge Additional Information on case: ' + gaCaseReference);
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
});
