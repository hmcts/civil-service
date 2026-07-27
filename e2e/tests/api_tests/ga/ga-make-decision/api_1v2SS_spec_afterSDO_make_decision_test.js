const config = require('../../../../config.js');

const mpScenario = 'ONE_V_TWO_SAME_SOL';
let civilCaseReference, gaCaseReference;

Feature('Spec 1v2 - General Application after SDO Journey').tag('@civil-service-nightly @api-ga-make-decision');

Scenario.skip('Spec Claimant create GA - CASE_PROGRESSION state', async ({ api_ga, I }) => {
  civilCaseReference = await api_ga.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  console.log('Civil Case created for general application: ' + civilCaseReference);
  await api_ga.defendantResponseSpecClaim(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_ga.claimantResponseClaimSpec(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO',
    'JUDICIAL_REFERRAL');
  await I.wait(10);
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Create SDO');
  await api_ga.createSDO(civilCaseReference, config.judgeUser2WithRegionId2, 'CREATE_FAST');

  await I.wait(10);
  await api_ga.scheduleCivilHearing(civilCaseReference, config.hearingCenterAdminWithRegionId2, 'FAST_TRACK_TRIAL');
  await api_ga.amendHearingDueDate(civilCaseReference, config.systemupdate);
  await api_ga.hearingFeePaid(civilCaseReference, config.hearingCenterAdminWithRegionId2);
  await api_ga.createFinalOrder(civilCaseReference, config.judgeUser2WithRegionId2, 'FREE_FORM_ORDER');
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);

  await api_ga.judgeMakesDecisionWrittenRep(config.judgeUser2WithRegionId2, gaCaseReference);
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'AWAITING_WRITTEN_REPRESENTATIONS');
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
