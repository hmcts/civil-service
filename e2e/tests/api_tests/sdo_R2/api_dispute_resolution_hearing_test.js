const config = require('../../../config.js');

const mpScenario1v1 = 'ONE_V_ONE';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
// To use on local because the idam images are different
//const judgeUser = config.judgeUserWithRegionId1Local;
//const hearingCenterAdminToBeUsed = config.hearingCenterAdminLocal;
const claimAmount = '100';

let mediationAdminRegion4 = config.localMediationTests ? config.nbcUserLocal : config.nbcTeamLeaderWithRegionId4;

Feature('Dispute resolution hearing API test - fast claim - unspec @api-unspec @api-tests-1v1 @api-prod @api-r2-sdo');

async function prepareClaim(api) {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario1v1, claimAmount);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario1v1, null);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario1v1, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO');
}

async function prepareClaim1v1(api_spec_small, carmEnabled) {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, carmEnabled);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', false, carmEnabled);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', carmEnabled);
}

Scenario('1v1 unspec create SDO for DRH', async ({api}) => {
    await prepareClaim(api);
    await api.createSDO(judgeUser, 'CREATE_SMALL_DRH');
    await api.evidenceUploadApplicant(config.applicantSolicitorUser, mpScenario1v1, 'DRH');
    await api.evidenceUploadRespondent(config.defendantSolicitorUser, mpScenario1v1, 'DRH');
    await api.scheduleHearing(hearingCenterAdminToBeUsed, 'SMALL_CLAIMS');
    await api.amendHearingDueDate(config.systemupdate);
    await api.hearingFeePaidDRH(hearingCenterAdminToBeUsed);
    if (['demo'].includes(config.runningEnv)) {
      await api.triggerBundle(config.systemupdate);
    }
    await api.createFinalOrderJO(judgeUser, 'FREE_FORM_ORDER');
});

Scenario('1v1 spec small create SDO for DRH - CARM enabled', async ({api_spec_small}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim1v1(api_spec_small, true);
    await api_spec_small.mediationUnsuccessful(mediationAdminRegion4, true);
    await api_spec_small.uploadMediationDocuments(config.applicantSolicitorUser);
    await api_spec_small.uploadMediationDocuments(config.defendantSolicitorUser);
    await api_spec_small.createSDO(config.judgeUser2WithRegionId4, 'CREATE_SMALL_DRH_CARM', true);
  }
});

Scenario('1v1 spec small create SDO for DRH - CARM disabled', async ({api_spec_small}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim1v1(api_spec_small, false);
    await api_spec_small.uploadMediationDocuments(config.applicantSolicitorUser);
    await api_spec_small.uploadMediationDocuments(config.defendantSolicitorUser);
    await api_spec_small.createSDO(config.judgeUser2WithRegionId4, 'CREATE_SMALL_DRH', false);
  }
});

AfterSuite(async ({api, api_spec_small}) => {
  await api.cleanUp();
  await api_spec_small.cleanUp();
});
