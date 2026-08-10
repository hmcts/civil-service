const config = require('../../../config.js');
const {addUserCaseMapping, assignCaseRoleToUser, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const {waitForFinishedBusinessProcess} = require('../../../api/testingSupport');

let caseNumber;

Feature('1v2DS spec small track case progression journey')
  .tag('@civil-ccd-master @civil-ccd-pr @civil-ccd-nightly @ui-case-progression');

Scenario('01 Applicant solicitor creates 1v2 Diff LRs specified claim defendant Different LRs for fast claims-spec', async ({api_spec_fast, LRspec}) => {
  console.log('Applicant solicitor creates 1v2 Diff LRs specified claim defendant Different LRs for fast claims-spec');
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO');
  caseNumber = await api_spec_fast.getCaseId();
  await LRspec.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(2);

Scenario('02 1v2 Diff LRs Fast Track Claim  - Assign roles to defendants', async () => {
    await assignCaseRoleToUser(caseNumber, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
    await assignCaseRoleToUser(caseNumber,  'RESPONDENTSOLICITORTWO', config.secondDefendantSolicitorUser);
  console.log('Assigned roles for defendant 1 and 2', caseNumber);
}).retry(2);

Scenario('03 1v2 Diff LRs Fast Track Claim  - First Defendant solicitor rejects claim', async ({LRspec}) => {
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.respondToClaimFullDefence({
    defendant1Response: 'fullDefence',
    claimType: 'fast',
    defenceType: 'dispute'
  });
}).retry(2);

Scenario('04 1v2 Diff LRs Fast Track Claim  - Second Defendant solicitor rejects claim', async ({LRspec}) => {
  await LRspec.login(config.secondDefendantSolicitorUser);
  await LRspec.respond1v2DiffLR_FullDefence({
    secondDefendant: true,
    defendant1Response: 'fullDefence',
    claimType: 'fast',
    defenceType: 'dispute'
  });
}).retry(2);

Scenario('05 1v2 Diff LRs Fast Track Claim  - claimant Intention to proceed', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.respondToDefence({mpScenario: 'ONE_V_ONE', claimType: 'fast'});
}).retry(2);


Scenario('06 Judge triggers SDO', async ({LRspec}) => {
   await LRspec.login(config.judgeUserWithRegionId1);
   await LRspec.initiateSDO('yes', 'yes', null, null);
}).retry(2);

Scenario.skip('07 Claimant solicitor uploads evidence', async ({LRspec}) => {
    await LRspec.login(config.applicantSolicitorUser);
    await LRspec.evidenceUploadSpec(caseNumber, false);
}).retry(2);

Scenario('08 Defendant solicitor uploads evidence', async ({LRspec}) => {
    await LRspec.login(config.defendantSolicitorUser);
    await LRspec.evidenceUploadSpec(caseNumber, true);
}).retry(2);

Scenario('09 Schedule a hearing', async ({LRspec}) => {
    await LRspec.login(config.hearingCenterAdminWithRegionId1);
    await LRspec.createHearingScheduled();
    await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('10 Pay hearing fee', async ({LRspec}) => {
  await LRspec.payHearingFee();
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});