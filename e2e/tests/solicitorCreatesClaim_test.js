const config = require('../config.js');
const {waitForFinishedBusinessProcess} = require('../api/testingSupport');

const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;
const caseId = () => `${caseNumber.split('-').join('').replace(/#/, '')}`;

const CASE_HEADER = 'ccd-case-header > h1';
const CASE_LIST = 'exui-case-list';

let caseNumber;

Feature('Claim creation @e2e-tests');

Scenario('Solicitor creates claim @create-claim', async (I) => {
  await I.login(config.solicitorUser);
  await I.createCase();

  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
});

Scenario('Solicitor notifies defendant solicitor of claim', async (I) => {
  await I.notifyClaim();
  await I.see(caseEventMessage('Notify claim'));
});

Scenario('Solicitor notifies defendant solicitor of claim details', async (I) => {
  await I.notifyClaimDetails();
  await I.see(caseEventMessage('Notify claim details'));
});

Scenario('Solicitor acknowledges service', async (I) => {
  await I.acknowledgeService('fullDefence');
  await I.see(caseEventMessage('Acknowledge service'));
});

Scenario('Solicitor requests deadline extension', async (I) => {
  await I.informAgreedExtensionDate();
  await I.see(caseEventMessage('Inform agreed extension date'));
});

Scenario('Solicitor adds defendant litigation friend', async (I) => {
  await I.addDefendantLitigationFriend();
  await I.see(caseEventMessage('Add litigation friend'));
});

Scenario('Solicitor responds to claim', async (I) => {
  await I.respondToClaim('fullDefence');
  await I.see(caseEventMessage('Respond to claim'));
});

Scenario('Solicitor responds to defence', async (I) => {
  await I.respondToDefence();
  if (config.idamStub.enabled) {
    I.waitForElement(CASE_HEADER);
    await I.see(caseEventMessage('View and respond to defence'));
  } else {
    I.waitForElement(CASE_LIST);
    await I.see('Case List');
  }
  await waitForFinishedBusinessProcess(caseId());
});
