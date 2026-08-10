const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const judgeDecisionStatus = states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name;
let gaCaseReference, civilCaseReference;

Feature('1v2 Different Solicitor - General Application Journey').tag('@civil-ccd-nightly @ui-ga-directions-order');

BeforeSuite(async ({ api_ga }) => {
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
});


Scenario('Without Notice application to With Notice application - Directions Order', async ({ api_ga, I }) => {
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(
    config.secondDefendantSolicitorUser,
    civilCaseReference
  );
  await I.login(config.secondDefendantSolicitorUser);
  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(judgeDecisionStatus);
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.applicantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    null
  );
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.defendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    null
  );

  await api_ga.judgeRequestMoreInformationUncloak(config.judgeUser2WithRegionId2, gaCaseReference, true, true);

  await api_ga.additionalPaymentSuccess(
    config.secondDefendantSolicitorUser,
    gaCaseReference,
    states.AWAITING_RESPONDENT_RESPONSE.id
  );

  await api_ga.verifyGAState(
    config.applicantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    states.AWAITING_RESPONDENT_RESPONSE.id
  );
  await api_ga.verifyGAState(
    config.defendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    states.AWAITING_RESPONDENT_RESPONSE.id
  );
  await api_ga.verifyGAState(
    config.secondDefendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    states.AWAITING_RESPONDENT_RESPONSE.id
  );

  await api_ga.respondentResponse1v2(config.defendantSolicitorUser, config.applicantSolicitorUser, gaCaseReference);

  await api_ga.judgeMakesDecisionDirectionsOrder(config.judgeUser2WithRegionId2, gaCaseReference);

  await api_ga.verifyGAState(
    config.defendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    states.AWAITING_DIRECTIONS_ORDER_DOCS.id
  );
  await api_ga.verifyGAState(
    config.applicantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    states.AWAITING_DIRECTIONS_ORDER_DOCS.id
  );
  await api_ga.verifyGAState(
    config.secondDefendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    states.AWAITING_DIRECTIONS_ORDER_DOCS.id
  );
}).retry(1);
