

const config = require('../../../config.js');
const {PUBLIC_QUERY} = require('../../../fixtures/queryTypes');
const mpScenario = 'ONE_V_TWO_ONE_LEGAL_REP';

Feature('1v2SS query management api journey').tag('@civil-service-nightly');

Scenario('01 Create claim', async ({I, api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
});

Scenario('02 Claimant queries', async ({ api, qmSteps }) => {
  const caseId = await api.getCaseId();
  const query = await qmSteps.raiseLRQuery(caseId, config.applicantSolicitorUser, PUBLIC_QUERY, false);
  await qmSteps.respondToQuery(caseId, config.ctscAdminUser, query, PUBLIC_QUERY);
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
