

const config = require('../../../config.js');
const {APPLICANT_SOLICITOR_QUERY, RESPONDENT_SOLICITOR_1_AND_2_QUERY, PUBLIC_QUERY} = require('../../../fixtures/queryTypes');
const {checkLRQueryManagementEnabled} = require('../../../api/testingSupport');
const mpScenario = 'ONE_V_TWO_ONE_LEGAL_REP';
let isQueryManagementEnabled = false;
const isTestEnv = ['preview', 'demo'].includes(config.runningEnv);

Feature('CCD 1v2 Same Solicitor API test @api-unspec @api-tests-1v2SS @api-nightly-prod @api-unspec-full-defence @QM');

async function raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, caseId, solicitorUser, caseworkerUser, queryType, isHearingRelated) {
  if (isQueryManagementEnabled) {
    const query = await qmSteps.raiseLRQuery(caseId, solicitorUser, queryType, isHearingRelated);
    await qmSteps.respondToQuery(caseId, caseworkerUser, query, queryType);
    await qmSteps.followUpOnLRQuery(caseId, solicitorUser, query, queryType);
  }
}

Before(async () => {
  isQueryManagementEnabled = await checkLRQueryManagementEnabled();
});

Scenario('Create claim', async ({I, api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
});

Scenario('HMCTS admin adds a case note to case', async ({I, api}) => {
  await api.addCaseNote(config.adminUser);
});

Scenario('Amend claim documents', async ({I, api}) => {
  await api.amendClaimDocuments(config.applicantSolicitorUser);
});

Scenario('Notify claim', async ({I, api}) => {
  await api.notifyClaim(config.applicantSolicitorUser);
});

Scenario('Notify claim details', async ({I, api}) => {
  await api.notifyClaimDetails(config.applicantSolicitorUser);
});

Scenario('Amend party details', async ({I, api}) => {
  await api.amendPartyDetails(config.adminUser);
});

Scenario('Acknowledge claim', async ({I, api}) => {
  await api.acknowledgeClaim(config.defendantSolicitorUser, mpScenario);
});

Scenario('Inform agreed extension date', async ({I, api}) => {
  await api.informAgreedExtension(config.defendantSolicitorUser, mpScenario);
});

Scenario('Defendant response', async ({I, api}) => {
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario);
});

Scenario('Claimant response', async ({I, api}) => {
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
});

Scenario('Claimant queries', async ({api, qmSteps}) => {
  if (isTestEnv) {
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, await api.getCaseId(),
      config.applicantSolicitorUser, config.ctscAdminUser,
      PUBLIC_QUERY, false
    );
  } else {
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, await api.getCaseId(),
      config.applicantSolicitorUser, config.ctscAdminUser,
      APPLICANT_SOLICITOR_QUERY, false
    );
  }
});

Scenario('Defendant queries', async ({api, qmSteps}) => {
  if (isTestEnv) {
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, await api.getCaseId(),
      config.defendantSolicitorUser, config.hearingCenterAdminWithRegionId1,
      PUBLIC_QUERY, true
    );
  } else {
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, await api.getCaseId(),
      config.defendantSolicitorUser, config.hearingCenterAdminWithRegionId1,
      RESPONDENT_SOLICITOR_1_AND_2_QUERY, true
    );
  }
});


Scenario('Add case flags', async ({api}) => {
  await api.createCaseFlags(config.hearingCenterAdminWithRegionId1);
});

Scenario('Manage case flags', async ({api}) => {
  await api.manageCaseFlags(config.hearingCenterAdminWithRegionId1);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
