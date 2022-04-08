const config = require('../../../config.js');
const {waitForFinishedBusinessProcess, assignCaseToDefendant} = require('../../../api/testingSupport');

const caseId = () => `${caseNumber.split('-').join('').replace(/#/, '')}`;
let caseNumber;


Feature('RPA handoff points tests @rpa-handoff-tests');

Scenario('Take claim offline', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);

  await I.login(config.defendantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.acknowledgeClaim('fullDefence');
  await I.informAgreedExtensionDate();

  await I.login(config.adminUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.caseProceedsInCaseman();
  await I.assertNoEventsAvailable();
  await I.signOut();
}).retry(3);

Scenario('Defendant - Litigant In Person', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.createCase(true);
  caseNumber = await I.grabCaseNumber();

  await waitForFinishedBusinessProcess(caseId());
  await I.navigateToCaseDetails(caseNumber);
  await I.assertNoEventsAvailable();
  await I.signOut();
}).retry(3);

Scenario('Defendant - Defend part of Claim', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);
  await defendantAcknowledgeAndRespondToClaim(I, 'partDefence', 'partAdmission');

  await waitForFinishedBusinessProcess(caseId());
  await I.navigateToCaseDetails(caseNumber);
  await I.assertNoEventsAvailable();
  await I.signOut();
}).retry(3);

Scenario('Defendant - Defends, Claimant decides not to proceed', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);
  await defendantAcknowledgeAndRespondToClaim(I, 'fullDefence', 'fullDefence');

  await I.login(config.applicantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.respondToDefenceDropClaim();
  await I.assertNoEventsAvailable();
  await I.signOut();
}).retry(3);

Scenario('Defendant - Defends, Claimant decides to proceed', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);
  await defendantAcknowledgeAndRespondToClaim(I, 'fullDefence', 'fullDefence');

  await I.login(config.applicantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.respondToDefence();
  await I.assertNoEventsAvailable();
  await I.signOut();
}).retry(3);

const createCaseUpUntilNotifyClaimDetails = async (I, shouldStayOnline = true) => {
  const claimant1 = {
    litigantInPerson: false
  };
  const respondent1 = {
    represented: true,
    representativeRegistered: true,
    representativeOrgNumber: 2
  };
  await I.login(config.applicantSolicitorUser);
  await I.createCase(claimant1, null , respondent1, null, shouldStayOnline);
  caseNumber = await I.grabCaseNumber();
  await I.notifyClaim();
  await assignCaseToDefendant(caseId());
  await I.notifyClaimDetails();
};

const defendantAcknowledgeAndRespondToClaim = async (I, acknowledgeClaimResponse, respondToClaimResponse) => {
  await I.login(config.defendantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.acknowledgeClaim(acknowledgeClaimResponse);
  await I.informAgreedExtensionDate();
  await I.respondToClaim({defendant1Response: respondToClaimResponse});
};
