

const config = require('../../../config.js');
const solicitorUser = config.applicantSolicitorUser;
const caseWorkerUser = config.hearingCenterAdminWithRegionId2;
// To use on local because the idam images are different:
// const caseWorkerUser = config.hearingCenterAdminLocal;

Feature('CCD Settle and discontinue claim 2v1 API test @api-spec @api-settle-discont @api-nightly-prod');
Scenario('Settle claim 2v1 scenario', async ({I, api_spec}) => {
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, 'TWO_V_ONE');
    await api_spec.createCaseFlags(caseWorkerUser);
    await api_spec.manageCaseFlags(caseWorkerUser);
    await api_spec.settleClaimSelectClaimant(solicitorUser, 'YES');
}).tag('@api-prod');

Scenario('Discontinue claim 1v2 scenario', async ({I, api_spec}) => {
    let mpScenario = 'ONE_V_TWO';
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, mpScenario);
    await api_spec.discontinueClaim(solicitorUser, mpScenario);
});

Scenario('Discontinue claim 2v1 scenario', async ({I, api_spec}) => {
    let mpScenario = 'TWO_V_ONE';
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, mpScenario);
    await api_spec.discontinueClaim(solicitorUser, mpScenario);
});

Scenario('Discontinue claim 1v1 scenario', async ({I, api_spec}) => {
    let mpScenario = 'ONE_V_ONE';
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, mpScenario);
    await api_spec.discontinueClaim(solicitorUser, mpScenario);
});

Scenario('Validate discontinue claim claimant 1v1 scenario', async ({I, api_spec}) => {
    let mpScenario = 'ONE_V_ONE';
    let permission = 'YES';
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, mpScenario);
    await api_spec.discontinueClaim(solicitorUser, mpScenario);
    await api_spec.validateDiscontinueClaimClaimant(caseWorkerUser, permission);
}).tag('@api-prod');

Scenario('Validate discontinue claim claimant 1v2 scenario', async ({I, api_spec}) => {
    let mpScenario = 'ONE_V_TWO';
    let permission = 'YES';
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, mpScenario);
    await api_spec.discontinueClaim(solicitorUser, 'ONE_V_TWO_P_NEEDED');
    await api_spec.validateDiscontinueClaimClaimant(caseWorkerUser, permission);
});

Scenario('Validate discontinue claim claimant 1v2 negative scenario', async ({I, api_spec}) => {
    let mpScenario = 'ONE_V_TWO';
    let permission = 'NO';
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, mpScenario);
    await api_spec.discontinueClaim(solicitorUser, 'ONE_V_TWO_P_NEEDED');
    await api_spec.validateDiscontinueClaimClaimant(caseWorkerUser, permission);
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
