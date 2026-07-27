const config = require('../../../config.js');
const {assignCaseToLRSpecDefendant} = require('../../../api/testingSupport');
const {addUserCaseMapping, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');
const {PARTY_FLAGS} = require('../../../fixtures/caseFlags');
// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;

let caseNumber;

Feature('Claim creation 1v1 small claims').tag('@civil-ccd-nightly @ui-spec-full-defence');

Scenario('01 1v1 Applicant solicitor creates specified claim for small track spec', async ({LRspec}) => {
  console.log('1v1 Applicant solicitor creates specified claim for small track-spec');
  await LRspec.login(config.applicantSolicitorUser);
  //Individual, Organisation, Company, Sole trader
  await LRspec.createCaseSpecified('1v1 small claim', 'Individual', null, 'Organisation', null, 9000);
  caseNumber = await LRspec.grabCaseNumber();
  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseNumber);

  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await LRspec.see(`Case ${caseNumber} has been created.`);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(2);


Scenario('02 1v1 Respond To Claim - Defendants solicitor rejects claim for defendant', async ({LRspec}) => {
  await assignCaseToLRSpecDefendant(caseNumber);
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.respondToClaimFullDefence({
    defendant1Response: 'fullDefence',
    claimType: 'small',
    defenceType: 'dispute'
  });
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await LRspec.see(caseEventMessage('Respond to claim'));
  //await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('03 1v1 Claimant solicitor responds to defence - claimant Intention to proceed', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.respondToDefence({mpScenario: 'ONE_V_ONE', claimType: 'small'});
}).retry(2);

Scenario('04 1v1 Mediation unsuccessful', async ({LRspec}) => {
  await LRspec.login(config.ctscAdminUser);
  await LRspec.mediationUnsuccessful();
}).retry(2);


AfterSuite(async  () => {
  await unAssignAllUsers();
});
