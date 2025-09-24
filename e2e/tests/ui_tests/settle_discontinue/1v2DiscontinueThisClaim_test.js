const config = require('../../../config.js');
const {addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const judgeUser = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
let caseNumber;
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
async function prepareClaimSpec1v2(api_spec_small) {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO');
}
  async function prepareClaim(api) {
    await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
    await api.notifyClaim(config.applicantSolicitorUser, mpScenario);
    await api.notifyClaimDetails(config.applicantSolicitorUser);
    await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
    await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
    await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
}

Feature('Discontinue This Claim - Full discontinuance  - 1v2 - spec @master-e2e-ft @e2e-settle-discontinue');

Scenario('1v2 spec Discontinue This Claim - Full discontinuance', async ({api_spec_small, LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaimSpec1v2(api_spec_small);
    caseNumber = await api_spec_small.getCaseId();
    await LRspec.setCaseId(caseNumber);
    addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
  }
}).retry(2);

Scenario('Discontinue This Claim', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.login(config.applicantSolicitorUser);
    await LRspec.requestForDiscontinueThisClaimForUI1v2();
  }
}).retry(2);

Scenario('Validate Discontinuance', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.login(config.ctscAdminUser);
    await LRspec.requestForValidateDiscontinuanceForUI();
  }
}).retry(2);

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
  await unAssignAllUsers();
});

Feature('Discontinue This Claim - Hearing Schedule - Full discontinuance  - 1v2 - spec @master-e2e-ft @e2e-settle-discontinue');

Scenario('1v2 full defence unspecified - judge draws fast track WITHOUT sum of damages - hearing scheduled', async ({api, LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaim(api);
    await api.createSDO(judgeUser, 'CREATE_FAST_NO_SUM');
    await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
    caseNumber = await api.getCaseId();
    await LRspec.setCaseId(caseNumber);
    addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
  }
});

Scenario('Discontinue This Claim', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.login(config.applicantSolicitorUser);
    await LRspec.requestForDiscontinueThisClaimForUI1v2();
  }
}).retry(2);

Scenario('Validate Discontinuance', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.login(config.ctscAdminUser);
    await LRspec.requestForValidateDiscontinuanceForUI();
  }
}).retry(2);

Scenario('Claim Discontinued - Remove Hearing', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.login(config.hearingCenterAdminWithRegionId1);
    await LRspec.addCaseNote();
  }
}).retry(2);

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
  await unAssignAllUsers();
});
