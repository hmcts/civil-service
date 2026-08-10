const config = require('../../../../config.js');

const mpScenario = 'ONE_V_TWO_SAME_SOL';
let civilCaseReference, gaCaseReference;

Feature('Spec 1v2 - General Application after SDO Journey').tag('@civil-service-nightly @api-ga-final-order');

Scenario('Spec Claimant create GA - JUDICIAL_REFERRAL state', async ({api_ga}) => {
  civilCaseReference = await api_ga.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  console.log('Civil Case created for general application: ' + civilCaseReference);
  await api_ga.defendantResponseSpecClaim(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_ga.claimantResponseClaimSpec(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO',
    'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);

  console.log('*** Start Judge makes decision order made: ' + gaCaseReference + ' ***');
  await api_ga.judgeMakesDecisionOrderMade(config.judgeUser2WithRegionId2, gaCaseReference);
  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'ORDER_MADE');

  await api_ga.hearingCenterAdminScheduleHearing(config.hearingCenterAdminWithRegionId2, gaCaseReference);

  await api_ga.verifyGAState(config.applicantSolicitorUser, civilCaseReference, gaCaseReference, 'HEARING_SCHEDULED');

  await api_ga.judgeMakeFinalOrder(config.judgeUser2WithRegionId2, gaCaseReference, 'ASSISTED_ORDER', true);
}).retry(1);

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
