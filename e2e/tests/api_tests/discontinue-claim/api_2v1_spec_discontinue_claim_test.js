

const config = require('../../../config.js');
const solicitorUser = config.applicantSolicitorUser;
// To use on local because the idam images are different:
// const caseWorkerUser = config.hearingCenterAdminLocal;

Feature('2v1 discontinue claim spec api journey').tag('@civil-service-nightly @api-discontinue-claim');

Scenario('2v1 discontinue claim spec', async ({I, api_spec}) => {
    let mpScenario = 'TWO_V_ONE';
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, mpScenario);
    await api_spec.discontinueClaim(solicitorUser, mpScenario);
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
