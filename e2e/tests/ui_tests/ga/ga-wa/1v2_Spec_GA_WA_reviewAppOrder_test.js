  const {
  waitForGACamundaEventsFinishedBusinessProcess,
  waitForFinishedBusinessProcess
} = require('../../../../api/testingSupport');
const states = require('../../../../fixtures/ga-events/ga-ccd/state.js');
const mpScenario = 'ONE_V_TWO_TWO_LEGAL_REP';
const config = require('../../../../config.js');
  const listForHearingStatus = states.LISTING_FOR_A_HEARING.id;

  let civilCaseReference, gaCaseReference,
  expectedLADecideOnApplicationBeforeSDOTask,
  expectedJudgeDecideOnApplicationAfterSDOTask,
  expectedJudgeDecideOnApplicationBeforeSDOTask,
  expectedScheduleAppHearingBeforeSDOTask,
  expectedScheduleAppHearingAfterSDOTask, judgeUser;

  if (config.runWAApiTest) {
  expectedLADecideOnApplicationBeforeSDOTask = require('../../../../../wa/tasks/laDecideOnApplicationForSAJTask.js');
  expectedJudgeDecideOnApplicationAfterSDOTask = require('../../../../../wa/tasks/judgeDecideOnApplicationAfterSDOTask.js');
  expectedJudgeDecideOnApplicationBeforeSDOTask = require('../../../../../wa/tasks/judgeDecideOnApplicationForSAJTask.js');
  expectedScheduleAppHearingBeforeSDOTask = require('../../../../../wa/tasks/scheduleAppHearingBeforeSDOTask.js');
  expectedScheduleAppHearingAfterSDOTask = require('../../../../../wa/tasks/scheduleAppHearingAfterSDOTask.js');
}

Feature('1v2 Spec claim: GA - WA Scenarios').tag('@ui-ga-wa');
//requires fixing

Scenario.skip('LA refer to judge - R2 Judge Make decision - NBC admin schedule Hearing', async ({ I, api_ga, wa }) => {
  civilCaseReference = await api_ga.createSpecifiedClaim(
    config.applicantSolicitorUser, mpScenario, 'Company');
  console.log('Civil Case created for general application: ' + civilCaseReference);
  console.log('Make a General Application');
  gaCaseReference = await api_ga.initiateGaForJudge(config.applicantSolicitorUser, civilCaseReference);

  console.log('Region 2 LA referring to Judge');
  if (config.runWAApiTest) {
    const actualLADecideOnApplicationTask = await api_ga.retrieveTaskDetails(config.tribunalCaseworkerWithRegionId2,
      gaCaseReference, config.waTaskIds.legalAdvisorDecideOnApplication);
    console.log('actualLADecideOnApplicationTask...', actualLADecideOnApplicationTask);
    wa.validateTaskInfo(actualLADecideOnApplicationTask, expectedLADecideOnApplicationBeforeSDOTask);
  }
  await I.login(config.hearingCenterAdminWithRegionId2);
  await wa.verifyAdminTask(gaCaseReference, config.waTaskIds.legalAdvisorDecideOnApplication);
  await wa.goToEvent('Refer to Judge');
  await wa.referToJudge();
  await wa.verifyNoActiveTask(gaCaseReference);

  if (['preview', 'aat'].includes(config.runningEnv)) {
    judgeUser = config.judgeUser2WithRegionId2;
  } else if (['demo'].includes(config.runningEnv)) {
    judgeUser = config.judgeUser2WithRegionId2;
  } else {
    judgeUser = config.judgeLocalUser;
  }
  console.log('Region 2 Judge List for a hearing');
  if (config.runWAApiTest) {
    const actualJudgeDecideOnApplicationTask = await api_ga.retrieveTaskDetails(judgeUser,
      gaCaseReference, config.waTaskIds.judgeDecideOnApplication);
    console.log('actualJudgeDecideOnApplicationTask...', actualJudgeDecideOnApplicationTask);
    wa.validateTaskInfo(actualJudgeDecideOnApplicationTask, expectedJudgeDecideOnApplicationBeforeSDOTask);
  }
  await I.login(judgeUser);
  await wa.goToTask(gaCaseReference, config.waTaskIds.judgeDecideOnApplication);
  await I.judgeListForAHearingDecisionWA('listForAHearing', gaCaseReference, 'no', 'Hearing_order');
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, listForHearingStatus, judgeUser);
  await wa.verifyNoActiveTask(gaCaseReference);

  console.log('Region 2 NBC admin review scheduled Application Hearing');
  if (config.runWAApiTest) {
    const actualScheduleApplicationHearingTask = await api_ga.retrieveTaskDetails(config.nbcAdminWithRegionId2, gaCaseReference, config.waTaskIds.scheduleApplicationHearing);
    console.log('actualScheduleApplicationHearingTask...', actualScheduleApplicationHearingTask);
    wa.validateTaskInfo(actualScheduleApplicationHearingTask, expectedScheduleAppHearingBeforeSDOTask);
  }
  await I.login(config.nbcAdminWithRegionId2);
  await wa.goToTask(gaCaseReference, config.waTaskIds.scheduleApplicationHearing);
  await I.fillHearingNotice(gaCaseReference, 'claimAndDef', 'basildon', 'VIDEO');
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, states.HEARING_SCHEDULED.id, config.nbcAdminWithRegionId4);
  console.log('Hearing Notice created for: ' + gaCaseReference);
  await wa.verifyNoActiveTask(gaCaseReference);
});

Scenario.skip('After SDO GA - Change court location  - HC admin review application order', async ({ I, api_ga, wa }) => {
  civilCaseReference = await api_ga.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');
  await api_ga.defendantResponseSpecClaim(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_ga.claimantResponseClaimSpec(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO',
    'JUDICIAL_REFERRAL');
  console.log('Civil Case created for general application: ' + civilCaseReference);

  gaCaseReference = await api_ga.initiateGaForJudge(config.applicantSolicitorUser, civilCaseReference);

  console.log('Region 2 LA referring to Judge');
  if (config.runWAApiTest) {
    const actualLADecideOnApplicationTask = await api_ga.retrieveTaskDetails(config.tribunalCaseworkerWithRegionId2,
      gaCaseReference, config.waTaskIds.legalAdvisorDecideOnApplication);
    console.log('actualLADecideOnApplicationTask...', actualLADecideOnApplicationTask);
    wa.validateTaskInfo(actualLADecideOnApplicationTask, expectedLADecideOnApplicationBeforeSDOTask);
  }
  await I.login(config.tribunalCaseworkerWithRegionId2);
  await wa.verifyAdminTask(gaCaseReference, config.waTaskIds.legalAdvisorDecideOnApplication);
  await wa.goToEvent('Refer to Judge');
  await wa.referToJudge();
  await wa.verifyNoActiveTask(gaCaseReference);

  console.log('Changing court location');
  await api_ga.defendantResponseSpecClaim(config.defendantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO');
  await api_ga.claimantResponseClaimSpec(config.applicantSolicitorUser, 'FULL_DEFENCE', 'ONE_V_TWO',
    'JUDICIAL_REFERRAL');
  await waitForFinishedBusinessProcess(civilCaseReference, config.applicantSolicitorUser);
  await I.wait(5);

  console.log('Region 1 Judge List for a hearing');
  if (config.runWAApiTest) {
    const actualJudgeDecideOnApplicationTask = await api_ga.retrieveTaskDetails(config.judgeUser2WithRegionId2,
      gaCaseReference, config.waTaskIds.judgeDecideOnApplication);
    console.log('actualJudgeDecideOnApplicationTask...', actualJudgeDecideOnApplicationTask);
    wa.validateTaskInfo(actualJudgeDecideOnApplicationTask, expectedJudgeDecideOnApplicationAfterSDOTask);
  }
  await I.login(config.judgeUser2WithRegionId2);
  await wa.goToTask(gaCaseReference, config.waTaskIds.judgeDecideOnApplication);
  await I.judgeListForAHearingDecisionWA('listForAHearing', gaCaseReference, 'no', 'Hearing_order');
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, listForHearingStatus, config.judgeUser2WithRegionId2);
  await wa.verifyNoActiveTask(gaCaseReference);

  console.log('Region 1 HCA admin review scheduled Application Hearing');
  if (config.runWAApiTest) {
    const actualScheduleApplicationHearingTask = await api_ga.retrieveTaskDetails(config.hearingCenterAdminWithRegionId1,
      gaCaseReference, config.waTaskIds.scheduleApplicationHearing);
    console.log('actualScheduleApplicationHearingTask...', actualScheduleApplicationHearingTask);
    wa.validateTaskInfo(actualScheduleApplicationHearingTask, expectedScheduleAppHearingAfterSDOTask);
  }
  await I.login(config.hearingCenterAdminWithRegionId1);
  await wa.goToTask(gaCaseReference, config.waTaskIds.scheduleApplicationHearing);
  await I.fillHearingNotice(gaCaseReference, 'claimAndDef', 'basildon', 'VIDEO');
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, states.HEARING_SCHEDULED.id, config.hearingCenterAdminWithRegionId1);
  console.log('Hearing Notice created for: ' + gaCaseReference);
  await wa.verifyNoActiveTask(gaCaseReference);
});

AfterSuite(async ({api_ga}) => {
  await api_ga.cleanUp();
});
