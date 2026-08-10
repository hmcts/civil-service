const config = require('../../../config.js');
const {getSupportWorkerFlag, getDetainedIndividualFlag, getDisruptiveIndividualFlag
} = require('../../../api/caseFlagsHelper');
const {PUBLIC_QUERY} = require('../../../fixtures/queryTypes');
const {respondToQueryCTSCTask} = require('../../../fixtures/wa/respondToQueryTasks');

const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const fastClaimAmount = '11000';
const hmcTest = true;
let caseId;

Feature('CCD 1v2 Unspec fast hearings API test').tag('@civil-service-nightly @api-qm @civil-wa-master @civil-wa-pr @civil-wa-nightly');

Scenario('01 1v2DS full defence defendant and claimant response', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, fastClaimAmount, false, hmcTest);
  await api.notifyClaim(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO');

  caseId = await api.getCaseId();
});

Scenario('02 Claimant queries', async ({ api, qmSteps }) => {
  const caseId = await api.getCaseId();
  let query = await qmSteps.raiseLRQuery(caseId, config.applicantSolicitorUser, PUBLIC_QUERY, false);
  await qmSteps.validateQmResponseTask(caseId, config.ctscAdminUser, respondToQueryCTSCTask(query.id), query.id);
  await qmSteps.respondToQuery(caseId, config.ctscAdminUser, query, PUBLIC_QUERY);
  query = await qmSteps.followUpOnLRQuery(caseId, config.applicantSolicitorUser, query, PUBLIC_QUERY);
  await qmSteps.validateQmResponseTask(caseId, config.ctscAdminUser, respondToQueryCTSCTask(query.id), query.id);
});

Scenario('03 Defendant 1 solicitor queries', async ({ api, qmSteps }) => {
  const caseId = await api.getCaseId();
  let query = await qmSteps.raiseLRQuery(caseId, config.defendantSolicitorUser, PUBLIC_QUERY, false);
  await qmSteps.validateQmResponseTask(caseId, config.ctscAdminUser, respondToQueryCTSCTask(query.id), query.id);
  await qmSteps.respondToQuery(caseId, config.ctscAdminUser, query, PUBLIC_QUERY);
  query = await qmSteps.followUpOnLRQuery(caseId, config.defendantSolicitorUser, query, PUBLIC_QUERY);
  await qmSteps.validateQmResponseTask(caseId, config.ctscAdminUser, respondToQueryCTSCTask(query.id), query.id);
});

Scenario('04 Defendant 2 solicitor queries', async ({ api, qmSteps }) => {
  const caseId = await api.getCaseId();
  let query = await qmSteps.raiseLRQuery(caseId, config.secondDefendantSolicitorUser, PUBLIC_QUERY, false);
  await qmSteps.validateQmResponseTask(caseId, config.ctscAdminUser, respondToQueryCTSCTask(query.id), query.id);
  await qmSteps.respondToQuery(caseId, config.ctscAdminUser, query, PUBLIC_QUERY);
  query = await qmSteps.followUpOnLRQuery(caseId, config.secondDefendantSolicitorUser, query, PUBLIC_QUERY);
  await qmSteps.validateQmResponseTask(caseId, config.ctscAdminUser, respondToQueryCTSCTask(query.id), query.id);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});