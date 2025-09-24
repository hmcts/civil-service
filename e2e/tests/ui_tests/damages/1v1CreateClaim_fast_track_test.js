const config = require('../../../config.js');
const {assignCaseRoleToUser, unAssignAllUsers, addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const {
  waitForFinishedBusinessProcess,
} = require('../../../api/testingSupport');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');
const {PARTY_FLAGS} = require('../../../fixtures/caseFlags');

// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;

const claimant1 = {
  litigantInPerson: true
};

const respondent1 = {
  represented: true,
  representativeRegistered: true,
  representativeOrgNumber: 2
};

let caseNumber; 

Feature('1v1 - Claim Journey @e2e-unspec-fast @e2e-1v1 @e2e-nightly-prod');

Scenario('Applicant solicitor creates claim @create-claim', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.createCase(claimant1, null, respondent1, null, 25000);
  caseNumber = await I.grabCaseNumber();
  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseNumber);
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(`Case ${caseNumber} has been created.`);
  await addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
  
}).retry(2);

Scenario('Applicant solicitor notifies defendant solicitor of claim', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.notifyClaim();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Notify claim'));
  await assignCaseRoleToUser(caseNumber, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
}).retry(2);

Scenario('Applicant solicitor notifies defendant solicitor of claim details', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.notifyClaimDetails();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Notify claim details'));
}).retry(2);

Scenario('Defendant solicitor acknowledges claim', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.acknowledgeClaim('fullDefence');
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Acknowledge claim'));
}).retry(2);

Scenario('Defendant solicitor requests deadline extension', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.informAgreedExtensionDate();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Inform agreed extension date'));
}).retry(2);

Scenario('Defendant solicitor adds defendant litigation friend', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.addDefendantLitigationFriend();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Add litigation friend'));
}).retry(2);

Scenario('Defendant solicitor responds to claim', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaim({defendant1Response: 'fullDefence', claimValue: 25000});
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Respond to claim'));
}).retry(2);


Scenario('Claimant solicitor responds to defence', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.respondToDefence('ONE_V_ONE', 25000);
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('View and respond to defence'));
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('Add case flags', async ({I}) => {
  const caseFlags = [{
    partyName: 'Example applicant1 company', roleOnCase: 'Claimant 1',
    details: [PARTY_FLAGS.vulnerableUser.value]
  }, {
    partyName: 'John Smith', roleOnCase: 'Defendant solicitor 1 expert',
    details: [PARTY_FLAGS.unacceptableBehaviour.value]
  }];

  await I.login(config.hearingCenterAdminWithRegionId1);
  await I.createCaseFlags(caseFlags);
  // await I.validateCaseFlags(caseFlags);
}).retry(2);

Scenario('Manage case flags', async ({I}) => {
  const caseFlags = [{
    partyName: 'Example applicant1 company', roleOnCase: 'Claimant 1',
    flagType: 'Vulnerable user',
    flagComment: 'test comment'
  }, {
    partyName: 'John Smith', roleOnCase: 'Defendant solicitor 1 expert',
    flagType: 'Unacceptable/disruptive customer behaviour',
    flagComment: 'test comment'
  }];

  await I.login(config.hearingCenterAdminWithRegionId1);
  await I.manageCaseFlags(caseFlags);
  // await I.validateUpdatedCaseFlags(caseFlags);
}).retry(2);

AfterSuite(async () => {
  await unAssignAllUsers();
});
