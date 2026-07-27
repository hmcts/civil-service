
const config = require('../../../config.js');

const claimAmountPenniesIntermediate = '9900000';
const claimAmountIntermediate = '99000';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;

Feature('1v1 spec intermediate track api journey').tag('@civil-service-nightly');

Scenario('1v1 spec full defence intermediate claim', async ({api_spec}) => {
  const mpScenario = 'ONE_V_ONE';
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'JUDICIAL_REFERRAL', false, true);
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
  await api_spec.evidenceUploadApplicant(config.applicantSolicitorUser, mpScenario);
  await api_spec.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL', true);
});

Scenario('1v1 spec full admission intermediate claim', async ({api_spec}) => {
  const mpScenario = 'ONE_V_ONE';
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_ADMISSION', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_ADMISSION', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true);
});

Scenario.skip('1v1 spec part admission intermediate claim', async ({api_spec}) => {
  const mpScenario = 'ONE_V_ONE';
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'PART_ADMISSION', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'PART_ADMISSION', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true);
});

Scenario('1v1 spec counter claim intermediate claim', async ({api_spec}) => {
  const mpScenario = 'ONE_V_ONE';
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'COUNTER_CLAIM', mpScenario, 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});