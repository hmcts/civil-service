

const config = require('../../../config.js');
const mpScenario = 'ONE_V_TWO';
const judgeUser = config.judgeUserWithRegionId1;
const caseWorkerUserReg1 = config.hearingCenterAdminWithRegionId1;
const caseWorkerUserReg2 = config.hearingCenterAdminWithRegionId2;
// to use on local because the idam images are different
//  const judgeUser = config.judgeUserWithRegionId1Local;
//  const caseWorkerUser = config.tribunalCaseworkerWithRegionId1Local;

//To reduce time of API test, temporarly stop running these tests. These test will modified to run in nightly build
Feature('Record Judgment 1v2 API test spec @api-spec-1v2 @api-jo  @api-nonprod-test');

async function prepareClaimSpecFinalOrderJO(api_spec){
  console.log('--createClaimWithRepresentedRespondent--');
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  console.log('--defendantResponse--');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE', mpScenario);
  console.log('--claimantResponse--');
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario,
    'JUDICIAL_REFERRAL');
  console.log('--sdo--');
  await api_spec.createSDO(judgeUser, 'CREATE_FAST_NO_SUM');
  console.log('--createFinalOrderJO--');
  await api_spec.createFinalOrderJO(judgeUser, 'FREE_FORM_ORDER');
  console.log('---confirm order review');
  await api_spec.confirmOrderReview(caseWorkerUserReg1);

}
Scenario('Default judgment Spec claim 1v2 - Set Aside After Order  - Record new judgment', async ({I, api_spec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL', false );
    await api_spec.amendRespondent1ResponseDeadline(config.systemupdate);
    await api_spec.defaultJudgmentSpec(config.applicantSolicitorUser, mpScenario, false);
    console.log('--setAsideJudgment--');
    await api_spec.setAsideJudgment(caseWorkerUserReg2, 'JUDGE_ORDER', 'ORDER_AFTER_APPLICATION','All_FINAL_ORDERS_ISSUED');
  }
});

Scenario('Default judgment Spec claim 1v2 - Set Aside after defence - Case taken offline', async ({I, api_spec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, false );
    await api_spec.amendRespondent1ResponseDeadline(config.systemupdate);
    await api_spec.defaultJudgmentSpec(config.applicantSolicitorUser, mpScenario, false);
    console.log('--markJudgmentPaid--');
    await api_spec.markJudgmentPaid(caseWorkerUserReg2);
    console.log('--setAsideJudgment--');
    await api_spec.setAsideJudgment(caseWorkerUserReg2, 'JUDGE_ORDER', 'ORDER_AFTER_DEFENCE', 'All_FINAL_ORDERS_ISSUED');
  }
});

Scenario.skip('Record Judgment with mark judgment paid Spec claim 1v2', async ({I, api_spec}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    console.log('--createClaimWithRepresentedRespondent--');
    await prepareClaimSpecFinalOrderJO(api_spec);
    console.log('--recordJudgment--');
    await api_spec.recordJudgment(caseWorkerUserReg1, mpScenario, 'DETERMINATION_OF_MEANS', 'PAY_IN_INSTALMENTS');
    await api_spec.editJudgment(caseWorkerUserReg1, mpScenario, 'DETERMINATION_OF_MEANS', 'PAY_BY_DATE');
  }
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
