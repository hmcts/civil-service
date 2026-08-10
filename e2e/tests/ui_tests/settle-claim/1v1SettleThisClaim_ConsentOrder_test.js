const config = require('../../../config.js');
const {addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const legalAdvUser = config.tribunalCaseworkerWithRegionId4;
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
let caseNumber;

Feature('Settle this Claim - Reason for settlement - Consent order - 1v1 - spec').tag('@civil-ccd-nightly @ui-settle-claim');

Scenario('01 Prepare 1v1 spec small track claim up to case progression', async ({api_spec_small, LRspec}) => {
  await api_spec_small.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  await api_spec_small.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', true);
  await api_spec_small.claimantResponse(config.applicantSolicitorUser, true);
  await api_spec_small.createSDO(legalAdvUser, 'CREATE_SMALL_NO_SUM');
  caseNumber = await api_spec_small.getCaseId();
  await LRspec.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(2);

Scenario('02 Reason for settlement - Consent order', async ({LRspec}) => {
  await LRspec.login(config.hearingCenterAdminWithRegionId1);
  await LRspec.requestSettleThisClaimConsentOrderForUI();
}).retry(2);

AfterSuite(async ({api_spec_small}) => {
  await api_spec_small.cleanUp();
  await unAssignAllUsers();
});
