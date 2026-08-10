const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const config = require('../../../../config.js');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const respondentStatus = states.AWAITING_RESPONDENT_RESPONSE.name;
const judgeDecisionStatus = states.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.name;
const writtenRepStatus = states.AWAITING_WRITTEN_REPRESENTATIONS.name;
let gaCaseReference, civilCaseReference;

Feature('1v2 Different Solicitor - General Application Collections test Journey').tag('@civil-ccd-nightly @ui-ga-collections');

Scenario(
  'Without Notice application - Org2 Solicitor Initiate GA - Awaiting Written Representations',
  async ({ api_ga, I }) => {
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
    console.log('Civil Case created for general application: ' + civilCaseReference);

    gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(
      config.defendantSolicitorUser,
      civilCaseReference
    );
    await I.login(config.defendantSolicitorUser);
    await I.navigateToApplicationsTab(civilCaseReference);
    await I.see(judgeDecisionStatus);

    await api_ga.judgeMakesDecisionWrittenRep(config.judgeUser2WithRegionId2, gaCaseReference);

    await I.navigateToApplicationsTab(civilCaseReference);
    await I.see(writtenRepStatus);

    await api_ga.assertGAApplicantDisplayName(config.defendantSolicitorUser, gaCaseReference);

    await api_ga.assertGACollectionNotVisiblityToUser(config.applicantSolicitorUser, civilCaseReference, gaCaseReference);
    await api_ga.assertGACollectionNotVisiblityToUser(
      config.secondDefendantSolicitorUser,
      civilCaseReference,
      gaCaseReference
    );
}).retry(1);

Scenario('With Notice application - Org3 Solicitor Initiate GA', async ({ api_ga, I }) => {
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
  console.log('Civil Case created for general application: ' + civilCaseReference);
  gaCaseReference = await api_ga.initiateGeneralApplicationWithNoStrikeOut(
    config.secondDefendantSolicitorUser,
    civilCaseReference
  );
  await I.login(config.secondDefendantSolicitorUser);
  await I.navigateToApplicationsTab(civilCaseReference);
  await I.see(respondentStatus);

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
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.applicantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    'Y'
  );
  await api_ga.assertGaAppCollectionVisiblityToUser(
    config.defendantSolicitorUser,
    civilCaseReference,
    gaCaseReference,
    'Y'
  );
  await api_ga.assertGAApplicantDisplayName(config.secondDefendantSolicitorUser, gaCaseReference);
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
});
