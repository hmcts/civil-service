const config = require('../../../config.js');
const {unAssignAllUsers, addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const {PARTY_FLAGS} = require('../../../fixtures/caseFlags');

// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;

let caseNumber; 
const mpScenario = 'ONE_V_ONE';

Feature('1v1 case flags journey').tag('@civil-ccd-nightly @ui-case-flags');

Scenario('01 Prepare 1v1 unspec claim up to case progression', async ({api, I}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario);
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO');
  caseNumber = await api.getCaseId();
  await I.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(1);

Scenario('02 Add case flags', async ({I}) => {
  const caseFlags = [{
    partyName: 'Test Inc', roleOnCase: 'Claimant 1',
    details: [PARTY_FLAGS.vulnerableUser.value]
  }, {
    partyName: 'John Doe', roleOnCase: 'Defendant solicitor 1 expert',
    details: [PARTY_FLAGS.unacceptableBehaviour.value]
  }];

  await I.login(config.hearingCenterAdminWithRegionId1);
  await I.createCaseFlags(caseFlags);
  // await I.validateCaseFlags(caseFlags);
}).retry(1);

Scenario('03 Manage case flags', async ({I}) => {
  const caseFlags = [{
    partyName: 'Test Inc', roleOnCase: 'Claimant 1',
    flagType: 'Vulnerable user',
    flagComment: 'test comment'
  }, {
    partyName: 'John Doe', roleOnCase: 'Defendant solicitor 1 expert',
    flagType: 'Unacceptable/disruptive customer behaviour',
    flagComment: 'test comment'
  }];

  await I.login(config.hearingCenterAdminWithRegionId1);
  await I.manageCaseFlags(caseFlags);
  // await I.validateUpdatedCaseFlags(caseFlags);
}).retry(1);

AfterSuite(async () => {
  await unAssignAllUsers();
});
