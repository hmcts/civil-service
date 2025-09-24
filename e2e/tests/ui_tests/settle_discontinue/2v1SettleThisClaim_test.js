const config = require('../../../config.js');
const {addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
let caseNumber;

async function prepareClaimSpec2v1(api_spec_small) {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
}
Feature('Settle this Claim - Confirm marking as paid in full - 2v1 - spec @e2e-nightly-prod');

Scenario('2v1 spec Settle this Claim - Confirm marking as paid in full', async ({api_spec_small, LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaimSpec2v1(api_spec_small);
    caseNumber = await api_spec_small.getCaseId();
    await LRspec.setCaseId(caseNumber);
    addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
  }
}).retry(2);

Scenario('Confirm marking as paid in full', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.login(config.applicantSolicitorUser);
    await LRspec.requestForSettleThisClaimForUI2v1();
    }
}).retry(2);

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
  await unAssignAllUsers();
});

Feature('Settle this Claim - Reason for settlement - judges order - 2v1 - spec @e2e-nightly-prod');

Scenario('2v1 spec Reason for settlement - judges order', async ({api_spec_small, LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaimSpec2v1(api_spec_small);
    caseNumber = await api_spec_small.getCaseId();
    await LRspec.setCaseId(caseNumber);
    addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
  }
}).retry(2);

Scenario('Reason for settlement - judges order', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.login(config.hearingCenterAdminWithRegionId1);
    await LRspec.requestSettleThisClaimJudgesOrderForUI();
  }
}).retry(2);

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
  await unAssignAllUsers();
});

Feature('Settle this Claim - Reason for settlement - Consent order - 2v1 - spec @e2e-nightly-prod');

Scenario('2v1 spec Reason for settlement - Consent order', async ({api_spec_small, LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaimSpec2v1(api_spec_small);
    caseNumber = await api_spec_small.getCaseId();
    await LRspec.setCaseId(caseNumber);
    addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
  }
}).retry(2);

Scenario('Reason for settlement - Consent order', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.login(config.hearingCenterAdminWithRegionId1);
    await LRspec.requestSettleThisClaimConsentOrderForUI();
  }
}).retry(2);

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
  await unAssignAllUsers();
});
