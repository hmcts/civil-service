

const config = require('../../../config.js');
const {createAccount, deleteAccount} = require('../../../api/idamHelper');
const {RESPONDENT_SOLICITOR_QUERY, APPLICANT_SOLICITOR_QUERY,
  PUBLIC_QUERY
} = require('../../../fixtures/queryTypes');
const {checkLRQueryManagementEnabled} = require('../../../api/testingSupport.js');
const {respondToQueryAdminTask} = require('../../../fixtures/wa/respondToQueryTasks');
const {adjustCaseSubmittedDateForPublicQueries} = require('../../../helpers/lipQueriesHelper');

const claimType = 'SmallClaims';
let caseId;
let isQueryManagementEnabled = false;
const isTestEnv = ['preview', 'demo'].includes(config.runningEnv);


Feature('CCD 1v1 API test @api-spec-cui @api-nonprod');

async function raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, caseId, solicitorUser, caseworkerUser, queryType, isHearingRelated) {
  if (isQueryManagementEnabled) {
    const query = await qmSteps.raiseLRQuery(caseId, solicitorUser, queryType, isHearingRelated);
    await qmSteps.respondToQuery(caseId, caseworkerUser, query, queryType);
    await qmSteps.followUpOnLRQuery(caseId, solicitorUser, query, queryType);
  }
}

async function raiseRespondAndFollowUpToLipQueriesScenario(qmSteps, caseId, citizenUser, caseworkerUser, queryType, isHearingRelated) {
  if (isQueryManagementEnabled) {
    const query = await qmSteps.raiseLipQuery(caseId, citizenUser, queryType, isHearingRelated);
    await qmSteps.validateQmResponseTask(caseId, caseworkerUser, respondToQueryAdminTask(query.id), query.id);
    await qmSteps.respondToQuery(caseId, caseworkerUser, query, queryType);
    const queryFollowUp = await qmSteps.followUpOnLipQuery(caseId, citizenUser, query, queryType);
    await qmSteps.validateQmResponseTask(caseId, caseworkerUser, respondToQueryAdminTask(queryFollowUp.id), queryFollowUp.id);
  }
}

Before(async () => {
  isQueryManagementEnabled = await checkLRQueryManagementEnabled();
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

async function prepareClaimLiPvLiP(api_spec_cui, carmEnabled, claimType = 'SmallClaims') {
  let expectedEndState = carmEnabled ? 'IN_MEDIATION' : 'JUDICIAL_REFERRAL';
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, claimType, carmEnabled);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, claimType, carmEnabled);
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, expectedEndState, carmEnabled);
  return caseId;
}

async function prepareClaimLiPvLiPMintiTrack(api_spec_cui, carmEnabled, claimType = 'INTERMEDIATE') {
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, claimType, carmEnabled, '', true);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, claimType, carmEnabled);
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, 'AWAITING_APPLICANT_INTENTION', carmEnabled);
}

async function prepareClaimLiPvLiPRequestForReconsideration(api_spec_cui, carmEnabled) {
  let expectedEndState = carmEnabled ? 'IN_MEDIATION' : 'JUDICIAL_REFERRAL';
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'Request for reconsideration track', carmEnabled);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'Request for reconsideration track', carmEnabled, 'FULL_DEFENCE');
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, expectedEndState, carmEnabled);
}

Scenario('1v1 LiP v LiP Part admit defendant and claimant response - claimant rejects installment plan - CARM enabled', async ({api_spec_cui}) => {
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, claimType, true);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'SmallClaimPartAdmit', true);
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, 'IN_MEDIATION', true, 'partadmit');
}).tag('@api-prod @api-nonprod');

Scenario('1v1 LiP v LiP defendant and claimant response - CARM enabled - Minti Enabled', async ({api_spec_cui}) => {
  await prepareClaimLiPvLiPMintiTrack(api_spec_cui, true);
});

Scenario('1v1 LiP v LiP Case Progression Journey', async ({api_spec_cui, qmSteps}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaimLiPvLiP(api_spec_cui, false, 'FastTrack');
    await api_spec_cui.createSDO(config.judgeUserWithRegionId1, 'CREATE_FAST');
    await adjustCaseSubmittedDateForPublicQueries(caseId, true);
    await raiseRespondAndFollowUpToLipQueriesScenario(qmSteps, caseId,
      config.applicantCitizenUser, config.hearingCenterAdminWithRegionId1,
      PUBLIC_QUERY, true
    );
    await raiseRespondAndFollowUpToLipQueriesScenario(qmSteps, caseId,
      config.defendantCitizenUser2, config.hearingCenterAdminWithRegionId1,
      PUBLIC_QUERY, true
    );
    await api_spec_cui.evidenceUploadApplicant(config.applicantCitizenUser);
    await api_spec_cui.evidenceUploadDefendant(config.defendantCitizenUser2);
    await api_spec_cui.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'FAST_TRACK_TRIAL', 'CUI');
    await api_spec_cui.trialReadinessCitizen(config.applicantCitizenUser);
    await api_spec_cui.trialReadinessCitizen(config.defendantCitizenUser2);
    await api_spec_cui.createFinalOrder(config.judgeUserWithRegionId1, 'FREE_FORM_ORDER');
  }
}).tag('@wa-task @QM @api-prod');

Scenario('1v1 LiP v LiP Request for reconsideration', async ({api_spec_cui}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaimLiPvLiPRequestForReconsideration(api_spec_cui, false);
    await api_spec_cui.createSDO(config.tribunalCaseworkerWithRegionId4);
    await api_spec_cui.requestForReconsiderationCitizen(config.applicantCitizenUser);
    await api_spec_cui.judgeDecisionOnReconsiderationRequest(config.judgeUserWithRegionId1, 'CREATE_SDO');
  }
});

async function prepareClaimLiPvLR(api_spec_cui, noc, carmEnabled) {
  let expectedEndState = carmEnabled ? 'IN_MEDIATION' : 'JUDICIAL_REFERRAL';
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, claimType, carmEnabled);
  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, config.defendantSolicitorUser);
  await api_spec_cui.checkUserCaseAccess(config.defendantCitizenUser2, false);
  await api_spec_cui.checkUserCaseAccess(config.defendantSolicitorUser, true);

  //After CIV-14085 Case will be in PROCEEDS_IN_HERITAGE_SYSTEM, so no need to perform defendant or claimant response
  //await api_spec_cui.defendantResponse(config.defendantSolicitorUser);
  //await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, expectedEndState, carmEnabled);
}

Scenario('1v1 LiP v LR defendant and claimant response- CARM enabled @api-nightly-prod', async ({noc, api_spec_cui, qmSteps
}) => {
  await prepareClaimLiPvLR(api_spec_cui, noc, true);
  await adjustCaseSubmittedDateForPublicQueries(caseId, true);
  if (isTestEnv) {
    await raiseRespondAndFollowUpToLipQueriesScenario(qmSteps, caseId,
      config.applicantCitizenUser, config.ctscAdminUser,
      PUBLIC_QUERY, false
    );
    await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, caseId,
      config.defendantSolicitorUser, config.ctscAdminUser,
      PUBLIC_QUERY, false
    );
  }
}).tag('@QM');

async function prepareClaimLRvLiP(api_spec_cui, noc, carmEnabled) {
  let expectedEndState = carmEnabled ? 'IN_MEDIATION' : 'JUDICIAL_REFERRAL';
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, claimType, carmEnabled);
  await noc.requestNoticeOfChangeForApplicant1Solicitor(caseId, config.applicantSolicitorUser);
  await api_spec_cui.checkUserCaseAccess(config.applicantCitizenUser, false);
  await api_spec_cui.checkUserCaseAccess(config.applicantSolicitorUser, true);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, claimType, carmEnabled);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'No', expectedEndState, carmEnabled);
}

Scenario('1v1 LR v LiP defendant and claimant response - claimant does NoC - CARM enabled @api-nightly-prod', async ({noc, api_spec_cui}) => {
  await  prepareClaimLRvLiP(api_spec_cui, noc, true);
});

async function prepareClaimLRvLiPExui(api_spec_cui, carmEnabled, claimType = 'SmallClaims') {
  let expectedEndState = carmEnabled ? 'IN_MEDIATION' : 'JUDICIAL_REFERRAL';
  caseId = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', claimType, carmEnabled);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, claimType, carmEnabled);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'No', expectedEndState, carmEnabled);
}

Scenario('1v1 LR v LiP defendant and claimant response - claim created from exui - CARM enabled @api-nightly-prod', async ({api_spec_cui}) => {
  await prepareClaimLRvLiPExui(api_spec_cui, true);
});

Scenario('1v1 LR v LiP case progression', async ({api_spec_cui, qmSteps}) => {
  if (['preview', 'demo'].includes(config.runningEnv)) {
    await prepareClaimLRvLiPExui(api_spec_cui, false, 'FastTrack');
    await api_spec_cui.createSDO(config.judgeUserWithRegionId1, 'CREATE_FAST');
    await adjustCaseSubmittedDateForPublicQueries(caseId, true);
    if (isTestEnv) {
      await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, caseId,
        config.applicantSolicitorUser, config.hearingCenterAdminWithRegionId1,
        PUBLIC_QUERY, true
      );
      await raiseRespondAndFollowUpToLipQueriesScenario(qmSteps, caseId,
        config.defendantCitizenUser2, config.hearingCenterAdminWithRegionId1,
        PUBLIC_QUERY, true
      );
    } else {
      await raiseRespondAndFollowUpToSolicitorQueriesScenario(qmSteps, caseId,
        config.applicantSolicitorUser, config.hearingCenterAdminWithRegionId1,
        APPLICANT_SOLICITOR_QUERY, true
      );
    }
    await api_spec_cui.evidenceUploadDefendant(config.defendantCitizenUser2);
    await api_spec_cui.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'FAST_TRACK_TRIAL');
    await api_spec_cui.amendHearingDueDate(config.systemupdate);
    await api_spec_cui.hearingFeePaid(config.hearingCenterAdminWithRegionId1);
    await api_spec_cui.trialReadinessCitizen(config.defendantCitizenUser2);
    await api_spec_cui.createFinalOrder(config.judgeUserWithRegionId1, 'FREE_FORM_ORDER');
  }
}).tag('@wa-task @QM @api-prod');

Scenario('1v1 LR v LiP Request for reconsideration', async ({api_spec_cui}) => {
    await  prepareClaimLRvLiPExui(api_spec_cui, false, 'Request for reconsideration track');
    await api_spec_cui.createSDO(config.tribunalCaseworkerWithRegionId4);
    await api_spec_cui.requestForReconsiderationCitizen(config.defendantCitizenUser2);
    await api_spec_cui.judgeDecisionOnReconsiderationRequest(config.judgeUserWithRegionId1, 'CREATE_SDO');

}).tag('@api-nightly-prod');

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteAccount(config.defendantCitizenUser2.email);
});

