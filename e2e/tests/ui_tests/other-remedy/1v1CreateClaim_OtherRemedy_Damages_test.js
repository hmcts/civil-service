const config = require('../../../config.js');
const {assignCaseRoleToUser, unAssignAllUsers, addUserCaseMapping} = require('../../../api/caseRoleAssignmentHelper');
const {
  waitForFinishedBusinessProcess,
} = require('../../../api/testingSupport');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');

const claimant1 = {
  litigantInPerson: true
};

const respondent1 = {
  represented: true,
  representativeRegistered: true,
  representativeOrgNumber: 2
};

let caseNumber;

Feature('1v1 - Claim Journey with OtherRemedy type Damages and other remedy').tag('@civil-ccd-nightly @ui-other-remedy');

Scenario('01 Applicant solicitor creates claim with type as Damages and other remedy', async ({I}) => {
  let claimType = 'Damages and other remedy';
  await I.login(config.applicantSolicitorUser);
  await I.createCase(claimant1, null, respondent1, null, 25000, claimType);
  caseNumber = await I.grabCaseNumber();
  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseNumber);
  await addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(2);

Scenario('02 Applicant solicitor notifies defendant solicitor of claim', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.notifyClaim();
  await assignCaseRoleToUser(caseNumber, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
}).retry(2);

Scenario('03 Applicant solicitor notifies defendant solicitor of claim details', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.notifyClaimDetails();
}).retry(2);

Scenario('04 Defendant solicitor acknowledges claim', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.acknowledgeClaim('fullDefence');
}).retry(2);

Scenario('05 Defendant solicitor responds to claim', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaim({defendant1Response: 'fullDefence', claimValue: 25000});
}).retry(2);

Scenario('06 Claimant solicitor responds to defence', async ({I}) => {
  await I.login(config.applicantSolicitorUser);
  await I.respondToDefence('ONE_V_ONE', 25000);
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

AfterSuite(async () => {
  await unAssignAllUsers();
});
