const config = require('../../../config.js');
const {getSupportWorkerFlag, getDetainedIndividualFlag, getDisruptiveIndividualFlag
} = require('../../../api/caseFlagsHelper');
const {checkLRQueryManagementEnabled} = require('../../../api/testingSupport');
const {
  APPLICANT_SOLICITOR_QUERY,
  RESPONDENT_SOLICITOR_1_QUERY,
  RESPONDENT_SOLICITOR_2_QUERY, PUBLIC_QUERY
} = require('../../../fixtures/queryTypes');
const {respondToQueryCTSCTask} = require('../../../fixtures/wa/respondToQueryTasks');

const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const fastClaimAmount = '11000';
const serviceId = 'AAA7';
const hmcTest = true;
let caseId;
let isQueryManagementEnabled = false;
const isTestEnv = ['preview', 'demo'].includes(config.runningEnv);

Feature('CCD 1v2 Unspec fast hearings API test @api-hearings-unspec @api-hearings @api-nonprod @api-prod @wa-task @QM');

Before(async () => {
  isQueryManagementEnabled = await checkLRQueryManagementEnabled();
});

async function raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, caseId, solicitorUser, caseworkerUser, queryType) {
  if (isQueryManagementEnabled) {
    const query = await qmSteps.raiseLRQuery(caseId, solicitorUser, queryType, false);
    await qmSteps.validateQmResponseTask(caseId, caseworkerUser, respondToQueryCTSCTask(query.id), query.id);
    await qmSteps.respondToQuery(caseId, caseworkerUser, query, queryType);
    const queryFollowUp = await qmSteps.followUpOnLRQuery(caseId, solicitorUser, query, queryType);
    await qmSteps.validateQmResponseTask(caseId, caseworkerUser, respondToQueryCTSCTask(queryFollowUp.id), queryFollowUp.id);
  }
}

Scenario('1v2DS full defence defendant and claimant response', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, fastClaimAmount, false, hmcTest);
  await api.notifyClaim(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO');

  caseId = await api.getCaseId();
});

Scenario('Listing officer adds case flags', async ({hearings}) => {
  await hearings.createCaseFlags(config.hearingCenterAdminWithRegionId2, caseId, 'respondent1', getDetainedIndividualFlag());
  await hearings.createCaseFlags(config.hearingCenterAdminWithRegionId2, caseId, 'respondent1', getDisruptiveIndividualFlag());
  await hearings.createCaseFlags(config.hearingCenterAdminWithRegionId2, caseId, 'respondent2Witnesses', getSupportWorkerFlag());
});

Scenario('Claimant queries', async ({api, qmSteps}) => {
  if (isTestEnv) {
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, await api.getCaseId(),
      config.applicantSolicitorUser, config.ctscAdminUser,
      PUBLIC_QUERY);
  } else {
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, await api.getCaseId(),
      config.applicantSolicitorUser, config.ctscAdminUser,
      APPLICANT_SOLICITOR_QUERY);
  }
});

Scenario('Defendant 1 solicitor queries', async ({api, qmSteps}) => {
  if (isTestEnv) {
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, await api.getCaseId(),
      config.defendantSolicitorUser, config.ctscAdminUser,
      PUBLIC_QUERY);
  } else {
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, await api.getCaseId(),
      config.defendantSolicitorUser, config.ctscAdminUser,
      RESPONDENT_SOLICITOR_1_QUERY);
  }
});

Scenario('Defendant 2 solicitor queries', async ({api, qmSteps}) => {
  if (isTestEnv) {
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, await api.getCaseId(),
      config.secondDefendantSolicitorUser, config.ctscAdminUser,
      PUBLIC_QUERY);
  } else {
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, await api.getCaseId(),
      config.secondDefendantSolicitorUser, config.ctscAdminUser,
      RESPONDENT_SOLICITOR_2_QUERY);
  }
});

Scenario('Judge choose hearing in person', async ({api}) => {
  await api.createSDO(config.judgeUser2WithRegionId2, 'CREATE_FAST_IN_PERSON');
});

Scenario('Hearing centre admin requests a hearing', async ({hearings}) => {
  await hearings.generateHearingsPayload(config.hearingCenterAdminWithRegionId2, caseId, serviceId);
});
