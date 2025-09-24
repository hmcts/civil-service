const config = require('../../../config.js');

const mpScenario1v1 = 'ONE_V_ONE';
const mpScenario1v2Spec = 'ONE_V_TWO';
const mpScenario1v2 = 'ONE_V_TWO_TWO_LEGAL_REP';
const judgeUser = config.judgeUserWithRegionId1;
const caseWorkerUser = config.hearingCenterAdminWithRegionId1;
// To use on local because the idam images are different
// const judgeUser = config.judgeUserWithRegionId1Local;
// const caseWorkerUser = config.tribunalCaseworkerWithRegionId1Local;
const fastClaimAmount = '11000';

Feature('Transfer Online Case 1v1 API test - fast claim - unspec @api-unspec @api-nightly-prod @api-toc');

async function prepareClaim(api) {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario1v1, fastClaimAmount);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario1v1, null, 'SMALL_CLAIMS');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario1v1, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
}

async function prepareClaimSpec(api_spec) {
await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', mpScenario1v2Spec);
await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO',
  'JUDICIAL_REFERRAL');
}

Scenario('1v1 full defence unspecified - not suitable SDO - Transfer Case)', async ({api}) => {
    await prepareClaim(api);
    await api.notSuitableSDO(judgeUser, 'CHANGE_LOCATION');
    await api.transferCase(caseWorkerUser);
});

Scenario('1v1 full defence unspecified - not suitable SDO - Other reasons)', async ({api}) => {
    await prepareClaim(api);
    await api.notSuitableSDO(judgeUser, 'OTHER_REASONS');
});

AfterSuite(async ({api}) => {
  await api.cleanUp();
});

Feature('Transfer Online Case 1v2 API test - fast claim - unspec @api-unspec @api-tests-1v2SS @api-nightly-prod @api-toc');

Scenario('1v2 full defence unspecified - not suitable SDO - Transfer Case)', async ({api}) => {
    await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario1v2, fastClaimAmount);
    await api.amendClaimDocuments(config.applicantSolicitorUser);
    await api.notifyClaim(config.applicantSolicitorUser, mpScenario1v2);
    await api.notifyClaimDetails(config.applicantSolicitorUser);
    await api.defendantResponse(config.defendantSolicitorUser, mpScenario1v2, 'solicitorOne');
    await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario1v2, 'solicitorTwo');
    await api.claimantResponse(config.applicantSolicitorUser, mpScenario1v2, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
    await api.notSuitableSDO(judgeUser, 'CHANGE_LOCATION');
    await api.transferCase(caseWorkerUser);
});

AfterSuite(async ({api}) => {
  await api.cleanUp();
});

Feature('Transfer Online Case 1v2 API test - small claim - spec @api-spec-1v2 @api-nightly-prod @api-toc');

Scenario('Transfer Online Spec claim 1v2 - not suitable SDO - Transfer Case', async ({api_spec}) => {
    await prepareClaimSpec(api_spec);
    await api_spec.notSuitableSDOspec(judgeUser, 'CHANGE_LOCATION');
    await api_spec.transferCaseSpec(caseWorkerUser);
});

Scenario('Transfer Online Spec claim 1v2 - not suitable SDO - Other reasons', async ({api_spec}) => {
    await prepareClaimSpec(api_spec);
    await api_spec.notSuitableSDOspec(judgeUser, 'OTHER_REASONS');
});

AfterSuite(async ({api_spec}) => {
  await api_spec.cleanUp();
});
