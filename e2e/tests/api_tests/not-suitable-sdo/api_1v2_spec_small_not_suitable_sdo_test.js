const config = require('../../../config.js');

const mpScenario1v2Spec = 'ONE_V_TWO';
const judgeUser = config.judgeUserWithRegionId1;
const caseWorkerUser = config.hearingCenterAdminWithRegionId1;

Feature('Transfer Online Case 1v2 API test - small claim - spec').tag('@civil-service-nightly');

Scenario('Transfer Online Spec claim 1v2 - not suitable SDO - Transfer Case', async ({api_spec}) => {
    await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
    await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', mpScenario1v2Spec);
    await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'JUDICIAL_REFERRAL');
    await api_spec.notSuitableSDOspec(judgeUser, 'CHANGE_LOCATION');
    await api_spec.transferCaseSpec(caseWorkerUser);
});

Scenario('Transfer Online Spec claim 1v2 - not suitable SDO - Other reasons', async ({api_spec}) => {
    await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
    await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', mpScenario1v2Spec);
    await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO', 'JUDICIAL_REFERRAL');
    await api_spec.notSuitableSDOspec(judgeUser, 'OTHER_REASONS');
}).tag('@api-not-suitable-sdo');

AfterSuite(async ({api_spec}) => {
  await api_spec.cleanUp();
});
