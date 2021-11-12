const config = require('../config.js');
const {waitForFinishedBusinessProcess, assignCaseToDefendant} = require('../api/testingSupport');

//const caseId = () => `${caseNumber.split('-').join('').replace(/#/, '')}`;
let caseNumber;
let caseId;

Feature('RPA handoff points tests @rpa-handoff-tests');

Scenario('Take claim offline', async ({I, api}) => {
  await createCaseUpUntilNotifyClaimDetails(I, api);
  await I.login(config.adminUser);
  await I.navigateToCaseDetails(caseId);
  await I.caseProceedsInCaseman(caseId);
  await I.assertCorrectEventsAreAvailableToUser(caseId, config.adminUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
  await I.signOut();
});

Scenario('Defendant - Litigant In Person', async ({I, api}) => {
  const caseId = await api.createClaimWithRespondentLitigantInPerson(config.applicantSolicitorUser)

   await waitForFinishedBusinessProcess(caseId);
   await I.login(config.applicantSolicitorUser);
   await I.navigateToCaseDetails(caseId);
   await I.assertNoEventsAvailable();
   await I.signOut();
});

Scenario.skip('Defendant - Defend part of Claim', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);
  await defendantAcknowledgeAndRespondToClaim(I, 'partDefence', 'partAdmission');

  await waitForFinishedBusinessProcess(caseId());
  await I.navigateToCaseDetails(caseNumber);
  await I.assertNoEventsAvailable();
  await I.signOut();
});

Scenario.skip('Defendant - Defends, Claimant decides not to proceed', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);
  await defendantAcknowledgeAndRespondToClaim(I, 'fullDefence', 'fullDefence');

  await I.login(config.applicantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.respondToDefenceDropClaim();
  await I.assertNoEventsAvailable();
  await I.signOut();
});

Scenario.skip('Defendant - Defends, Claimant decides to proceed', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);
  await defendantAcknowledgeAndRespondToClaim(I, 'fullDefence', 'fullDefence');

  await I.login(config.applicantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.respondToDefence();
  await I.assertNoEventsAvailable();
  await I.signOut();
});

const createCaseUpUntilNotifyClaimDetails = async (I, api) => {
  caseId = await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api.addCaseNote(config.adminUser, caseId);
  await api.amendClaimDocuments(config.applicantSolicitorUser, caseId);
  await api.notifyClaim(config.applicantSolicitorUser, caseId);
  await assignCaseToDefendant(caseId);
  await api.notifyClaimDetails(config.applicantSolicitorUser, caseId);
};

const defendantAcknowledgeAndRespondToClaim = async (I, acknowledgeClaimResponse, respondToClaimResponse) => {
  await I.login(config.defendantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.acknowledgeClaim(acknowledgeClaimResponse);
  await I.informAgreedExtensionDate();
  await I.respondToClaim(respondToClaimResponse);
};
