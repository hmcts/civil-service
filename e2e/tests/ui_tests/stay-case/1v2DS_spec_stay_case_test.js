const config = require('../../../config.js');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const {waitForFinishedBusinessProcess} = require('../../../api/testingSupport');

let caseNumber, validFastTrackDirectionsTask;

if (config.runWAApiTest) {
  validFastTrackDirectionsTask = require('../../../../wa/tasks/fastTrackDirectionsTask.js');
}

Feature('1v2DS spec stay case journey').tag('@ui-stay-case');

Scenario('01 Prepare case to awaiting applicant intention', async ({api_spec, LRspec}) => {
  caseNumber = await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO', true);
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_RESPONDENT_ACKNOWLEDGEMENT', true);
  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_APPLICANT_INTENTION', true);
  LRspec.setCaseId(caseNumber);
}).retry(1);

Scenario('02 Claimant responds to claim', async ({LRspec}) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.respondToDefence({mpScenario: 'ONE_V_ONE', claimType: 'small'});
}).retry(2);

Scenario('04 Stay the case', async ({LRspec}) => {
  await LRspec.stayCase();
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('05 Request update on the stay case - Manage stay', async ({LRspec}) => {
  await LRspec.manageStay('REQ_UPDATE');
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

Scenario('06 Lift the stay case - Manage stay', async ({LRspec}) => {
  await LRspec.manageStay('LIFT_STAY', 'IN_MEDIATION');
  await waitForFinishedBusinessProcess(caseNumber);
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});