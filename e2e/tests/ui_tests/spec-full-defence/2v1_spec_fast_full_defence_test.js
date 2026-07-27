const config = require('../../../config.js');
const {assignCaseToLRSpecDefendant} = require('../../../api/testingSupport');
const {addUserCaseMapping, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');

// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;

let caseNumber;

Feature('2v1 Multi Party full defence Claim Creation 2v1').tag('@civil-ccd-nightly @ui-spec-full-defence');

Scenario('01 Applicant solicitor creates 2v1 specified claim with 2 organisation vs 1 company for fast-track claims', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.createCaseSpecified('2v1 specified claim - fast track', 'organisation', 'organisation', 'company', null, 18000);
  caseNumber = await LRspec.grabCaseNumber();
  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseNumber);
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await LRspec.see(`Case ${caseNumber} has been created.`);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(2);

Scenario('02 2v1 Respond To Claim - Defendants solicitor rejects claim for defendant', async ({LRspec}) => {
  await assignCaseToLRSpecDefendant(caseNumber);
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.respondToClaimFullDefence({
    twoClaimants: true,
    defendant1Response: 'fullDefence',
    claimType: 'fast',
    defenceType: 'dispute'
  });
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await LRspec.see(caseEventMessage('Respond to claim'));
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
