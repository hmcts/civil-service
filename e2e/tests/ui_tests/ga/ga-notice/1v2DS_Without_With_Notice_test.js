const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const respondentStatus = states.AWAITING_RESPONDENT_RESPONSE.name;
const judgeDecisionStatus = states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name;
const listForHearingStatus = states.LISTING_FOR_A_HEARING.name;
const judgeDirectionsOrderStatus = states.AWAITING_DIRECTIONS_ORDER_DOCS.name;
let gaCaseReference, civilCaseReference;

Feature('1v2 Different Solicitor - General Application Journey').tag('@civil-ccd-nightly @ui-ga-notice');

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

Scenario('Without Notice application for a hearing', async ({ api_ga, I }) => {
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(
    config.applicantSolicitorUser,
    civilCaseReference
  );
  await api_ga.judgeListApplicationForHearing(config.judgeUser2WithRegionId2, gaCaseReference);

  await I.login(config.applicantSolicitorUser);
  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(listForHearingStatus);

  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.defendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    null
  );
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.secondDefendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    null
  );
}).retry(1);

Scenario('With Notice application - Org2 Solicitor Initiate GA', async ({ api_ga, I }) => {
  gaCaseReference = await api_ga.initiateGeneralApplicationWithNoStrikeOut(
    config.defendantSolicitorUser,
    civilCaseReference
  );
  await I.login(config.defendantSolicitorUser);
  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(respondentStatus);

  await api_ga.verifyGAState(
    config.applicantSolicitorUser,
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
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.applicantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    'Y'
  );
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.secondDefendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    'Y'
  );
  await api_ga.assertGAApplicantDisplayName(config.defendantSolicitorUser, gaCaseReference);
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
});
