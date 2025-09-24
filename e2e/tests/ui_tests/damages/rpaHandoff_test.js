const config = require('../../../config.js');
const {assignCaseRoleToUser, addUserCaseMapping, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const {waitForFinishedBusinessProcess} = require('../../../api/testingSupport');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');

const caseId = () => `${caseNumber.split('-').join('').replace(/#/, '')}`;
let caseNumber;

Feature('RPA handoff points tests @e2e-rpa-handoff-tests');

Scenario('Take claim offline', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);
  await I.login(config.defendantSolicitorUser);
  await I.acknowledgeClaim('fullDefence');
  await I.informAgreedExtensionDate();

  await I.login(config.adminUser);
  await I.caseProceedsInCaseman();
  await I.assertHasEvents(['Amend party details', 'Add a case note']);
  await I.signOut();
}).retry(0);

Scenario('Defendant - Defend part of Claim', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);
  await defendantAcknowledgeAndRespondToClaim(I, 'partDefence', 'partAdmission');

  await waitForFinishedBusinessProcess(caseId());
  await I.navigateToCaseDetails(caseNumber);
  if(['preview', 'demo'].includes(config.runningEnv))
    await I.assertHasEvents(['Raise a new query']);
  else
    await I.assertNoEventsAvailable();
  await I.signOut();
}).retry(2);

Scenario('Defendant - Defends, Claimant decides not to proceed', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);
  await defendantAcknowledgeAndRespondToClaim(I, 'fullDefence', 'fullDefence');

  await I.login(config.applicantSolicitorUser);
  await I.respondToDefenceDropClaim();
  if(['preview', 'demo'].includes(config.runningEnv))
    await I.assertHasEvents(['Raise a new query']);
  else
    await I.assertNoEventsAvailable();
  await I.signOut();
}).retry(3);

Scenario('Defendant - Defends, Claimant decides to proceed', async ({I}) => {
  await createCaseUpUntilNotifyClaimDetails(I);
  await defendantAcknowledgeAndRespondToClaim(I, 'fullDefence', 'fullDefence');

  await I.login(config.applicantSolicitorUser);
  await I.respondToDefence('ONE_V_ONE', 25000);
  if(['preview', 'demo'].includes(config.runningEnv))
    await I.assertHasEvents(['Raise a new query']);
  else
    await I.assertNoEventsAvailable();
  await I.signOut();
}).retry(2);

const createCaseUpUntilNotifyClaimDetails = async (I) => {
  const claimant1 = {
    litigantInPerson: false
  };
  const respondent1 = {
    represented: true,
    representativeRegistered: true,
    representativeOrgNumber: 2
  };
  await I.login(config.applicantSolicitorUser);
  await I.createCase(claimant1, null , respondent1, null, 25000);
  caseNumber = await I.grabCaseNumber();

  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseId());

  await I.notifyClaim();
  await addUserCaseMapping(caseId(),config.applicantSolicitorUser);
  await assignCaseRoleToUser(caseId(), 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
  await I.notifyClaimDetails();
};

const defendantAcknowledgeAndRespondToClaim = async (I, acknowledgeClaimResponse, respondToClaimResponse) => {
  await I.login(config.defendantSolicitorUser);
  await I.navigateToCaseDetails(caseNumber);
  await I.acknowledgeClaim(acknowledgeClaimResponse);
  await I.informAgreedExtensionDate();
  await I.respondToClaim({defendant1Response: respondToClaimResponse, claimValue: 25000});
};

AfterSuite(async  () => {
  await unAssignAllUsers();
});
