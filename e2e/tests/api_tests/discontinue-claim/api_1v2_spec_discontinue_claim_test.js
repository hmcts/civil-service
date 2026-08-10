

const config = require('../../../config.js');
const solicitorUser = config.applicantSolicitorUser;
const caseWorkerUser = config.hearingCenterAdminWithRegionId2;
// To use on local because the idam images are different:
// const caseWorkerUser = config.hearingCenterAdminLocal;

Feature('1v2 discontinue claim spec api journey').tag('@civil-service-nightly @api-discontinue-claim');

Scenario('1v2 discontinue claim spec', async ({I, api_spec}) => {
    let mpScenario = 'ONE_V_TWO';
    let permission = 'YES';
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, mpScenario);
    await api_spec.discontinueClaim(solicitorUser, 'ONE_V_TWO_P_NEEDED');
    await api_spec.validateDiscontinueClaimClaimant(caseWorkerUser, permission);
});

Scenario('1v2 discontinue claim spec negative', async ({I, api_spec}) => {
    let mpScenario = 'ONE_V_TWO';
    let permission = 'NO';
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, mpScenario);
    await api_spec.discontinueClaim(solicitorUser, 'ONE_V_TWO_P_NEEDED');
    await api_spec.validateDiscontinueClaimClaimant(caseWorkerUser, permission);
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
