

const config = require('../../../config.js');
const solicitorUser = config.applicantSolicitorUser;
// To use on local because the idam images are different:
// const caseWorkerUser = config.hearingCenterAdminLocal;

Feature('2v1 settle claim spec api journey').tag('@civil-service-nightly @api-settle-claim');

Scenario('2v1 settle claim spec', async ({I, api_spec}) => {
    await api_spec.createClaimWithRepresentedRespondent(solicitorUser, 'TWO_V_ONE');
    await api_spec.settleClaimSelectClaimant(solicitorUser, 'YES');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
