
const config = require('../../../config.js');

const claimAmountPenniesIntermediate = '9900000';
const claimAmountIntermediate = '99000';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;

Feature('1v2DS spec intermediate track api journey').tag('@civil-service-nightly @api-intermediate-track');

Scenario('1v2DS spec full defence intermediate claim', async ({api_spec}) => {
  const mpScenario = 'ONE_V_TWO';
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL', 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT', false, true, claimAmountIntermediate);
  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL', 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'JUDICIAL_REFERRAL', false, true);
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
  await api_spec.evidenceUploadApplicant(config.applicantSolicitorUser, mpScenario);
  await api_spec.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL', true);
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});

