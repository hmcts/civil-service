const { waitForGACamundaEventsFinishedBusinessProcess } = require('../../../../api/testingSupport');
const { getAppTypes } = require('../../../../pages/generalApplication/generalApplicationTypes');
const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const respondentStatus = states.AWAITING_RESPONDENT_RESPONSE.name;
const judgeDecisionStatus = states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name;
const listForHearingStatus = states.LISTING_FOR_A_HEARING.name;
const awaitingPaymentStatus = states.AWAITING_APPLICATION_PAYMENT.name;
let gaCaseReference, civilCaseReference, user;

Feature('GA for Specified Claim 1v2 different Solicitor - respond to application - Hearing order journey').tag('@civil-ccd-nightly @ui-ga-hearing-notice');
// This test should be enabled after early adopters goes live for all regions

Scenario.skip(
  'GA for Specified Claim 1v2 different Solicitor - respond to application - Hearing order journey',
  async ({ api_ga, I }) => {
    civilCaseReference = await api_ga.createSpecifiedClaim(config.applicantSolicitorUser, mpScenario);
    console.log('Case created for general application: ' + civilCaseReference);
    await I.login(config.defendantSolicitorUser);
    await I.navigateToCaseDetails(civilCaseReference);
    await I.createGeneralApplication(
      getAppTypes().slice(0, 3),
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
    gaCaseReference = await api_ga.getGACaseReference(config.defendantSolicitorUser, civilCaseReference);
    await waitForGACamundaEventsFinishedBusinessProcess(
      gaCaseReference,
      states.AWAITING_APPLICATION_PAYMENT.id,
      config.defendantSolicitorUser
    );
    await I.clickAndVerifyTab(civilCaseReference, 'Applications', getAppTypes().slice(0, 3), 1);
    await I.see(awaitingPaymentStatus);

    await I.login(config.applicantSolicitorUser);
    await I.navigateToCaseDetails(gaCaseReference);
    await I.verifyNoServiceReqElements();

    await I.login(config.secondDefendantSolicitorUser);
    await I.navigateToCaseDetails(gaCaseReference);
    await I.verifyNoServiceReqElements();

    await I.payAndVerifyGAStatus(
      civilCaseReference,
      gaCaseReference,
      states.AWAITING_RESPONDENT_RESPONSE.id,
      config.defendantSolicitorUser,
      respondentStatus
    );
    await I.login(config.defendantSolicitorUser);
    await I.navigateToCaseDetails(gaCaseReference);
    await I.verifyApplicantSummaryPage();

    console.log('Applicant solicitor responding:' + gaCaseReference);
    await I.login(config.applicantSolicitorUser);
    await I.respondToApplication(
      gaCaseReference,
      'yes',
      'yes',
      'yes',
      'yes',
      'no',
      'signLanguageInterpreter',
      getAppTypes().slice(0, 3)
    );
    console.log('Defendant 1 solicitor responded to application: ' + gaCaseReference);
    await I.respCloseAndReturnToCaseDetails();
    await I.verifyResponseSummaryPage();
    await I.respondToSameApplicationAndVerifyErrorMsg();
    await I.navigateToTab(civilCaseReference, 'Applications');
    await I.see(respondentStatus);

    console.log('Defendant 2 solicitor responding:' + gaCaseReference);
    await I.login(config.secondDefendantSolicitorUser);
    await I.respondToApplication(
      gaCaseReference,
      'yes',
      'yes',
      'yes',
      'yes',
      'no',
      'signLanguageInterpreter',
      getAppTypes().slice(0, 3)
    );
    console.log('Defendant 2 solicitor Responded to application: ' + gaCaseReference);
    await I.respCloseAndReturnToCaseDetails();
    await waitForGACamundaEventsFinishedBusinessProcess(
      gaCaseReference,
      states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.id,
      config.secondDefendantSolicitorUser
    );
    await I.navigateToTab(civilCaseReference, 'Applications');
    await I.see(judgeDecisionStatus);

    console.log('Judge Making decision:' + gaCaseReference);

    user = config.judgeUser2WithRegionId2;
    await I.login(user);
  
    await I.judgeListForAHearingDecision('listForAHearing', gaCaseReference, 'Defendant 1', 'Hearing_order');
    await waitForGACamundaEventsFinishedBusinessProcess(
      gaCaseReference,
      states.LISTING_FOR_A_HEARING.id,
      config.applicantSolicitorUser
    );
    await I.judgeCloseAndReturnToCaseDetails();
    await I.verifyJudgesSummaryPage('Hearing order', 'Defendant 1', 'Defendant 1');
    await I.verifyUploadedApplicationDocument(gaCaseReference, 'Hearing order');
    await I.dontSee('Go');
    await I.dontSee('Next step');
    console.log('Judges list for a hearing on case: ' + gaCaseReference);
    await I.login(config.applicantSolicitorUser);
    await I.navigateToTab(civilCaseReference, 'Applications');
    await I.see(listForHearingStatus);
    await I.verifyCaseFileAppDocument(civilCaseReference, 'Hearing order');
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
});
