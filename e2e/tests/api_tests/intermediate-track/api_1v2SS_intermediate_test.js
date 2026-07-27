

const config = require('../../../config.js');
const intermediateTrackClaimAmount = '99000';
const mintiEnabled = true;
const track = 'INTERMEDIATE_CLAIM';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;

Feature('1v2SS unspec intermediate track api journey').tag('@civil-service-nightly @api-intermediate-track');

Scenario('1v2 Same Solicitor Create Unspecified Intermediate Track claim', async ({api}) => {
  const mpScenario = 'ONE_V_TWO_ONE_LEGAL_REP';
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, intermediateTrackClaimAmount, mintiEnabled);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, null, track);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL', 'FOR_SDO', track);
  await api.createFinalOrder(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
  await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario);
  await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL', true);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
