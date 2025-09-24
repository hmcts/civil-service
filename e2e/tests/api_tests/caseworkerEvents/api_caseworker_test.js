const config = require('../../../config.js');
const {createAccount} = require('../../../api/idamHelper');

Feature('CCD API test @api-caseworker @api-nightly-prod');

Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

async function prepareClaimLiPvLiP(api_spec_cui, carmEnabled, claimType = 'SmallClaims') {
  let expectedEndState = carmEnabled ? 'IN_MEDIATION' : 'JUDICIAL_REFERRAL';
  let caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, claimType, carmEnabled);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, claimType, carmEnabled);
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, expectedEndState, carmEnabled);
}

Scenario('1v1 JUDICIAL_REFERRAL Lip v Lip stay case dismiss case', async ({api_spec_cui}) => {
  await prepareClaimLiPvLiP(api_spec_cui, false, 'FastTrack');
  await api_spec_cui.stayCase(config.hearingCenterAdminWithRegionId1);
  await api_spec_cui.manageStay(config.hearingCenterAdminWithRegionId1, true);
  await api_spec_cui.manageStay(config.hearingCenterAdminWithRegionId1, false);
  await api_spec_cui.createSDO(config.judgeUserWithRegionId1, 'CREATE_SMALL');
  await api_spec_cui.dismissCase(config.hearingCenterAdminWithRegionId1);
}).retry(1);

Scenario('1v1 LR FAST TRACK prepare for conduct hearing stay case @api-nonprod', async ({api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  await api_spec.defendantResponse(config.defendantSolicitorUser);
  await api_spec.claimantResponse(config.applicantSolicitorUser);
  await api_spec.createSDO(config.judgeUserWithRegionId1);
  await api_spec.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'FAST_TRACK_TRIAL');
  await api_spec.amendHearingDueDate(config.systemupdate);
  await api_spec.hearingFeePaid(config.hearingCenterAdminWithRegionId1);
  await api_spec.stayCase(config.hearingCenterAdminWithRegionId1);
  await api_spec.manageStay(config.hearingCenterAdminWithRegionId1, true);
  await api_spec.sendMessage(config.ctscAdminUser);
  await api_spec.replyMessage(config.judgeUserWithRegionId1);
  await api_spec.replyMessage(config.ctscAdminUser);
  await api_spec.manageStay(config.hearingCenterAdminWithRegionId1, false);
  await api_spec.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'FAST_TRACK_TRIAL');
  await api_spec.dismissCase(config.hearingCenterAdminWithRegionId1);
}).retry(1);

let caseId;

Scenario('1v2 LR UNSPEC claim hearing readiness', async ({api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP');
  await api.notifyClaim(config.applicantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP');
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  caseId = await api.getCaseId();
  await api.amendRespondent1ResponseDeadline(config.systemupdate);
  await api.defaultJudgment(config.applicantSolicitorUser, 'TRIAL_HEARING');
  await api.sdoDefaultJudgment(config.judgeUserWithRegionId1, 'TRIAL_HEARING');
  await api.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'OTHER');
  await api.stayCase(config.hearingCenterAdminWithRegionId1);
  await api.manageStay(config.hearingCenterAdminWithRegionId1, true);
  await api.sendMessage(config.ctscAdminUser);
  await api.replyMessage(config.judgeUserWithRegionId1);
  await api.replyMessage(config.ctscAdminUser);
  await api.manageStay(config.hearingCenterAdminWithRegionId1, false);
  await api.dismissCase(config.hearingCenterAdminWithRegionId1);
}).retry(1);

let claimRef;
const claimType = 'SmallClaims';
let carmEnabled = false;
Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LR  LR v Lip In mediation', async ({api_spec_cui}) => {
  claimRef = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, '', claimType, carmEnabled);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, claimRef, claimType, carmEnabled);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'Yes', 'IN_MEDIATION', carmEnabled);
  await api_spec_cui.stayCase(config.hearingCenterAdminWithRegionId1);
  await api_spec_cui.manageStay(config.hearingCenterAdminWithRegionId1, true);
  await api_spec_cui.sendMessage(config.ctscAdminUser);
  await api_spec_cui.replyMessage(config.judgeUserWithRegionId1);
  await api_spec_cui.replyMessage(config.ctscAdminUser);
  await api_spec_cui.manageStay(config.hearingCenterAdminWithRegionId1, false);
  await api_spec_cui.createSDO(config.judgeUserWithRegionId1, 'CREATE_SMALL');
  await api_spec_cui.dismissCase(config.hearingCenterAdminWithRegionId1);
}).retry(1);

const mintiEnabled = true;
const claimAmountMulti = '200001';

Scenario('1v1 Multi Claim Stay Case Judicial Referral', async ({api}) => {
  const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
  const judgeUser = config.judgeUserWithRegionId1;
  const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, mpScenario, claimAmountMulti, mintiEnabled);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);
  await api.defendantResponse(config.defendantSolicitorUser, mpScenario, 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, mpScenario, 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, mpScenario, 'JUDICIAL_REFERRAL', 'FOR_SDO', 'MULTI_CLAIM');
  await api.stayCase(config.hearingCenterAdminWithRegionId1);
  await api.manageStay(config.hearingCenterAdminWithRegionId1, true);
  //commenting this for multi claim as send and reply is not enabled yet
  // await api.sendMessage(config.ctscAdminUser);
  // await api.replyMessage(config.judgeUserWithRegionId1);
  // await api.replyMessage(config.ctscAdminUser);
  await api.manageStay(config.hearingCenterAdminWithRegionId1, false, true);
  await api.createFinalOrder(judgeUser, 'DOWNLOAD_ORDER_TEMPLATE', 'MULTI');
  await api.scheduleHearing(hearingCenterAdminToBeUsed, 'FAST_TRACK_TRIAL');
  await api.dismissCase(config.hearingCenterAdminWithRegionId1);
}).retry(1);
