const config = require('../config.js');
const {waitForFinishedBusinessProcess, assignCaseToDefendant} = require('../api/testingSupport');

const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;
const caseId = () => `${caseNumber.split('-').join('').replace(/#/, '')}`;

let caseNumber;
let attempt = 1;

Feature('End-to-end journey @cross-browser-tests');

Scenario('Full end-to-end journey', async ({I}) => {
  console.log(`Test run attempt: #${attempt++}`);
  await I.login(config.applicantSolicitorUser);
  await I.createCase();
  console.log('Applicant solicitor created claim');

  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);

  await I.notifyClaim();
  console.log('Applicant solicitor notified defendant solicitor of claim');
  await I.see(caseEventMessage('Notify claim'));
  await assignCaseToDefendant(caseId());

  await I.notifyClaimDetails();
  console.log('Applicant solicitor notified defendant solicitor of claim details');
  await I.see(caseEventMessage('Notify claim details'));

  await I.login(config.defendantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.acknowledgeClaim('fullDefence');
  console.log('Defendant solicitor acknowledged claim');
  await I.see(caseEventMessage('Acknowledge claim'));

  await I.informAgreedExtensionDate();
  console.log('Defendant solicitor requested deadline extension');
  await I.see(caseEventMessage('Inform agreed extension date'));

  await I.login(config.defendantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.addDefendantLitigationFriend();
  console.log('Defendant solicitor added defendant litigation friend');
  await I.see(caseEventMessage('Add litigation friend'));

  await I.login(config.defendantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.respondToClaim('fullDefence');
  console.log('Defendant solicitor responded to claim');
  await I.see(caseEventMessage('Respond to claim'));

  await I.login(config.applicantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.respondToDefence();
  console.log('Applicant solicitor responded to defence');
  await I.see(caseEventMessage('View and respond to defence'));
  await waitForFinishedBusinessProcess(caseId());
}).retry(2);
