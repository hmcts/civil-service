

const config = require('../../../config.js');
const solicitorUser = config.applicantSolicitorUser;
const caseWorkerUser = config.hearingCenterAdminWithRegionId2;
// To use on local because the idam images are different:
// const caseWorkerUser = config.hearingCenterAdminLocal;

Feature('1v1 discontinue claim spec api journey').tag('@civil-service-nightly @api-discontinue-claim');

Scenario('1v1 discontinue claim spec', async ({I, api_spec}) => {
    let mpScenario = 'ONE_V_ONE';
    let permission = 'YES';
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, mpScenario);
    await api_spec.discontinueClaim(solicitorUser, mpScenario);
    await api_spec.validateDiscontinueClaimClaimant(caseWorkerUser, permission);
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
