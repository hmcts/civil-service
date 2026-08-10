

const config = require('../../../config.js');
const mpScenario = 'ONE_V_ONE';
const judgeUser = config.judgeUserWithRegionId1;
const caseWorkerUserReg1 = config.hearingCenterAdminWithRegionId1;
const caseWorkerUserReg2 = config.hearingCenterAdminWithRegionId2;
// to use on local because the idam images are different
// const judgeUser = config.judgeUserWithRegionId1Local;
// const caseWorkerUser = config.tribunalCaseworkerWithRegionId1Local;

Feature('1v1 spec record judgment api test').tag('@civil-service-nightly');

Scenario('SetAside Default Judgment after judgment error - Spec claim 1v1 - Case taken offline', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api_spec.amendRespondent1ResponseDeadline(config.systemupdate);
  await api_spec.defaultJudgmentSpec(config.applicantSolicitorUser, mpScenario, false);
  await api_spec.markJudgmentPaid(config.applicantSolicitorUser);
  await api_spec.setAsideJudgment(caseWorkerUserReg2, 'JUDGMENT_ERROR','ORDER_AFTER_DEFENCE','All_FINAL_ORDERS_ISSUED');
}).tag('@api-jo');

Scenario.skip('Record Judgment Spec claim 1v1 with mark paid in full', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.informAgreedExtensionDate(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario,
    'AWAITING_APPLICANT_INTENTION');
  await api_spec.createSDO(judgeUser, 'CREATE_FAST_NO_SUM');
  await api_spec.createFinalOrderJO(judgeUser, 'FREE_FORM_ORDER');
  await api_spec.confirmOrderReview(caseWorkerUserReg1);
  await api_spec.recordJudgment(caseWorkerUserReg1, mpScenario, 'DETERMINATION_OF_MEANS', 'PAY_IMMEDIATELY');
  await api_spec.editJudgment(caseWorkerUserReg1, mpScenario, 'DETERMINATION_OF_MEANS', 'PAY_BY_DATE');
  await api_spec.markJudgmentPaid(config.applicantSolicitorUser);
});

Scenario.skip('Refer To Judge Spec claim 1v1 Defence Received In Time', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.informAgreedExtensionDate(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE', mpScenario,
    'AWAITING_APPLICANT_INTENTION');
  await api_spec.createSDO(judgeUser, 'CREATE_FAST_NO_SUM');
  await api_spec.createFinalOrderJO(judgeUser, 'FREE_FORM_ORDER');
  await api_spec.confirmOrderReview(caseWorkerUserReg1);
  await api_spec.recordJudgment(caseWorkerUserReg1, mpScenario, 'DETERMINATION_OF_MEANS', 'PAY_IMMEDIATELY');
  await api_spec.referToJudgeDefenceReceived(caseWorkerUserReg1);
});

Scenario('SetAside Default Judgment Spec claim 1v1 - Record new judgment after hearing', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario);
  await api_spec.amendRespondent1ResponseDeadline(config.systemupdate);
  await api_spec.defaultJudgmentSpec(config.applicantSolicitorUser, mpScenario, false);
  await api_spec.setAsideJudgment(caseWorkerUserReg2, 'JUDGE_ORDER','ORDER_AFTER_APPLICATION', 'All_FINAL_ORDERS_ISSUED');
});

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
