const config = require('../../../config.js');
const {PUBLIC_QUERY} = require('../../../fixtures/queryTypes');
const mpScenario = 'TWO_V_ONE';

Feature('2v1 query management api journey').tag('@civil-service-nightly');

Scenario('01 Prepare claim', async ({I, api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, 'TWO_V_ONE');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
});

Scenario('02 Claimant queries', async ({ api, qmSteps }) => {
  const caseId = await api.getCaseId();
  const query = await qmSteps.raiseLRQuery(caseId, config.applicantSolicitorUser, PUBLIC_QUERY, true);
  await qmSteps.respondToQuery(caseId, config.hearingCenterAdminWithRegionId1, query, PUBLIC_QUERY);
  await qmSteps.followUpOnLRQuery(caseId, config.applicantSolicitorUser, query, PUBLIC_QUERY);
});

Scenario('03 Defendant queries', async ({ api, qmSteps }) => {
  const caseId = await api.getCaseId();
  const query = await qmSteps.raiseLRQuery(caseId, config.defendantSolicitorUser, PUBLIC_QUERY, true);
  await qmSteps.respondToQuery(caseId, config.hearingCenterAdminWithRegionId1, query, PUBLIC_QUERY);
  await qmSteps.followUpOnLRQuery(caseId, config.defendantSolicitorUser, query, PUBLIC_QUERY);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
