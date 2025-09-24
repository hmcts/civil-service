const config = require('../../../config.js');
const {assignCaseToLRSpecDefendant} = require('../../../api/testingSupport');
const {addUserCaseMapping, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');
const {PARTY_FLAGS} = require('../../../fixtures/caseFlags');
// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;

let caseNumber;

Feature('Claim creation 1v1 @e2e-spec-fast @e2e-nightly-prod');

Scenario('1v1 Applicant solicitor creates specified claim for fast track @create-claim-spec', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.createCaseSpecified('1v1 fast claim', 'organisation', null, 'company', null, 19000);
  caseNumber = await LRspec.grabCaseNumber();
  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseNumber);

  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await LRspec.see(`Case ${caseNumber} has been created.`);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(2);

//As part of CIV-13925 this functionality is hidden
Scenario.skip('1v1 Claimant solicitor Enter Breathing Space', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.enterBreathingSpace();
}).retry(2);

Scenario.skip('1v1 Claimant solicitor Lift Breathing Space', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.liftBreathingSpace();
}).retry(2);

Scenario('1v1 Defendant solicitor perform Inform Agreed Extension', async ({LRspec}) => {
  console.log('1v1 Defendant solicitor Inform Agreed Extension claim-spec: ' + caseNumber);
  await assignCaseToLRSpecDefendant(caseNumber);
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.informAgreedExtensionDateSpec();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await LRspec.see(caseEventMessage('Inform agreed extension date'));
}).retry(2);

Scenario('1v1 Respond To Claim - Defendants solicitor rejects claim for defendant', async ({LRspec}) => {
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.respondToClaimFullDefence({
    defendant1Response: 'fullDefence',
    claimType: 'fast',
    defenceType: 'dispute'
  });
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await LRspec.see(caseEventMessage('Respond to claim'));
  //await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('1v1 Claimant solicitor responds to defence - claimant Intention to proceed', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.respondToDefence({mpScenario: 'ONE_V_ONE', claimType: 'fast'});
}).retry(2);

Scenario('Add case flags', async ({LRspec}) => {
  const caseFlags = [{
    partyName: 'Example applicant1 company', roleOnCase: 'Claimant 1',
    details: [PARTY_FLAGS.vulnerableUser.value]
  },{
    partyName: 'John Smith', roleOnCase: 'Claimant solicitor expert',
    details: [PARTY_FLAGS.unacceptableBehaviour.value]
  }
  ];

  await LRspec.login(config.hearingCenterAdminWithRegionId1);
  await LRspec.createCaseFlags(caseFlags);
  // await LRspec.validateCaseFlags(caseFlags);
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
