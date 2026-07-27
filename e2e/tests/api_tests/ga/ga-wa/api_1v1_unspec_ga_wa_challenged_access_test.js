const config = require('../../../../config.js');

const mpScenario = 'ONE_V_ONE';
const claimAmountJudge = '11000';
let civilCaseReference, gaCaseReference,expectedJudgeDecideOnApplicationBeforeSDOTask,expectedLADecideOnApplicationBeforeSDOTask;
if (config.runWAApiTest) {
  expectedJudgeDecideOnApplicationBeforeSDOTask = require('../../../../../wa/tasks/judgeDecideOnApplicationBeforeSDOTask.js');
  expectedLADecideOnApplicationBeforeSDOTask = require('../../../../../wa/tasks/laDecideOnApplicationBeforeSDOTask.js');
}

Feature('GA - WA Challenged Access');

Scenario('GA - Challenged Access test - NBCAdmin & judge', async ({ I, api_ga, wa }) => {
  civilCaseReference = await api_ga.createUnspecifiedClaim(config.applicantSolicitorUser, mpScenario, 'Company', claimAmountJudge);
  await api_ga.amendClaimDocuments(config.applicantSolicitorUser);
  await api_ga.notifyClaim(config.applicantSolicitorUser, mpScenario, civilCaseReference);
  await api_ga.notifyClaimDetails(config.applicantSolicitorUser, civilCaseReference);
  await api_ga.acknowledgeClaim(config.defendantSolicitorUser, civilCaseReference, true);
  await api_ga.defendantResponseClaim(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api_ga.claimantResponseUnSpec(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGeneralApplicationWithOutNotice(config.applicantSolicitorUser, civilCaseReference);
  console.log('*** General Application case created ***' + gaCaseReference);

  console.log('*** Challenged Access steps for nbcAdmin - Start ***');
  await I.login(config.hearingCenterAdminWithRegionId2);
  await wa.runChallengedAccessSteps(gaCaseReference);
  console.log('*** Challenged Access steps for nbcAdmin - End ***');

  console.log('*** Validate Task Initiation for Judge Decide On Application - Start ***');
  if (config.runWAApiTest) {
    const actualJudgeDecideOnApplicationTask = await api_ga.retrieveTaskDetails(config.judgeUserWithRegionId4,
      gaCaseReference, config.waTaskIds.judgeDecideOnApplication);
    console.log('actualJudgeDecideOnApplicationTask...', actualJudgeDecideOnApplicationTask);
    wa.validateTaskInfo(actualJudgeDecideOnApplicationTask, expectedJudgeDecideOnApplicationBeforeSDOTask);
  }
  console.log('*** Validate Task Initiation for Judge Decide On Application - End ***');

  console.log('*** Challenged Access steps for Judge - Start ***');
  await I.login(config.judgeUser2WithRegionId2);
  await wa.runChallengedAccessSteps(gaCaseReference);
  console.log('*** Challenged Access steps for Judge - End ***');

}).retry(0);

Scenario('GA - Challenged Access test - LegalAdvisor', async ({ I, api_ga, wa }) => {
  civilCaseReference = await api_ga.createSpecifiedClaim(config.applicantSolicitorUser, mpScenario);
  console.log('Civil Case created for general application: ' + civilCaseReference);

  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGaForLA(config.applicantSolicitorUser, civilCaseReference);
  console.log('General Application Created Successfully');

  console.log('*** Validate Task Initiation for Legal Advisor Decide On Application - Start ***');
  if (config.runWAApiTest) {
    const actualLegalAdvisorDecideOnApplicationTask = await api_ga.retrieveTaskDetails(config.tribunalCaseworkerWithRegionId4,
      gaCaseReference, config.waTaskIds.legalAdvisorDecideOnApplication);
    console.log('actualLegalAdvisorDecideOnApplicationTask...', actualLegalAdvisorDecideOnApplicationTask);
    wa.validateTaskInfo(actualLegalAdvisorDecideOnApplicationTask, expectedLADecideOnApplicationBeforeSDOTask);
  }
  console.log('*** Validate Task Initiation for legal Advisor Decide On Application- End ***');

  console.log('*** Challenged Access steps for LegalAdvisor - Start ***');
  await I.login(config.tribunalCaseworkerWithRegionId);
  await wa.runChallengedAccessSteps(gaCaseReference);
  console.log('*** Challenged Access steps for LegalAdvisor - End ***');
}).retry(0);


AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
