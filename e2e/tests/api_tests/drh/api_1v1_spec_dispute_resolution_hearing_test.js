const config = require('../../../config.js');

const mpScenario1v1 = 'ONE_V_ONE';
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
// To use on local because the idam images are different
//const judgeUser = config.judgeUserWithRegionId1Local;
//const hearingCenterAdminToBeUsed = config.hearingCenterAdminLocal;
const claimAmount = '100';

let mediationAdminRegion4 = config.localMediationTests ? config.nbcUserLocal : config.nbcTeamLeaderWithRegionId4;

Feature('Dispute resolution hearing API test - fast claim - unspec').tag('@civil-service-nightly @api-drh');

Scenario('1v1 spec small create SDO for DRH - CARM enabled', async ({api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, true);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', false, true);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', true);
  await api_spec_small.mediationUnsuccessful(mediationAdminRegion4, true);
  await api_spec_small.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec_small.uploadMediationDocuments(config.defendantSolicitorUser);
  await api_spec_small.createSDO(config.judgeUser2WithRegionId4, 'CREATE_SMALL_DRH_CARM', true);
});

Scenario('1v1 spec small create SDO for DRH - CARM disabled', async ({api_spec_small}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', false, false);
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', false, false);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true, 'No', false);
  await api_spec_small.uploadMediationDocuments(config.applicantSolicitorUser);
  await api_spec_small.uploadMediationDocuments(config.defendantSolicitorUser);
  await api_spec_small.createSDO(config.judgeUser2WithRegionId4, 'CREATE_SMALL_DRH', false);
});

AfterSuite(async ({api, api_spec_small}) => {
  await api.cleanUp();
  await api_spec_small.cleanUp();
});
