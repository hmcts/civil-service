const config = require('../../../config.js');
const {addUserCaseMapping, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const {PARTY_FLAGS} = require('../../../fixtures/caseFlags');
// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;

let caseNumber;

Feature('1v1 spec case flags journey').tag('@civil-ccd-nightly @ui-case-flags');

Scenario('01 Prepare 1v1 spec small track claim up to case progression', async ({api_spec, LRspec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE', true);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_ONE',
      'AWAITING_APPLICANT_INTENTION');
  caseNumber = await api_spec.getCaseId();
  await LRspec.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(1);

Scenario('02 Add case flags', async ({LRspec}) => {
  const caseFlags = [{
    partyName: 'Test Inc', roleOnCase: 'Claimant 1',
    details: [PARTY_FLAGS.vulnerableUser.value]
  }, {
    partyName: 'John Doe', roleOnCase: 'Defendant solicitor 1 expert',
    details: [PARTY_FLAGS.unacceptableBehaviour.value]
  }];

  await LRspec.login(config.hearingCenterAdminWithRegionId1);
  await LRspec.createCaseFlags(caseFlags);
  // await I.validateCaseFlags(caseFlags);
}).retry(1);

Scenario('03 Manage case flags', async ({LRspec}) => {
  const caseFlags = [{
    partyName: 'Test Inc', roleOnCase: 'Claimant 1',
    flagType: 'Vulnerable user',
    flagComment: 'test comment'
  }, {
    partyName: 'John Doe', roleOnCase: 'Defendant solicitor 1 expert',
    flagType: 'Unacceptable/disruptive customer behaviour',
    flagComment: 'test comment'
  }];

  await LRspec.login(config.hearingCenterAdminWithRegionId1);
  await LRspec.manageCaseFlags(caseFlags);
  // await I.validateUpdatedCaseFlags(caseFlags);
}).retry(1);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
