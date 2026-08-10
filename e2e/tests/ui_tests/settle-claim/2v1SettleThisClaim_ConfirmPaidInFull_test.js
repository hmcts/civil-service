const config = require('../../../config.js');
const {addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
let caseNumber;

Feature('Settle this Claim - Confirm marking as paid in full - 2v1 - spec').tag('@civil-ccd-nightly @ui-settle-claim');

Scenario('01 Prepare 2v1 spec small track claim up to claim issue', async ({api_spec_small, LRspec}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');
  caseNumber = await api_spec_small.getCaseId();
  await LRspec.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(2);

Scenario('02 Confirm marking as paid in full', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.requestForSettleThisClaimForUI2v1();
}).retry(2);

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
  await unAssignAllUsers();
});
