const { waitForGACamundaEventsFinishedBusinessProcess } = require('../../../../api/testingSupport');
const { getAppTypes } = require('../../../../pages/generalApplication/generalApplicationTypes');
const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_ONE';
const awaitingPaymentStatus = states.AWAITING_APPLICATION_PAYMENT.name;
const respondentStatus = states.AWAITING_RESPONDENT_RESPONSE.name;
let civilCaseReference, gaCaseReference, user;

Feature('1v1 - With Notice - Unless order - Make a decision journey').tag('@civil-ccd-nightly @ui-ga-make-decision');

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

Scenario.skip('1v1 - With Notice - Unless order - Make a decision journey', async ({ I, api_ga }) => {
  await I.login(config.applicantSolicitorUser);
  await I.createGeneralApplication(
    getAppTypes().slice(9, 10),
    civilCaseReference,
    'no',
    'no',
    'yes',
    'no',
    'no',
    'no',
    'disabledAccess'
  );
  gaCaseReference = await api_ga.getGACaseReference(config.applicantSolicitorUser, civilCaseReference);
  await waitForGACamundaEventsFinishedBusinessProcess(
    gaCaseReference,
    states.AWAITING_APPLICATION_PAYMENT.id,
    config.applicantSolicitorUser
  );
  await I.clickAndVerifyTab(civilCaseReference, 'Applications', getAppTypes().slice(9, 10), 1);
  await I.see(awaitingPaymentStatus);
  await I.payAndVerifyGAStatus(
    civilCaseReference,
    gaCaseReference,
    states.AWAITING_RESPONDENT_RESPONSE.id,
    config.applicantSolicitorUser,
    respondentStatus
  );

  await api_ga.respondentResponse(config.defendantSolicitorUser, gaCaseReference);

  await api_ga.judgeMakesDecisionOrderMade(config.judgeUser2WithRegionId2, gaCaseReference);

  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, states.ORDER_MADE.id);
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
});
