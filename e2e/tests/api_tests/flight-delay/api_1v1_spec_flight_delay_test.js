const config = require('../../../config.js');
// To use on local because the idam images are different
// const judgeUser = config.judgeUserWithRegionId1Local;
// const hearingCenterAdminToBeUsed = config.hearingCenterAdminLocal;
const judgeUser = config.judgeUser2WithRegionId4; //small claim specified goes to region4 judge
const mpScenario = 'ONE_V_ONE_FLIGHT_DELAY';
const claimAmountSmallTrack = '1500';

Feature('Create SDO SmallTrack- Flight Delay - 1v1 - spec').tag('@civil-service-nightly @api-flight-delay');

Scenario('1v1 full defence unspecified - judge draws small claims WITHOUT sum of damages - flight delay', async ({api_spec}) => {
  await api_spec.createClaimSpecFlightDelay(config.applicantSolicitorUser, mpScenario);
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponseForFlightDelay(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', 'AWAITING_APPLICANT_INTENTION');
  await api_spec.createSDO(judgeUser, 'CREATE_SMALL');
});

Scenario('1v1 specified - flight delay other option Small-claim', async ({api_spec}) => {
  await api_spec.createClaimSpecFlightDelay(config.applicantSolicitorUser, 'ONE_V_ONE_FLIGHT_DELAY_OTHER');
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponseForFlightDelay(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', 'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async ({api_spec}) => {
  await api_spec.cleanUp();
});
