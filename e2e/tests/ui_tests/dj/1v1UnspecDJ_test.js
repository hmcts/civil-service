

const config = require('../../../config.js');
let caseId, taskId, hearingDateIsLessThan3Weeks, validSummaryJudgmentDirectionsTask, validScheduleAHearingTask;
const serviceRequest = require('../../../pages/createClaim/serviceRequest.page');
const judgeUserToBeUsed = config.judgeUserWithRegionId1;
const hearingCenterAdminToBeUsed = config.hearingCenterAdminWithRegionId1;

if (config.runWAApiTest) {
  validSummaryJudgmentDirectionsTask = require('../../../../wa/tasks/summaryJudgmentDirectionsTask.js');
  validScheduleAHearingTask = require('../../../../wa/tasks/scheduleADisposalHearing.js');
}

Feature('1v1 unspec default judgment').tag('@civil-ccd-nightly @ui-dj');

Scenario('01 Create 1v1 unspec claim, notify claim, notify claim details, request default judgement', async ({I, api}) => {
  await api.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', '11000');
  caseId = await api.getCaseId();

  //below amend claim documents only needed as assertion was failing on notify claims
  await api.amendClaimDocuments(config.applicantSolicitorUser);
  await api.notifyClaim(config.applicantSolicitorUser);
  await api.notifyClaimDetails(config.applicantSolicitorUser);

  await api.amendRespondent1ResponseDeadline(config.systemupdate);
  await I.login(config.applicantSolicitorUser);
  await I.initiateDJUnspec(caseId, 'ONE_V_ONE');
}).retry(2).tag('@civil-ccd-master @civil-ccd-pr');

//DTSCCI-358
Scenario.skip('02 Judge add case notes', async ({I, api}) => {
  await I.login(judgeUserToBeUsed);
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
  await I.waitForText('Summary');
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId + '/trigger/EVIDENCE_UPLOAD_JUDGE/EVIDENCE_UPLOAD_JUDGE');
  await I.waitForText('How do you want to add a case note?');
  await I.judgeAddsCaseNotes();
}).retry(2);

Scenario('03 Judge perform direction order', async ({I, api, WA}) => {
  await I.login(judgeUserToBeUsed);
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
  await I.waitForText('Summary');
  if (config.runWAApiTest) {
    const summaryJudgmentDirectionsTask = await api.retrieveTaskDetails(judgeUserToBeUsed, caseId, config.waTaskIds.judgeUnspecDJTask);
    console.log('summaryJudgmentDirectionsTask...' , summaryJudgmentDirectionsTask);
    WA.validateTaskInfo(summaryJudgmentDirectionsTask, validSummaryJudgmentDirectionsTask);
    taskId = summaryJudgmentDirectionsTask['id'];
    api.assignTaskToUser(judgeUserToBeUsed, taskId);
  }
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId + '/trigger/STANDARD_DIRECTION_ORDER_DJ/STANDARD_DIRECTION_ORDER_DJ');
  await I.judgePerformDJDirectionOrder();
  if (config.runWAApiTest) {
    api.completeTaskByUser(judgeUserToBeUsed, taskId);
  }
}).retry(2);

Scenario('04 Hearing schedule', async ({I, api, WA}) => {
  //Permission fields in task details are different in AAT and Demo.
  if (config.runWAApiTest && ['aat'].includes(config.runningEnv)) {
    const scheduleAHearingTask = await api.retrieveTaskDetails(hearingCenterAdminToBeUsed, caseId, config.waTaskIds.scheduleAHearing);
    console.log('Schedule a hearing task...' , scheduleAHearingTask);
    WA.validateTaskInfo(scheduleAHearingTask, validScheduleAHearingTask);
    taskId = scheduleAHearingTask['id'];
  }
  await createHearingScheduled(I);
}).retry(2);

Scenario.skip('05 Verify error on trial readiness', async ({I, api}) => {
  await api.amendHearingDate(config.systemupdate, '2022-01-10');
  hearingDateIsLessThan3Weeks = true;
  await performConfirmTrialReadiness(I, config.defendantSolicitorUser, 'yes');
}).retry(2);

Scenario.skip('06 Confirm trial readiness', async ({I, api}) => {
  await api.amendHearingDate(config.systemupdate, '2025-01-10');
  hearingDateIsLessThan3Weeks = false;
  await performConfirmTrialReadiness(I, config.applicantSolicitorUser, hearingDateIsLessThan3Weeks, 'no');
  await performConfirmTrialReadiness(I, config.defendantSolicitorUser, hearingDateIsLessThan3Weeks, 'yes');
}).retry(2);

Scenario.skip('07 Pay hearing fee', async ({I}) => {
  await payHearingFee(I);
}).retry(2);

async function createHearingScheduled(I) {
    await I.login(hearingCenterAdminToBeUsed);
    await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
    await I.waitForText('Summary');
    await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId+ '/trigger/HEARING_SCHEDULED/HEARING_SCHEDULED');
    await I.createHearingScheduled();
}

async function performConfirmTrialReadiness(I, user = config.applicantSolicitorUser, readyForTrial = 'yes') {
    await I.login(user);
    console.log('value of hearingDateIsLessThan3Weeks..', hearingDateIsLessThan3Weeks);
    await I.confirmTrialReadiness(user, hearingDateIsLessThan3Weeks, readyForTrial);
}

async function payHearingFee(I, user = config.applicantSolicitorUser) {
  await I.login(user);
  await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
  await serviceRequest.openServiceRequestTab();
  await serviceRequest.payFee(caseId, true);
}

//DTSCCI-358
Scenario.skip('08 Verify Challenged access check for judge', async ({I, WA}) => {
  if (config.runWAApiTest) {
    await I.login(config.judgeUser2WithRegionId4);
    await WA.runChallengedAccessSteps(caseId);
  }
}).retry(2);

Scenario.skip('09 Verify Challenged access check for admin', async ({I, WA}) => {
  if (config.runWAApiTest) {
    await I.login(config.hearingCenterAdminWithRegionId4);
    await WA.runChallengedAccessSteps(caseId);
  }
}).retry(2);

Scenario.skip('10 erify Challenged access check for legalops', async ({I, WA}) => {
  if (config.runWAApiTest) {
    await I.login(config.tribunalCaseworkerWithRegionId4);
    await WA.runChallengedAccessSteps(caseId);
  }
}).retry(2);

Scenario.skip('11 Verify Specific access check for judge', async ({I, WA, api}) => {
  await I.login(config.iacLeadershipJudge);
  await WA.runSpecificAccessRequestSteps(caseId);
  if (config.runWAApiTest) {
    const sarTask = await api.retrieveTaskDetails(judgeUserToBeUsed, caseId, config.waTaskIds.reviewSpecificAccessRequestJudiciary);
  } else {
    console.log('WA flag is not enabled');
    return;
  }
  await I.login(judgeUserToBeUsed);
  await WA.runSpecificAccessApprovalSteps(caseId);
  await I.login(config.iacLeadershipJudge);
  await WA.verifyApprovedSpecificAccess(caseId);
}).retry(2);

Scenario.skip('12 Request Specific access check for admin', async ({I, WA, api}) => {
   let userToBeLoggedIn = config.runningEnv == 'demo' ? config.iacAdminUser : config.iacAATAdminUser;
   await I.login(userToBeLoggedIn);
   await WA.runSpecificAccessRequestSteps(caseId);
 }).retry(2);

 Scenario.skip('13 Approve Specific access check for admin', async ({I, WA, api}) => {
  if (config.runWAApiTest) {
    const sarTask = await api.retrieveTaskDetails(config.nbcTeamLeaderWithRegionId1, caseId, config.waTaskIds.reviewSpecificAccessRequestAdmin);
  } else {
    console.log('WA flag is not enabled');
    return;
  }
  await I.login(config.nbcTeamLeaderWithRegionId1);
  await WA.runSpecificAccessApprovalSteps(caseId);
}).retry(2);

Scenario.skip('14 Verify approved Specific access check for admin', async ({I, WA, api}) => {
  let userToBeLoggedIn = config.runningEnv == 'demo' ? config.iacAdminUser : config.iacAATAdminUser;
  await I.login(userToBeLoggedIn);
  await WA.verifyApprovedSpecificAccess(caseId);
}).retry(2);

Scenario.skip('15 Verify Specific access check for legalops', async ({I, WA, api}) => {
  await I.login(config.iacLegalOpsUser);
  await WA.runSpecificAccessRequestSteps(caseId);
  if (config.runWAApiTest) {
    const sarTask = await api.retrieveTaskDetails(config.seniorTBCWWithRegionId4, caseId, config.waTaskIds.reviewSpecificAccessRequestLegalOps);
  } else {
    console.log('WA flag is not enabled');
    return;
  }
  await I.login(config.seniorTBCWWithRegionId4);
  await WA.runSpecificAccessApprovalSteps(caseId);
  await I.login(config.iacLegalOpsUser);
  await WA.verifyApprovedSpecificAccess(caseId);
}).retry(2);


Scenario.skip('16 Verify Specific access check for CTSC', async ({I, WA, api}) => {
  await I.login(config.iacCtscTeamLeaderUser);
  await WA.runSpecificAccessRequestSteps(caseId);
  if (config.runWAApiTest) {
    const sarTask = await api.retrieveTaskDetails(config.ctscTeamLeaderUser, caseId, config.waTaskIds.reviewSpecificAccessRequestCTSC);
  } else {
    console.log('WA flag is not enabled');
    return;
  }
  await I.login(config.ctscTeamLeaderUser);
  await WA.runSpecificAccessApprovalSteps(caseId);
  await I.login(config.iacCtscTeamLeaderUser);
  await WA.verifyApprovedSpecificAccess(caseId);
}).retry(2);

Scenario.skip('17 Verify Staff UI', async ({I, WA, api}) => {
  await I.login(config.staffUIAdmin);
  await WA.verifyStaffLink();
}).retry(2);

Scenario.skip('18 Verify Judicial booking UI', async ({I, WA, api}) => {
  await I.login(config.feePaidJudge);
  await WA.createBooking('Central London County Court');
  await WA.createBooking('Liverpool Civil and Family Court');
  await WA.verifyCreatedBooking('Central London County Court');
  await WA.verifyCreatedBooking('Liverpool Civil and Family Court');
}).retry(2);

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});