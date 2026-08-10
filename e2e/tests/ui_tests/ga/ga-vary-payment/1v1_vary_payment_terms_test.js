const { waitForGACamundaEventsFinishedBusinessProcess } = require('../../../../api/testingSupport');
const { getAppTypes } = require('../../../../pages/generalApplication/generalApplicationTypes');
const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_ONE';
const awaitingPaymentStatus = states.AWAITING_APPLICATION_PAYMENT.name;
const respondentStatus = states.AWAITING_RESPONDENT_RESPONSE.name;
let civilCaseReference, gaCaseReference, user;

Feature('GA 1v1 Vary Payment Terms of Judgment - General Application Journey').tag('@civil-ccd-nightly @ui-ga-vary-payment');

BeforeSuite(async ({ api_ga }) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', '11000');
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Case created for general application: ' + civilCaseReference);
});

Scenario.skip(
  'Defendant of main claim initiates Vary payment terms of judgment application',
  async ({ I, api_ga }) => {
    await I.login(config.applicantSolicitorUser);
    await I.verifyNoN245Form(civilCaseReference, getAppTypes().slice(10, 11), 'no');
    await I.login(config.defendantSolicitorUser);
    await I.initiateVaryJudgementGA(civilCaseReference, getAppTypes().slice(10, 11), 'yes', 'no', 'no');
    gaCaseReference = await api_ga.getGACaseReference(config.defendantSolicitorUser, civilCaseReference);
    await waitForGACamundaEventsFinishedBusinessProcess(
      gaCaseReference,
      states.AWAITING_APPLICATION_PAYMENT.id,
      config.defendantSolicitorUser
    );
    await I.clickAndVerifyTab(civilCaseReference, 'Applications', getAppTypes().slice(10, 11), 1);
    await I.see(awaitingPaymentStatus);
    await I.navigateToTab(gaCaseReference, 'Application');
    await I.verifyN245FormElements();
    await I.clickOnTab('Application Documents');
    await I.verifyN245FormElements();

    user = config.judgeUser2WithRegionId2;
    await I.login(user);

    await I.verifyCaseFileAppDocument(civilCaseReference, 'N245 and supporting evidence');
    await I.login(config.applicantSolicitorUser);
    await I.verifyCaseFileAppDocument(civilCaseReference, 'No document');

    await I.payAndVerifyGAStatus(
      civilCaseReference,
      gaCaseReference,
      states.AWAITING_RESPONDENT_RESPONSE.id,
      config.defendantSolicitorUser,
      respondentStatus
    );

    await I.respondToVaryJudgementApp(gaCaseReference, getAppTypes().slice(10, 11), 'doNotAccept', 'fullPayment');
    await I.respCloseAndReturnToCaseDetails();
    await waitForGACamundaEventsFinishedBusinessProcess(
      gaCaseReference,
      states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.id,
      config.applicantSolicitorUser
    );

    await api_ga.judgeListApplicationForHearing(user, gaCaseReference);
    await api_ga.verifyGAState(
      config.defendantSolicitorUser,
      civilCaseReference,
      gaCaseReference,
      'LISTING_FOR_A_HEARING'
    );
    await api_ga.verifyGAState(
      config.applicantSolicitorUser,
      civilCaseReference,
      gaCaseReference,
      'LISTING_FOR_A_HEARING'
    );
    await api_ga.assertGaAppCollectionVisiblityToUser(
      config.defendantSolicitorUser,
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

    await I.navigateToTab(civilCaseReference, 'Applications');
    await I.see(states.LISTING_FOR_A_HEARING.name);
    await I.verifyCaseFileAppDocument(civilCaseReference, 'N245 Evidence');
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
});
