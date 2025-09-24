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
const {checkToggleEnabled, checkMintiToggleEnabled, uploadDocument} = require('./testingSupport');
const {PBAv3, isJOLive} = require('../fixtures/featureKeys');
const {adjustCaseSubmittedDateForCarm} = require('../helpers/carmHelper');
const {fetchCaseDetails} = require('./apiRequest');
const lipClaimantResponse = require('../fixtures/events/cui/lipClaimantResponse');
const discontinueClaimSpec = require('../fixtures/events/discontinueClaimSpec');
const sdoTracks = require('../fixtures/events/createSDO');
const hearingScheduled = require('../fixtures/events/scheduleHearing');
const evidenceUploadApplicant = require('../fixtures/events/evidenceUploadApplicant');
const evidenceUploadRespondent = require('../fixtures/events/evidenceUploadRespondent');
const requestForReconsideration = require('../fixtures/events/requestForReconsideration');
const trialReadiness = require('../fixtures/events/trialReadiness.js');
const lodash = require('lodash');
const createFinalOrder = require('../fixtures/events/finalOrder.js');
const judgeDecisionToReconsiderationRequest = require('../fixtures/events/judgeDecisionOnReconsiderationRequest');
const {adjustCaseSubmittedDateForMinti} = require('../helpers/mintiHelper');
const stayCase = require('../fixtures/events/stayCase');
const manageStay = require('../fixtures/events/manageStay');
const dismissCase = require('../fixtures/events/dismissCase');
const { toJSON } = require('lodash/seq');
const sendAndReplyMessage = require('../fixtures/events/sendAndReplyMessages');
const judgmentMarkPaidInFull = require('../fixtures/events/cui/judgmentMarkPaidInFullCui');
const judgmentOnline1v1Spec = require('../fixtures/events/judgmentOnline1v1Spec');


let caseId, eventName;
let caseData = {};

const data = {
  CREATE_CLAIM: (scenario) => claimData.createClaim(scenario),
  CREATE_SPEC_CLAIM_FASTTRACK: (scenario) => claimDataSpecFastLRvLiP.createClaim(scenario),
  CREATE_SPEC_CLAIM_INTTRACK: (scenario) => claimDataSpecIntLRvLiP.createClaim(scenario),
  CREATE_SPEC_CLAIM_MULTITRACK: (scenario) => claimDataSpecMultiLRvLiP.createClaim(scenario),
  CREATE_SPEC_CLAIM: (scenario) => claimDataSpecSmallLRvLiP.createClaim(scenario),
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
    await adjustCaseSubmittedDateForCarm(caseId, carmEnabled);
    if (isMintiCase) {
      const isMintiToggleEnabled = await checkMintiToggleEnabled();
      await adjustCaseSubmittedDateForMinti(caseId, (isMintiToggleEnabled && isMintiCase), carmEnabled);
    }
    return caseId;
  },

  retrieveTaskDetails: async (user, caseNumber, taskId) => {
    return apiRequest.fetchTaskDetails(user, caseNumber, taskId);
  },

  assignTaskToUser: async (user, taskId) => {
    return apiRequest.taskActionByUser(user, taskId, 'claim');
  },

  createSpecifiedClaimWithUnrepresentedRespondent: async (user, multipartyScenario, claimType, carmEnabled = false) => {
    console.log(' Creating specified claim');
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
      createClaimSpecData = data.CREATE_SPEC_CLAIM(multipartyScenario);
    }

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    for (let pageId of Object.keys(createClaimSpecData.userInput)) {
      await assertValidData(createClaimSpecData, pageId);
    }

    await assertSubmittedEvent('PENDING_CASE_ISSUED');
    await waitForFinishedBusinessProcess(caseId);

    const pbaV3 = await checkToggleEnabled(PBAv3);
    if (pbaV3) {
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
        claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    }
    await waitForFinishedBusinessProcess(caseId);
    if (claimType !== 'pinInPost') {
      await assignCaseRoleToUser(caseId, 'DEFENDANT', config.defendantCitizenUser2);
    }

    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');

    await adjustCaseSubmittedDateForCarm(caseId, carmEnabled);
    const isMintiToggleEnabled = await checkMintiToggleEnabled();
    await adjustCaseSubmittedDateForMinti(caseId, (isMintiToggleEnabled && (claimType === 'INTERMEDIATE' || claimType === 'MULTI')), carmEnabled);

    return caseId;
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
    const isJudgmentOnlineLive = await checkToggleEnabled(isJOLive);

    if (isJudgmentOnlineLive && (typeOfData === 'FA_ACCEPT_CCJ' || typeOfData === 'PA_ACCEPT_CCJ')) {
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
    } else {
      eventName = 'CREATE_SDO';
      disposalData = data.CREATE_SDO();
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

  scheduleHearing: async (user, allocatedTrack = 'OTHER', claimType) => {
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
    await assertSubmittedEvent('HEARING_READINESS', null, false);
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

