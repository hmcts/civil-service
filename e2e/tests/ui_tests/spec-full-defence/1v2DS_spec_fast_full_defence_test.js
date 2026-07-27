const config = require('../../../config.js');
const {assignCaseRoleToUser, addUserCaseMapping, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');

let caseNumber;

Feature('Claim creation 1v2 Diff Solicitor with fast claims')
  .tag('@civil-ccd-master @civil-ccd-pr @civil-ccd-nightly @ui-spec-full-defence');

Scenario('01 Applicant solicitor creates 1v2 Diff LRs specified claim defendant Different LRs for fast claims-spec', async ({api_spec_fast, LRspec}) => {
  console.log('Applicant solicitor creates 1v2 Diff LRs specified claim defendant Different LRs for fast claims-spec');
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO');
  caseNumber = await api_spec_fast.getCaseId();
  await LRspec.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(1);

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

AfterSuite(async  () => {
  await unAssignAllUsers();
});