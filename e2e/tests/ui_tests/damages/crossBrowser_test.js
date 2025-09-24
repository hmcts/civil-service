const config = require('../../../config.js');
const {assignCaseToDefendant} = require('../../../api/testingSupport');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');

// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;
const caseId = () => `${caseNumber.split('-').join('').replace(/#/, '')}`;

let caseNumber;
let attempt = 1;

Feature('End-to-end journey @cross-browser-tests');

Scenario('Full end-to-end journey', async ({I}) => {
  const claimant1 = {
    litigantInPerson: false
  };
  const respondent1 = {
    represented: true,
    representativeRegistered: true,
    representativeOrgNumber: 2
  };
  console.log(`Test run attempt: #${attempt++}`);
  await I.login(config.applicantSolicitorUser);
  await I.createCase(claimant1, null, respondent1, null, 25000);
  console.log('Applicant solicitor created claim');

  caseNumber = await I.grabCaseNumber();
  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseId());

  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(`Case ${caseNumber} has been created.`);

  await I.notifyClaim();
  console.log('Applicant solicitor notified defendant solicitor of claim');
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Notify claim'));
  await assignCaseToDefendant(caseId());

  await I.notifyClaimDetails();
  console.log('Applicant solicitor notified defendant solicitor of claim details');
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await I.see(caseEventMessage('Notify claim details'));
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});