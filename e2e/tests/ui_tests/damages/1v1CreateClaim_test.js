const config = require('../../../config.js');
const {assignCaseRoleToUser, unAssignAllUsers, addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const {
  waitForFinishedBusinessProcess,
  checkToggleEnabled,
  checkCaseFlagsEnabled
} = require('../../../api/testingSupport');
const {PBAv3} = require('../../../fixtures/featureKeys');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');
const {PARTY_FLAGS} = require('../../../fixtures/caseFlags');

// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;
const caseId = () => `${caseNumber.split('-').join('').replace(/#/, '')}`;

const claimant1 = {
  litigantInPerson: true
};

const respondent1 = {
  represented: true,
  representativeRegistered: true,
  representativeOrgNumber: 2
};

let caseNumber;

Feature('1v1 - Claim Journey @e2e-unspec @e2e-1v1 @e2e-nightly-prod ');

Scenario('Applicant solicitor creates claim @create-claim', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.createCase(claimant1, null, respondent1, null);
  caseNumber = await I.grabCaseNumber();

  const pbaV3 = await checkToggleEnabled(PBAv3);
  console.log('Is PBAv3 toggle on?: ' + pbaV3);

  if (pbaV3) {
    await serviceRequest.openServiceRequestTab();
    await serviceRequest.payFee(caseId());
  }
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(`Case ${caseNumber} has been created.`);
  await addUserCaseMapping(caseId(), config.applicantSolicitorUser);
}).retry(3);

Scenario('Applicant solicitor notifies defendant solicitor of claim', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.notifyClaim();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Notify claim'));
  await assignCaseRoleToUser(caseId(), 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
}).retry(3);

Scenario('Applicant solicitor notifies defendant solicitor of claim details', async ({I}) => {

  await I.login(config.applicantSolicitorUser);
  await I.notifyClaimDetails();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Notify claim details'));
}).retry(3);

Scenario('Defendant solicitor acknowledges claim', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.acknowledgeClaim('fullDefence');
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Acknowledge claim'));
}).retry(3);

Scenario('Defendant solicitor requests deadline extension', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.informAgreedExtensionDate(1);
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Inform agreed extension date'));
}).retry(3);

Scenario('Defendant solicitor adds defendant litigation friend', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.addDefendantLitigationFriend();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Add litigation friend'));
});

Scenario('Defendant solicitor responds to claim', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaim({defendant1Response: 'fullDefence'});
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Respond to claim'));
}).retry(3);

Scenario('Add case flags', async ({I}) => {
  if(await checkCaseFlagsEnabled()) {
    const caseFlags = [{
      partyName: 'Example applicant1 company', roleOnCase: 'Claimant 1',
      details: [PARTY_FLAGS.vulnerableUser.value]
    }, {
      partyName: 'John Smith', roleOnCase: 'Defendant solicitor 1 expert',
      details: [PARTY_FLAGS.unacceptableBehaviour.value]
    }];

    await I.login(config.hearingCenterAdminWithRegionId1);
    await I.createCaseFlags(caseFlags);
    await I.validateCaseFlags(caseFlags);
  }
});

Scenario('Manage case flags', async ({I}) => {
  if(await checkCaseFlagsEnabled()) {
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
    await I.validateUpdatedCaseFlags(caseFlags);
  }
});

Scenario('Claimant solicitor responds to defence', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.respondToDefence('ONE_V_ONE');
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('View and respond to defence'));
  await waitForFinishedBusinessProcess(caseId());
}).retry(3);

AfterSuite(async () => {
  await unAssignAllUsers();
});
