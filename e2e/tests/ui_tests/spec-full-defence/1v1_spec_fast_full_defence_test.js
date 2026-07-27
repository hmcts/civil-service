const config = require('../../../config.js');
const { assignCaseToLRSpecDefendant, waitForFinishedBusinessProcess } = require('../../../api/testingSupport');
const { addUserCaseMapping, unAssignAllUsers } = require('../../../api/caseRoleAssignmentHelper');
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');

// Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
//const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;

const TEST_DIRECTORY = 'e2e/tests/ui_tests/spec-full-defence';
const BASELINE_DIRECTORY = 'e2e/pdf-baselines/ui-spec-full-defence';
const SEALED_CLAIM_PDF = 'sealed_claim_form.pdf';
const DEFENDANT_DIRECTIONS_QUESTIONNAIRE_PDF = 'defendant_directions_questionnaire.pdf';
const DEFENDANT_DEFENCE_PDF = 'response_sealed_form.pdf';
const CLAIMANT_DIRECTIONS_QUESTIONNAIRE_PDF = 'claimant_directions_questionnaire.pdf';

let caseNumber;

Feature('1v1 spec claim journey').tag('@civil-ccd-nightly @ui-spec-full-defence');

Scenario('01 1v1 Applicant solicitor creates specified claim for fast track-spec', async ({ LRspec, I }) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.createCaseSpecified('1v1 fast claim', 'organisation', null, 'company', null, 19000);
  caseNumber = await LRspec.grabCaseNumber();
  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseNumber);

  await waitForFinishedBusinessProcess(caseNumber);
  await addUserCaseMapping(caseNumber, config.applicantSolicitorUser);

  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await LRspec.see(`Case ${caseNumber} has been created.`);

  await I.viewAndAssertPdf('sealed_claim_form', TEST_DIRECTORY, BASELINE_DIRECTORY, SEALED_CLAIM_PDF, caseNumber);
}).retry(2);

Scenario('02 1v1 Defendant solicitor perform Inform Agreed Extension', async ({ LRspec }) => {
  console.log('1v1 Defendant solicitor Inform Agreed Extension claim-spec: ' + caseNumber);
  await assignCaseToLRSpecDefendant(caseNumber);
  await LRspec.login(config.defendantSolicitorUser);
  //await LRspec.informAgreedExtensionDateSpec();
  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await LRspec.see(caseEventMessage('Inform agreed extension date'));
}).retry(2);

Scenario('03 1v1 Respond To Claim - Defendants solicitor rejects claim for defendant', async ({ LRspec, I }) => {
  await LRspec.login(config.defendantSolicitorUser);
  await LRspec.respondToClaimFullDefence({
    defendant1Response: 'fullDefence',
    claimType: 'fast',
    defenceType: 'dispute'
  });

  // Reinstate the line below when https://tools.hmcts.net/jira/browse/EUI-6286 is fixed
  //await LRspec.see(caseEventMessage('Respond to claim'));

  await I.viewAndAssertPdf('defendant_directions_questionnaire_form', TEST_DIRECTORY, BASELINE_DIRECTORY, DEFENDANT_DIRECTIONS_QUESTIONNAIRE_PDF, caseNumber);
  await I.viewAndAssertPdf('response_sealed_form', TEST_DIRECTORY, BASELINE_DIRECTORY, DEFENDANT_DEFENCE_PDF, caseNumber);
}).retry(2);

Scenario('04 1v1 Claimant solicitor responds to defence - claimant Intention to proceed', async ({ LRspec, I }) => {
  await LRspec.login(config.applicantSolicitorUser);
  await LRspec.respondToDefence({ mpScenario: 'ONE_V_ONE', claimType: 'fast' });

  await I.viewAndAssertPdf('claimant_directions_questionnaire_form', TEST_DIRECTORY, BASELINE_DIRECTORY, CLAIMANT_DIRECTIONS_QUESTIONNAIRE_PDF, caseNumber);
}).retry(2);

AfterSuite(async () => {
  await unAssignAllUsers();
});
