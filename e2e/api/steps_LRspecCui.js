const config = require('../config.js');
const deepEqualInAnyOrder = require('deep-equal-in-any-order');
const chai = require('chai');

chai.use(deepEqualInAnyOrder);
chai.config.truncateThreshold = 0;
const {expect, assert} = chai;

const {waitForFinishedBusinessProcess} = require('../api/testingSupport');
const {assignCaseRoleToUser, addUserCaseMapping, unAssignAllUsers} = require('./caseRoleAssignmentHelper');
const apiRequest = require('./apiRequest.js');
const claimData = require('../fixtures/events/createClaimSpec.js');
const claimDataSpecFastLRvLiP = require('../fixtures/events/cui/createClaimSpecFastTrackCui.js');
const claimDataSpecIntLRvLiP = require('../fixtures/events/cui/createClaimSpecIntermediateTrackCui.js');
const claimDataSpecMultiLRvLiP = require('../fixtures/events/cui/createClaimSpecMultiTrackCui.js');
const claimDataSpecSmallLRvLiP = require('../fixtures/events/cui/createClaimSpecSmallCui.js');
const createClaimLipClaimant = require('../fixtures/events/cui/createClaimUnrepresentedClaimant');
const defendantResponse = require('../fixtures/events/cui/defendantResponseCui.js');
const mediationUnsuccessful = require('../fixtures/events/cui/unsuccessfulMediationCui.js');
const expectedEvents = require('../fixtures/ccd/expectedEventsLRSpec.js');
const nonProdExpectedEvents = require('../fixtures/ccd/nonProdExpectedEventsLRSpec.js');
const testingSupport = require('./testingSupport');
const {dateNoWeekends, dateNoWeekendsBankHolidayNextDay, date} = require('./dataHelper');
const {uploadDocument, checkOtherRemedyEnabled} = require('./testingSupport');
const {adjustCaseSubmittedDateForCarm} = require('../helpers/carmHelper');
const {fetchCaseDetails} = require('./apiRequest');
const lipClaimantResponse = require('../fixtures/events/cui/lipClaimantResponse');
const discontinueClaimSpec = require('../fixtures/events/discontinueClaimSpec');
const sdoTracks = require('../fixtures/events/createSDO.js');
const sdoTracksOtherRemedy = require('../fixtures/events/createSDOOtherRemedy.js');
const hearingScheduled = require('../fixtures/events/scheduleHearing');
const evidenceUploadApplicant = require('../fixtures/events/evidenceUploadApplicant');
const evidenceUploadRespondent = require('../fixtures/events/evidenceUploadRespondent');
const requestForReconsideration = require('../fixtures/events/requestForReconsideration');
const trialReadiness = require('../fixtures/events/trialReadiness.js');
const lodash = require('lodash');
const createFinalOrder = require('../fixtures/events/finalOrder.js');
const judgeDecisionToReconsiderationRequest = require('../fixtures/events/judgeDecisionOnReconsiderationRequest');
const stayCase = require('../fixtures/events/stayCase');
const manageStay = require('../fixtures/events/manageStay');
const dismissCase = require('../fixtures/events/dismissCase');
const { toJSON } = require('lodash/seq');
const sendAndReplyMessage = require('../fixtures/events/sendAndReplyMessages');
const judgmentMarkPaidInFull = require('../fixtures/events/cui/judgmentMarkPaidInFullCui');
const judgmentOnline1v1Spec = require('../fixtures/events/judgmentOnline1v1Spec');


let caseId, eventName;
let caseData = {};
let bufferAmountSnapshot = {};
let lastGrantProcessInstanceId = null; // grant camunda process instance, captured during verifyJudgmentBufferIssued polling

// Expected activeJudgment.orderedAmount (pence) the buffer freezes at DJ request; mirrors InterestCalculator.
// Deterministic only when the interest "to" date is fixed (UNTIL_CLAIM_SUBMIT_DATE).
const daysBetween = (fromDate, toDate) => {
  const p = (s) => String(s).slice(0, 10).split('-').map(Number); // dates may arrive as full ISO timestamps
  const [fy, fm, fd] = p(fromDate);
  const [ty, tm, td] = p(toDate);
  const ms = Date.UTC(ty, tm - 1, td) - Date.UTC(fy, fm - 1, fd);
  if (Number.isNaN(ms)) {
    throw new Error(`daysBetween: unparseable date(s) from='${fromDate}' to='${toDate}'`);
  }
  const n = Math.floor(ms / 86400000);
  return n > 0 ? n : 0; // 0 when to-date is not after from-date
};

const expectedOrderedAmountPence = (claimPounds, ratePct, fromDate, toDate, partialPaymentPounds = 0) => {
  const days = daysBetween(fromDate, toDate);
  const interestPerDay = Math.round((claimPounds * (ratePct / 100) / 365) * 100 + 1e-9) / 100; // per day, HALF_UP 2dp
  const interest = interestPerDay * days;
  const debt = claimPounds + interest - partialPaymentPounds;
  return String(Math.round(debt * 100));
};

const data = {
  CREATE_CLAIM: (scenario) => claimData.createClaim(scenario),
  CREATE_SPEC_CLAIM_FASTTRACK: (scenario) => claimDataSpecFastLRvLiP.createClaim(scenario),
  CREATE_SPEC_CLAIM_INTTRACK: (scenario) => claimDataSpecIntLRvLiP.createClaim(scenario),
  CREATE_SPEC_CLAIM_MULTITRACK: (scenario) => claimDataSpecMultiLRvLiP.createClaim(scenario),
  CREATE_SPEC_CLAIM: (scenario, withInterest = false) => claimDataSpecSmallLRvLiP.createClaim(withInterest),
  DEFENDANT_RESPONSE: (response) => require('../fixtures/events/defendantResponseSpecCui.js').respondToClaim(response),
  CLAIMANT_RESPONSE: (mpScenario, citizenDefendantResponse, freeMediation, carmEnabled) => require('../fixtures/events/claimantResponseSpecCui.js').claimantResponse(mpScenario, citizenDefendantResponse, freeMediation, carmEnabled),
  CLAIMANT_RESPONSE_INTERMEDIATE_CLAIM: (response, hasLip) => require('../fixtures/events/claimantResponseIntermediateClaimSpec.js').claimantResponse(response, hasLip),
  CLAIMANT_RESPONSE_MULTI_CLAIM: (response, hasLip) => require('../fixtures/events/claimantResponseMultiClaimSpec.js').claimantResponse(response, hasLip),
  REQUEST_JUDGEMENT: (mpScenario) => require('../fixtures/events/requestJudgementSpecCui.js').response(mpScenario),
  INFORM_AGREED_EXTENSION_DATE: () => require('../fixtures/events/informAgreeExtensionDateSpec.js'),
  EXTEND_RESPONSE_DEADLINE_DATE: () => require('../fixtures/events/extendResponseDeadline.js'),
  DISCONTINUE_CLAIM: (mpScenario) => discontinueClaimSpec.discontinueClaim(mpScenario),
  CREATE_SDO: (userInput) => sdoTracks.createSDOSmallWODamageSumInPerson(userInput),
  CREATE_SDO_FAST_TRACK: (userInput) => sdoTracks.createSDOFastTrackSpec(userInput),
  CREATE_SDO_OTHER_REMEDY: (userInput) => sdoTracksOtherRemedy.createSDOSmallWODamageSumInPerson(userInput),
  CREATE_SDO_FAST_TRACK_OTHER_REMEDY: (userInput) => sdoTracksOtherRemedy.createSDOFastTrackSpec(userInput),
  HEARING_SCHEDULED: (allocatedTrack) => hearingScheduled.scheduleHearingForTrialReadiness(allocatedTrack),
  HEARING_SCHEDULED_CUI: (allocatedTrack) => hearingScheduled.scheduleHearingForCui(allocatedTrack),
  EVIDENCE_UPLOAD_CLAIMANT: (mpScenario, document) => evidenceUploadApplicant.createClaimantSmallClaimsEvidenceUpload(document),
  EVIDENCE_UPLOAD_DEFENDANT: (mpScenario, document) => evidenceUploadRespondent.createDefendantSmallClaimsEvidenceUpload(document),
  REQUEST_FOR_RECONSIDERATION: (userType) => requestForReconsideration.createRequestForReconsiderationSpecCitizen(userType),
  TRIAL_READINESS: (user) => trialReadiness.confirmTrialReady(user),
  FINAL_ORDERS: (finalOrdersRequestType, dayPlus0, dayPlus7, dayPlus14, dayPlus21) => createFinalOrder.requestFinalOrder(finalOrdersRequestType, dayPlus0, dayPlus7, dayPlus14, dayPlus21),
  DECISION_ON_RECONSIDERATION_REQUEST: (decisionSelection)=> judgeDecisionToReconsiderationRequest.judgeDecisionOnReconsiderationRequestSpec(decisionSelection),
  STAY_CASE: () => stayCase.stayCaseSpec(),
  MANAGE_STAY_UPDATE: () => manageStay.manageStayRequestUpdate(),
  MANAGE_STAY_LIFT: () => manageStay.manageStayLiftStay(),
  DISMISS_CASE: () => dismissCase.dismissCase(),
  SEND_MESSAGE: () => sendAndReplyMessage.sendMessageLr(),
  REPLY_MESSAGE: (messageCode, messageLabel) => sendAndReplyMessage.replyMessageLr(messageCode, messageLabel)
};

const eventData = {
  defendantResponses: {
    ONE_V_ONE: {
      FULL_DEFENCE: data.DEFENDANT_RESPONSE('FULL_DEFENCE'),
      FULL_ADMISSION: data.DEFENDANT_RESPONSE('FULL_ADMISSION'),
      PART_ADMISSION: data.DEFENDANT_RESPONSE('PART_ADMISSION'),
      COUNTER_CLAIM: data.DEFENDANT_RESPONSE('COUNTER_CLAIM'),
      REQUEST_JUDGEMENT: data.DEFENDANT_RESPONSE('REQUEST_JUDGEMENT'),
    }
  },
  claimantResponses: {
    ONE_V_ONE: {
      FULL_DEFENCE: data.CLAIMANT_RESPONSE('FULL_DEFENCE'),
      FULL_DEFENCE_CITIZEN_DEFENDANT:  {
        Yes: data.CLAIMANT_RESPONSE('FULL_DEFENCE', true, 'Yes'),
        No: data.CLAIMANT_RESPONSE('FULL_DEFENCE', true, 'No'),
      },
      FULL_DEFENCE_CITIZEN_DEFENDANT_MEDIATION: {
        Yes: data.CLAIMANT_RESPONSE('FULL_DEFENCE', true, 'Yes', true),
        No: data.CLAIMANT_RESPONSE('FULL_DEFENCE', true, 'No', true)
      },
      FULL_DEFENCE_CITIZEN_DEFENDANT_INTERMEDIATE: {
        No: data.CLAIMANT_RESPONSE_INTERMEDIATE_CLAIM('FULL_DEFENCE', true)
      },
      FULL_DEFENCE_CITIZEN_DEFENDANT_MULTI: {
        No: data.CLAIMANT_RESPONSE_MULTI_CLAIM('FULL_DEFENCE', true)
      },
      FULL_ADMISSION: data.CLAIMANT_RESPONSE('FULL_ADMISSION'),
      PART_ADMISSION: data.CLAIMANT_RESPONSE('PART_ADMISSION'),
      COUNTER_CLAIM: data.CLAIMANT_RESPONSE('COUNTER_CLAIM'),
      PART_ADMISSION_SETTLE: data.CLAIMANT_RESPONSE('PART_ADMISSION_SETTLE'),
    }
  },
  requestJudgement: {
    ONE_V_ONE: {
      REQUEST_JUDGEMENT: data.REQUEST_JUDGEMENT('REQUEST_JUDGEMENT'),
    }
  }
};

module.exports = {

  /**
   * Creates a claim
   *
   * @param user user to create the claim
   * @return {Promise<void>}
   */
  createClaimWithRepresentedRespondent: async (user,scenario = 'ONE_V_ONE') => {

    eventName = 'CREATE_CLAIM_SPEC';
    caseId = null;
    caseData = {};

    let createClaimData  = {};

    createClaimData = data.CREATE_CLAIM(scenario);
    //==============================================================

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    for (let pageId of Object.keys(createClaimData.userInput)) {
      await assertValidData(createClaimData, pageId);
    }

    await assertSubmittedEvent('PENDING_CASE_ISSUED');

    await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'CASE_ISSUED');

    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');
  },

  createClaimWithUnrepresentedClaimant: async (user, claimType = 'SmallClaims', carmEnabled = false, typeOfData = '', isMintiCase = false) => {
    console.log('Starting to create claim');
    let payload = {};
    await apiRequest.setupTokens(user);
    let userId = await apiRequest.fetchUserId();

    if (claimType === 'FastTrack') {
      console.log('FastTrack claim...');
      payload = createClaimLipClaimant.createClaimUnrepresentedClaimant('15000', userId);
    }
    if ( claimType === 'Request for reconsideration track') {
      console.log('Request for reconsideration claim');
      payload = createClaimLipClaimant.createClaimUnrepresentedClaimant('500', userId);
    }
    if (claimType === 'SmallClaims') {
      console.log('SmallClaim...');
      payload = createClaimLipClaimant.createClaimUnrepresentedClaimant('1500', userId, typeOfData);
    }
    if (claimType === 'INTERMEDIATE') {
      console.log('Intermediate claim...');
      payload = createClaimLipClaimant.createClaimUnrepresentedClaimant('99999', userId);
    }

    caseId = await apiRequest.startCreateCaseForCitizen(payload);
    await waitForFinishedBusinessProcess(caseId);
    console.log('Claim submitted');

    // issue claim
    payload = createClaimLipClaimant.issueClaim();
    await apiRequest.startCreateCaseForCitizen(payload, caseId);
    await waitForFinishedBusinessProcess(caseId);
    console.log('Claim issued');
    await assignCaseRoleToUser(caseId, 'DEFENDANT', config.defendantCitizenUser2);
    await adjustCaseSubmittedDateForCarm(caseId, carmEnabled, (claimType === 'INTERMEDIATE' || claimType === 'MULTI'));
    return caseId;
  },

  retrieveTaskDetails: async (user, caseNumber, taskId) => {
    return apiRequest.fetchTaskDetails(user, caseNumber, taskId);
  },

  assignTaskToUser: async (user, taskId) => {
    return apiRequest.taskActionByUser(user, taskId, 'claim');
  },

  createSpecifiedClaimWithUnrepresentedRespondent: async (user, multipartyScenario, claimType, carmEnabled = false, withInterest = false) => {
    console.log(' Creating specified claim' + (withInterest ? ' WITH 8% interest' : ''));
    eventName = 'CREATE_CLAIM_SPEC';
    caseId = null;
    caseData = {};
    let createClaimSpecData;
    if (claimType === 'MULTI') {
      console.log('Creating MultiTrack claim...');
      createClaimSpecData = data.CREATE_SPEC_CLAIM_MULTITRACK(multipartyScenario);
    } else if (claimType === 'INTERMEDIATE') {
      console.log('Creating IntermediateTrack claim...');
      createClaimSpecData = data.CREATE_SPEC_CLAIM_INTTRACK(multipartyScenario);
    } else if (claimType === 'FastTrack') {
      console.log('Creating FastTrack claim...');
      createClaimSpecData = data.CREATE_SPEC_CLAIM_FASTTRACK(multipartyScenario);
    } else {
      console.log('Creating small claims...');
      createClaimSpecData = data.CREATE_SPEC_CLAIM(multipartyScenario, withInterest);
    }

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    for (let pageId of Object.keys(createClaimSpecData.userInput)) {
      await assertValidData(createClaimSpecData, pageId);
    }

    await assertSubmittedEvent('PENDING_CASE_ISSUED');
    await waitForFinishedBusinessProcess(caseId);

    await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
    claimData.serviceUpdateDto(caseId, 'paid'));
    console.log('Service request update sent to callback URL');

    await waitForFinishedBusinessProcess(caseId);
    if (claimType !== 'pinInPost') {
      await assignCaseRoleToUser(caseId, 'DEFENDANT', config.defendantCitizenUser2);
    }

    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');

    await adjustCaseSubmittedDateForCarm(caseId, carmEnabled, (claimType === 'INTERMEDIATE' || claimType === 'MULTI'));

    return caseId;
  },

  // Pure-API DJ request (no browser): drives the DEFAULT_JUDGEMENT_SPEC event so the interest VALUE can be
  // asserted at PENDING_ISSUE without Playwright, the scheduler, or a testing-support PUT. With the buffer flag
  // on, the event parks at JUDGMENT_REQUESTED. The defendant dynamic-list code is read from the started event.
  requestDefaultJudgmentSpecViaApi: async (user) => {
    const djFixture = require('../fixtures/events/defaultJudgmentSpec.js');
    await apiRequest.setupTokens(user);
    eventName = 'DEFAULT_JUDGEMENT_SPEC';
    caseData = await apiRequest.startEvent(eventName, caseId);
    caseData = update(caseData, {
      claimIssuedPBADetails: {
        applicantsPbaAccounts: {
          value: {code: '66b21c60-aed1-11ed-8aa3-494efce63912', label: 'PBAFUNC12345'},
          list_items: [
            {code: '66b21c60-aed1-11ed-8aa3-494efce63912', label: 'PBAFUNC12345'},
            {code: '66b21c61-aed1-11ed-8aa3-494efce63912', label: 'PBA0078095'},
          ],
        },
        fee: {calculatedAmountInPence: '8000', code: 'FEE0205', version: '6'},
        serviceRequestReference: '2023-1676644996295',
      },
    });
    const realCode = (((caseData.defendantDetailsSpec || {}).list_items || [{}])[0] || {}).code;
    for (const pageId of Object.keys(djFixture.userInput)) {
      const pageData = JSON.parse(JSON.stringify(djFixture.userInput[pageId]));
      if (pageId === 'defendantDetailsSpec' && realCode) {
        pageData.defendantDetailsSpec.value.code = realCode;
        pageData.defendantDetailsSpec.list_items = [{code: realCode, label: pageData.defendantDetailsSpec.value.label}];
      }
      caseData = update(caseData, pageData);
      const response = await apiRequest.validatePage(eventName, pageId, caseData, caseId);
      assert.equal(response.status, 200, `DJ page '${pageId}' validation failed (status ${response.status})`);
      const body = clearDataForSearchCriteria(await response.json());
      if (pageId === 'defendantDetailsSpec') {
        delete body.data.registrationTypeRespondentOne;
        delete body.data.registrationTypeRespondentTwo;
      }
      caseData = update(caseData, body.data);
    }
    await assertSubmittedEvent('JUDGMENT_REQUESTED');
    console.log(`DJ requested via API -> JUDGMENT_REQUESTED (case ${caseId})`);
  },

  // Pure-API SET ASIDE (post-issue lifecycle). The judgment moves out of activeJudgment into historicJudgment;
  // JUDGE_ORDER -> state SET_ASIDE, JUDGMENT_ERROR -> state SET_ASIDE_ERROR. Fires from All_FINAL_ORDERS_ISSUED.
  setAsideJudgmentViaApi: async (user, reason = 'JUDGE_ORDER', orderType = 'ORDER_AFTER_APPLICATION') => {
    const fixture = require('../fixtures/events/judgmentOnline1v1Spec.js').setAsideJudgment(reason, orderType);
    await apiRequest.setupTokens(user);
    eventName = 'SET_ASIDE_JUDGMENT';
    caseData = await apiRequest.startEvent(eventName, caseId);
    for (const pageId of Object.keys(fixture.userInput)) {
      await assertValidData(fixture, pageId);
    }
    await assertSubmittedEvent('All_FINAL_ORDERS_ISSUED');
    await waitForFinishedBusinessProcess(caseId);
    console.log(`Set aside via API (reason=${reason}, orderType=${orderType}, case ${caseId})`);
  },

  // Pure-API EDIT JUDGMENT (post-issue lifecycle): activeJudgment ISSUED -> MODIFIED. Fires from All_FINAL_ORDERS_ISSUED.
  editJudgmentViaApi: async (user) => {
    const fixture = require('../fixtures/events/judgmentOnline1v1Spec.js').editJudgment('DETERMINATION_OF_MEANS', 'PAY_BY_DATE');
    await apiRequest.setupTokens(user);
    eventName = 'EDIT_JUDGMENT';
    caseData = await apiRequest.startEvent(eventName, caseId);
    for (const pageId of Object.keys(fixture.userInput)) {
      await assertValidData(fixture, pageId);
    }
    await assertSubmittedEvent('All_FINAL_ORDERS_ISSUED');
    await waitForFinishedBusinessProcess(caseId);
    console.log(`Edited judgment via API (case ${caseId})`);
  },

  // Pure-API MARK PAID IN FULL (post-issue lifecycle). Payment is recorded today - a future date is rejected (422).
  // Whether it lands CANCELLED or SATISFIED is decided by DAYS.between(issueDate, paymentDate) vs the issue month's
  // length, so backdate the issue date first (amendJudgmentIssueDate) to exercise the SATISFIED branch.
  markJudgmentPaidViaApi: async (user) => {
    const today = new Date().toISOString().slice(0, 10);
    await apiRequest.setupTokens(user);
    eventName = 'JUDGMENT_PAID_IN_FULL';
    caseData = await apiRequest.startEvent(eventName, caseId);
    caseData = update(caseData, {joJudgmentPaidInFull: {dateOfFullPaymentMade: today, confirmFullPaymentMade: ['CONFIRMED']}});
    const response = await apiRequest.validatePage(eventName, 'MarkJudgmentPaidInFull', caseData, caseId);
    assert.equal(response.status, 200, `MarkJudgmentPaidInFull validation failed (status ${response.status})`);
    caseData = update(caseData, clearDataForSearchCriteria(await response.json()).data);
    await assertSubmittedEvent('All_FINAL_ORDERS_ISSUED');
    await waitForFinishedBusinessProcess(caseId);
    console.log(`Judgment marked paid via API: paidDate=${today}, case ${caseId}`);
  },

  // Backdate activeJudgment.issueDate (read-modify-PUT the whole object so the other frozen fields survive) so that
  // a payment recorded today is more than a month after issue -> SATISFIED rather than CANCELLED.
  amendJudgmentIssueDate: async (daysAgo) => {
    const cd = (await apiRequest.fetchCaseDetails(config.adminUser, caseId)).case_data;
    const aj = {...cd.activeJudgment};
    const backdated = new Date(Date.now() - daysAgo * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);
    aj.issueDate = backdated;
    await testingSupport.updateCaseData(caseId, {activeJudgment: aj});
    console.log(`activeJudgment.issueDate backdated to ${backdated} (${daysAgo}d ago), case ${caseId}`);
  },

  // Backdate issueDate so a payment recorded today lands on the CANCELLED/SATISFIED boundary, which is the issue
  // month's length (CANCELLED when days-between <= month length, SATISFIED when greater). offset 0 sits on the
  // boundary (CANCELLED), offset +1 one day past it (SATISFIED).
  amendJudgmentIssueDateBoundary: async (offsetVsMonthLen) => {
    const today = new Date(); today.setUTCHours(0, 0, 0, 0);
    for (let d = 27; d <= 35; d++) {
      const cand = new Date(today.getTime() - d * 24 * 60 * 60 * 1000);
      const monthLen = new Date(Date.UTC(cand.getUTCFullYear(), cand.getUTCMonth() + 1, 0)).getUTCDate();
      if (d === monthLen + offsetVsMonthLen) {
        const iso = cand.toISOString().slice(0, 10);
        const cd = (await apiRequest.fetchCaseDetails(config.adminUser, caseId)).case_data;
        const aj = {...cd.activeJudgment, issueDate: iso};
        await testingSupport.updateCaseData(caseId, {activeJudgment: aj});
        const expected = d > monthLen ? 'SATISFIED' : 'CANCELLED';
        console.log(`Boundary: issueDate->${iso} (daysBetween=${d}, monthLen=${monthLen}, offset=${offsetVsMonthLen} => expect ${expected}), case ${caseId}`);
        return expected;
      }
    }
    throw new Error(`could not find a boundary backdate for offset ${offsetVsMonthLen}`);
  },

  // Set a future response deadline + agreed extension so the case is not yet DJ-eligible (an active/extended
  // deadline blocks the DJ request at the about-to-start eligibility check).
  setResponseDeadlineExtension: async (user, daysAhead = 60) => {
    const future = new Date(Date.now() + daysAhead * 24 * 60 * 60 * 1000);
    const futureTs = future.toISOString().slice(0, 19); // YYYY-MM-DDTHH:mm:ss
    const futureDate = future.toISOString().slice(0, 10);
    await testingSupport.updateCaseData(caseId, {
      respondent1ResponseDeadline: futureTs,
      respondentSolicitor1AgreedDeadlineExtension: futureDate,
    });
    console.log(`Response deadline extended to ${futureTs} (agreed extension ${futureDate}), case ${caseId}`);
  },

  // Assert the DJ request is rejected by the eligibility guard (the about-to-start event-token call fails) and the
  // case never enters JUDGMENT_REQUESTED.
  requestDefaultJudgmentSpecExpectingRejection: async (user) => {
    await apiRequest.setupTokens(user);
    let rejected = false;
    try {
      await apiRequest.startEvent('DEFAULT_JUDGEMENT_SPEC', caseId);
    } catch (e) {
      rejected = true;
      console.log(`DJ request rejected at eligibility (extension in force): ${String(e.message).slice(0, 120)}`);
    }
    const st = (await apiRequest.fetchCaseDetails(config.adminUser, caseId)).state;
    assert.notEqual(st, 'JUDGMENT_REQUESTED',
      `case must NOT enter the buffer while an active/extended response deadline is in force, but state=${st}`);
    assert.isTrue(rejected, 'expected the DJ request to be blocked by the response-deadline/extension eligibility guard');
  },

  // On a non-grant buffer exit (defence or offline), the judgment must never have been issued or registered with
  // RTL. activeJudgment may be parked (PENDING_ISSUE) or cleared (null); either is fine as long as it was never
  // ISSUED and rtlState was never set to 'R'.
  verifyJudgmentNeverRegistered: async () => {
    const cd = (await apiRequest.fetchCaseDetails(config.adminUser, caseId)).case_data || {};
    const aj = cd.activeJudgment;
    if (aj) {
      assert.notEqual(aj.state, 'ISSUED', `expected judgment never ISSUED on this branch, got ${aj.state}`);
      assert.notEqual(aj.isRegisterWithRTL, 'Yes', `expected isRegisterWithRTL!='Yes' (never registered), got ${aj.isRegisterWithRTL}`);
      assert.notEqual(aj.rtlState, 'R', `expected rtlState!='R' (never registered with RTL), got ${aj.rtlState}`);
    }
    assert.notEqual(cd.joIsLiveJudgmentExists, 'Yes', `expected joIsLiveJudgmentExists!='Yes', got ${cd.joIsLiveJudgmentExists}`);
    console.log(`Judgment never registered on this branch: activeJudgment.state=${aj && aj.state}, isRegisterWithRTL=${aj && aj.isRegisterWithRTL}, rtlState=${aj && aj.rtlState}`);
  },

  // Post-issue RPA/CJES check: the grant camunda process completed (its RPA + CJES tasks ran) and the RPA/CJES
  // payloads mapped from the issued case are correct (registration type for rtlState='R', judgment amounts,
  // defendant). The external SendGrid email / CJES POST have no case_data write-back and are covered by the
  // civil-service unit tests.
  verifyPostIssueDelivery: async () => {
    // (1) The grant process (containing the RPA + CJES service tasks) reached COMPLETED.
    if (lastGrantProcessInstanceId) {
      const procs = await testingSupport.getCamundaProcesses(lastGrantProcessInstanceId, null, null);
      console.log(`Grant process ${lastGrantProcessInstanceId}: ${JSON.stringify((procs || []).map(p => ({k: p.processDefinitionKey, s: p.state})))}`);
      assert.isTrue(Array.isArray(procs) && procs.length > 0 && procs[0].state === 'COMPLETED',
        `expected grant camunda process ${lastGrantProcessInstanceId} COMPLETED (RPA+CJES tasks ran), got ${JSON.stringify(procs)}`);
    } else {
      console.log('WARN: no grant processInstanceId captured - process completion check skipped');
    }
    const cd = (await apiRequest.fetchCaseDetails(config.adminUser, caseId)).case_data;
    cd.ccdCaseReference = caseId; // the mapper endpoints map from CaseData and need the case reference set
    // (2) CJES/RTL payload mapped from the issued case: references the case + carries the judgment total.
    const cjes = await testingSupport.getRtlActiveJudgment(cd);
    console.log(`CJES/RTL payload: ${cjes}`);
    assert.include(cjes, String(caseId), 'expected the CJES payload to reference the issued case');
    assert.include(cjes, String(cd.activeJudgment.totalAmount), 'expected the CJES payload to carry the judgment total');
    // (3) RPA payload mapped from the issued case (well-formed, non-empty robotics JSON).
    const rpa = String(await testingSupport.getRpaJsonSpec(cd));
    console.log(`RPA payload (first 600): ${rpa.slice(0, 600)}`);
    assert.isTrue(rpa.length > 100 && rpa.trim().startsWith('{'), 'expected a non-empty RPA robotics JSON payload');
  },

  verifyActiveJudgmentState: async (expectedState) => {
    const aj = (await apiRequest.fetchCaseDetails(config.adminUser, caseId)).case_data.activeJudgment || {};
    console.log(`activeJudgment.state = ${aj.state} (expected ${expectedState}), case ${caseId}`);
    assert.equal(aj.state, expectedState, `expected activeJudgment.state=${expectedState}, got ${aj.state}`);
  },

  // Log the current ccd state + activeJudgment state without asserting (useful when the exact state depends on the
  // branch, e.g. defence clears vs offline parks the judgment).
  reportState: async (label = '') => {
    const r = await apiRequest.fetchCaseDetails(config.adminUser, caseId);
    const aj = r.case_data && r.case_data.activeJudgment;
    console.log(`OBSERVE ${label}: ccdState=${r.state}, activeJudgment.state=${aj && aj.state}, joIsLiveJudgmentExists=${r.case_data && r.case_data.joIsLiveJudgmentExists}`);
    return r.state;
  },

  // Some lifecycle events (set aside, paid in full) move the judgment out of activeJudgment into historicJudgment,
  // so scan both and assert at least one judgment is in the expected terminal state.
  verifyAnyJudgmentState: async (expectedState) => {
    const cd = (await apiRequest.fetchCaseDetails(config.adminUser, caseId)).case_data;
    const active = cd.activeJudgment;
    const historic = (cd.historicJudgment || []).map(e => e.value);
    const states = [];
    if (active) states.push(active.state);
    historic.forEach(h => states.push(h.state));
    console.log(`judgment states: active=${active ? active.state : 'none'}, historic=[${historic.map(h => h.state).join(',')}] (expected ${expectedState}), case ${caseId}`);
    assert.ok(states.includes(expectedState),
      `expected a judgment in state ${expectedState}, got active=${active ? active.state : 'none'}, historic=[${historic.map(h => h.state).join(',')}]`);
  },

  performCitizenDefendantResponse: async (user, caseId, claimType = 'SmallClaims', carmEnabled = false, typeOfResponse = '') => {
    let eventName = 'DEFENDANT_RESPONSE_CUI';
    let payload = {};
    if (claimType === 'FastTrack') {
      console.log('FastTrack claim...');
      payload = defendantResponse.createDefendantResponse('15000', carmEnabled);
    }
    if (claimType === 'Request for reconsideration track') {
      console.log('Request for reconsideration claim');
      payload = defendantResponse.createDefendantResponse('500', carmEnabled);
    }
    if (claimType === 'SmallClaims') {
      console.log('SmallClaim...');
      payload = defendantResponse.createDefendantResponse('1500', carmEnabled, typeOfResponse);
    }
    if (claimType === 'SmallClaimPartAdmit') {
      console.log('SmallClaim part admit lip defendant response...');
      payload = defendantResponse.createDefendantResponseSmallClaimPartAdmitCarm();
    }
    if (claimType === 'INTERMEDIATE') {
      console.log('Intermediate lip defendant response...');
      payload = defendantResponse.createDefendantResponseIntermediateTrack();
    }
    if (claimType === 'MULTI') {
      console.log('Multi lip defendant response...');
      payload = defendantResponse.createDefendantResponseMultiTrack();
    }

    //console.log('The payload : ' + payload);
    await apiRequest.setupTokens(user);
    await apiRequest.startEventForCitizen(eventName, caseId, payload);
    await waitForFinishedBusinessProcess(caseId);
  },

  performCitizenClaimantResponse: async (user, caseId, expectedEndState, carmEnabled, typeOfData) => {
    let eventName = 'CLAIMANT_RESPONSE_CUI';
    let payload = lipClaimantResponse.claimantResponse(carmEnabled, typeOfData);
    if (typeOfData === 'partadmit') {
      payload = lipClaimantResponse.claimantResponsePartAdmitRejectCarm();
    }

    await apiRequest.setupTokens(user);
    await apiRequest.startEventForCitizen(eventName, caseId, payload, expectedEndState);
    await waitForFinishedBusinessProcess(caseId);

    if (typeOfData === 'FA_ACCEPT_CCJ' || typeOfData === 'PA_ACCEPT_CCJ') {
      expectedEndState = 'All_FINAL_ORDERS_ISSUED';
    }
    if (expectedEndState) {
      const response = await apiRequest.fetchCaseDetails(config.adminUser, caseId);
      assert.equal(response.state, expectedEndState);
    }
  },

  judgmentPaidInFullCui: async (user, caseId) => {
    let eventName = 'JUDGMENT_PAID_IN_FULL';
    let payload = judgmentMarkPaidInFull.markJudgmentPaidInFull();
    await apiRequest.setupTokens(user);
    await apiRequest.startEventForCitizen(eventName, caseId, payload);
    await waitForFinishedBusinessProcess(caseId);
  },

  markJudgmentPaid: async (user) => {
    console.log(`case in All final orders issued ${caseId}`);
    await apiRequest.setupTokens(user);
    eventName = 'JUDGMENT_PAID_IN_FULL';
    caseData = await apiRequest.startEvent(eventName, caseId);
    let payload = judgmentOnline1v1Spec.markJudgmentPaidInFull();
    for (let pageId of Object.keys(payload.userInput)) {
      await assertValidData(payload, pageId);
    }
    await assertSubmittedEvent('All_FINAL_ORDERS_ISSUED', {
      header: '# Judgment marked as paid in full',
      body: 'The judgment has been marked as paid in full'
    }, true);
    await waitForFinishedBusinessProcess(caseId);
  },

  createSDO: async (user, response = 'CREATE_DISPOSAL') => {
    console.log('SDO for case id ' + caseId);
    await apiRequest.setupTokens(user);
    let disposalData;
    if (response === 'UNSUITABLE_FOR_SDO') {
      eventName = 'NotSuitable_SDO';
    } else if (response === 'CREATE_FAST') {
      eventName = 'CREATE_SDO';
      disposalData = data.CREATE_SDO_FAST_TRACK();
      if(await checkOtherRemedyEnabled())
        disposalData = data.CREATE_SDO_FAST_TRACK_OTHER_REMEDY();
    } else {
      eventName = 'CREATE_SDO';
      disposalData = data.CREATE_SDO();
      if(await checkOtherRemedyEnabled())
        disposalData = data.CREATE_SDO_OTHER_REMEDY();
    }

    caseData = await apiRequest.startEvent(eventName, caseId);

    for (let pageId of Object.keys(disposalData.valid)) {
      await assertValidData(disposalData, pageId);
    }

    if (response === 'UNSUITABLE_FOR_SDO') {
      await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM', null, false);
    } else {
      await assertSubmittedEvent('CASE_PROGRESSION', null, false);
    }

    await waitForFinishedBusinessProcess(caseId);
  },

  judgeDecisionOnReconsiderationRequest: async (user, decisionOption) => {
    console.log('judgeDecisionOnReconsiderationRequest for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'DECISION_ON_RECONSIDERATION_REQUEST';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    let disposalData = data.DECISION_ON_RECONSIDERATION_REQUEST(decisionOption);
    for (let pageId of Object.keys(disposalData.userInput)) {
      await assertValidData(disposalData, pageId);
    }
    await assertSubmittedEvent('CASE_PROGRESSION', {
      header: '# Response has been submitted',
      body: ''
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },


  evidenceUploadApplicant: async (user) => {
    await apiRequest.setupTokens(user);
    const document = await uploadDocument();
    let payload = data.EVIDENCE_UPLOAD_CLAIMANT('ONE_V_ONE', document);

    caseData = await apiRequest.startEventForCitizen(eventName, caseId, payload);
    await waitForFinishedBusinessProcess(caseId);
  },

  evidenceUploadDefendant: async (user) => {
    await apiRequest.setupTokens(user);
    const document = await uploadDocument();
    let payload = data.EVIDENCE_UPLOAD_DEFENDANT('ONE_V_ONE', document);

    caseData = await apiRequest.startEventForCitizen(eventName, caseId, payload);

    await waitForFinishedBusinessProcess(caseId);
  },

  requestForReconsiderationCitizen: async (user) => {
    await apiRequest.setupTokens(user);
    let payload;
    if (user === 'Claimant') {
      payload = data.REQUEST_FOR_RECONSIDERATION('Claimant');
    } else {
      payload = data.REQUEST_FOR_RECONSIDERATION('Defendant');
    }

    caseData = await apiRequest.startEventForCitizen(eventName, caseId, payload);

    await waitForFinishedBusinessProcess(caseId);

  },

  amendHearingDueDate: async (user) => {
    let hearingDueDate;
    hearingDueDate = {'hearingDueDate': '2022-01-10'};
    await testingSupport.updateCaseData(caseId, hearingDueDate, user);
  },

  hearingFeePaid: async (user) => {
    await apiRequest.setupTokens(user);

    await apiRequest.paymentUpdate(caseId, '/service-request-update',
      claimData.serviceUpdateDto(caseId, 'paid'));

    const response_msg = await apiRequest.hearingFeePaidEvent(caseId);
    assert.equal(response_msg.status, 200);
    console.log('Hearing Fee Paid');
  },

  trialReadinessCitizen: async (user) => {
    await apiRequest.setupTokens(user);

    let payload = data.TRIAL_READINESS(user);

    caseData = await apiRequest.startEventForCitizen(eventName, caseId, payload);

    await waitForFinishedBusinessProcess(caseId);

  },

  scheduleHearing: async (user, allocatedTrack = 'OTHER', claimType, expectedState = 'HEARING_READINESS') => {
    console.log('Hearing Scheduled for case id ' + caseId);
    await apiRequest.setupTokens(user);

    eventName = 'HEARING_SCHEDULED';

    caseData = await apiRequest.startEvent(eventName, caseId);
    delete caseData['SearchCriteria'];
    let scheduleData;
    if (claimType === 'CUI') {
      scheduleData = data.HEARING_SCHEDULED_CUI(allocatedTrack);
    } else {
      scheduleData = data.HEARING_SCHEDULED(allocatedTrack);
    }

    for (let pageId of Object.keys(scheduleData.valid)) {
      await assertValidData(scheduleData, pageId);
    }
    await assertSubmittedEvent(expectedState, null, false);
    await waitForFinishedBusinessProcess(caseId);
  },

  createFinalOrder: async (user, finalOrderRequestType) => {
    console.log(`case in Final Order ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'GENERATE_DIRECTIONS_ORDER';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    const dayPlus0 = await dateNoWeekendsBankHolidayNextDay(0);
    const dayPlus7 = await dateNoWeekendsBankHolidayNextDay(7);
    const dayPlus14 = await dateNoWeekendsBankHolidayNextDay(14);
    const dayPlus21 = await dateNoWeekendsBankHolidayNextDay(21);

    if (finalOrderRequestType === 'ASSISTED_ORDER') {
      await validateEventPages(data.FINAL_ORDERS('ASSISTED_ORDER', dayPlus0, dayPlus7, dayPlus14, dayPlus21));
    } else {
      await validateEventPages(data.FINAL_ORDERS('FREE_FORM_ORDER', dayPlus0, dayPlus7, dayPlus14, dayPlus21));
    }

    await waitForFinishedBusinessProcess(caseId);
  },


  mediationUnsuccessful: async (user, carmEnabled = false) => {
    eventName = 'MEDIATION_UNSUCCESSFUL';

    caseData = await apiRequest.startEvent(eventName, caseId);
    caseData = {...caseData, ...mediationUnsuccessful.unsuccessfulMediation(carmEnabled)};
    await apiRequest.setupTokens(user);
    await assertSubmittedEvent('JUDICIAL_REFERRAL');
    await waitForFinishedBusinessProcess(caseId);
    console.log('End of unsuccessful mediation');
  },

  cleanUp: async () => {
    await unAssignAllUsers();
  },

  getCaseId: async () => {
    console.log(`case created: ${caseId}`);
    return caseId;
  },

  verifyEventsAvailable: async (user, state) => {
    await assertCorrectEventsAreAvailableToUser(user, state);
  },

  verifyCaseState: async (expectedState) => {
    const response = await apiRequest.fetchCaseDetails(config.adminUser, caseId);
    assert.equal(response.state, expectedState,
      `Expected case state ${expectedState} but got ${response.state}`);
  },

  verifyActiveJudgmentCancelled: async () => {
    const response = await apiRequest.fetchCaseDetails(config.adminUser, caseId);
    const cd = response.case_data || {};
    const activeJudgment = cd.activeJudgment;
    if (activeJudgment !== null && activeJudgment !== undefined) {
      assert.equal(activeJudgment.state, 'CANCELLED',
        `Expected activeJudgment.state CANCELLED but got ${activeJudgment.state}`);
    } else {
      assert.notEqual(cd.joIsLiveJudgmentExists, 'Yes',
        'activeJudgment cleared but joIsLiveJudgmentExists still Yes - pending DJ not properly cancelled');
    }
  },

  // Asserts the exact orderedAmount (claim + interest). Deterministic only with UNTIL_CLAIM_SUBMIT_DATE
  // (to-date = submittedDate, pinned to 2024-10-10 by the buffer flow). expectZero: assert orderedAmount == claim.
  verifyJudgmentInterestValue: async (claimPounds, ratePct, fromDate, expectZero = false) => {
    const response = await apiRequest.fetchCaseDetails(config.adminUser, caseId);
    const cd = response.case_data || {};
    const aj = cd.activeJudgment || {};
    const submittedDate = String(cd.submittedDate || '').slice(0, 10);
    assert.match(submittedDate, /^\d{4}-\d{2}-\d{2}$/,
      `expected a parseable submittedDate, got '${cd.submittedDate}'`);
    const expectedPence = expectZero
      ? String(Math.round(claimPounds * 100))
      : expectedOrderedAmountPence(claimPounds, ratePct, fromDate, submittedDate);
    const days = daysBetween(fromDate, submittedDate);
    console.log(`Interest value check [case ${caseId}, state ${aj.state}]: from=${fromDate} to(submittedDate)=${submittedDate} days=${days} rate=${ratePct}% => expected orderedAmount=${expectedPence}p, actual=${aj.orderedAmount}p`);
    assert.equal(String(aj.orderedAmount), expectedPence,
      `orderedAmount mismatch: expected ${expectedPence}p (claim £${claimPounds} + ${ratePct}% interest over ${days} days to ${submittedDate}), got ${aj.orderedAmount}p`);
    return expectedPence;
  },

  verifyBufferStateInitialFields: async () => {
    const response = await apiRequest.fetchCaseDetails(config.adminUser, caseId);
    const cd = response.case_data || {};
    const aj = cd.activeJudgment;

    assert.ok(aj, 'Expected activeJudgment to exist in buffer state but got null/undefined');

    // State / RTL fields - prove no judgment issued/registered yet
    assert.equal(aj.state, 'PENDING_ISSUE', `expected activeJudgment.state=PENDING_ISSUE, got ${aj.state}`);
    assert.isTrue(aj.issueDate === null || aj.issueDate === undefined,
      `expected issueDate null/undefined, got ${aj.issueDate}`);
    assert.equal(aj.isRegisterWithRTL, 'No', `expected isRegisterWithRTL='No', got ${aj.isRegisterWithRTL}`);
    assert.isTrue(aj.rtlState === null || aj.rtlState === undefined,
      `expected rtlState null/undefined, got ${aj.rtlState}`);

    // The DJ-grant business process is what fires NOTIFY_RPA_DJ_SPEC (RPA) and SEND_JUDGMENT_DETAILS_CJES (CJES).
    // On the buffer branch civil-service does not start that business process, so its camundaEvent must be absent
    // here - confirming RPA/CJES stay suppressed during the buffer (isRegisterWithRTL='No' / rtlState=null above
    // are the data side of the same check).
    const GRANT_EVENTS = ['DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC', 'GENERATE_DJ_FORM_SPEC', 'NOTIFY_RPA_DJ_SPEC', 'SEND_JUDGMENT_DETAILS_CJES'];
    const bpEvent = cd.businessProcess && cd.businessProcess.camundaEvent;
    assert.isFalse(GRANT_EVENTS.includes(bpEvent),
      `RPA/CJES suppression: no DJ-grant business process expected during the buffer, but businessProcess.camundaEvent=${bpEvent}`);

    // Request-time fields (from DefaultJudgementSpecHandler / DefaultJudgmentOnlineMapper)
    assert.equal(aj.type, 'DEFAULT_JUDGMENT', `expected type=DEFAULT_JUDGMENT, got ${aj.type}`);
    assert.ok(aj.requestDate, 'expected requestDate populated');
    assert.ok(aj.judgmentId, 'expected judgmentId populated');
    assert.ok(aj.defendant1Name, 'expected defendant1Name populated');
    assert.ok(aj.defendant1Address, 'expected defendant1Address populated');
    assert.ok(aj.paymentPlan, 'expected paymentPlan populated');
    assert.ok(aj.paymentPlan.type, 'expected paymentPlan.type populated');
    if (aj.paymentPlan.type === 'PAY_IN_INSTALMENTS') {
      assert.ok(aj.instalmentDetails, 'expected instalmentDetails for PAY_IN_INSTALMENTS plan');
      assert.ok(aj.instalmentDetails.amount, 'expected instalmentDetails.amount');
      assert.ok(aj.instalmentDetails.startDate, 'expected instalmentDetails.startDate');
      assert.ok(aj.instalmentDetails.paymentFrequency, 'expected instalmentDetails.paymentFrequency');
    }

    // Amount fields + calculation check
    assert.ok(aj.orderedAmount, 'expected orderedAmount populated');
    assert.ok(aj.claimFeeAmount, 'expected claimFeeAmount populated');
    assert.ok(aj.costs, 'expected costs populated');
    assert.ok(aj.totalAmount, 'expected totalAmount populated');
    const expectedTotal = parseInt(aj.orderedAmount) + parseInt(aj.claimFeeAmount) + parseInt(aj.costs);
    assert.equal(parseInt(aj.totalAmount), expectedTotal,
      `totalAmount calc: expected ${aj.orderedAmount}+${aj.claimFeeAmount}+${aj.costs}=${expectedTotal}, got ${aj.totalAmount}`);

    assert.isTrue(cd.joIsLiveJudgmentExists === null || cd.joIsLiveJudgmentExists === undefined || cd.joIsLiveJudgmentExists === 'No',
      `expected joIsLiveJudgmentExists NOT yet 'Yes' at PENDING_ISSUE, got ${cd.joIsLiveJudgmentExists}`);
    assert.ok(cd.joDJCreatedDate, 'expected joDJCreatedDate populated (DJ request timestamp)');
    assert.isTrue(typeof cd.joDJCreatedDate === 'string' && cd.joDJCreatedDate.startsWith(aj.requestDate),
      `expected joDJCreatedDate (${cd.joDJCreatedDate}) to start with requestDate (${aj.requestDate})`);
    console.log(`Request-time interest/breakdown captured: totalInterest=${cd.totalInterest}, joRepaymentSummaryObject=${JSON.stringify(cd.joRepaymentSummaryObject)}`);

    bufferAmountSnapshot = {
      caseId,
      orderedAmount: aj.orderedAmount,
      claimFeeAmount: aj.claimFeeAmount,
      costs: aj.costs,
      totalAmount: aj.totalAmount,
      totalInterest: cd.totalInterest,
      joRepaymentSummaryObject: cd.joRepaymentSummaryObject,
      requestDate: aj.requestDate,
      joDJCreatedDate: cd.joDJCreatedDate,
      paymentPlanType: aj.paymentPlan.type,
    };

    console.log(`PENDING_ISSUE verified: state=${aj.state}, joIsLiveJudgmentExists=${cd.joIsLiveJudgmentExists}, type=${aj.type}, requestDate=${aj.requestDate}, paymentPlan=${aj.paymentPlan.type}, ordered=${aj.orderedAmount}, claimFee=${aj.claimFeeAmount}, costs=${aj.costs}, total=${aj.totalAmount}, totalInterest=${cd.totalInterest}, joDJCreatedDate=${cd.joDJCreatedDate}`);
  },

  verifyNotificationSent: async (caseIdToQuery, templateId, recipientEmail, user = config.applicantSolicitorUser) => {
    const entries = await apiRequest.fetchSentNotifications(user, caseIdToQuery, templateId, recipientEmail);
    assert.isAtLeast(entries.length, 1,
      `Expected at least 1 notification for caseId=${caseIdToQuery}, templateId=${templateId}, recipient=${recipientEmail}, got ${entries.length}`);
  },

  verifyAnyNotificationSent: async (caseIdToQuery, user = config.applicantSolicitorUser) => {
    const entries = await apiRequest.fetchSentNotifications(user, caseIdToQuery);
    console.log(`Notification audit for caseId=${caseIdToQuery}: ${entries.length} entries`);
    entries.forEach(e => console.log(`  template=${e.templateId} recipient=${e.recipientEmail} ref=${e.reference}`));
    assert.isAtLeast(entries.length, 1,
      `Expected at least 1 notification for caseId=${caseIdToQuery}, got 0`);
  },

  verifyDefendantResponseNotificationsSent: async (
    ccdCaseId,
    user = config.applicantSolicitorUser,
    expectedApplicantTemplateId = '51fd3ba4-63ca-4ab7-b11a-0ceb8775de9f',
    expectedRespondentTemplateId = 'e763e30d-ac6d-4d95-8dc9-1ed71dd1aa1b'
  ) => {
    const response = await apiRequest.fetchCaseDetails(config.adminUser, ccdCaseId);
    const legacyRef = response.case_data ? response.case_data.legacyCaseReference : null;
    assert.ok(legacyRef, `Expected legacyCaseReference for ccdCaseId=${ccdCaseId}`);
    const entries = await apiRequest.fetchSentNotifications(user, legacyRef);
    console.log(`Notification audit for legacyRef=${legacyRef}: ${entries.length} entries`);
    entries.forEach(e => console.log(`  template=${e.templateId} recipient=${e.recipientEmail} ref=${e.reference}`));

    const applicantNotif = entries.find(e => e.reference && e.reference.includes('defendant-response-applicant-notification'));
    const respondentNotif = entries.find(e => e.reference && e.reference.includes('defendant-lip-response-respondent-notification'));

    assert.ok(applicantNotif,
      `AC6: Expected defendant-response-applicant-notification reference for legacyRef=${legacyRef}`);
    assert.ok(respondentNotif,
      `AC6: Expected defendant-lip-response-respondent-notification reference for legacyRef=${legacyRef}`);

    assert.equal(applicantNotif.templateId, expectedApplicantTemplateId,
      `AC6: Expected applicant template ${expectedApplicantTemplateId} (unchanged from normal defence flow), got ${applicantNotif.templateId}`);
    assert.equal(respondentNotif.templateId, expectedRespondentTemplateId,
      `AC6: Expected respondent template ${expectedRespondentTemplateId} (unchanged from normal defence flow), got ${respondentNotif.templateId}`);
  },

  amendRespondent1ResponseDeadline: async (user) => {
    await apiRequest.setupTokens(user);
    const respondent1deadline = {'respondent1ResponseDeadline': '2025-11-19T15:59:50'};
    await testingSupport.updateCaseData(caseId, respondent1deadline);
    console.log('ResponseDeadline updated');
  },

  waitForBusinessProcessFinished: async () => {
    await waitForFinishedBusinessProcess(caseId);
  },

  amendJoDJCreatedDate: async (hoursAgo = 144) => {
    // joDJCreatedDate is compared against a Europe/London wall-clock cutoff, so build it in London local time
    const target = new Date(Date.now() - hoursAgo * 60 * 60 * 1000);
    const p = new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Europe/London',
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false,
    }).formatToParts(target).reduce((acc, part) => (acc[part.type] = part.value, acc), {});
    const hour = p.hour === '24' ? '00' : p.hour;
    const backdated = `${p.year}-${p.month}-${p.day}T${hour}:${p.minute}:${p.second}`;
    await testingSupport.updateCaseData(caseId, {'joDJCreatedDate': backdated});
    console.log(`joDJCreatedDate backdated to ${backdated} (Europe/London, ${hoursAgo}h ago)`);
  },

  listSchedulers: async (user = config.applicantSolicitorUser) => {
    const names = await apiRequest.listSchedulers(user);
    console.log(`Registered schedulers: ${JSON.stringify(names)}`);
    return names;
  },

  runScheduler: async (schedulerName, user = config.applicantSolicitorUser) => {
    await apiRequest.triggerScheduler(user, schedulerName);
    console.log(`Scheduler ${schedulerName} triggered`);
  },

  runSchedulerExpectingNotFound: async (schedulerName, user = config.applicantSolicitorUser) => {
    await apiRequest.triggerScheduler(user, schedulerName, 404);
    console.log(`Scheduler ${schedulerName} correctly returned 404`);
  },

  verifyJudgmentBufferIssued: async (maxAttempts = 96, intervalMs = 5000) => {
    const todayLondon = new Intl.DateTimeFormat('en-CA', {timeZone: 'Europe/London'}).format(new Date());
    const todayUtc = new Intl.DateTimeFormat('en-CA', {timeZone: 'UTC'}).format(new Date());
    let last = {};
    let cdSnapshot = {};
    // The grant business process is cleared from the terminal snapshot once it finishes, so capture every
    // camundaEvent seen while it runs and assert on it after issue.
    const seenGrantEvents = new Set();
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
      const response = await apiRequest.fetchCaseDetails(config.adminUser, caseId);
      cdSnapshot = response.case_data || {};
      last.state = response.state;
      last.judgment = cdSnapshot.activeJudgment;
      last.bp = cdSnapshot.businessProcess;
      if (last.bp && last.bp.camundaEvent) {
        seenGrantEvents.add(last.bp.camundaEvent);
      }
      if (last.bp && last.bp.processInstanceId) {
        lastGrantProcessInstanceId = last.bp.processInstanceId; // for the post-issue RPA/CJES process check
      }
      const bpFinished = !last.bp || !last.bp.status || last.bp.status === 'FINISHED';
      const judgmentIssued = last.judgment && last.judgment.state === 'ISSUED';
      const stateFinal = last.state === 'All_FINAL_ORDERS_ISSUED';
      if (judgmentIssued && stateFinal && bpFinished) {
        const aj = last.judgment;
        console.log(`Buffer issued after ${attempt} attempts (state=${last.state}, bp=${last.bp && last.bp.status}, issueDate=${aj.issueDate})`);

        // State-transition fields (set by DefaultJudgementGrantedSpecCallbackHandler.updateCaseData)
        assert.isTrue(aj.issueDate === todayLondon || aj.issueDate === todayUtc,
          `expected issueDate to be today (${todayLondon} London / ${todayUtc} UTC), got ${aj.issueDate}`);
        assert.equal(aj.rtlState, 'R', `expected rtlState='R', got ${aj.rtlState}`);
        assert.equal(aj.isRegisterWithRTL, 'Yes', `expected isRegisterWithRTL='Yes', got ${aj.isRegisterWithRTL}`);
        assert.equal(cdSnapshot.joIsLiveJudgmentExists, 'Yes',
          `expected joIsLiveJudgmentExists='Yes' at grant, got ${cdSnapshot.joIsLiveJudgmentExists}`);
        // The buffer grant must have run the DJ-grant camunda process (which drives doc generation and the now-due
        // NOTIFY_RPA_DJ_SPEC / SEND_JUDGMENT_DETAILS_CJES tasks).
        if (last.bp && last.bp.camundaEvent) {
          seenGrantEvents.add(last.bp.camundaEvent);
        }
        assert.isTrue(seenGrantEvents.has('DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC'),
          `expected the buffer grant to run camundaEvent=DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC, saw [${[...seenGrantEvents].join(', ') || 'none'}]`);

        // Preserved request-time fields (must survive request → grant transition)
        assert.equal(aj.type, 'DEFAULT_JUDGMENT', `expected type=DEFAULT_JUDGMENT, got ${aj.type}`);
        assert.ok(aj.requestDate, 'expected requestDate preserved from request');
        assert.ok(aj.judgmentId, 'expected judgmentId preserved');
        assert.ok(aj.defendant1Name, 'expected defendant1Name preserved');
        assert.ok(aj.defendant1Address, 'expected defendant1Address preserved');
        assert.ok(aj.paymentPlan && aj.paymentPlan.type, 'expected paymentPlan preserved');
        if (aj.paymentPlan.type === 'PAY_IN_INSTALMENTS') {
          assert.ok(aj.instalmentDetails, 'expected instalmentDetails preserved for PAY_IN_INSTALMENTS plan');
          assert.ok(aj.instalmentDetails.amount, 'expected instalmentDetails.amount preserved');
          assert.ok(aj.instalmentDetails.paymentFrequency, 'expected instalmentDetails.paymentFrequency preserved');
        }
        assert.ok(aj.orderedAmount, 'expected orderedAmount preserved');
        assert.ok(aj.claimFeeAmount, 'expected claimFeeAmount preserved');
        assert.ok(aj.costs, 'expected costs preserved');
        assert.ok(aj.totalAmount, 'expected totalAmount preserved');
        const expectedTotal = parseInt(aj.orderedAmount) + parseInt(aj.claimFeeAmount) + parseInt(aj.costs);
        assert.equal(parseInt(aj.totalAmount), expectedTotal,
          `totalAmount calc: expected ${aj.orderedAmount}+${aj.claimFeeAmount}+${aj.costs}=${expectedTotal}, got ${aj.totalAmount}`);

        // Case-level fields
        assert.ok(cdSnapshot.joDJCreatedDate, 'expected joDJCreatedDate preserved');

        // Freeze check only: the stored activeJudgment amounts are unchanged request->grant. The order
        // document's request-date interest cap itself is unit-tested in civil-service, not here.
        assert.equal(bufferAmountSnapshot.caseId, caseId,
          `Buffer snapshot missing or for a different case (snapshot=${bufferAmountSnapshot.caseId}, current=${caseId}) - capture verifyBufferStateInitialFields on this case before issuing`);
        {
          assert.equal(aj.orderedAmount, bufferAmountSnapshot.orderedAmount,
            `Buffer freeze: orderedAmount changed during buffer (was ${bufferAmountSnapshot.orderedAmount}, now ${aj.orderedAmount})`);
          assert.equal(aj.claimFeeAmount, bufferAmountSnapshot.claimFeeAmount,
            `Buffer freeze: claimFeeAmount changed during buffer (was ${bufferAmountSnapshot.claimFeeAmount}, now ${aj.claimFeeAmount})`);
          assert.equal(aj.costs, bufferAmountSnapshot.costs,
            `Buffer freeze: costs changed during buffer (was ${bufferAmountSnapshot.costs}, now ${aj.costs})`);
          assert.equal(aj.totalAmount, bufferAmountSnapshot.totalAmount,
            `Buffer freeze: totalAmount changed during buffer (was ${bufferAmountSnapshot.totalAmount}, now ${aj.totalAmount})`);
          assert.equal(String(cdSnapshot.totalInterest), String(bufferAmountSnapshot.totalInterest),
            `Buffer freeze: totalInterest accrued during buffer (was ${bufferAmountSnapshot.totalInterest}, now ${cdSnapshot.totalInterest})`);
          assert.equal(aj.requestDate, bufferAmountSnapshot.requestDate,
            `requestDate changed during buffer (was ${bufferAmountSnapshot.requestDate}, now ${aj.requestDate})`);
          assert.equal(JSON.stringify(cdSnapshot.joRepaymentSummaryObject), JSON.stringify(bufferAmountSnapshot.joRepaymentSummaryObject),
            'Buffer freeze: joRepaymentSummaryObject (interest breakdown) changed during the buffer');
        }

        console.log(`ISSUED verified: state=${aj.state}, issueDate=${aj.issueDate}, joIsLiveJudgmentExists=${cdSnapshot.joIsLiveJudgmentExists}, rtlState=${aj.rtlState}, isRegisterWithRTL=${aj.isRegisterWithRTL}, requestDate=${aj.requestDate}, type=${aj.type}, paymentPlan=${aj.paymentPlan.type}, ordered=${aj.orderedAmount}, claimFee=${aj.claimFeeAmount}, costs=${aj.costs}, total=${aj.totalAmount}, totalInterest=${cdSnapshot.totalInterest}, camundaEvent=${last.bp && last.bp.camundaEvent}`);
        return aj;
      }
      console.log(`Attempt ${attempt}/${maxAttempts}: state=${last.state}, judgment.state=${last.judgment && last.judgment.state}, bp=${last.bp && last.bp.status}`);
      // re-fire the scheduler periodically in case a trigger was queued/delayed
      if (attempt % 6 === 0) {
        await apiRequest.triggerScheduler(config.applicantSolicitorUser, 'JudgementBuffer');
        console.log(`Re-triggered JudgementBuffer scheduler at attempt ${attempt}`);
      }
      await new Promise(resolve => setTimeout(resolve, intervalMs));
    }
    assert.fail(`Judgment buffer did not complete in ${maxAttempts * intervalMs / 1000}s. Last: ${JSON.stringify(last)}`);
  },

  verifyJudgmentBufferUnchanged: async (waitMs = 15000) => {
    console.log(`Waiting ${waitMs}ms then verifying case stayed in JUDGMENT_REQUESTED...`);
    await new Promise(resolve => setTimeout(resolve, waitMs));
    const response = await apiRequest.fetchCaseDetails(config.adminUser, caseId);
    const judgment = response.case_data ? response.case_data.activeJudgment : null;
    assert.equal(response.state, 'JUDGMENT_REQUESTED',
      `Expected case state JUDGMENT_REQUESTED but got ${response.state}. Buffer scheduler issued too early.`);
    if (judgment) {
      assert.notEqual(judgment.state, 'ISSUED',
        `activeJudgment unexpectedly ISSUED (issueDate=${judgment.issueDate}). Buffer scheduler issued too early.`);
    }
    console.log(`Confirmed: state=${response.state}, activeJudgment.state=${judgment && judgment.state}`);
  },

  verifyBufferNotIssuedAfterScheduler: async (expectedState, waitMs = 12000) => {
    await new Promise(resolve => setTimeout(resolve, waitMs));
    const response = await apiRequest.fetchCaseDetails(config.adminUser, caseId);
    const cd = response.case_data || {};
    const aj = cd.activeJudgment;
    assert.equal(response.state, expectedState,
      `Expected case to stay in ${expectedState} after scheduler run but got ${response.state}`);
    assert.isTrue(!aj || aj.state !== 'ISSUED',
      `activeJudgment unexpectedly ISSUED (state=${aj && aj.state}) for a case in ${expectedState}`);
    console.log(`Confirmed NOT issued by scheduler: state=${response.state}, activeJudgment.state=${aj && aj.state}`);
  },

  verifyInstalmentDetails: async (expectedFrequency = 'MONTHLY') => {
    const response = await apiRequest.fetchCaseDetails(config.adminUser, caseId);
    const aj = (response.case_data || {}).activeJudgment;
    assert.ok(aj, 'expected activeJudgment for instalment check');
    assert.equal(aj.paymentPlan.type, 'PAY_IN_INSTALMENTS',
      `expected paymentPlan.type=PAY_IN_INSTALMENTS, got ${aj.paymentPlan.type}`);
    assert.ok(aj.instalmentDetails, 'expected instalmentDetails populated for instalment plan');
    assert.equal(aj.instalmentDetails.paymentFrequency, expectedFrequency,
      `expected instalmentDetails.paymentFrequency=${expectedFrequency}, got ${aj.instalmentDetails.paymentFrequency}`);
    assert.ok(aj.instalmentDetails.amount, 'expected instalmentDetails.amount');
    assert.ok(aj.instalmentDetails.startDate, 'expected instalmentDetails.startDate');
    console.log(`Instalment plan verified: type=${aj.paymentPlan.type}, frequency=${aj.instalmentDetails.paymentFrequency}, amount=${aj.instalmentDetails.amount}, startDate=${aj.instalmentDetails.startDate}`);
  },

  verifyDjGrantNotificationsSent: async (ccdCaseId, user = config.applicantSolicitorUser) => {
    const cd = (await apiRequest.fetchCaseDetails(config.adminUser, ccdCaseId)).case_data;
    const legacyRef = cd && cd.legacyCaseReference;
    assert.ok(legacyRef, `expected legacyCaseReference for ccdCaseId=${ccdCaseId}`);
    const entries = await apiRequest.fetchSentNotifications(user, legacyRef);
    entries.forEach(e => console.log(`  notif audit: ref=${e.reference} template=${e.templateId} recipient=${e.recipientEmail}`));
    const djNotifs = entries.filter(e => e.reference && /dj|default.?judg|judgment/i.test(e.reference));
    assert.isAtLeast(djNotifs.length, 1,
      `AC1: expected >=1 default-judgment notification at grant for legacyRef=${legacyRef}, got refs: ${JSON.stringify(entries.map(e => e.reference))}`);
    console.log(`DJ grant notifications sent (${djNotifs.length}): ${djNotifs.map(e => e.reference).join(', ')}`);
  },

  defendantResponse: async (user, response = 'FULL_DEFENCE', scenario = 'ONE_V_ONE') => {
    await apiRequest.setupTokens(user);
    eventName = 'DEFENDANT_RESPONSE_SPEC';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);

    let defendantResponseData = eventData['defendantResponses'][scenario][response];

    caseData = returnedCaseData;

    console.log(`${response} ${scenario}`);

    for (let pageId of Object.keys(defendantResponseData.userInput)) {
      await assertValidData(defendantResponseData, pageId);
    }

    if(scenario === 'ONE_V_ONE')
      await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION');
    else if(scenario === 'ONE_V_TWO')
      await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION');
    else if (scenario === 'TWO_V_ONE')
      if (response === 'DIFF_FULL_DEFENCE' || response === 'DIFF_FULL_DEFENCE_PBAv3')
        await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM');
      else
        await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION');

    await waitForFinishedBusinessProcess(caseId);

    deleteCaseFields('respondent1Copy');
  },

  claimantResponse: async (user, response = 'FULL_DEFENCE', scenario = 'ONE_V_ONE', freeMediation = 'Yes',
                           expectedCcdState, carmEnabled = false, claimType = 'SmallClaims') => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');
    deleteCaseFields('respondentResponseIsSame');

    await apiRequest.setupTokens(user);

    eventName = 'CLAIMANT_RESPONSE_SPEC';
    caseData = await apiRequest.startEvent(eventName, caseId);

    if (carmEnabled) {
      response = 'FULL_DEFENCE_CITIZEN_DEFENDANT_MEDIATION';
    } else if (claimType === 'INTERMEDIATE') {
      response = 'FULL_DEFENCE_CITIZEN_DEFENDANT_INTERMEDIATE';
    } else if (claimType === 'MULTI') {
      response = 'FULL_DEFENCE_CITIZEN_DEFENDANT_MULTI';
    }

    let claimantResponseData = eventData['claimantResponses'][scenario][response][freeMediation];

    for (let pageId of Object.keys(claimantResponseData.userInput)) {
      await assertValidData(claimantResponseData, pageId);
    }

    let validState = expectedCcdState || 'PROCEEDS_IN_HERITAGE_SYSTEM';

    await assertSubmittedEvent(validState || 'PROCEEDS_IN_HERITAGE_SYSTEM');

    await waitForFinishedBusinessProcess(caseId);
  },

  discontinueClaim: async (user, mpScenario) => {
    console.log('discontinueClaim for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'DISCONTINUE_CLAIM_CLAIMANT';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;

    assertContainsPopulatedFields(returnedCaseData);

    let disposalData = data.DISCONTINUE_CLAIM(mpScenario);
    for (let pageId of Object.keys(disposalData.userInput)) {
      await assertValidData(disposalData, pageId);
    }

    if (mpScenario === 'TWO_V_ONE') {
      await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
        header: '#  We have noted your claim has been partly discontinued and your claim has been updated',
        body: ''
      }, true);
    } else if (mpScenario === 'ONE_V_TWO' || mpScenario === 'ONE_V_ONE_NO_P_NEEDED' ) {
      await assertSubmittedEvent('CASE_DISCONTINUED', {
        header: '# Your claim has been discontinued',
        body: ''
      }, true);
    } else {
      await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
        header: '# Your request is being reviewed',
        body: ''
      }, true);
    }
    await waitForFinishedBusinessProcess(caseId);
  },

  checkUserCaseAccess: async (user, shouldHaveAccess) => {
    console.log(`Checking ${user.email} ${shouldHaveAccess ? 'has' : 'does not have'} access to the case.`);
    const expectedStatus = shouldHaveAccess ? 200 : 403;
    return await fetchCaseDetails(user, caseId, expectedStatus);
  },

  requestJudgement: async (user, response = 'FULL_ADMISSION', scenario = 'ONE_V_ONE') => {

    await apiRequest.setupTokens(user);

    eventName = 'REQUEST_JUDGEMENT_ADMISSION_SPEC';
    caseData = await apiRequest.startEvent(eventName, caseId);
    let requestJudgementData = eventData['requestJudgement'][scenario][response];

    for (let pageId of Object.keys(requestJudgementData.userInput)) {
      await assertValidData(requestJudgementData, pageId);
    }
  },

  extendResponseDeadline: async (user) => {
    eventName = 'EXTEND_RESPONSE_DEADLINE';
    await apiRequest.setupTokens(user);
    caseData = await apiRequest.startEvent(eventName, caseId);

    let informAgreedExtensionData = await data.EXTEND_RESPONSE_DEADLINE_DATE();
    informAgreedExtensionData.userInput.ResponseDeadlineExtension.respondentSolicitor1AgreedDeadlineExtension = await dateNoWeekends(40);

    for (let pageId of Object.keys(informAgreedExtensionData.userInput)) {
      await assertValidData(informAgreedExtensionData, pageId);
    }

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  },

  amendRespondent1ResponseDate: async (user) => {
    await apiRequest.setupTokens(user);
    let respondent1ResponseDate ={};
    respondent1ResponseDate = {'respondent1ResponseDate':'2022-01-10T15:59:50'};
    testingSupport.updateCaseData(caseId, respondent1ResponseDate);
  },

  stayCase: async (user) => {
    console.log('Stay Case for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'STAY_CASE';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    let disposalData = data.STAY_CASE();
    for (let pageId of Object.keys(disposalData.userInput)) {
      await assertValidData(disposalData, pageId);
    }
    await assertSubmittedEvent('CASE_STAYED', {
      header: '# Stay added to the case \n\n ## All parties have been notified and any upcoming hearings must be cancelled',
      body: '&nbsp;'
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

  manageStay: async (user, requestUpdate) => {
    console.log('Manage Stay for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'MANAGE_STAY';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    let disposalData, header;
    if (requestUpdate) {
      disposalData = data.MANAGE_STAY_UPDATE();
      header = '# You have requested an update on \n\n # this case \n\n ## All parties have been notified';
    } else {
      disposalData = data.MANAGE_STAY_LIFT();
      header = '# You have lifted the stay from this \n\n # case \n\n ## All parties have been notified';
    }
    for (let pageId of Object.keys(disposalData.userInput)) {
      await assertValidData(disposalData, pageId);
    }

    if (requestUpdate) {
      await assertSubmittedEvent('CASE_STAYED', {
        header: header,
        body: '&nbsp;'
      }, true);
    } else {
      if (caseData.preStayState === 'IN_MEDIATION') {
        await assertSubmittedEvent('JUDICIAL_REFERRAL', {
          header: header,
          body: '&nbsp;'
        }, true);
      } else {
        await assertSubmittedEvent(caseData.preStayState, {
          header: header,
          body: '&nbsp;'
        }, true);
      }
    }


    await waitForFinishedBusinessProcess(caseId);
  },

  dismissCase: async (user) => {
    console.log('Dismiss case for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'DISMISS_CASE';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    let disposalData = data.DISMISS_CASE();
    for (let pageId of Object.keys(disposalData.userInput)) {
      await assertValidData(disposalData, pageId);
    }
    await assertSubmittedEvent('CASE_DISMISSED', {
      header: '# The case has been dismissed\n## All parties have been notified',
      body: '&nbsp;'
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

  sendMessage: async (user) => {
    console.log('Send message  case for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'SEND_AND_REPLY';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    let disposalData = data.SEND_MESSAGE();
    for (let pageId of Object.keys(disposalData.userInput)) {
      await assertValidData(disposalData, pageId);
    }
    await assertSubmittedEvent('CASE_STAYED', {
      header: '# Your message has been sent',
      body: '<br /><h2 class="govuk-heading-m">What happens next</h2><br />A task has been created to review your message'
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

  replyMessage: async (user) => {
    console.log('Send message  case for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'SEND_AND_REPLY';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;

    const latestMessage = getLatestMessageToReplyTo(caseData);
    const disposalData = data.REPLY_MESSAGE(latestMessage.code, latestMessage.label);
    for (let pageId of Object.keys(disposalData.userInput)) {
      await assertValidData(disposalData, pageId);
    }
    await assertSubmittedEvent('CASE_STAYED', {
      header: '# Reply sent',
      body: '<br /><h2 class="govuk-heading-m">What happens next</h2><br />A task has been created to review your reply.'
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },
};

const getLatestMessageToReplyTo = (caseData) => {
  const messagesToReplyTo = caseData.messagesToReplyTo;
  if (messagesToReplyTo && messagesToReplyTo.list_items && messagesToReplyTo.list_items.length > 0) {
    const latestMessage = messagesToReplyTo.list_items[messagesToReplyTo.list_items.length - 1];
    return {
      code: latestMessage.code,
      label: latestMessage.label
    };
  }
  return null;
};

const validateEventPages = async (data, solicitor) => {
  //transform the data
  console.log('validateEventPages....');
  for (let pageId of Object.keys(data.valid)) {
    if (pageId === 'DefendantLitigationFriend' || pageId === 'DocumentUpload' || pageId === 'Upload' || pageId === 'DraftDirections'|| pageId === 'ApplicantDefenceResponseDocument' || pageId === 'DraftDirections' || pageId === 'FinalOrderPreview' || pageId === 'FixedRecoverableCosts') {
      const document = await testingSupport.uploadDocument();
      data = await updateCaseDataWithPlaceholders(data, document);
    }
    // data = await updateCaseDataWithPlaceholders(data);
    await assertValidData(data, pageId, solicitor);
  }
};

async function updateCaseDataWithPlaceholders(data, document) {
  const placeholders = {
    TEST_DOCUMENT_URL: document.document_url,
    TEST_DOCUMENT_BINARY_URL: document.document_binary_url,
    TEST_DOCUMENT_FILENAME: document.document_filename
  };

  data = lodash.template(JSON.stringify(data))(placeholders);

  return JSON.parse(data);
}

// Functions
const assertValidData = async (data, pageId) => {
  console.log(`asserting page: ${pageId} has valid data`);
  if (pageId === 'FixedRecoverableCosts' || pageId === 'DraftDirections') {
    const document = await testingSupport.uploadDocument();
    data = await updateCaseDataWithPlaceholders(data, document);
  }
  let userData;
  if (eventName === 'CREATE_SDO' || eventName === 'NotSuitable_SDO' || eventName === 'HEARING_SCHEDULED'
  || eventName === 'GENERATE_DIRECTIONS_ORDER') {
    userData = data.valid[pageId];
  } else {
    userData = data.userInput[pageId];
  }
  caseData = update(caseData, userData);
  const response = await apiRequest.validatePage(
    eventName,
    pageId,
    caseData,
    caseId
  );
  let responseBody = await response.json();
  responseBody = clearDataForSearchCriteria(responseBody); //Until WA release

  assert.equal(response.status, 200);

  if (data.midEventData && data.midEventData[pageId]) {
    checkExpected(responseBody.data, data.midEventData[pageId]);
  }

  if (data.midEventGeneratedData && data.midEventGeneratedData[pageId]) {
    checkGenerated(responseBody.data, data.midEventGeneratedData[pageId]);
  }

  caseData = update(caseData, responseBody.data);
};

const clearDataForSearchCriteria = (responseBody) => {
  delete responseBody.data['SearchCriteria'];
  return responseBody;
};

function checkExpected(responseBodyData, expected, prefix = '') {
  if (!(responseBodyData) && expected) {
    if (expected) {
      assert.fail('Response' + prefix ? '[' + prefix + ']' : '' + ' is empty but it was expected to be ' + expected);
    } else {
      // null and undefined may reach this point bc typeof null is object
      return;
    }
  }
  for (const key in expected) {
    if (Object.prototype.hasOwnProperty.call(expected, key)) {
      if (typeof expected[key] === 'object') {
        checkExpected(responseBodyData[key], expected[key], key + '.');
      } else {
        assert.equal(responseBodyData[key], expected[key], prefix + key + ': expected ' + expected[key]
          + ' but actual ' + responseBodyData[key]);
      }
    }
  }
}

function checkGenerated(responseBodyData, generated, prefix = '') {
  if (!(responseBodyData)) {
    assert.fail('Response' + prefix ? '[' + prefix + ']' : '' + ' is empty but it was not expected to be');
  }
  for (const key in generated) {
    if (Object.prototype.hasOwnProperty.call(generated, key)) {
      const checkType = function (type) {
        if (type === 'array') {
          assert.isTrue(Array.isArray(responseBodyData[key]),
            'responseBody[' + prefix + key + '] was expected to be an array');
        } else {
          assert.equal(typeof responseBodyData[key], type,
            'responseBody[' + prefix + key + '] was expected to be of type ' + type);
        }
      };
      const checkFunction = function (theFunction) {
        assert.isTrue(theFunction.call(responseBodyData[key], responseBodyData[key]),
          'responseBody[' + prefix + key + '] does not satisfy the condition it should');
      };
      if (typeof generated[key] === 'string') {
        checkType(generated[key]);
      } else if (typeof generated[key] === 'function') {
        checkFunction(generated[key]);
      } else if (typeof generated[key] === 'object') {
        if (generated[key]['type']) {
          checkType(generated[key]['type']);
        }
        if (generated[key]['condition']) {
          checkType(generated[key]['condition']);
        }
        for (const key2 in generated[key]) {
          if (Object.prototype.hasOwnProperty.call(generated, key2) && 'condition' !== key2 && 'type' !== key2) {
            checkGenerated(responseBodyData[key2], generated[key2], key2 + '.');
          }
        }
      }
    }
  }
}

/**
 * {...obj1, ...obj2} replaces elements. For instance, if obj1 = { check : { correct : false }}
 * and obj2 = { check: { newValue : 'ASDF' }} the result will be { check : {newValue : 'ASDF} }.
 *
 * What this method does is a kind of deep spread, in a case like the one before,
 * @param currentObject the object we want to modify
 * @param modifications the object holding the modifications
 * @return a caseData with the new values
 */
function update(currentObject, modifications) {
  const modified = {...currentObject};
  for (const key in modifications) {
    if (currentObject[key] && typeof currentObject[key] === 'object') {
      if (Array.isArray(currentObject[key])) {
        modified[key] = modifications[key];
      } else {
        modified[key] = update(currentObject[key], modifications[key]);
      }
    } else {
      modified[key] = modifications[key];
    }
  }
  return modified;
}

const assertSubmittedEvent = async (expectedState, submittedCallbackResponseContains, hasSubmittedCallback = true) => {
  await apiRequest.startEvent(eventName, caseId);

  const response = await apiRequest.submitEvent(eventName, caseData, caseId);
  const responseBody = await response.json();
  assert.equal(response.status, 201);
  assert.equal(responseBody.state, expectedState);
  if (hasSubmittedCallback && submittedCallbackResponseContains) {
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, submittedCallbackResponseContains.header);
    assert.include(responseBody.after_submit_callback_response.confirmation_body, submittedCallbackResponseContains.body);
  }

  if (eventName === 'CREATE_CLAIM_SPEC') {
    caseId = responseBody.id;
    await addUserCaseMapping(caseId, config.applicantSolicitorUser);
    console.log('Case created: ' + caseId);
  }
};

// Mid event will not return case fields that were already filled in another event if they're present on currently processed event.
// This happens until these case fields are set again as a part of current event (note that this data is not removed from the case).
// Therefore these case fields need to be removed from caseData, as caseData object is used to make assertions
const deleteCaseFields = (...caseFields) => {
  caseFields.forEach(caseField => delete caseData[caseField]);
};

const assertContainsPopulatedFields = returnedCaseData => {
  for (let populatedCaseField of Object.keys(caseData)) {
    assert.property(returnedCaseData,  populatedCaseField);
  }
};

const assertCorrectEventsAreAvailableToUser = async (user, state) => {
  console.log(`Asserting user ${user.type} in env ${config.runningEnv} has correct permissions`);
  const caseForDisplay = await apiRequest.fetchCaseForDisplay(user, caseId);
  if (['preview', 'demo'].includes(config.runningEnv)) {
    expect(caseForDisplay.triggers).to.deep.include.members(nonProdExpectedEvents[user.type][state],
      'Unexpected events for state ' + state + ' and user type ' + user.type);
  } else {
    // expect(caseForDisplay.triggers).to.deep.include.members(expectedEvents[user.type][state],
    expect(caseForDisplay.triggers).to.deep.include.members(expectedEvents[user.type][state],
      'Unexpected events for state ' + state + ' and user type ' + user.type);
  }
};
