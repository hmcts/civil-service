const config = require('../../../config.js');
const {unAssignAllUsers} = require('../../../api/caseRoleAssignmentHelper');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const judgeUser = config.judgeUserWithRegionId1;
const hearingUser = config.hearingCenterAdminWithRegionId1;
const claimAmountJudge = '11000';
let caseNumber;


Feature('1v2 Diff Sols Hearing Request Journey @e2e-hearing-request @e2e-nightly-prod');

async function prepareClaim(api, claimAmount) {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmount);
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne', 'FAST_CLAIM');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo', 'FAST_CLAIM');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO', 'FAST_CLAIM');
}

Scenario('Claimant solicitor raises a claim against 2 defendants', async ( {api}) => {
  if (['demo', 'aat'].includes(config.runningEnv)) {
    await prepareClaim(api, claimAmountJudge);
    await api.createSDO(judgeUser, 'CREATE_FAST_NO_SUM');
    caseNumber = await api.getCaseId();
  }
}).retry(2);

Scenario('Request, Edit and Cancel a Hearing', async ({I}) => {
  if (['demo', 'aat'].includes(config.runningEnv)) {
    const normalizedCaseId = caseNumber.toString().replace(/\D/g, '');
    await I.login(hearingUser);
    console.log(`Navigating to case: ${normalizedCaseId}`);
    await I.amOnPage(`${config.url.manageCase}/cases/case-details/${normalizedCaseId}`);
    await I.requestNewHearing();
    await I.updateHearing();
    await I.cancelHearing();
  }
}).retry(2);

AfterSuite(async  () => {
  await unAssignAllUsers();
});
