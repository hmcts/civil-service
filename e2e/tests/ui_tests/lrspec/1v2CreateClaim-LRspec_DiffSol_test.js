const config = require('../../../config.js');
const {assignCaseRoleToUser, addUserCaseMapping, unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const {checkCaseFlagsEnabled, waitForFinishedBusinessProcess} = require('../../../api/testingSupport');
const {PARTY_FLAGS} = require('../../../fixtures/caseFlags');

let caseNumber;

Feature('Claim creation 1v2 Diff Solicitor with fast claims @e2e-spec-fast @e2e-spec-1v2DS @master-e2e-ft');

Scenario('Applicant solicitor creates 1v2 Diff LRs specified claim defendant Different LRs for fast claims @create-claim-spec', async ({api_spec_fast, LRspec}) => {
  console.log('AApplicant solicitor creates 1v2 Diff LRs specified claim defendant Different LRs for fast claims @create-claim-spec');
  await api_spec_fast.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO');
  caseNumber = await api_spec_fast.getCaseId();
  await LRspec.setCaseId(caseNumber);
  addUserCaseMapping(caseNumber, config.applicantSolicitorUser);
}).retry(2);

Scenario('1v2 Diff LRs Fast Track Claim  - Assign roles to defendants', async () => {
    await assignCaseRoleToUser(caseNumber, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
    await assignCaseRoleToUser(caseNumber,  'RESPONDENTSOLICITORTWO', config.secondDefendantSolicitorUser);
  console.log('Assigned roles for defendant 1 and 2', caseNumber);
}).retry(2);

Scenario('1v2 Diff LRs Fast Track Claim  - First Defendant solicitor rejects claim', async ({LRspec}) => {
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.respondToClaimFullDefence({
    defendant1Response: 'fullDefence',
    claimType: 'fast',
    defenceType: 'dispute'
  });
}).retry(2);

Scenario('1v2 Diff LRs Fast Track Claim  - Second Defendant solicitor rejects claim', async ({LRspec}) => {
  await LRspec.login(config.secondDefendantSolicitorUser);
  await LRspec.respond1v2DiffLR_FullDefence({
    secondDefendant: true,
    defendant1Response: 'fullDefence',
    claimType: 'fast',
    defenceType: 'dispute'
  });
}).retry(2);

Scenario('1v2 Diff LRs Fast Track Claim  - claimant Intention to proceed', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.respondToDefence({mpScenario: 'ONE_V_ONE', claimType: 'fast'});
}).retry(2);

// Skip case flags scenario as it's covered in the unspec e2e
Scenario.skip('Add case flags', async ({LRspec}) => {
  if(await checkCaseFlagsEnabled()) {
    const caseFlags = [{
      partyName: 'Example applicant1 company', roleOnCase: 'Claimant 1',
      details: [PARTY_FLAGS.vulnerableUser.value]
    }, {
      partyName: 'Example respondent1 company', roleOnCase: 'Defendant 1',
      details: [PARTY_FLAGS.unacceptableBehaviour.value]
    }
    ];

    await LRspec.login(config.hearingCenterAdminWithRegionId1);
    await LRspec.createCaseFlags(caseFlags);
    // await LRspec.validateCaseFlags(caseFlags);
  }
}).retry(2);

Scenario('Judge triggers SDO', async ({LRspec}) => {
   await LRspec.login(config.judgeUserWithRegionId1);
   await LRspec.initiateSDO('yes', 'yes', null, null);
}).retry(2);

Scenario.skip('Claimant solicitor uploads evidence', async ({LRspec}) => {
    await LRspec.login(config.applicantSolicitorUser);
    await LRspec.evidenceUploadSpec(caseNumber, false);
}).retry(2);

Scenario('Defendant solicitor uploads evidence', async ({LRspec}) => {
    await LRspec.login(config.defendantSolicitorUser);
    await LRspec.evidenceUploadSpec(caseNumber, true);
}).retry(2);

Scenario('Schedule a hearing', async ({LRspec}) => {
    await LRspec.login(config.hearingCenterAdminWithRegionId1);
    await LRspec.createHearingScheduled();
    await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('Pay hearing fee', async ({LRspec}) => {
  await LRspec.payHearingFee();
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('Stay the case', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.stayCase();
    await waitForFinishedBusinessProcess(caseNumber);
  }
}).retry(2);

Scenario('Request update on the stay case - Manage stay', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.manageStay('REQ_UPDATE');
    await waitForFinishedBusinessProcess(caseNumber);
  }
}).retry(2);

Scenario('Lift the stay case - Manage stay', async ({LRspec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await LRspec.manageStay('LIFT_STAY', 'HEARING_READINESS');
    await waitForFinishedBusinessProcess(caseNumber);
  }
}).retry(2);


// ToDo: Refactor to trigger create case flags event
Scenario.skip('Add case flags - validateCaseFlags', async ({LRspec}) => {
  await LRspec.login(config.adminUser);
  await LRspec.createCaseFlags();
  await LRspec.validateCaseFlags([
    { partyName: 'Example applicant1 company', details: [] },
    { partyName: 'Example respondent1 company', details: [] },
    { partyName: 'Example respondent2 company', details: [] }
  ]);
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
