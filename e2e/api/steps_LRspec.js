const config = require('../config.js');
const deepEqualInAnyOrder = require('deep-equal-in-any-order');
const chai = require('chai');
const {dateTime, dateNoWeekendsBankHolidayNextDay} = require('./dataHelper');

chai.use(deepEqualInAnyOrder);
chai.config.truncateThreshold = 0;
const {expect, assert} = chai;

const {waitForFinishedBusinessProcess} = require('../api/testingSupport');
const {assignCaseRoleToUser, addUserCaseMapping, unAssignAllUsers} = require('./caseRoleAssignmentHelper');
const {PBAv3, isJOLive, COSC} = require('../fixtures/featureKeys');
const apiRequest = require('./apiRequest.js');
const claimData = require('../fixtures/events/createClaimSpec.js');
const expectedEvents = require('../fixtures/ccd/expectedEventsLRSpec.js');
const nonProdExpectedEvents = require('../fixtures/ccd/nonProdExpectedEventsLRSpec.js');
const testingSupport = require('./testingSupport');
const {checkCaseFlagsEnabled, checkMintiToggleEnabled} = require('./testingSupport');
const {checkToggleEnabled} = require('./testingSupport');
const {fetchCaseDetails} = require('./apiRequest');
const {assertCaseFlags, assertFlagsInitialisedAfterCreateClaim} = require('../helpers/assertions/caseFlagsAssertions');
const {addAndAssertCaseFlag, getPartyFlags, getDefinedCaseFlagLocations, updateAndAssertCaseFlag} = require('./caseFlagsHelper');
const {CASE_FLAGS} = require('../fixtures/caseFlags');
const {dateNoWeekends} = require('./dataHelper');
const {addFlagsToFixture} = require('../helpers/caseFlagsFeatureHelper');
const lodash = require('lodash');
const createFinalOrderSpec = require('../fixtures/events/finalOrderSpec');
const judgmentOnline1v1Spec = require('../fixtures/events/judgmentOnline1v1Spec');
const judgmentOnline1v2Spec = require('../fixtures/events/judgmentOnline1v2Spec');
const transferOnlineCaseSpec = require('../fixtures/events/transferOnlineCaseSpec');
const sdoTracks = require('../fixtures/events/createSDO.js');
const mediationDocuments = require('../fixtures/events/mediation/uploadMediationDocuments');
const hearingScheduled = require('../fixtures/events/specScheduleHearing');
const {adjustCaseSubmittedDateForCarm} = require('../helpers/carmHelper');
const mediationUnsuccessful = require('../fixtures/events/cui/unsuccessfulMediationCui.js');
const evidenceUploadApplicant = require('../fixtures/events/evidenceUploadApplicant');
const evidenceUploadRespondent = require('../fixtures/events/evidenceUploadRespondent');
const settleClaim1v1Spec = require('../fixtures/events/settleClaim1v1Spec');
const discontinueClaimSpec = require('../fixtures/events/discontinueClaimSpec');
const validateDiscontinueClaimClaimantSpec = require('../fixtures/events/validateDiscontinueClaimClaimantSpec');
const {cloneDeep} = require('lodash');
const {adjustCaseSubmittedDateForMinti, getMintiTrackByClaimAmount, assertTrackAfterClaimCreation} = require('../helpers/mintiHelper');
const stayCase = require('../fixtures/events/stayCase');
const manageStay = require('../fixtures/events/manageStay');
const dismissCase = require('../fixtures/events/dismissCase');
const genAppClaimData = require('../fixtures/events/createGeneralApplication');
const genAppClaimDataLR = require('../fixtures/events/createGeneralApplicationLR');
const sendAndReplyMessage = require('../fixtures/events/sendAndReplyMessages');

let caseId, eventName, mintiClaimTrack;
let caseData = {};

let mpScenario = 'ONE_V_ONE';

const data = {
  CREATE_CLAIM: (scenario, pbaV3, isMintiCaseEnabled, mintiClaimAmount) => claimData.createClaim(scenario, pbaV3, isMintiCaseEnabled, mintiClaimAmount),
  INITIATE_GENERAL_APPLICATION: genAppClaimData.createGAData('Yes', null, '27500','FEE0442'),
  INITIATE_GENERAL_APPLICATION_LR: genAppClaimDataLR.createGAData('Yes', null, '27500','FEE0442'),
  DEFENDANT_RESPONSE: (response, camundaEvent) => require('../fixtures/events/defendantResponseSpec.js').respondToClaim(response, camundaEvent),
  DEFENDANT_RESPONSE_MULTI_CLAIM: (response, camundaEvent) => require('../fixtures/events/defendantResponseMultiClaimSpec.js').respondToClaim(response, camundaEvent),
  DEFENDANT_RESPONSE_INTERMEDIATE_CLAIM: (response, camundaEvent) => require('../fixtures/events/defendantResponseIntermediateClaimSpec.js').respondToClaim(response, camundaEvent),
  DEFENDANT_RESPONSE_MULTI_CLAIM_SECOND_SOL: (response, camundaEvent) => require('../fixtures/events/defendantResponseMultiClaimSpec.js').respondToClaim2(response, camundaEvent),
  DEFENDANT_RESPONSE_INTERMEDIATE_CLAIM_SECOND_SOL: (response, camundaEvent) => require('../fixtures/events/defendantResponseIntermediateClaimSpec.js').respondToClaim2(response, camundaEvent),
  DEFENDANT_RESPONSE2: (response, camundaEvent) => require('../fixtures/events/defendantResponseSpec.js').respondToClaim2(response, camundaEvent),
  DEFENDANT_RESPONSE_1v2: (response, camundaEvent) => require('../fixtures/events/defendantResponseSpec1v2.js').respondToClaim(response, camundaEvent),
  DEFENDANT_RESPONSE_1v2_Mediation: (response, camundaEvent) => require('../fixtures/events/defendantResponseSpec1v2Mediation.js').respondToClaim(response, camundaEvent),
  DEFENDANT_RESPONSE2_1v2_Mediation: (response, camundaEvent) => require('../fixtures/events/defendantResponseSpec1v2Mediation.js').respondToClaim2(response, camundaEvent),
  DEFENDANT_RESPONSE_2v1: (response, camundaEvent) => require('../fixtures/events/defendantResponseSpec2v1.js').respondToClaim(response, camundaEvent),
  CLAIMANT_RESPONSE: (mpScenario, fastTrack, carmEnabled) => require('../fixtures/events/claimantResponseSpec.js').claimantResponse(mpScenario, fastTrack, carmEnabled),
  CLAIMANT_RESPONSE_1v2: (response, carmEnabled) => require('../fixtures/events/claimantResponseSpec1v2.js').claimantResponse(response, carmEnabled),
  CLAIMANT_RESPONSE_2v1: (response, carmEnabled) => require('../fixtures/events/claimantResponseSpec2v1.js').claimantResponse(response, carmEnabled),
  CLAIMANT_RESPONSE_MULTI_CLAIM: (response) => require('../fixtures/events/claimantResponseMultiClaimSpec.js').claimantResponse(response),
  CLAIMANT_RESPONSE_INTERMEDIATE_CLAIM: (response) => require('../fixtures/events/claimantResponseIntermediateClaimSpec.js').claimantResponse(response),
  INFORM_AGREED_EXTENSION_DATE: async (camundaEvent) => require('../fixtures/events/informAgreeExtensionDateSpec.js').informExtension(camundaEvent),
  DEFAULT_JUDGEMENT_SPEC: require('../fixtures/events/defaultJudgmentSpec.js'),
  DEFAULT_JUDGEMENT_SPEC_1V2: require('../fixtures/events/defaultJudgment1v2Spec.js'),
  DEFAULT_JUDGEMENT_SPEC_1V2_DIVERGENT: require('../fixtures/events/defaultJudgment1v2DivergentSpec.js'),
  DEFAULT_JUDGEMENT_SPEC_2V1: require('../fixtures/events/defaultJudgment2v1Spec.js'),
  CREATE_FAST_NO_SUM_SPEC: () => sdoTracks.createSDOFastTrackSpec(),
  CREATE_SDO: (userInput) => sdoTracks.createSDOSmallWODamageSumInPerson(userInput),
  HEARING_SCHEDULED: (allocatedTrack, isMinti) => hearingScheduled.scheduleHearing(allocatedTrack, isMinti),
  FINAL_ORDERS_SPEC: (finalOrdersRequestType, dayPlus0, dayPlus7, dayPlus14, dayPlus21, orderType) => createFinalOrderSpec.requestFinalOrder(finalOrdersRequestType, dayPlus0, dayPlus7, dayPlus14, dayPlus21, orderType),
  RECORD_JUDGMENT_SPEC: (whyRecorded, paymentPlanSelection) => judgmentOnline1v1Spec.recordJudgment(whyRecorded, paymentPlanSelection),
  CONFIRM_ORDER_REVIEW: () => judgmentOnline1v1Spec.confirmOrderReview(),
  RECORD_JUDGMENT_ONE_V_TWO_SPEC: (whyRecorded, paymentPlanSelection) => judgmentOnline1v2Spec.recordJudgment(whyRecorded, paymentPlanSelection),
  EDIT_JUDGMENT_SPEC: (whyRecorded, paymentPlanSelection) => judgmentOnline1v1Spec.editJudgment(whyRecorded, paymentPlanSelection),
  EDIT_JUDGMENT_ONE_V_TWO_SPEC: (whyRecorded, paymentPlanSelection) => judgmentOnline1v2Spec.editJudgment(whyRecorded, paymentPlanSelection),
  SET_ASIDE_JUDGMENT: (setAsideReason, setAsideOrderType) => judgmentOnline1v1Spec.setAsideJudgment(setAsideReason, setAsideOrderType),
  JUDGMENT_PAID_IN_FULL: () => judgmentOnline1v1Spec.markJudgmentPaidInFull(),
  NOT_SUITABLE_SDO_SPEC: (option) => transferOnlineCaseSpec.notSuitableSDOspec(option),
  TRANSFER_CASE_SPEC: () => transferOnlineCaseSpec.transferCaseSpec(),
  EVIDENCE_UPLOAD_APPLICANT_SMALL: (mpScenario) => evidenceUploadApplicant.createApplicantSmallClaimsEvidenceUploadFlightDelay(mpScenario),
  EVIDENCE_UPLOAD_RESPONDENT_SMALL: (mpScenario) => evidenceUploadRespondent.createRespondentSmallClaimsEvidenceUploadFlightDelay(mpScenario),
  EVIDENCE_UPLOAD_APPLICANT_FAST: (mpScenario, claimTrack) => evidenceUploadApplicant.createApplicantFastClaimsEvidenceUpload(mpScenario, claimTrack),
  EVIDENCE_UPLOAD_RESPONDENT_FAST: (mpScenario, claimTrack) => evidenceUploadRespondent.createRespondentFastClaimsEvidenceUpload(mpScenario, claimTrack),
  REFER_JUDGE_DEFENCE_RECEIVED: () => judgmentOnline1v1Spec.referJudgeDefenceReceived(),
  SETTLE_CLAIM_MARK_PAID_FULL: (addApplicant2) => settleClaim1v1Spec.settleClaim(addApplicant2),
  SETTLE_CLAIM_MARK_PAID_FULL_SELECT_CLAIMANT: (addApplicant2) => settleClaim1v1Spec.claimantDetails(addApplicant2),
  DISCONTINUE_CLAIM: (mpScenario) => discontinueClaimSpec.discontinueClaim(mpScenario),
  VALIDATE_DISCONTINUE_CLAIM_CLAIMANT: (permission) => validateDiscontinueClaimClaimantSpec.validateDiscontinueClaimClaimant(permission),
  STAY_CASE: () => stayCase.stayCaseSpec(),
  MANAGE_STAY_UPDATE: () => manageStay.manageStayRequestUpdate(),
  MANAGE_STAY_LIFT: () => manageStay.manageStayLiftStay(),
  DISMISS_CASE: () => dismissCase.dismissCase(),
  SEND_MESSAGE: () => sendAndReplyMessage.sendMessageLr(),
  REPLY_MESSAGE: (messageCode, messageLabel) => sendAndReplyMessage.replyMessageLr(messageCode, messageLabel),
  CLAIMANT_RESPONSE_JBA: (response) => require('../fixtures/events/claimantResponseSpec1v1.js').claimantResponse_JBA(response)
};

const eventData = {
  defendantResponses: {
    ONE_V_ONE: {
      FULL_DEFENCE: data.DEFENDANT_RESPONSE('FULL_DEFENCE'),
      FULL_DEFENCE_PBAv3: data.DEFENDANT_RESPONSE('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE_PBAv3_MULTI_CLAIM: data.DEFENDANT_RESPONSE_MULTI_CLAIM('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE_PBAv3_INTERMEDIATE_CLAIM: data.DEFENDANT_RESPONSE_INTERMEDIATE_CLAIM('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION_PBAv3_MULTI_CLAIM: data.DEFENDANT_RESPONSE_MULTI_CLAIM('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT', 'MULTI_CLAIM'),
      FULL_ADMISSION_PBAv3_INTERMEDIATE_CLAIM: data.DEFENDANT_RESPONSE_INTERMEDIATE_CLAIM('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE_PBAv3_SetAside_DJ: data.DEFENDANT_RESPONSE('FULL_DEFENCE', 'DEFAULT_JUDGEMENT_NON_DIVERGENT_SPEC'),
      FULL_ADMISSION: data.DEFENDANT_RESPONSE('FULL_ADMISSION'),
      FULL_ADMISSION_PBAv3: data.DEFENDANT_RESPONSE('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION_PBAv3_MULTI_CLAIM: data.DEFENDANT_RESPONSE_MULTI_CLAIM('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION_PBAv3_INTERMEDIATE_CLAIM: data.DEFENDANT_RESPONSE_INTERMEDIATE_CLAIM('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION: data.DEFENDANT_RESPONSE('PART_ADMISSION'),
      PART_ADMISSION_PBAv3: data.DEFENDANT_RESPONSE('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM: data.DEFENDANT_RESPONSE('COUNTER_CLAIM'),
      COUNTER_CLAIM_PBAv3: data.DEFENDANT_RESPONSE('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM_PBAv3_MULTI_CLAIM: data.DEFENDANT_RESPONSE_MULTI_CLAIM('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM_PBAv3_INTERMEDIATE_CLAIM: data.DEFENDANT_RESPONSE_INTERMEDIATE_CLAIM('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION_JBA_PBAv3: data.DEFENDANT_RESPONSE('FULL_ADMISSION_JBA', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
    },
    ONE_V_TWO: {
      FULL_DEFENCE: data.DEFENDANT_RESPONSE_1v2('FULL_DEFENCE'),
      FULL_DEFENCE_PBAv3: data.DEFENDANT_RESPONSE_1v2('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE_PBAv3_SetAside_DJ: data.DEFENDANT_RESPONSE_1v2('FULL_DEFENCE', 'SET_ASIDE_JUDGMENT'),
      FULL_DEFENCE_PBAv3_MULTI_CLAIM: data.DEFENDANT_RESPONSE_MULTI_CLAIM('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE_PBAv3_INTERMEDIATE_CLAIM: data.DEFENDANT_RESPONSE_INTERMEDIATE_CLAIM('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION: data.DEFENDANT_RESPONSE_1v2('FULL_ADMISSION'),
      FULL_ADMISSION_PBAv3: data.DEFENDANT_RESPONSE_1v2('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION: data.DEFENDANT_RESPONSE_1v2('PART_ADMISSION'),
      PART_ADMISSION_PBAv3: data.DEFENDANT_RESPONSE_1v2('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM: data.DEFENDANT_RESPONSE_1v2('COUNTER_CLAIM'),
      COUNTER_CLAIM_PBAv3: data.DEFENDANT_RESPONSE_1v2('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      DIFF_FULL_DEFENCE: data.DEFENDANT_RESPONSE_1v2('DIFF_FULL_DEFENCE'),
      DIFF_FULL_DEFENCE_PBAv3: data.DEFENDANT_RESPONSE_1v2('DIFF_FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      DIFF_NOT_FULL_DEFENCE: data.DEFENDANT_RESPONSE_1v2('DIFF_NOT_FULL_DEFENCE'),
      DIFF_NOT_FULL_DEFENCE_PBAv3: data.DEFENDANT_RESPONSE_1v2('DIFF_NOT_FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
    },
    ONE_V_ONE_DIF_SOL: {
      FULL_DEFENCE1: data.DEFENDANT_RESPONSE('FULL_DEFENCE'),
      FULL_DEFENCE1_PBAv3: data.DEFENDANT_RESPONSE('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE1_PBAv3_Mediation: data.DEFENDANT_RESPONSE_1v2_Mediation('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE1_PBAv3_MULTI_CLAIM: data.DEFENDANT_RESPONSE_MULTI_CLAIM('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE1_PBAv3_INTERMEDIATE_CLAIM: data.DEFENDANT_RESPONSE_INTERMEDIATE_CLAIM('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION1: data.DEFENDANT_RESPONSE('FULL_ADMISSION'),
      FULL_ADMISSION1_PBAv3: data.DEFENDANT_RESPONSE('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION1: data.DEFENDANT_RESPONSE('PART_ADMISSION'),
      PART_ADMISSION1_PBAv3: data.DEFENDANT_RESPONSE('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM1: data.DEFENDANT_RESPONSE('COUNTER_CLAIM'),
      COUNTER_CLAIM1_PBAv3: data.DEFENDANT_RESPONSE('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),

      FULL_DEFENCE2: data.DEFENDANT_RESPONSE2('FULL_DEFENCE'),
      FULL_DEFENCE2_PBAv3: data.DEFENDANT_RESPONSE2('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE2_PBAv3_Mediation: data.DEFENDANT_RESPONSE2_1v2_Mediation('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE2_PBAv3_MULTI_CLAIM: data.DEFENDANT_RESPONSE_MULTI_CLAIM_SECOND_SOL('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE2_PBAv3_INTERMEDIATE_CLAIM: data.DEFENDANT_RESPONSE_INTERMEDIATE_CLAIM_SECOND_SOL('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION2: data.DEFENDANT_RESPONSE2('FULL_ADMISSION'),
      FULL_ADMISSION2_PBAv3: data.DEFENDANT_RESPONSE2('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION2: data.DEFENDANT_RESPONSE2('PART_ADMISSION'),
      PART_ADMISSION2_PBAv3: data.DEFENDANT_RESPONSE2('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM2: data.DEFENDANT_RESPONSE2('COUNTER_CLAIM'),
      COUNTER_CLAIM2_PBAv3: data.DEFENDANT_RESPONSE2('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT')
    },
    TWO_V_ONE: {
      FULL_DEFENCE: data.DEFENDANT_RESPONSE_2v1('FULL_DEFENCE'),
      FULL_DEFENCE_PBAv3: data.DEFENDANT_RESPONSE_2v1('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION: data.DEFENDANT_RESPONSE_2v1('FULL_ADMISSION'),
      FULL_ADMISSION_PBAv3: data.DEFENDANT_RESPONSE_2v1('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION: data.DEFENDANT_RESPONSE_2v1('PART_ADMISSION'),
      PART_ADMISSION_PBAv3: data.DEFENDANT_RESPONSE_2v1('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM: data.DEFENDANT_RESPONSE_2v1('COUNTER_CLAIM'),
      COUNTER_CLAIM_PBAv3: data.DEFENDANT_RESPONSE_2v1('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      DIFF_FULL_DEFENCE: data.DEFENDANT_RESPONSE_2v1('DIFF_FULL_DEFENCE'),
      DIFF_FULL_DEFENCE_PBAv3: data.DEFENDANT_RESPONSE_2v1('DIFF_FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      DIFF_NOT_FULL_DEFENCE: data.DEFENDANT_RESPONSE_2v1('DIFF_NOT_FULL_DEFENCE'),
      DIFF_NOT_FULL_DEFENCE_PBAv3: data.DEFENDANT_RESPONSE_2v1('DIFF_NOT_FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
    }
  },
  claimantResponses: {
    ONE_V_ONE: {
      FULL_DEFENCE: data.CLAIMANT_RESPONSE('FULL_DEFENCE'),
      FULL_DEFENCE_MULTI_CLAIM: data.CLAIMANT_RESPONSE_MULTI_CLAIM('FULL_DEFENCE'),
      FULL_DEFENCE_INTERMEDIATE_CLAIM: data.CLAIMANT_RESPONSE_INTERMEDIATE_CLAIM('FULL_DEFENCE'),
      FULL_ADMISSION_MULTI_CLAIM: data.CLAIMANT_RESPONSE_MULTI_CLAIM('FULL_ADMISSION'),
      FULL_ADMISSION_INTERMEDIATE_CLAIM: data.CLAIMANT_RESPONSE_INTERMEDIATE_CLAIM('FULL_ADMISSION'),
      PART_ADMISSION_MULTI_CLAIM: data.CLAIMANT_RESPONSE_MULTI_CLAIM('PART_ADMISSION'),
      PART_ADMISSION_INTERMEDIATE_CLAIM: data.CLAIMANT_RESPONSE_INTERMEDIATE_CLAIM('PART_ADMISSION'),
      FULL_DEFENCE_MEDIATION: data.CLAIMANT_RESPONSE('FULL_DEFENCE', false, true),
      FULL_ADMISSION: data.CLAIMANT_RESPONSE('FULL_ADMISSION'),
      PART_ADMISSION: data.CLAIMANT_RESPONSE('PART_ADMISSION'),
      COUNTER_CLAIM: data.CLAIMANT_RESPONSE('COUNTER_CLAIM')
    },
    ONE_V_TWO: {
      FULL_DEFENCE: data.CLAIMANT_RESPONSE_1v2('FULL_DEFENCE'),
      FULL_DEFENCE_MEDIATION: data.CLAIMANT_RESPONSE_1v2('FULL_DEFENCE', true),
      FULL_DEFENCE_MULTI_CLAIM: data.CLAIMANT_RESPONSE_MULTI_CLAIM('FULL_DEFENCE'),
      FULL_DEFENCE_INTERMEDIATE_CLAIM: data.CLAIMANT_RESPONSE_INTERMEDIATE_CLAIM('FULL_DEFENCE'),
      FULL_ADMISSION: data.CLAIMANT_RESPONSE_1v2('FULL_ADMISSION'),
      PART_ADMISSION: data.CLAIMANT_RESPONSE_1v2('PART_ADMISSION'),
      NOT_PROCEED: data.CLAIMANT_RESPONSE_1v2('NOT_PROCEED'),
    },
    TWO_V_ONE: {
      FULL_DEFENCE: data.CLAIMANT_RESPONSE_2v1('FULL_DEFENCE'),
      FULL_DEFENCE_MEDIATION: data.CLAIMANT_RESPONSE_2v1('FULL_DEFENCE', true),
      FULL_ADMISSION: data.CLAIMANT_RESPONSE_2v1('FULL_ADMISSION'),
      PART_ADMISSION: data.CLAIMANT_RESPONSE_2v1('PART_ADMISSION'),
      NOT_PROCEED: data.CLAIMANT_RESPONSE_2v1('NOT_PROCEED')
    }
  },
  sdoTracks: {
    CREATE_FAST_NO_SUM: data.CREATE_FAST_NO_SUM_SPEC(),
  }
};

const validateEventPagesFlightDelay = async (data, solicitor) => {
  //transform the data
  console.log('validateEventPages....');
  for (let pageId of Object.keys(data.valid)) {
    if (pageId === 'DefendantLitigationFriend' || pageId === 'DocumentUpload' || pageId === 'Upload' || pageId === 'DraftDirections'|| pageId === 'ApplicantDefenceResponseDocument' || pageId === 'DraftDirections' || pageId === 'FinalOrderPreview') {
      const document = await testingSupport.uploadDocument();
      data = await updateCaseDataWithPlaceholders(data, document);
    }
    // data = await updateCaseDataWithPlaceholders(data);
    await assertValidDataForEvidenceUpload(data, pageId, solicitor);
  }
};

const assertValidDataForEvidenceUpload = async (data, pageId, solicitor) => {
  console.log(`asserting page: ${pageId} has valid data`);

  const validDataForPage = data.valid[pageId];
  caseData = {...caseData, ...validDataForPage};
  caseData = adjustDataForSolicitor(solicitor, caseData);
  const response = await apiRequest.validatePage(
    eventName,
    pageId,
    caseData,
    addCaseId(pageId) ? caseId : null
  );

  let responseBody = await response.json();
  responseBody = clearDataForSearchCriteria(responseBody); //Until WA release
  responseBody = clearNoCData(responseBody);
  if (eventName === 'INFORM_AGREED_EXTENSION_DATE' && mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP') {
    responseBody = clearDataForExtensionDate(responseBody, solicitor);
  } else if (eventName === 'DEFENDANT_RESPONSE' && mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP') {
    responseBody = clearDataForDefendantResponse(responseBody, solicitor);
  }

  if(eventName === 'EVIDENCE_UPLOAD_APPLICANT') {
    responseBody = clearDataForEvidenceUpload(responseBody, eventName);
    delete responseBody.data['respondent1DQExperts'];
    delete responseBody.data['responseClaimMediationSpecRequired'];
    delete responseBody.data['responseClaimExpertSpecRequired'];
    delete responseBody.data['responseClaimCourtLocationRequired'];
    delete responseBody.data['responseClaimWitnesses'];
    delete responseBody.data['respondent1DQRequestedCourt'];
    delete responseBody.data['respondent1DQWitnessesSmallClaim'];
    delete responseBody.data['respondent1DQWitnesses'];
  } else if(eventName === 'EVIDENCE_UPLOAD_RESPONDENT') {
    responseBody = clearDataForEvidenceUpload(responseBody, eventName);
    delete responseBody.data['businessProcess'];
  }

  if(eventName === 'HEARING_SCHEDULED' && pageId === 'HearingNoticeSelect')
  {
    responseBody = clearHearingLocationData(responseBody);
  }
  if(eventName === 'GENERATE_DIRECTIONS_ORDER') {
    responseBody = clearFinalOrderLocationData(responseBody);
  }
  assert.equal(response.status, 200);


  let claimValue;
  if (data.valid && data.valid.ClaimValue && data.valid.ClaimValue.claimValue
    && data.valid.ClaimValue.claimValue.statementOfValueInPennies) {
    claimValue = ''+data.valid.ClaimValue.claimValue.statementOfValueInPennies/100;
  }
  if (Object.prototype.hasOwnProperty.call(midEventFieldForPage, pageId)) {
    addMidEventFields(pageId, responseBody, eventName === 'CREATE_SDO' ? data : null, claimValue);
    caseData = removeUiFields(pageId, caseData);
  } else if (eventName === 'CREATE_SDO' && data.midEventData && data.midEventData[pageId]) {
    addMidEventFields(pageId, responseBody, eventName === 'CREATE_SDO' ? data : null, claimValue);
  }
  if (!(responseBody.data.applicant1DQRemoteHearing) && caseData.applicant1DQRemoteHearing) {
    // CIV-3883 depends on backend having the field
    responseBody.data.applicant1DQRemoteHearing = caseData.applicant1DQRemoteHearing;
  }
  if (eventName === 'CREATE_SDO') {
    if(['ClaimsTrack', 'OrderType'].includes(pageId)) {
      delete caseData.hearingMethodValuesDisposalHearing;
      delete caseData.hearingMethodValuesFastTrack;
      delete caseData.hearingMethodValuesSmallClaims;
    }
    if (responseBody.data.sdoOrderDocument) {
      caseData.sdoOrderDocument = responseBody.data.sdoOrderDocument;
    }

    // noinspection EqualityComparisonWithCoercionJS
    if (caseData.drawDirectionsOrder && caseData.drawDirectionsOrder.judgementSum
      && responseBody.data.drawDirectionsOrder && responseBody.data.drawDirectionsOrder.judgementSum
      && caseData.drawDirectionsOrder.judgementSum !== responseBody.data.drawDirectionsOrder.judgementSum
      && caseData.drawDirectionsOrder.judgementSum == responseBody.data.drawDirectionsOrder.judgementSum) {
      // sometimes difference may be because of decimals .0, not an actual difference
      caseData.drawDirectionsOrder.judgementSum = responseBody.data.drawDirectionsOrder.judgementSum;
    }
    if (pageId === 'ClaimsTrack'
      && !(responseBody.data.disposalHearingSchedulesOfLoss)) {
      // disposalHearingSchedulesOfLoss is populated on pageId SDO but then in pageId ClaimsTrack has been removed
      delete caseData.disposalHearingSchedulesOfLoss;
    }
  }

  if (pageId === 'Claimant') {
    delete caseData.applicant1OrganisationPolicy;
  }

  if (responseBody.data.requestForReconsiderationDeadline) {
    caseData.requestForReconsiderationDeadline = responseBody.data.requestForReconsiderationDeadline;
  }
  try {
    assert.deepEqual(responseBody.data, caseData);
  }
  catch(err) {
    console.error('Validate data is failed due to a mismatch ..', err);
    console.error('Data different in page ' + pageId);
    whatsTheDifference(caseData, responseBody.data);
    throw err;
  }
};

const newSdoR2FieldsSmallClaims = {
  sdoR2SmallClaimsWitnessStatementOther: (data) => {
    return typeof data.sdoStatementOfWitness === 'string'
      && typeof data.isRestrictWitness === 'string'
      && typeof data.isRestrictPages === 'string'
      && typeof data.text === 'string';
  },
};

const newSdoR2FastTrackCreditHireFields ={
  sdoR2FastTrackCreditHire: (data) => {
    return typeof data.input1 === 'string'
      && typeof data.input5 === 'string'
      && typeof data.input6 === 'string'
      && typeof data.input7 === 'string'
      && typeof data.input8 === 'string';
  }
};

/**
 * helper function to help locate differences between expected and actual.
 *
 * @param caseData expected
 * @param responseBodyData actual
 * @param path initially undefined
 */
function whatsTheDifference(caseData, responseBodyData, path) {
  Object.keys(caseData).forEach(key => {
    if (Object.keys(responseBodyData).indexOf(key) < 0) {
      console.log('response does not have ' + appendToPath(path, key)
        + '. CaseData has ' + JSON.stringify(caseData[key]));
    } else if (typeof caseData[key] === 'object') {
      whatsTheDifference(caseData[key], responseBodyData[key], [key]);
    } else if (caseData[key] !== responseBodyData[key]) {
      console.log('response and case data are different on ' + appendToPath(path, key));
      console.log('caseData has ' + caseData[key] + ' while response has ' + responseBodyData[key]);
    }
  });
  Object.keys(responseBodyData).forEach(key => {
    if (Object.keys(caseData).indexOf(key) < 0) {
      console.log('caseData does not have ' + appendToPath(path, key)
        + '. Response has ' + JSON.stringify(responseBodyData[key]));
    }
  });
}

function removeUuidsFromDynamicList(data, dynamicListField) {
  const dynamicElements = data[dynamicListField].list_items;

  return dynamicElements.map(({code, ...item}) => item);
}

function appendToPath(path, key) {
  if (path) {
    return path.concat([key]);
  } else {
    return [key];
  }
}

function addMidEventFields(pageId, responseBody, instanceData, claimAmount) {
  console.log(`Adding mid event fields for pageId: ${pageId}`);
  const midEventField = midEventFieldForPage[pageId];
  let midEventData;
  let calculated;

  if (instanceData && instanceData.calculated && instanceData.calculated[pageId]) {
    calculated = instanceData.calculated[pageId];
  }
  if(pageId === 'ClaimsTrack' || pageId === 'OrderType' || pageId === 'SmallClaims') {
    calculated = {...calculated};
  }
  if(eventName === 'CREATE_CLAIM'){
    midEventData = data[eventName](mpScenario, claimAmount).midEventData[pageId];
  } else if(eventName === 'CLAIMANT_RESPONSE'){
    midEventData = data[eventName](mpScenario).midEventData[pageId];
  } else if(eventName === 'DEFENDANT_RESPONSE'){
    midEventData = data[eventName]().midEventData[pageId];
  } else if (instanceData && instanceData.midEventData && instanceData.midEventData[pageId]) {
    midEventData = instanceData.midEventData[pageId];
  } else {
    midEventData = data[eventName].midEventData[pageId];
  }
  if (calculated) {
    checkCalculated(calculated, responseBody.data);
  }

  if (midEventField && midEventField.dynamicList === true && midEventField.id != 'applicantSolicitor1PbaAccounts') {
    assertDynamicListListItemsHaveExpectedLabels(responseBody, midEventField.id, midEventData);
  }
  if(pageId === 'ClaimsTrack' && typeof midEventData.isSdoR2NewScreen === 'undefined') {
    let sdoR2Var = { ['isSdoR2NewScreen'] : 'No' };
    midEventData = {...midEventData, ...sdoR2Var};
  }

  if(pageId === 'SmallClaims') {
    delete caseData.isSdoR2NewScreen;
  }

  caseData = {...caseData, ...midEventData};
  if (midEventField) {
    responseBody.data[midEventField.id] = caseData[midEventField.id];
  }
}

function checkCalculated(calculated, responseBodyData) {
  const checked = {};
  // strictly check
  Object.keys(calculated).forEach(key => {
    if (caseData[key]) {
      if (calculated[key].call(null, caseData[key]) !== false) {
        checked[key] = caseData[key];
      } else {
        console.log('Failed calculated key on caseData ' + key);
      }
    } else if (responseBodyData[key]) {
      if (calculated[key].call(null, responseBodyData[key]) !== false) {
        checked[key] = caseData[key];
      } else {
        console.log('Failed calculated key on responseBody' + key);
      }
    }
  });
  // update
  Object.keys(checked).forEach((key) => {
    if (caseData[key]) {
      responseBodyData[key] = caseData[key];
    } else {
      caseData[key] = responseBodyData[key];
    }
  });
}

function assertDynamicListListItemsHaveExpectedLabels(responseBody, dynamicListFieldName, midEventData) {
  const actualDynamicElementLabels = removeUuidsFromDynamicList(responseBody.data, dynamicListFieldName);
  const expectedDynamicElementLabels = removeUuidsFromDynamicList(midEventData, dynamicListFieldName);

  expect(actualDynamicElementLabels).to.deep.equalInAnyOrder(expectedDynamicElementLabels);
}

function removeUiFields(pageId, caseData) {
  console.log(`Removing ui fields for pageId: ${pageId}`);
  const midEventField = midEventFieldForPage[pageId];

  if (midEventField.uiField.remove === true) {
    const fieldToRemove = midEventField.uiField.field;
    delete caseData[fieldToRemove];
  }
  return caseData;
}

const midEventFieldForPage = {
  ClaimValue: {
    id: 'applicantSolicitor1PbaAccounts',
    dynamicList: true,
    uiField: {
      remove: false,
    },
  },
  ClaimantLitigationFriend: {
    id: 'applicantSolicitor1CheckEmail',
    dynamicList: false,
    uiField: {
      remove: false,
    },
  },
  StatementOfTruth: {
    id: 'applicantSolicitor1ClaimStatementOfTruth',
    dynamicList: false,
    uiField: {
      remove: true,
      field: 'uiStatementOfTruth'
    },
  }
};

const clearNoCData = (responseBody) => {
  delete responseBody.data['changeOfRepresentation'];
  return responseBody;
};

const clearHearingLocationData = (responseBody) => {
  delete responseBody.data['hearingLocation'];
  return responseBody;
};

const clearDataForExtensionDate = (responseBody, solicitor) => {
  delete responseBody.data['businessProcess'];
  delete responseBody.data['caseNotes'];
  delete responseBody.data['systemGeneratedCaseDocuments'];
  delete responseBody.data['respondent1OrganisationIDCopy'];
  delete responseBody.data['respondent2OrganisationIDCopy'];


  // solicitor cannot see data from respondent they do not represent
  if (solicitor === 'solicitorOne') {
    delete responseBody.data['respondent2ResponseDeadline'];
  }

  if (solicitor === 'solicitorTwo') {
    delete responseBody.data['respondent1'];
    delete responseBody.data['respondent1ResponseDeadline'];
  } else {
    delete responseBody.data['respondent2'];
  }
  return responseBody;
};

const clearDataForDefendantResponse = (responseBody, solicitor) => {
  delete responseBody.data['businessProcess'];
  delete responseBody.data['caseNotes'];
  delete responseBody.data['systemGeneratedCaseDocuments'];
  delete responseBody.data['respondentSolicitor2Reference'];
  delete responseBody.data['respondent1OrganisationIDCopy'];
  delete responseBody.data['respondent2OrganisationIDCopy'];

  // solicitor cannot see data from respondent they do not represent
  if (solicitor === 'solicitorOne') {
    delete responseBody.data['respondent2ResponseDeadline'];
  }
  if (solicitor === 'solicitorTwo') {
    delete responseBody.data['respondent1'];
    delete responseBody.data['respondent1ClaimResponseType'];
    delete responseBody.data['respondent1ClaimResponseDocument'];
    delete responseBody.data['respondent1DQFileDirectionsQuestionnaire'];
    delete responseBody.data['respondent1DQDisclosureOfElectronicDocuments'];
    delete responseBody.data['respondent1DQDisclosureOfNonElectronicDocuments'];
    delete responseBody.data['respondent1DQExperts'];
    delete responseBody.data['respondent1DQWitnesses'];
    delete responseBody.data['respondent1DQLanguage'];
    delete responseBody.data['respondent1DQHearing'];
    delete responseBody.data['respondent1DQHearingSupport'];
    delete responseBody.data['respondent1DQVulnerabilityQuestions'];
    delete responseBody.data['respondent1DQDraftDirections'];
    delete responseBody.data['respondent1DQRequestedCourt'];
    delete responseBody.data['respondent1DQRemoteHearing'];
    delete responseBody.data['respondent1DQFurtherInformation'];
    delete responseBody.data['respondent1DQFurtherInformation'];
    delete responseBody.data['respondent1ResponseDeadline'];
    delete responseBody.data['respondent1Experts'];
    delete responseBody.data['respondent1Witnesses'];
    delete responseBody.data['respondent1DetailsForClaimDetailsTab'];
  } else {
    delete responseBody.data['respondent2'];
  }
  return responseBody;
};

const clearDataForEvidenceUpload = (responseBody, eventName) => {
  delete responseBody.data['caseNoteType'];
  delete responseBody.data['caseNotes'];
  delete responseBody.data['caseNotesTA'];
  delete responseBody.data['disposalHearingFinalDisposalHearingTimeDJ'];
  delete responseBody.data['disposalHearingHearingNotesDJ'];
  delete responseBody.data['disposalHearingOrderMadeWithoutHearingDJ'];
  delete responseBody.data['documentAndName'];
  delete responseBody.data['documentAndNote'];
  delete responseBody.data['hearingNotes'];
  delete responseBody.data['respondent1OrganisationIDCopy'];
  delete responseBody.data['respondent2OrganisationIDCopy'];
  delete responseBody.data['applicantExperts'];
  delete responseBody.data['applicantWitnesses'];
  delete responseBody.data['disposalHearingBundle'];
  delete responseBody.data['disposalHearingBundleToggle'];
  delete responseBody.data['disposalHearingClaimSettlingToggle'];
  delete responseBody.data['disposalHearingCostsToggle'];
  delete responseBody.data['disposalHearingDisclosureOfDocuments'];
  delete responseBody.data['disposalHearingDisclosureOfDocumentsToggle'];
  delete responseBody.data['disposalHearingFinalDisposalHearing'];
  delete responseBody.data['disposalHearingFinalDisposalHearingToggle'];
  delete responseBody.data['disposalHearingJudgementDeductionValue'];
  delete responseBody.data['disposalHearingJudgesRecital'];
  delete responseBody.data['disposalHearingMedicalEvidence'];
  delete responseBody.data['disposalHearingMedicalEvidenceToggle'];
  delete responseBody.data['disposalHearingMethodInPerson'];
  delete responseBody.data['disposalHearingMethodToggle'];
  delete responseBody.data['disposalHearingNotes'];
  delete responseBody.data['disposalHearingQuestionsToExperts'];
  delete responseBody.data['disposalHearingQuestionsToExpertsToggle'];
  delete responseBody.data['disposalHearingSchedulesOfLossToggle'];
  delete responseBody.data['disposalHearingWitnessOfFact'];
  delete responseBody.data['disposalHearingWitnessOfFactToggle'];
  delete responseBody.data['drawDirectionsOrder'];
  delete responseBody.data['drawDirectionsOrderRequired'];
  delete responseBody.data['drawDirectionsOrderSmallClaims'];
  delete responseBody.data['fastTrackAddNewDirections'];
  delete responseBody.data['fastTrackAllocation'];
  delete responseBody.data['fastTrackAltDisputeResolutionToggle'];
  delete responseBody.data['fastTrackBuildingDispute'];
  delete responseBody.data['fastTrackClinicalNegligence'];
  delete responseBody.data['fastTrackCostsToggle'];
  delete responseBody.data['fastTrackCreditHire'];
  delete responseBody.data['fastTrackDisclosureOfDocuments'];
  delete responseBody.data['fastTrackDisclosureOfDocumentsToggle'];
  delete responseBody.data['fastTrackHearingNotes'];
  delete responseBody.data['fastTrackHearingTime'];
  delete responseBody.data['fastTrackHousingDisrepair'];
  delete responseBody.data['fastTrackJudgementDeductionValue'];
  delete responseBody.data['fastTrackJudgesRecital'];
  delete responseBody.data['fastTrackMethod'];
  delete responseBody.data['fastTrackMethodInPerson'];
  delete responseBody.data['fastTrackMethodTelephoneHearing'];
  delete responseBody.data['fastTrackMethodToggle'];
  delete responseBody.data['fastTrackNotes'];
  delete responseBody.data['fastTrackOrderWithoutJudgement'];
  delete responseBody.data['fastTrackPersonalInjury'];
  delete responseBody.data['fastTrackRoadTrafficAccident'];
  delete responseBody.data['fastTrackSchedulesOfLoss'];
  delete responseBody.data['fastTrackSchedulesOfLossToggle'];
  delete responseBody.data['fastTrackSettlementToggle'];
  delete responseBody.data['fastTrackTrial'];
  delete responseBody.data['fastTrackTrialToggle'];
  delete responseBody.data['fastTrackTrialBundleToggle'];
  delete responseBody.data['fastTrackVariationOfDirectionsToggle'];
  delete responseBody.data['fastTrackWitnessOfFact'];
  delete responseBody.data['fastTrackWitnessOfFactToggle'];
  delete responseBody.data['orderType'];
  delete responseBody.data['finalOrderTrackToggle'];
  delete responseBody.data['respondent1Experts'];
  delete responseBody.data['respondent1Witnesses'];
  delete responseBody.data['setFastTrackFlag'];
  delete responseBody.data['setSmallClaimsFlag'];
  delete responseBody.data['smallClaimsCreditHire'];
  delete responseBody.data['smallClaimsDocuments'];
  delete responseBody.data['smallClaimsDocumentsToggle'];
  delete responseBody.data['smallClaimsHearing'];
  delete responseBody.data['smallClaimsHearingToggle'];
  delete responseBody.data['smallClaimsJudgementDeductionValue'];
  delete responseBody.data['smallClaimsJudgesRecital'];
  delete responseBody.data['smallClaimsMethod'];
  delete responseBody.data['smallClaimsMethodInPerson'];
  delete responseBody.data['smallClaimsMethodToggle'];
  delete responseBody.data['smallClaimsNotes'];
  delete responseBody.data['smallClaimsWitnessStatementToggle'];
  delete responseBody.data['smallClaimsWitnessStatement'];
  delete responseBody.data['smallClaimsRoadTrafficAccident'];
  delete responseBody.data['documentAndNoteToAdd'];
  delete responseBody.data['documentAndNameToAdd'];
  delete responseBody.data['channel'];
  delete responseBody.data['disposalHearingMethodTelephoneHearing'];
  delete responseBody.data['disposalHearingSchedulesOfLoss'];
  delete responseBody.data['disposalHearingMethod'];
  delete responseBody.data['hearingNoticeList'];
  delete responseBody.data['information'];
  delete responseBody.data['hearingDueDate'];
  delete responseBody.data['disposalHearingAddNewDirections'];
  delete responseBody.data['hearingFee'];
  delete responseBody.data['hearingFeePBADetails'];
  delete responseBody.data['hearingNoticeListOther'];
  delete responseBody.data['sdoR2SmallClaimsJudgesRecital'];
  delete responseBody.data['sdoR2SmallClaimsUploadDocToggle'];
  delete responseBody.data['sdoR2SmallClaimsUploadDoc'];
  delete responseBody.data['sdoR2SmallClaimsWitnessStatements'];
  delete responseBody.data['sdoR2SmallClaimsImpNotes'];
  delete responseBody.data['isSdoR2NewScreen'];
  delete responseBody.data['sdoR2SmallClaimsPPI'];
  delete responseBody.data['sdoR2SmallClaimsHearing'];
  delete responseBody.data['sdoR2SmallClaimsWitnessStatementsToggle'];
  delete responseBody.data['sdoR2SmallClaimsHearingToggle'];
  delete responseBody.data['smallClaims'];
  delete responseBody.data['smallClaimsFlightDelay'];
  delete responseBody.data['smallClaimsFlightDelayToggle'];
  delete responseBody.data['hearingMethodValuesDisposalHearing'];
  delete responseBody.data['hearingMethodValuesFastTrack'];
  delete responseBody.data['hearingMethodValuesSmallClaims'];
  delete responseBody.data['smallClaimsAddNewDirections'];
  delete responseBody.data['applicant1DQStatementOfTruth'];
  delete responseBody.data['respondent1DQStatementOfTruth'];
  delete responseBody.data['sdoR2SmallClaimsUseOfWelshLanguage'];
  delete responseBody.data['sdoR2NihlUseOfWelshLanguage'];
  delete responseBody.data['sdoR2FastTrackUseOfWelshLanguage'];
  delete responseBody.data['sdoR2DrhUseOfWelshLanguage'];
  delete responseBody.data['sdoR2DisposalHearingUseOfWelshLanguage'];
  delete responseBody.data['sdoR2SmallClaimsWitnessStatementOther'];
  delete responseBody.data['sdoR2FastTrackWitnessOfFact'];
  delete responseBody.data['sdoR2FastTrackCreditHire'];
  delete responseBody.data['sdoDJR2TrialCreditHire'];

  responseBody = clearNIHLDataFromResponseBody(responseBody);

  if(mpScenario === 'TWO_V_ONE' && eventName === 'EVIDENCE_UPLOAD_RESPONDENT') {
    delete responseBody.data['evidenceUploadOptions'];
  }

  if ( eventName === 'EVIDENCE_UPLOAD_RESPONDENT') {
    delete responseBody.data['claimantResponseScenarioFlag'];
    delete responseBody.data['claimant2ResponseFlag'];
    delete responseBody.data['claimantResponseDocumentToDefendant2Flag'];
    delete responseBody.data['applicantsProceedIntention'];
  }

  return responseBody;
};

const clearNIHLDataFromResponseBody = (responseBody) => {
  delete responseBody.data['sdoR2ImportantNotesTxt'];
  delete responseBody.data['sdoR2SeparatorUploadOfDocumentsToggle'];
  delete responseBody.data['sdoR2UploadOfDocuments'];
  delete responseBody.data['sdoR2SeparatorAddendumReportToggle'];
  delete responseBody.data['sdoR2SeparatorPermissionToRelyOnExpertToggle'];
  delete responseBody.data['sdoR2Trial'];
  delete responseBody.data['sdoR2EvidenceAcousticEngineer'];
  delete responseBody.data['sdoR2TrialToggle'];
  delete responseBody.data['sdoR2DisclosureOfDocumentsToggle'];
  delete responseBody.data['sdoAltDisputeResolution'];
  delete responseBody.data['sdoR2AddendumReport'];
  delete responseBody.data['sdoR2DisclosureOfDocuments'];
  delete responseBody.data['sdoR2SeparatorFurtherAudiogramToggle'];
  delete responseBody.data['sdoR2QuestionsToEntExpert'];
  delete responseBody.data['sdoR2SeparatorExpertEvidenceToggle'];
  delete responseBody.data['sdoR2ExpertEvidence'];
  delete responseBody.data['sdoR2Settlement'];
  delete responseBody.data['sdoFastTrackJudgesRecital'];
  delete responseBody.data['sdoR2SeparatorWitnessesOfFactToggle'];
  delete responseBody.data['sdoR2QuestionsClaimantExpert'];
  delete responseBody.data['sdoR2SeparatorQuestionsToEntExpertToggle'];
  delete responseBody.data['sdoR2ScheduleOfLossToggle'];
  delete responseBody.data['sdoR2ScheduleOfLoss'];
  delete responseBody.data['sdoR2SeparatorEvidenceAcousticEngineerToggle'];
  delete responseBody.data['sdoVariationOfDirections'];
  delete responseBody.data['sdoR2FurtherAudiogram'];
  delete responseBody.data['sdoR2WitnessesOfFact'];
  delete responseBody.data['sdoR2SeparatorQuestionsClaimantExpertToggle'];
  delete responseBody.data['sdoR2PermissionToRelyOnExpert'];
  delete responseBody.data['sdoR2ImportantNotesDate'];

  return responseBody;
};

const clearFinalOrderLocationData = (responseBody) => {
  delete responseBody.data['finalOrderFurtherHearingComplex'];
  if (responseBody.data.finalOrderDownloadTemplateOptions) {
    caseData.finalOrderDownloadTemplateOptions = responseBody.data.finalOrderDownloadTemplateOptions;
  }
  if (responseBody.data.finalOrderDownloadTemplateDocument) {
    caseData.finalOrderDownloadTemplateDocument = responseBody.data.finalOrderDownloadTemplateDocument;
  }
  return responseBody;
};

const adjustDataForSolicitor = (user, data) => {
  let fixtureClone = cloneDeep(data);
  if (mpScenario !== 'ONE_V_TWO_TWO_LEGAL_REP') {
    delete fixtureClone['defendantSolicitorNotifyClaimOptions'];
  }
  if (user === 'solicitorOne') {
    delete fixtureClone['respondent2ResponseDeadline'];
  } else if (user === 'solicitorTwo') {
    delete fixtureClone['respondent1ResponseDeadline'];
  }
  return fixtureClone;
};

const addCaseId = (pageId) => {
  return isDifferentSolicitorForDefendantResponseOrExtensionDate() || isEvidenceUpload(pageId) || isManageContactInformation();
};

const isEvidenceUpload = (pageId) => {
  return (pageId === 'DocumentSelectionFastTrack'
      || pageId === 'DocumentSelectionSmallClaim')
    && (eventName === 'EVIDENCE_UPLOAD_APPLICANT'
      || eventName === 'EVIDENCE_UPLOAD_RESPONDENT');
};

const isManageContactInformation = () => {
  return eventName === 'MANAGE_CONTACT_INFORMATION';
};

const isDifferentSolicitorForDefendantResponseOrExtensionDate = () => {
  return (mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP' && (eventName === 'DEFENDANT_RESPONSE' || eventName === 'INFORM_AGREED_EXTENSION_DATE'));
};

module.exports = {

  /**
   * Creates a claim
   *
   * @param user user to create the claim
   * @return {Promise<void>}
   */
  createClaimWithRepresentedRespondent: async (user, scenario = 'ONE_V_ONE', carmEnabled = false,
                                               isMintiCase = false, mintiClaimAmount = '00000') => {
    const pbaV3 = await checkToggleEnabled(PBAv3);
    eventName = 'CREATE_CLAIM_SPEC';
    caseId = null;
    caseData = {};

    let createClaimData  = {};
    createClaimData = data.CREATE_CLAIM(scenario, pbaV3, isMintiCase, mintiClaimAmount);

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    for (let pageId of Object.keys(createClaimData.userInput)) {
      await assertValidData(createClaimData, pageId);
    }

    await assertSubmittedEvent('PENDING_CASE_ISSUED');

    await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);

    if (scenario === 'ONE_V_TWO_SAME_SOL' && createClaimData.userInput.SameLegalRepresentative
      && createClaimData.userInput.SameLegalRepresentative.respondent2SameLegalRepresentative === 'Yes') {
      await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORTWO', config.defendantSolicitorUser);
    }

    if (scenario === 'ONE_V_TWO'
      && createClaimData.userInput.SameLegalRepresentative
      && createClaimData.userInput.SameLegalRepresentative.respondent2SameLegalRepresentative === 'No') {
        await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORTWO', config.secondDefendantSolicitorUser);
    }

    await waitForFinishedBusinessProcess(caseId);


    console.log('Is PBAv3 toggle on?: ' + pbaV3);

    if (pbaV3) {
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
        claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    }

    await waitForFinishedBusinessProcess(caseId);
    if(await checkCaseFlagsEnabled()) {
      await assertFlagsInitialisedAfterCreateClaim(config.adminUser, caseId);
    }
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'CASE_ISSUED');

    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');

    await adjustCaseSubmittedDateForCarm(caseId, carmEnabled);
    const isMintiToggleEnabled = await checkMintiToggleEnabled();
    await adjustCaseSubmittedDateForMinti(caseId, (isMintiToggleEnabled && isMintiCase), carmEnabled);

    return caseId;
  },

  createClaimSpecFlightDelay: async (user, scenario = 'ONE_V_ONE_FLIGHT_DELAY', carmEnabled = false) => {
    const pbaV3 = await checkToggleEnabled(PBAv3);
    eventName = 'CREATE_CLAIM_SPEC';
    caseId = null;
    caseData = {};

    let createClaimData  = {};

    createClaimData = data.CREATE_CLAIM(scenario, pbaV3);
    //==============================================================

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    for (let pageId of Object.keys(createClaimData.userInput)) {
      await assertValidData(createClaimData, pageId);
    }

    await assertSubmittedEvent('PENDING_CASE_ISSUED');

    await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);

    await waitForFinishedBusinessProcess(caseId);


    console.log('Is PBAv3 toggle on?: ' + pbaV3);

    if (pbaV3) {
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
        claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    }

    await waitForFinishedBusinessProcess(caseId);
    if(await checkCaseFlagsEnabled()) {
      await assertFlagsInitialisedAfterCreateClaim(config.adminUser, caseId);
    }
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'CASE_ISSUED');

    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');
    await adjustCaseSubmittedDateForCarm(caseId, carmEnabled);
  },

  informAgreedExtensionDate: async (user) => {
    eventName = 'INFORM_AGREED_EXTENSION_DATE_SPEC';
    await apiRequest.setupTokens(user);
    caseData = await apiRequest.startEvent(eventName, caseId);
    const pbaV3 = await checkToggleEnabled(PBAv3);

    let informAgreedExtensionData = await data.INFORM_AGREED_EXTENSION_DATE(pbaV3 ? 'CREATE_CLAIM_SPEC_AFTER_PAYMENT': 'CREATE_CLAIM_SPEC');
    informAgreedExtensionData.userInput.ExtensionDate.respondentSolicitor1AgreedDeadlineExtension = await dateNoWeekends(42);

    for (let pageId of Object.keys(informAgreedExtensionData.userInput)) {
      await assertValidData(informAgreedExtensionData, pageId);
    }

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  },

  cleanUp: async () => {
    await unAssignAllUsers();
  },

  defendantResponse: async (user, response = 'FULL_DEFENCE', scenario = 'ONE_V_ONE',
                            expectedEvent = 'AWAITING_APPLICANT_INTENTION', carmEnabled = false,
                            isMintiCase = false, claimAmountMinti, djSetaside=false) => {

    await adjustCaseSubmittedDateForCarm(caseId, carmEnabled, isMintiCase);
    await apiRequest.setupTokens(user);
    eventName = 'DEFENDANT_RESPONSE_SPEC';

    const pbaV3 = await checkToggleEnabled(PBAv3);
    if(pbaV3){
      response = response+'_PBAv3';
    }

    if(djSetaside){
      response = response+'_SetAside_DJ';
    }

    if(carmEnabled){
      response = response+'_Mediation';
    }

    if(isMintiCase){
      mintiClaimTrack = getMintiTrackByClaimAmount(claimAmountMinti);
      response = response+ '_' + mintiClaimTrack;
    }

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);

    let defendantResponseData = eventData['defendantResponses'][scenario][response];

    caseData = returnedCaseData;

    caseData = await addFlagsToFixture(caseData);

    console.log(`${response} ${scenario}`);

    await validateEventPages(defendantResponseData);

    switch (scenario) {
      case 'ONE_V_ONE_DIF_SOL':
        /* when camunda process is done, when both respondents have answered
        this should be AWAITING_APPLICANT_INTENTION; while only one has answered
        this will be AWAITING_RESPONDENT_ACKNOWLEDGEMENT
         */
        await assertSubmittedEvent(expectedEvent);
        break;
      case 'ONE_V_ONE':
        await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION');
        break;
      case 'ONE_V_TWO':
        await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION');
        break;
      case 'TWO_V_ONE':
        if (response === 'DIFF_FULL_DEFENCE' || response === 'DIFF_FULL_DEFENCE_PBAv3') {
          await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM');
        } else {
          await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION');
        }
        break;
    }

    await waitForFinishedBusinessProcess(caseId);

    const caseFlagsEnabled = await checkCaseFlagsEnabled();
    if (caseFlagsEnabled) {
      await assertCaseFlags(caseId, user, response);
    }
    deleteCaseFields('respondent1Copy');
    const isMintiToggleEnabled = await checkMintiToggleEnabled();
    let claimAmount = caseData.totalClaimAmount;
    if (!response.includes('COUNTER_CLAIM')) {
      await assertTrackAfterClaimCreation(config.adminUser, caseId, claimAmount, (isMintiCase && isMintiToggleEnabled), true);
    }
  },

  claimantResponse: async (user, response = 'FULL_DEFENCE', scenario = 'ONE_V_ONE',
                           expectedEndState, carmEnabled = false, isMintiCase = false) => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');
    deleteCaseFields('respondentResponseIsSame');

    await adjustCaseSubmittedDateForCarm(caseId, carmEnabled, isMintiCase);

    await apiRequest.setupTokens(user);

    eventName = 'CLAIMANT_RESPONSE_SPEC';
    caseData = await apiRequest.startEvent(eventName, caseId);
    caseData = await addFlagsToFixture(caseData);

    if (carmEnabled) {
      response = response+'_MEDIATION';
    }

    if (isMintiCase) {
      response = response+ '_' + mintiClaimTrack;
    }
    let claimantResponseData;

    const isJudgmentOnlineLive = await checkToggleEnabled(isJOLive);
    if (isJudgmentOnlineLive && response === 'FA_ACCEPT_CCJ') {
      claimantResponseData = data.CLAIMANT_RESPONSE_JBA(response);
    } else {
      claimantResponseData = eventData['claimantResponses'][scenario][response];
    }
    await validateEventPages(claimantResponseData);

    let validState = expectedEndState || 'PROCEEDS_IN_HERITAGE_SYSTEM';
    if (response === 'FULL_DEFENCE') {
      validState = 'JUDICIAL_REFERRAL';
    }

    carmEnabled ? validState = 'IN_MEDIATION' : validState;

    if (isJudgmentOnlineLive && response === 'FA_ACCEPT_CCJ') {
      validState = 'All_FINAL_ORDERS_ISSUED';
    }

    await assertSubmittedEvent(validState || 'PROCEEDS_IN_HERITAGE_SYSTEM');

    await waitForFinishedBusinessProcess(caseId);

    const caseFlagsEnabled = await checkCaseFlagsEnabled();
    if (caseFlagsEnabled) {
      await assertCaseFlags(caseId, user, response);
    }
  },

  initiateGeneralApplication: async (caseNumber, user, expectedState) => {
    eventName = 'INITIATE_GENERAL_APPLICATION';
    caseId = caseId || caseNumber;
    console.log('caseid is..', caseId);

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName, caseId);

    var isCOSCEnabled = await checkToggleEnabled(COSC);
    var gaData = isCOSCEnabled ? data.INITIATE_GENERAL_APPLICATION_LR : data.INITIATE_GENERAL_APPLICATION;
    const response = await apiRequest.submitEvent(eventName, gaData, caseId);
    const responseBody = await response.json();
    assert.equal(response.status, 201);
    assert.equal(responseBody.state, expectedState);

    console.log('General application created when main case state is', expectedState);
    assert.equal(responseBody.callback_response_status_code, 200);
  },

  mediationUnsuccessful: async (user, carmEnabled = false) => {
    eventName = 'MEDIATION_UNSUCCESSFUL';

    await apiRequest.setupTokens(user);
    caseData = await apiRequest.startEvent(eventName, caseId);
    caseData = {...caseData, ...mediationUnsuccessful.unsuccessfulMediation(carmEnabled)};
    await assertSubmittedEvent('JUDICIAL_REFERRAL');
    await waitForFinishedBusinessProcess(caseId);
    console.log('End of unsuccessful mediation');
  },

  uploadMediationDocuments: async (user, sameDefendantSolicitor = false) => {
    await apiRequest.setupTokens(user);

    let eventData;
    if (user === config.applicantSolicitorUser) {
      eventData = mediationDocuments.uploadMediationDocuments('claimant');
    }  else {
          eventData = mediationDocuments.uploadMediationDocuments(sameDefendantSolicitor || user === config.defendantSolicitorUser ? 'defendant' : 'defendantTwo', sameDefendantSolicitor);
        }

    eventName = 'UPLOAD_MEDIATION_DOCUMENTS';
    caseData = await apiRequest.startEvent(eventName, caseId);


    await validateEventPages(eventData);

    await assertSubmittedEvent('JUDICIAL_REFERRAL');
  },

  claimantResponseForFlightDelay: async (user, response = 'FULL_DEFENCE', scenario = 'ONE_V_ONE',
                           expectedEndState) => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');
    deleteCaseFields('respondentResponseIsSame');

    await apiRequest.setupTokens(user);

    eventName = 'CLAIMANT_RESPONSE_SPEC';
    caseData = await apiRequest.startEvent(eventName, caseId);
    caseData = await addFlagsToFixture(caseData);
    let claimantResponseData = eventData['claimantResponses'][scenario][response];

    for (let pageId of Object.keys(claimantResponseData.userInput)) {
      await assertValidData(claimantResponseData, pageId);
    }

    let validState = expectedEndState || 'PROCEEDS_IN_HERITAGE_SYSTEM';
    if (response == 'FULL_DEFENCE') {
      validState = 'JUDICIAL_REFERRAL';
    }


    await assertSubmittedEventFlightDelay(validState || 'PROCEEDS_IN_HERITAGE_SYSTEM');

    await waitForFinishedBusinessProcess(caseId);

    const caseFlagsEnabled = await checkCaseFlagsEnabled();
    if (caseFlagsEnabled) {
      await assertCaseFlags(caseId, user, response);
    }
  },

  amendClaimMovedToMediationDate: async (user, date) => {
    await apiRequest.setupTokens(user);
    let claimMovedToMediationDate ={};
    claimMovedToMediationDate = {'claimMovedToMediationOn': date};
    testingSupport.updateCaseData(caseId, claimMovedToMediationDate);
  },

  amendRespondent1ResponseDeadline: async (user) => {
    await apiRequest.setupTokens(user);
    let respondent1deadline ={};
    respondent1deadline = {'respondent1ResponseDeadline':'2022-01-10T15:59:50'};
    testingSupport.updateCaseData(caseId, respondent1deadline);
  },

  amendRespondent2ResponseDeadline: async (user) => {
    await apiRequest.setupTokens(user);
    let respondent2deadline ={};
    respondent2deadline = {'respondent2ResponseDeadline':'2022-01-10T15:59:50'};
    testingSupport.updateCaseData(caseId, respondent2deadline);
  },

  defaultJudgmentSpec: async (user, scenario, isDivergent) => {
    await apiRequest.setupTokens(user);

    let state;
    let registrationData;
    eventName = 'DEFAULT_JUDGEMENT_SPEC';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    const pbaV3 = await checkToggleEnabled(PBAv3);
    const isJudgmentOnlineLive = await checkToggleEnabled(isJOLive);
    if(pbaV3){
      let claimIssuedPBADetails = {
        claimIssuedPBADetails:{
          applicantsPbaAccounts: {
              value: {
                code:'66b21c60-aed1-11ed-8aa3-494efce63912',
                label:'PBAFUNC12345'
              },
            list_items:[
              {
                code:'66b21c60-aed1-11ed-8aa3-494efce63912',
                label:'PBAFUNC12345'
              },
              {
                code:'66b21c61-aed1-11ed-8aa3-494efce63912',
                label:'PBA0078095'
              }
            ]
          },
          fee:{
            calculatedAmountInPence:'8000',
            code:'FEE0205',
            version:'6'
          },
          serviceRequestReference:'2023-1676644996295'
        }
      };
      caseData = update(caseData, claimIssuedPBADetails);
    }

    if (scenario === 'ONE_V_TWO') {
      registrationData = {
        registrationTypeRespondentOne: [
          {
            value: {
            registrationType: 'R',
            judgmentDateTime: dateTime(0)
          },
          id: '9f30e576-f5b7-444f-8ba9-27dabb21d966' } ],
          registrationTypeRespondentTwo: [
            {
              value: {
              registrationType: 'R',
              judgmentDateTime: dateTime(0)
            },
            id: '9f30e576-f5b7-444f-8ba9-27dabb21d966' } ],
      };
      let DJSpec = isDivergent ? data.DEFAULT_JUDGEMENT_SPEC_1V2_DIVERGENT : data.DEFAULT_JUDGEMENT_SPEC_1V2;
      if (isJudgmentOnlineLive) {
        state = isDivergent ? 'PROCEEDS_IN_HERITAGE_SYSTEM' : 'All_FINAL_ORDERS_ISSUED';
      } else {
        state = 'PROCEEDS_IN_HERITAGE_SYSTEM';
      }
      await validateEventPagesDefaultJudgments(DJSpec, scenario,isDivergent);
    } else if (scenario === 'TWO_V_ONE') {
      registrationData = {
        registrationTypeRespondentOne: [
          {
            value: {
            registrationType: 'R',
            judgmentDateTime: dateTime(0)
          },
          id: '9f30e576-f5b7-444f-8ba9-27dabb21d966' } ],
          registrationTypeRespondentTwo: []
      };
      if (isJudgmentOnlineLive) {
        state = 'All_FINAL_ORDERS_ISSUED';
      } else {
        state = 'PROCEEDS_IN_HERITAGE_SYSTEM';
      }
      await validateEventPagesDefaultJudgments(data.DEFAULT_JUDGEMENT_SPEC_2V1, scenario,isDivergent);
    } else {
      registrationData = {
        registrationTypeRespondentOne: [
          {
            value: {
            registrationType: 'R',
            judgmentDateTime: dateTime(0)
          },
          id: '9f30e576-f5b7-444f-8ba9-27dabb21d966' } ],
          registrationTypeRespondentTwo: []
      };
      if (isJudgmentOnlineLive) {
        state = 'All_FINAL_ORDERS_ISSUED';
      } else {
        state = 'PROCEEDS_IN_HERITAGE_SYSTEM';
      }
      await validateEventPagesDefaultJudgments(data.DEFAULT_JUDGEMENT_SPEC, scenario,isDivergent);
    }

    caseData = update(caseData, registrationData);
    await assertSubmittedEvent(state, {
      header: '',
      body: ''
    }, true);



    await waitForFinishedBusinessProcess(caseId);
  },

  requestForReconsideration: async (user) => {
    console.log('RequestForReconsideration for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'REQUEST_FOR_RECONSIDERATION';

    let response = await apiRequest.startEventForCallbackError(eventName, caseId);
    assert(response === 'You can only request a reconsideration for claims of £1,000 or less.');
    await waitForFinishedBusinessProcess(caseId);
  },

  createSDO: async (user, response = 'CREATE_DISPOSAL') => {
    console.log('SDO for case id ' + caseId);
    await apiRequest.setupTokens(user);
    if (response === 'UNSUITABLE_FOR_SDO') {
      eventName = 'NotSuitable_SDO';
    } else {
      eventName = 'CREATE_SDO';
    }

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete caseData['sdoR2SmallClaimsUseOfWelshLanguage'];
    delete caseData['sdoR2NihlUseOfWelshLanguage'];
    delete caseData['sdoR2FastTrackUseOfWelshLanguage'];
    delete caseData['sdoR2DrhUseOfWelshLanguage'];
    delete caseData['sdoR2DisposalHearingUseOfWelshLanguage'];
    delete caseData['sdoR2SmallClaimsWitnessStatementOther'];
    delete caseData['sdoR2FastTrackWitnessOfFact'];
    delete caseData['sdoR2FastTrackCreditHire'];
    delete caseData['sdoDJR2TrialCreditHire'];

    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);
    if (response === 'CREATE_SMALL') {
      let disposalData = data.CREATE_SDO();
      delete disposalData.calculated.ClaimsTrack.smallClaimsWitnessStatement;
      disposalData.calculated.ClaimsTrack = {...disposalData.calculated.ClaimsTrack, ...newSdoR2FieldsSmallClaims};
      for (let pageId of Object.keys(disposalData.valid)) {
        await assertValidData(disposalData, pageId);
      }
    } else {
      let disposalData = data.CREATE_FAST_NO_SUM_SPEC();
      if (response === 'CREATE_FAST') {
        delete disposalData.calculated.FastTrack.fastTrackCreditHire;
        disposalData.calculated.FastTrack = {...disposalData.calculated.FastTrack, ...newSdoR2FastTrackCreditHireFields};
      }
      for (let pageId of Object.keys(disposalData.valid)) {
        await assertValidData(disposalData, pageId);
      }
    }

    if (response === 'UNSUITABLE_FOR_SDO') {
      await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM', null, false);
    } else {
      delete caseData['showConditionFlags'];
      await assertSubmittedEvent('CASE_PROGRESSION', null, false);
    }

    await waitForFinishedBusinessProcess(caseId);

  },

  evidenceUploadApplicant: async (user, mpScenario='') => {
    await apiRequest.setupTokens(user);
    eventName = 'EVIDENCE_UPLOAD_APPLICANT';
    caseData = await apiRequest.startEvent(eventName, caseId);

    console.log('caseData.caseProgAllocatedTrack ..', caseData.caseProgAllocatedTrack );

    if(caseData.caseProgAllocatedTrack === 'SMALL_CLAIM') {
      console.log('evidence upload small claim applicant for case id ' + caseId);
      let ApplicantEvidenceSmallClaimData = data.EVIDENCE_UPLOAD_APPLICANT_SMALL(mpScenario);
      await validateEventPagesFlightDelay(ApplicantEvidenceSmallClaimData);
    }
    if(caseData.caseProgAllocatedTrack === 'FAST_CLAIM' || caseData.caseProgAllocatedTrack === 'MULTI_CLAIM' || caseData.caseProgAllocatedTrack === 'INTERMEDIATE_CLAIM') {
      console.log('evidence upload applicant fast track for case id ' + caseId);
      let ApplicantEvidenceFastClaimData = data.EVIDENCE_UPLOAD_APPLICANT_FAST(mpScenario, caseData.caseProgAllocatedTrack);
      await validateEventPagesMinti(ApplicantEvidenceFastClaimData);
    }
    await assertSubmittedEvent('CASE_PROGRESSION', null, false);
    await waitForFinishedBusinessProcess(caseId);
  },

  evidenceUploadRespondent: async (user, multipartyScenario) => {
    await apiRequest.setupTokens(user);
    eventName = 'EVIDENCE_UPLOAD_RESPONDENT';
    mpScenario = multipartyScenario;
    caseData = await apiRequest.startEvent(eventName, caseId);

    if(caseData.caseProgAllocatedTrack === 'SMALL_CLAIM') {
      console.log('evidence upload small claim respondent for case id ' + caseId);
      let RespondentEvidenceSmallClaimData = data.EVIDENCE_UPLOAD_RESPONDENT_SMALL(mpScenario);
      await validateEventPagesFlightDelay(RespondentEvidenceSmallClaimData);
    }
    if(caseData.caseProgAllocatedTrack === 'FAST_CLAIM' || caseData.caseProgAllocatedTrack === 'MULTI_CLAIM' || caseData.caseProgAllocatedTrack === 'INTERMEDIATE_CLAIM') {
      console.log('evidence upload fast claim respondent for case id ' + caseId);
      let RespondentEvidenceFastClaimData = data.EVIDENCE_UPLOAD_RESPONDENT_FAST(mpScenario, caseData.caseProgAllocatedTrack);
      await validateEventPagesMinti(RespondentEvidenceFastClaimData);
    }
    await assertSubmittedEvent('CASE_PROGRESSION', null, false);
    await waitForFinishedBusinessProcess(caseId);
  },

  scheduleHearing: async (user, allocatedTrack, isMinti = false) => {
    console.log('Hearing Scheduled for case id ' + caseId);
    await apiRequest.setupTokens(user);

    eventName = 'HEARING_SCHEDULED';

    caseData = await apiRequest.startEvent(eventName, caseId);
    delete caseData['SearchCriteria'];

    let scheduleData = data.HEARING_SCHEDULED(allocatedTrack, isMinti);

    for (let pageId of Object.keys(scheduleData.userInput)) {
      await assertValidData(scheduleData, pageId);
    }
    delete caseData['showConditionFlags'];
    await assertSubmittedEvent('HEARING_READINESS', null, false);
    await waitForFinishedBusinessProcess(caseId);
  },

  amendHearingDueDate: async (user) => {
    let hearingDueDate = {};
    hearingDueDate = {'hearingDueDate': '2022-01-10'};
    await testingSupport.updateCaseData(caseId, hearingDueDate, user);
  },

  triggerBundle: async () => {
    const response_msg = await apiRequest.bundleTriggerEvent(caseId);
    const response = await response_msg.text();
    assert.equal(response, 'success');
  },

  hearingFeePaid: async (user) => {
    await apiRequest.setupTokens(user);

    await apiRequest.paymentUpdate(caseId, '/service-request-update',
      claimData.serviceUpdateDto(caseId, 'paid'));

    const response_msg = await apiRequest.hearingFeePaidEvent(caseId);
    assert.equal(response_msg.status, 200);
    console.log('Hearing Fee Paid');

    await apiRequest.setupTokens(config.applicantSolicitorUser);
    const caseState = (await apiRequest.fetchCaseDetails(config.applicantSolicitorUser, caseId)).state;
    assert.equal(caseState, 'PREPARE_FOR_HEARING_CONDUCT_HEARING');
    console.log('State moved to:'+caseState);
  },

  hearingFeePaidFlightDelay: async (user) => {
    await apiRequest.setupTokens(user);

    await apiRequest.paymentUpdate(caseId, '/service-request-update',
      claimData.serviceUpdateDto(caseId, 'paid'));

    const response_msg = await apiRequest.hearingFeePaidEvent(caseId);
    assert.equal(response_msg.status, 200);
    console.log('Hearing Fee Paid');

  },

  createCaseFlags: async (user) => {
    if(!(await checkCaseFlagsEnabled())) {
      return;
    }

    eventName = 'CREATE_CASE_FLAGS';

    await apiRequest.setupTokens(user);

    await addAndAssertCaseFlag('caseFlags', CASE_FLAGS.complexCase, caseId);

    const partyFlags = [...getPartyFlags(), ...getPartyFlags()];
    const caseFlagLocations = await getDefinedCaseFlagLocations(user, caseId);

    for (const [index, value] of caseFlagLocations.entries()) {
      await addAndAssertCaseFlag(value, partyFlags[index], caseId);
    }
  },

  manageCaseFlags: async (user) => {
    if(!(await checkCaseFlagsEnabled())) {
      return;
    }

    eventName = 'MANAGE_CASE_FLAGS';

    await apiRequest.setupTokens(user);

    await updateAndAssertCaseFlag('caseFlags', CASE_FLAGS.complexCase, caseId);

    const partyFlags = [...getPartyFlags(), ...getPartyFlags()];
    const caseFlagLocations = await getDefinedCaseFlagLocations(user, caseId);

    for(const [index, value] of caseFlagLocations.entries()) {
      await updateAndAssertCaseFlag(value, partyFlags[index], caseId);
    }
  },

  getCaseId: async () => {
    console.log (`case created: ${caseId}`);
    return caseId;
  },

  retrieveTaskDetails: async (user, caseNumber, taskId) => {
    return apiRequest.fetchTaskDetails(user, caseNumber, taskId);
  },

  assignTaskToUser: async (user, taskId) => {
    return apiRequest.taskActionByUser(user, taskId, 'claim');
  },

  completeTaskByUser: async (user, taskId) => {
    return apiRequest.taskActionByUser(user, taskId, 'complete');
  },

  checkUserCaseAccess: async (user, shouldHaveAccess) => {
    console.log(`Checking ${user.email} ${shouldHaveAccess ? 'has' : 'does not have'} access to the case.`);
    const expectedStatus = shouldHaveAccess ? 200 : 404;
    return await fetchCaseDetails(user, caseId, expectedStatus);
  },

  createFinalOrderJO: async (user, finalOrderRequestType, orderTrack) => {
    console.log(`case in Final Order ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'GENERATE_DIRECTIONS_ORDER';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    const dayPlus0 = await dateNoWeekendsBankHolidayNextDay(0);
    const dayPlus7 = await dateNoWeekendsBankHolidayNextDay(7);
    const dayPlus14 = await dateNoWeekendsBankHolidayNextDay(14);
    const dayPlus21 = await dateNoWeekendsBankHolidayNextDay(21);

    if (finalOrderRequestType === 'ASSISTED_ORDER') {
      await validateEventPages(data.FINAL_ORDERS_SPEC('ASSISTED_ORDER',  dayPlus0, dayPlus7, dayPlus14, dayPlus21));
    }
    if (finalOrderRequestType === 'FREE_FORM_ORDER') {
      await validateEventPages(data.FINAL_ORDERS_SPEC('FREE_FORM_ORDER',  dayPlus0, dayPlus7, dayPlus14, dayPlus21));
    }
    if (finalOrderRequestType === 'DOWNLOAD_ORDER_TEMPLATE') {
      await validateEventPages(data.FINAL_ORDERS_SPEC('DOWNLOAD_ORDER_TEMPLATE', dayPlus0, dayPlus7, dayPlus14, dayPlus21, orderTrack));
    }

    await apiRequest.startEvent(eventName, caseId);
    delete caseData['showConditionFlags'];
    await apiRequest.submitEvent(eventName, caseData, caseId);

    await waitForFinishedBusinessProcess(caseId);
  },

  confirmOrderReview: async (user, mpScenario) => {
    console.log(`Confirm order review ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'CONFIRM_ORDER_REVIEW';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);


    await validateEventPages(data.CONFIRM_ORDER_REVIEW());

    await apiRequest.startEvent(eventName, caseId);
    await apiRequest.submitEvent(eventName, caseData, caseId);

    await waitForFinishedBusinessProcess(caseId);
  },

  recordJudgment: async (user, mpScenario, whyRecorded, paymentPlanSelection) => {
    console.log(`case in All final orders issued ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'RECORD_JUDGMENT';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    if (mpScenario === 'ONE_V_ONE') {
      await validateEventPages(data.RECORD_JUDGMENT_SPEC(whyRecorded, paymentPlanSelection));
    } else {
      await validateEventPages(data.RECORD_JUDGMENT_ONE_V_TWO_SPEC(whyRecorded, paymentPlanSelection));
    }

    await assertSubmittedEvent('All_FINAL_ORDERS_ISSUED', {
      header: '',
      body: ''
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

  editJudgment: async (user, mpScenario, whyRecorded, paymentPlanSelection) => {
    console.log(`case in edit judgement ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'EDIT_JUDGMENT';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    if (mpScenario === 'ONE_V_ONE') {
      await validateEventPages(data.EDIT_JUDGMENT_SPEC(whyRecorded, paymentPlanSelection));
    } else {
      await validateEventPages(data.EDIT_JUDGMENT_ONE_V_TWO_SPEC(whyRecorded, paymentPlanSelection));
    }

    await assertSubmittedEvent('All_FINAL_ORDERS_ISSUED', {
      header: '# Judgment edited',
      body: 'The judgment has been edited'
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

  setAsideJudgment: async (user, setAsideReason, setAsideOrderType,expectedState = 'All_FINAL_ORDERS_ISSUED') => {
    console.log(`case in All set aside judgment ${caseId}`);
    console.log(`calling setup token *** setAside case ${caseId}  user : ${user.email}`);
    await apiRequest.setupTokens(user);
    eventName = 'SET_ASIDE_JUDGMENT';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);
    await validateEventPages(data.SET_ASIDE_JUDGMENT(setAsideReason, setAsideOrderType));
    console.log(`setAside case ${caseId}  user : ${user.email}`);
    await assertSubmittedEvent(expectedState, {
      header: '',
      body: ''
    }, true);
    await waitForFinishedBusinessProcess(caseId);
  },

  referToJudgeDefenceReceived: async (user) => {
    console.log(`case in Refer To Judge ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'REFER_JUDGE_DEFENCE_RECEIVED';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);
    await validateEventPages(data.REFER_JUDGE_DEFENCE_RECEIVED());
    await assertSubmittedEvent('All_FINAL_ORDERS_ISSUED', {
      header: '# The case has been referred to a judge for a decision',
      body: ''
    }, true);
    await waitForFinishedBusinessProcess(caseId);
  },

  markJudgmentPaid: async (user) => {
    console.log(`case in All final orders issued ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'JUDGMENT_PAID_IN_FULL';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    await validateEventPages(data.JUDGMENT_PAID_IN_FULL());

    await assertSubmittedEvent('All_FINAL_ORDERS_ISSUED', {
      header: '# Judgment marked as paid in full',
      body: 'The judgment has been marked as paid in full'
    }, true);
    await waitForFinishedBusinessProcess(caseId);
  },

  notSuitableSDOspec: async (user, option) => {
    console.log(`case in Judicial Referral ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'NotSuitable_SDO';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    await validateEventPages(data.NOT_SUITABLE_SDO_SPEC(option));

    if (option === 'CHANGE_LOCATION') {
      await assertSubmittedEvent('JUDICIAL_REFERRAL', {
        header: '',
        body: ''
      }, true);
      await waitForFinishedBusinessProcess(caseId);
    } else {
      await assertSubmittedEvent('JUDICIAL_REFERRAL', {
        header: '',
        body: ''
      }, true);
      await waitForFinishedBusinessProcess(caseId);
      const caseData = await fetchCaseDetails(config.adminUser, caseId, 200);
      assert(caseData.state === 'PROCEEDS_IN_HERITAGE_SYSTEM');
    }
  },

  transferCaseSpec: async (user) => {
    console.log(`case in Judicial Referral ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'TRANSFER_ONLINE_CASE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    await validateEventPages(data.TRANSFER_CASE_SPEC());

    await assertSubmittedEvent('JUDICIAL_REFERRAL', {
      header: '',
      body: ''
    }, true);
    await waitForFinishedBusinessProcess(caseId);
  },

  settleClaim: async (user, addApplicant2) => {
    console.log('settleClaim for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'SETTLE_CLAIM_MARK_PAID_FULL';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;

    await validateEventPages(data.SETTLE_CLAIM_MARK_PAID_FULL(addApplicant2));

    await assertSubmittedEvent('CASE_STAYED', {
      header: '### This claim has been marked as settled',
      body: ''
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

  settleClaimSelectClaimant: async (user, addApplicant2) => {
    console.log('settleClaim for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'SETTLE_CLAIM_MARK_PAID_FULL';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;

    await validateEventPages(data.SETTLE_CLAIM_MARK_PAID_FULL_SELECT_CLAIMANT(addApplicant2));

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: '### Request is being reviewed',
      body: ''
    }, true);

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
        header: '# Your claim will be fully discontinued against the specified defendants',
        body: ''
      }, true);
    } else if (mpScenario === 'ONE_V_TWO') {
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

  validateDiscontinueClaimClaimant: async (user, permission) => {
    console.log('discontinueClaim for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'VALIDATE_DISCONTINUE_CLAIM_CLAIMANT';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;

    assertContainsPopulatedFields(returnedCaseData);

    let disposalData = data.VALIDATE_DISCONTINUE_CLAIM_CLAIMANT(permission);
    for (let pageId of Object.keys(disposalData.userInput)) {
      await assertValidData(disposalData, pageId);
    }

    if (permission === 'YES') {
      await assertSubmittedEvent('CASE_DISCONTINUED', {
        header: '# Information successfully validated',
        body: '### Next steps:\n\nNo further action required.'
      }, true);
      await waitForFinishedBusinessProcess(caseId);
    } else {
      await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
        header: '# Unable to validate information',
        body: '### Next steps:\n\nNo further action required.'
      }, true);
      await waitForFinishedBusinessProcess(caseId);
    }
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
      if (caseData.preStayState === 'PREPARE_FOR_HEARING_CONDUCT_HEARING') {
        await assertSubmittedEvent('CASE_PROGRESSION', {
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
    delete caseData['showConditionFlags'];
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

// Functions
const assertValidData = async (data, pageId) => {
  console.log(`asserting page: ${pageId} has valid data`);
  let userData;

  if (eventName === 'CREATE_SDO' || eventName === 'EVIDENCE_UPLOAD_APPLICANT' || eventName === 'EVIDENCE_UPLOAD_RESPONDENT') {
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

  delete responseBody.data['sdoR2SmallClaimsUseOfWelshLanguage'];
  delete responseBody.data['sdoR2NihlUseOfWelshLanguage'];
  delete responseBody.data['sdoR2FastTrackUseOfWelshLanguage'];
  delete responseBody.data['sdoR2DrhUseOfWelshLanguage'];
  delete responseBody.data['sdoR2DisposalHearingUseOfWelshLanguage'];
  delete responseBody.data['sdoR2SmallClaimsWitnessStatementOther'];
  delete responseBody.data['sdoR2FastTrackWitnessOfFact'];
  delete responseBody.data['sdoR2FastTrackCreditHire'];
  delete responseBody.data['sdoDJR2TrialCreditHire'];

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

const assertSubmittedEventFlightDelay = async (expectedState, submittedCallbackResponseContains, hasSubmittedCallback = true) => {
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

  if(responseBody.case_data.responseClaimTrack === 'SMALL_CLAIM' && responseBody.case_data.flightDelayDetails.airlineList.value.code === 'OTHER'){
    assert.include(responseBody.case_data.caseManagementLocation, responseBody.case_data.applicant1DQRequestedCourt.caseLocation);
  }else if(responseBody.case_data.responseClaimTrack === 'SMALL_CLAIM' && responseBody.case_data.flightDelayDetails.airlineList.value.code !== 'OTHER'){
    assert.include(responseBody.case_data.caseManagementLocation, responseBody.case_data.flightDelayDetails.flightCourtLocation);
  }

  if (eventName === 'CREATE_CLAIM_SPEC') {
    caseId = responseBody.id;
    await addUserCaseMapping(caseId, config.applicantSolicitorUser);
    console.log('Case created: ' + caseId);
  }
};

const validateEventPagesDefaultJudgments = async (data, scenario,isDivergent) => {
  //transform the data
  console.log('validateEventPages');
  for (let pageId of Object.keys(data.userInput)) {
    await assertValidDataDefaultJudgments(data, pageId, scenario,isDivergent);
  }
};

const assertValidDataDefaultJudgments = async (data, pageId, scenario,isDivergent) => {
  console.log(`asserting page: ${pageId} has valid data`);
  const userData = data.userInput[pageId];

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

  if (pageId === 'defendantDetailsSpec') {
    delete responseBody.data['registrationTypeRespondentOne'];
    delete responseBody.data['registrationTypeRespondentTwo'];
  }
  if (pageId === 'paymentConfirmationSpec') {
    if (scenario === 'ONE_V_ONE' || scenario === 'TWO_V_ONE' || (scenario === 'ONE_V_TWO' && isDivergent)) {
      responseBody.data.currentDefendantName = 'Sir John Doe';
    } else {
      responseBody.data.currentDefendantName = 'both defendants';
    }

  } else if (pageId === 'paymentSetDate') {
    responseBody.data.repaymentDue= '1702.00';
  }
  if (pageId === 'paymentSetDate' || pageId === 'paymentType') {
    responseBody.data.currentDatebox = '25 August 2022';
  }
  if (pageId === 'claimPartialPayment') {
    delete responseBody.data['showDJFixedCostsScreen'];
    if (scenario === 'ONE_V_ONE' || scenario === 'TWO_V_ONE' || (scenario === 'ONE_V_TWO' && isDivergent)) {
      responseBody.data.currentDefendantName = 'Sir John Doe';
    } else {
      responseBody.data.currentDefendantName = 'both defendants';
    }
  }

  if (pageId === 'fixedCostsOnEntry') {
    delete responseBody.data['showDJFixedCostsScreen'];
    if (scenario === 'ONE_V_ONE' || scenario === 'TWO_V_ONE' || (scenario === 'ONE_V_TWO' && isDivergent)) {
      responseBody.data.currentDefendantName = 'Sir John Doe';
    } else {
      responseBody.data.currentDefendantName = 'both defendants';
    }
  }


  try {
    assert.deepEqual(responseBody.data, caseData);
  }
  catch(err) {
    console.error('Validate data is failed due to a mismatch ..', err);
    throw err;
  }
};

const assertValidDataSettleClaim = async (data, pageId) => {
  console.log(`asserting page: ${pageId} has valid data`);
  caseData = data.userInput[pageId];

  const response = await apiRequest.validatePage(
    eventName,
    pageId,
    caseData,
    caseId
  );
  let responseBody = await response.json();
  responseBody = clearDataForSearchCriteria(responseBody); //Until WA release

  assert.equal(response.status, 200);
  assert.equal(response.status, 200);

  if (data.midEventData && data.midEventData[pageId]) {
    checkExpected(responseBody.data, data.midEventData[pageId]);
  }

  if (data.midEventGeneratedData && data.midEventGeneratedData[pageId]) {
    checkGenerated(responseBody.data, data.midEventGeneratedData[pageId]);
  }

  caseData = update(caseData, responseBody.data);

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
    expect(caseForDisplay.triggers).to.deep.include.members(expectedEvents[user.type][state],
      'Unexpected events for state ' + state + ' and user type ' + user.type);
  }
};

const validateEventPagesMinti = async (data, solicitor) => {
  //transform the data
  console.log('validateEventPages....');
  for (let pageId of Object.keys(data.valid)) {
    if (pageId === 'UploadOrder' || pageId === 'DocumentUpload' || pageId === 'Upload' || pageId === 'DraftDirections'|| pageId === 'ApplicantDefenceResponseDocument' || pageId === 'DraftDirections' || pageId === 'FinalOrderPreview' || pageId === 'FixedRecoverableCosts') {
      const document = await testingSupport.uploadDocument();
      data = await updateCaseDataWithPlaceholders(data, document);
    }
    await assertValidData(data, pageId, solicitor);
  }
};

const validateEventPages = async (data, solicitor) => {
  //transform the data
  console.log('validateEventPages....');
  for (let pageId of Object.keys(data.userInput)) {
    if (pageId === 'UploadOrder' || pageId === 'DocumentUpload' || pageId === 'Upload' || pageId === 'DraftDirections'|| pageId === 'ApplicantDefenceResponseDocument' || pageId === 'DraftDirections' || pageId === 'FinalOrderPreview' || pageId === 'FixedRecoverableCosts') {
      const document = await testingSupport.uploadDocument();
      data = await updateCaseDataWithPlaceholders(data, document);
    }
    if (pageId === 'OptionsForSettlement' || pageId === 'ClaimantDetails'){
      await assertValidDataSettleClaim(data, pageId);
    } if (pageId === 'MultipleClaimant' || pageId === 'ClaimantConsent'){
      await assertValidDataSettleClaim(data, pageId);
    } else {
      // data = await updateCaseDataWithPlaceholders(data);
      await assertValidData(data, pageId, solicitor);
    }

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
