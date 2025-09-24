const config = require('../../../config.js');
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
let civilCaseReference;
const claimAmountPenniesIntermediate = '9900000';
const claimAmountIntermediate = '99000';

Feature('Intermediate track - Upload documents - Bundle @master-e2e-ft @e2e-minti');

async function prepareSpecClaim(api_spec, mpScenario) {
  civilCaseReference = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false, true, claimAmountPenniesIntermediate);
}

Scenario('Spec Claim - Int track - 1v2 diff solicitor - Upload bundle', async ({ api_spec, I }) => {
  const mpScenario = 'ONE_V_TWO';
  await prepareSpecClaim(api_spec, mpScenario);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL', 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT', false, true, claimAmountIntermediate);
  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL', 'AWAITING_APPLICANT_INTENTION', false, true, claimAmountIntermediate);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario, 'JUDICIAL_REFERRAL', false, true);
  await api_spec.createFinalOrderJO(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'INTERMEDIATE');
  await api_spec.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
  await api_spec.amendHearingDueDate(config.systemupdate);
  await api_spec.hearingFeePaid(hearingCenterAdminToBeUsed);
  await api_spec.createFinalOrderJO(judgeUser, 'FREE_FORM_ORDER');
  await I.login(config.secondDefendantSolicitorUser);
  await I.evidenceUpload(civilCaseReference, true, true);
}).retry(1);

AfterSuite(async ({api_spec}) => {
 await api_spec.cleanUp();
});
