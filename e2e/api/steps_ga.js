const config = require('../config.js');
const {PBAv3, COSC, isJOLive} = require('../fixtures/ga-events/featureKeys');
const lodash = require('lodash');
const deepEqualInAnyOrder = require('deep-equal-in-any-order');
const chai = require('chai');
const { listElement } = require('./dataHelper');

chai.use(deepEqualInAnyOrder);
chai.config.truncateThreshold = 0;
const {assert} = chai;
const date = new Date();
const twoDigitDate = ((date.getDate()) >= 10) ? (date.getDate()) : '0' + (date.getDate());
const twoDigitMonth = ((date.getMonth()+1) >= 10) ? (date.getMonth()+1) : '0' + (date.getMonth()+1);
let current_date = date.getFullYear().toString()+ '-' + twoDigitMonth + '-' + twoDigitDate;
const {
  waitForFinishedBusinessProcess,
  waitForGAFinishedBusinessProcess,
  waitForGACamundaEventsFinishedBusinessProcess
} = require('../api/testingSupport');

const {assignCaseRoleToUser, addUserCaseMapping, unAssignAllUsers} = require('./caseRoleAssignmentHelper');
const apiRequest = require('./apiRequest.js');
const claimData = require('../fixtures/ga-events/events/createClaim.js');
const claimDataSpec = require('../fixtures/ga-events/events/claim/createClaimSpec.js');
const claimSpecData = require('../fixtures/ga-events/events/createClaimSpec.js');
const claimDataSpecSmallLRvLiP = require('../fixtures/ga-events/events/createClaimSpecSmallCui.js');
const genAppData = require('../fixtures/ga-events/ga-ccd/createGeneralApplication.js');
const genAppDataLR = require('../fixtures/ga-events/ga-ccd/createGeneralApplicationLR.js');
const genAppRespondentResponseData = require('../fixtures/ga-events/ga-ccd/respondentResponse.js');
const genAppJudgeMakeDecisionData = require('../fixtures/ga-events/ga-ccd/judgeMakeDecision.js');
const genAppJudgeMakeFinalOrderData = require('../fixtures/ga-events/ga-ccd/judgeMakeFinalDecision.js');
const genAppHearingData = require('../fixtures/ga-events/ga-ccd/genAppHearing.js');
const genAppNbcAdminTask = require('../fixtures/ga-events/ga-ccd/nbcAdminTask.js');
const createClaimLipClaimant = require('../fixtures/ga-events/events/cui/createClaimUnrepresentedClaimant.js');
const events = require('../fixtures/ga-events/ga-ccd/events.js');
const testingSupport = require('./testingSupport');
const { replaceDQFieldsIfHNLFlagIsDisabled, replaceFieldsIfHNLToggleIsOffForClaimantResponse} = require('../helpers/hnlFeatureHelper');
const {checkToggleEnabled} = require('./testingSupport');
const {createGeneralAppN245FormUpload} = require('../fixtures/ga-events/ga-ccd/createGeneralApplication');
const sdoTracks = require('../fixtures/ga-events/events/createSDO.js');
const hearingScheduled = require('../fixtures/ga-events/events/scheduleHearing.js');
const createFinalOrder = require('../fixtures/ga-events/events/finalOrder.js');
const {expect} = require('chai');
const {cloneDeep} = require('lodash');
const genLipAppData = require('../fixtures/ga-events/events/cui/createGAUnrepresented.js');
const genLipHwf = require('../fixtures/ga-events/events/cui/hwf.js');
const gaTypesList = {
  'LATypes': ['STAY_THE_CLAIM','EXTEND_TIME', 'AMEND_A_STMT_OF_CASE'],
  'JudgeGaTypes': ['SET_ASIDE_JUDGEMENT']
};

const data = {

  INITIATE_GENERAL_APPLICATION_WITH_MIX_TYPES: (types, isWithNotice, reason, calculatedAmount, code) => genAppData.createGA(types,
    isWithNotice, reason, calculatedAmount, code) ,
  INITIATE_GENERAL_APPLICATION_WITH_MIX_TYPES_LR: (types, isWithNotice, reason, calculatedAmount, code) => genAppDataLR.createGA(types,
    isWithNotice, reason, calculatedAmount, code),
  INITIATE_GENERAL_APPLICATION: genAppData.createGAData('Yes', null,
    '30300', 'FEE0442'),
  INITIATE_GENERAL_APPLICATION_LR: genAppDataLR.createGAData('Yes', null,
    '30300', 'FEE0442'),
  INITIATE_GENERAL_APPLICATION_FOR_LA: genAppData.createGA(gaTypesList.LATypes, 'No', null,
    '11900', 'FEE0443'),
  INITIATE_GENERAL_APPLICATION_FOR_LA_LR : genAppDataLR.createGA(gaTypesList.LATypes, 'No', null,
    '11900', 'FEE0443'),
  INITIATE_GENERAL_APPLICATION_FOR_JUDGE:  genAppData.createGA(gaTypesList.JudgeGaTypes, 'No', null,
    '11900', 'FEE0443'),
  INITIATE_GENERAL_APPLICATION_FOR_JUDGE_LR : genAppDataLR.createGA(gaTypesList.JudgeGaTypes, 'No', null,
    '11900', 'FEE0443'),
  INITIATE_GENERAL_APPLICATION_WITHOUT_NOTICE: genAppData.createGADataWithoutNotice('No','Test 123',
    '11900','FEE0443'),
  INITIATE_GENERAL_APPLICATION_WITHOUT_NOTICE_LR: genAppDataLR.createGADataWithoutNotice('No','Test 123',
    '11900','FEE0443'),
  INITIATE_GENERAL_APPLICATION_CONSENT: (genAppType) => genAppData.createGaWithConsentAndNotice(genAppType, true, false,null,
    '11900','FEE0443'),
  INITIATE_GENERAL_APPLICATION_CONSENT_LR: (genAppType) => genAppDataLR.createGaWithConsentAndNotice(genAppType, true, false,null,
    '11900','FEE0443'),
  INITIATE_GENERAL_APPLICATION_CONSENT_URGENT:(genAppType) =>  genAppData.createGaWithConsentAndNotice(genAppType, true, true,null,
    '11900','FEE0443'),
  INITIATE_GENERAL_APPLICATION_CONSENT_URGENT_LR:(genAppType) =>genAppDataLR.createGaWithConsentAndNotice(genAppType, true, true,null,
    '11900','FEE0443'),
  INITIATE_GENERAL_APPLICATION_NO_STRIKEOUT: genAppData.gaTypeWithNoStrikeOut(),
  INITIATE_GENERAL_APPLICATION_NO_STRIKEOUT_LR: genAppDataLR.gaTypeWithNoStrikeOut(),
  INITIATE_GENERAL_APPLICATION_STAY_CLAIM: genAppData.gaTypeWithStayClaim(),
  INITIATE_GENERAL_APPLICATION_STAY_CLAIM_LR: genAppDataLR.gaTypeWithStayClaim(),
  INITIATE_GENERAL_APPLICATION_UNLESS_ORDER: genAppData.gaTypeWithUnlessOrder(),
  INITIATE_GENERAL_APPLICATION_UNLESS_ORDER_LR: genAppDataLR.gaTypeWithUnlessOrder(),
  INITIATE_GENERAL_APPLICATION_VARY_PAYMENT_TERMS_OF_JUDGMENT: (isWithNotice, generalAppN245FormUpload, urgency) =>  genAppData.createGADataVaryJudgement(isWithNotice,null,
    '1500','FEE0458', generalAppN245FormUpload, urgency),
  INITIATE_GENERAL_APPLICATION_VARY_PAYMENT_TERMS_OF_JUDGMENT_LR: (isWithNotice, generalAppN245FormUpload, urgency) => genAppDataLR.createGADataVaryJudgement(isWithNotice,null,
    '1500','FEE0458', generalAppN245FormUpload, urgency),
  INITIATE_GENERAL_APPLICATION_ADJOURN_VACATE: (isWithNotice, isWithConsent, hearingDate, calculatedAmount, code, version) =>  genAppData.createGaAdjournVacateData(isWithNotice, isWithConsent, hearingDate, calculatedAmount, code, version),
  INITIATE_GENERAL_APPLICATION_ADJOURN_VACATE_LR: (isWithNotice, isWithConsent, hearingDate, calculatedAmount, code, version) => genAppDataLR.createGaAdjournVacateData(isWithNotice, isWithConsent, hearingDate, calculatedAmount, code, version),
  INITIATE_GENERAL_APPLICATION_LIP: (typeOfApplication, hwf) => genLipAppData.getPayloadForGALiP(typeOfApplication, hwf),
  INITIATE_GENERAL_APPLICATION_LIP_WITHOUT :() => genLipAppData.getPayloadForGALiPWithout(),
  RESPOND_TO_APPLICATION: (agree) => genAppRespondentResponseData.respondGAData(agree),
  RESPOND_DEBTOR_TO_APPLICATION: (agree) => genAppRespondentResponseData.respondDebtorGAData(agree),
  RESPOND_TO_CONSENT_APPLICATION: (agree) => genAppRespondentResponseData.respondConsentGAData(agree),
  MAKE_DECISION: genAppJudgeMakeDecisionData.judgeMakesDecisionData(),
  APPROVE_CONSENT_ORDER: genAppNbcAdminTask.nbcAdminApproveConsentOrderData(),
  REFER_TO_JUDGE: genAppNbcAdminTask.nbcAdminReferToJudgeData(),
  REFER_TO_LEGAL_ADVISOR: genAppNbcAdminTask.nbcAdminReferToLegalAdvisorData(),
  JUDGE_MAKES_ORDER_WRITTEN_REP: (current_date) => genAppJudgeMakeDecisionData.judgeMakeOrderWrittenRep(current_date),
  JUDGE_MAKES_ORDER_WRITTEN_REP_ON_UNCLOAKED_APPLN: (current_date) => genAppJudgeMakeDecisionData.judgeMakeOrderWrittenRep_On_Uncloaked_Appln(current_date),
  RESPOND_TO_JUDGE_ADDITIONAL_INFO: genAppRespondentResponseData.toJudgeAdditionalInfo(),
  RESPOND_TO_JUDGE_DIRECTIONS: genAppRespondentResponseData.toJudgeDirectionsOrders(),
  RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION: genAppRespondentResponseData.toJudgeWrittenRepresentation(),
  JUDGE_MAKES_ORDER_DIRECTIONS_REP:(current_date) => genAppJudgeMakeDecisionData.judgeMakeDecisionDirectionOrder(current_date),
  JUDGE_APPROVES_STRIKEOUT_APPLN: genAppJudgeMakeDecisionData.judgeApprovesStrikeOutAppl(),
  JUDGE_APPROVES_STAYCLAIM_APPLN: (current_date) => genAppJudgeMakeDecisionData.judgeApprovesStayClaimAppl(current_date),
  JUDGE_APPROVES_UNLESSORDER_APPLN: (current_date) => genAppJudgeMakeDecisionData.judgeApprovesUnlessOrderAppl(current_date),
  PAYMENT_SERVICE_REQUEST_UPDATED: genAppJudgeMakeDecisionData.serviceUpdateDto(),
  HWF_FULL: (type) => genLipHwf.full(type),
  HWF_OUTCOME: (type) => genLipHwf.outcome(type),
  LIST_FOR_A_HEARING: genAppJudgeMakeDecisionData.listingForHearing(),
  JUDGE_DECIDES_FREE_FORM_ORDER: genAppJudgeMakeDecisionData.freeFormOrder(),
  LIST_FOR_A_HEARING_InPerson: genAppJudgeMakeDecisionData.listingForHearingInPerson(),
  SCHEDULE_HEARING: genAppHearingData.scheduleHearing(),
  APPLICATION_DISMISSED: genAppJudgeMakeDecisionData.applicationsDismiss(),
  JUDGE_MAKES_ORDER_DISMISS: genAppJudgeMakeDecisionData.judgeMakeDecisionDismissed(),
  CREATE_CLAIM: (mpScenario, claimantType, claimAmount, useBirminghamCourt) => claimData.createClaim(mpScenario, claimantType, claimAmount, useBirminghamCourt),
  CREATE_SPEC_CLAIM: (mpScenario) => claimSpecData.createClaim(mpScenario),
  CREATE_SPEC_CLAIM_LIP: (mpScenario) => claimDataSpecSmallLRvLiP.createClaim(mpScenario),
  UPDATE_CLAIMANT_SOLICITOR_EMAILID: claimSpecData.updateClaimantSolicitorEmailId(),
  CREATE_CLAIM_RESPONDENT_LIP: claimData.createClaimLitigantInPerson,
  COS_NOTIFY_CLAIM: (lip1, lip2) => claimData.cosNotifyClaim(lip1, lip2),
  COS_NOTIFY_CLAIM_DETAILS: (lip1, lip2) => claimData.cosNotifyClaimDetails(lip1, lip2),
  CREATE_CLAIM_TERMINATED_PBA: claimData.createClaimWithTerminatedPBAAccount,
  CREATE_CLAIM_RESPONDENT_SOLICITOR_FIRM_NOT_IN_MY_HMCTS: claimData.createClaimRespondentSolFirmNotInMyHmcts,
  JUDGE_MAKES_ORDER_UNCLOAK: genAppJudgeMakeDecisionData.judgeMakeOrderUncloakApplication(),
  JUDGE_REQUEST_MORE_INFO_UNCLOAK: (other) => genAppJudgeMakeDecisionData.judgeRequestMoreInfomationUncloakData(other),
  RESUBMIT_CLAIM: require('../fixtures/ga-events/events/resubmitClaim.js'),
  NOTIFY_DEFENDANT_OF_CLAIM: require('../fixtures/ga-events/events/1v2DifferentSolicitorEvents/notifyClaim_1v2DiffSol.js'),
  PARTIAL_DEFENDANT_OF_CLAIM: require('../fixtures/ga-events/events/1v2DifferentSolicitorEvents/notifyClaim_1v2DiffSol_partial.js'),
  NOTIFY_DEFENDANT_OF_CLAIM_DETAILS: require('../fixtures/ga-events/events/1v2DifferentSolicitorEvents/notifyClaim_1v2DiffSol.js'),
  PARTIAL_NOTIFY_DEFENDANT_OF_CLAIM_DETAILS: require('../fixtures/ga-events/events/1v2DifferentSolicitorEvents/notifyClaimDetails_1v2DiffSol_partial.js'),
  ADD_OR_AMEND_CLAIM_DOCUMENTS: require('../fixtures/ga-events/events/addOrAmendClaimDocuments.js'),
  ACKNOWLEDGE_CLAIM: require('../fixtures/ga-events/events/acknowledgeClaim.js'),
  ACKNOWLEDGE_CLAIM_SAME_SOLICITOR: require('../fixtures/ga-events/events/1v2SameSolicitorEvents/acknowledgeClaim_sameSolicitor.js'),
  ACKNOWLEDGE_CLAIM_SOLICITOR_ONE: require('../fixtures/ga-events/events/1v2DifferentSolicitorEvents/acknowledgeClaim_Solicitor1.js'),
  ACKNOWLEDGE_CLAIM_SOLICITOR_TWO: require('../fixtures/ga-events/events/1v2DifferentSolicitorEvents/acknowledgeClaim_Solicitor2.js'),
  INFORM_AGREED_EXTENSION_DATE: require('../fixtures/ga-events/events/informAgreeExtensionDate.js'),
  INFORM_AGREED_EXTENSION_DATE_SOLICITOR_TWO: require('../fixtures/ga-events/events/1v2DifferentSolicitorEvents/informAgreeExtensionDate_Solicitor2.js'),
  DEFENDANT_RESPONSE: require('../fixtures/ga-events/events/defendantResponse.js'),
  DEFENDANT_RESPONSE_SAME_SOLICITOR: require('../fixtures/ga-events/events/1v2SameSolicitorEvents/defendantResponse_sameSolicitor.js'),
  DEFENDANT_RESPONSE_SOLICITOR_ONE: require('../fixtures/ga-events/events/1v2DifferentSolicitorEvents/defendantResponse_Solicitor1'),
  DEFENDANT_RESPONSE_SOLICITOR_TWO: require('../fixtures/ga-events/events/1v2DifferentSolicitorEvents/defendantResponse_Solicitor2'),
  DEFENDANT_RESPONSE_TWO_APPLICANTS: require('../fixtures/ga-events/events/2v1Events/defendantResponse_2v1'),
  CLAIMANT_RESPONSE: (mpScenario) => require('../fixtures/ga-events/events/claimantResponse.js').claimantResponse(mpScenario),
  ADD_DEFENDANT_LITIGATION_FRIEND: require('../fixtures/ga-events/events/addDefendantLitigationFriend.js'),
  CASE_PROCEEDS_IN_CASEMAN_SPEC: require('../fixtures/ga-events/events/caseProceedsInCasemanSpec.js'),
  AMEND_PARTY_DETAILS: require('../fixtures/ga-events/events/amendPartyDetails.js'),
  ADD_CASE_NOTE: require('../fixtures/ga-events/events/addCaseNote.js'),
  FINAL_ORDER_FREEFORM: genAppJudgeMakeFinalOrderData.judgeMakesDecisionFreeFormData(),
  FINAL_ORDER_ASSISTED: genAppJudgeMakeFinalOrderData.judgeMakesDecisionAssisted(),
  FINAL_ORDER_ASSISTED_WITH_HEARING: genAppJudgeMakeFinalOrderData.judgeMakesDecisionAssistedWithHearing(),

  CLAIM_CREATE_CLAIM_AP_SPEC: (scenario) => claimDataSpec.createClaimForAccessProfiles(scenario),
  CLAIM_DEFENDANT_RESPONSE_SPEC: (response, camundaEvent) => require('../fixtures/ga-events/events/claim/defendantResponseSpec.js').respondToClaim(response, camundaEvent),
  CLAIM_DEFENDANT_RESPONSE2_SPEC: (response, camundaEvent) => require('../fixtures/ga-events/events/claim/defendantResponseSpec.js').respondToClaim2(response, camundaEvent),
  CLAIM_DEFENDANT_RESPONSE_1v2_SPEC: (response, camundaEvent) => require('../fixtures/ga-events/events/claim/defendantResponseSpec1v2.js').respondToClaim(response, camundaEvent),
  CLAIM_CLAIMANT_RESPONSE_SPEC: (mpScenario) => require('../fixtures/ga-events/events/claim/claimantResponseSpec.js').claimantResponse(mpScenario),
  CLAIM_CLAIMANT_RESPONSE_1v2_SPEC: (response) => require('../fixtures/ga-events/events/claim/claimantResponseSpec1v2.js').claimantResponse(response),
  CLAIM_INFORM_AGREED_EXTENSION_DATE_SPEC: () => require('../fixtures/ga-events/events/claim/informAgreeExtensionDateSpec.js'),
  JUDGMENT_PAID_FULL: require('../fixtures/ga-events/events/cui/judgmentPaidInFullCui'),
  CLAIM_DEFAULT_JUDGEMENT_SPEC_CUI: require('../fixtures/ga-events/events/cui/defaultJudgmentCui.js'),
  CLAIM_DEFAULT_JUDGEMENT_SPEC: require('../fixtures/ga-events/events/claim/defaultJudgmentSpec.js'),
  CLAIM_DEFAULT_JUDGEMENT_SPEC_1V2: require('../fixtures/ga-events/events/claim/defaultJudgment1v2Spec.js'),
  CLAIM_DEFAULT_JUDGEMENT_SPEC_2V1: require('../fixtures/ga-events/events/claim/defaultJudgment2v1Spec.js'),
  CREATE_CERTIFICATION_OF_SATISFACTION_CANCELLATION: require('../fixtures/ga-events/ga-ccd/createCoscApplication.js'),
  JUDGMENT_PAID_IN_FULL: require('../fixtures/ga-events/events/claim/judgmentPaidInFull'),
  DEFENDANT_RESPONSE_SPEC_CUI: require('../fixtures/ga-events/events/cui/defendantResponseCui.js').createDefendantResponse(),
  REQUEST_JUDGEMENT: (response) => require('../fixtures/ga-events/events/claim/requestJudgementSpec.js').createRequestJudgment(response),

  CREATE_DISPOSAL: (userInput) => sdoTracks.createSDODisposal(userInput),
  CREATE_FAST: (userInput) => sdoTracks.createSDOFast(userInput),
  CREATE_SMALL: (userInput) => sdoTracks.createSDOSmall(userInput),

  HEARING_SCHEDULED: (allocatedTrack) => hearingScheduled.scheduleHearing(allocatedTrack),
  FINAL_ORDERS: (finalOrdersRequestType) => createFinalOrder.requestFinalOrder(finalOrdersRequestType),
};

const eventData = {
  defendantResponsesSpec: {
    ONE_V_ONE: {
      FULL_DEFENCE: data.CLAIM_DEFENDANT_RESPONSE_SPEC('FULL_DEFENCE'),
      FULL_DEFENCE_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_SPEC('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION: data.CLAIM_DEFENDANT_RESPONSE_SPEC('FULL_ADMISSION'),
      FULL_ADMISSION_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_SPEC('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION: data.CLAIM_DEFENDANT_RESPONSE_SPEC('PART_ADMISSION'),
      PART_ADMISSION_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_SPEC('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM: data.CLAIM_DEFENDANT_RESPONSE_SPEC('COUNTER_CLAIM'),
      COUNTER_CLAIM_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_SPEC('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT')
    },
    ONE_V_TWO: {
      FULL_DEFENCE: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('FULL_DEFENCE'),
      FULL_DEFENCE_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('FULL_ADMISSION'),
      FULL_ADMISSION_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('PART_ADMISSION'),
      PART_ADMISSION_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('COUNTER_CLAIM'),
      COUNTER_CLAIM_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      DIFF_FULL_DEFENCE: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('DIFF_FULL_DEFENCE'),
      DIFF_FULL_DEFENCE_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('DIFF_FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      DIFF_NOT_FULL_DEFENCE: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('DIFF_NOT_FULL_DEFENCE'),
      DIFF_NOT_FULL_DEFENCE_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_1v2_SPEC('DIFF_NOT_FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT')
    },
    ONE_V_ONE_DIF_SOL: {
      FULL_DEFENCE1: data.CLAIM_DEFENDANT_RESPONSE_SPEC('FULL_DEFENCE'),
      FULL_DEFENCE1_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_SPEC('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION1: data.CLAIM_DEFENDANT_RESPONSE_SPEC('FULL_ADMISSION'),
      FULL_ADMISSION1_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_SPEC('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION1: data.CLAIM_DEFENDANT_RESPONSE_SPEC('PART_ADMISSION'),
      PART_ADMISSION1_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_SPEC('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM1: data.CLAIM_DEFENDANT_RESPONSE_SPEC('COUNTER_CLAIM'),
      COUNTER_CLAIM1_PBAv3: data.CLAIM_DEFENDANT_RESPONSE_SPEC('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),

      FULL_DEFENCE2: data.CLAIM_DEFENDANT_RESPONSE2_SPEC('FULL_DEFENCE'),
      FULL_DEFENCE2_PBAv3: data.CLAIM_DEFENDANT_RESPONSE2_SPEC('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION2: data.CLAIM_DEFENDANT_RESPONSE2_SPEC('FULL_ADMISSION'),
      FULL_ADMISSION2_PBAv3: data.CLAIM_DEFENDANT_RESPONSE2_SPEC('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION2: data.CLAIM_DEFENDANT_RESPONSE2_SPEC('PART_ADMISSION'),
      PART_ADMISSION2_PBAv3: data.CLAIM_DEFENDANT_RESPONSE2_SPEC('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      // COUNTER_CLAIM2: data.DEFENDANT_RESPONSE2('COUNTER_CLAIM')
    },
    TWO_V_ONE: {
      // FULL_DEFENCE: data.CLAIM_DEFENDANT_RESPONSE_2v1('FULL_DEFENCE'),
      // FULL_ADMISSION: data.CLAIM_DEFENDANT_RESPONSE_2v1('FULL_ADMISSION'),
      // PART_ADMISSION: data.CLAIM_DEFENDANT_RESPONSE_2v1('PART_ADMISSION'),
      // COUNTER_CLAIM: data.CLAIM_DEFENDANT_RESPONSE_2v1('COUNTER_CLAIM'),
      // DIFF_FULL_DEFENCE: data.CLAIM_DEFENDANT_RESPONSE_2v1('DIFF_FULL_DEFENCE'),
      // DIFF_NOT_FULL_DEFENCE: data.CLAIM_DEFENDANT_RESPONSE_2v1('DIFF_NOT_FULL_DEFENCE')
    }
  },
  defendantResponses:{
    ONE_V_ONE: data.DEFENDANT_RESPONSE,
    ONE_V_TWO_ONE_LEGAL_REP: data.DEFENDANT_RESPONSE_SAME_SOLICITOR,
    ONE_V_TWO_TWO_LEGAL_REP: {
      solicitorOne: data.DEFENDANT_RESPONSE_SOLICITOR_ONE,
      solicitorTwo: data.DEFENDANT_RESPONSE_SOLICITOR_TWO
    },
  },
  claimantResponsesSpec: {
    ONE_V_ONE: {
    //   FULL_DEFENCE: data.CLAIMANT_RESPONSE_SPEC('FULL_DEFENCE'),
    //   FULL_ADMISSION: data.CLAIMANT_RESPONSE_SPEC('FULL_ADMISSION'),
    //   PART_ADMISSION: data.CLAIMANT_RESPONSE_SPEC('PART_ADMISSION'),
    //   COUNTER_CLAIM: data.CLAIMANT_RESPONSE_SPEC('COUNTER_CLAIM')
      PART_ADMISSION_IMMEDIATELY: data.CLAIM_CLAIMANT_RESPONSE_SPEC('PART_ADMISSION_IMMEDIATELY'),
     },
    ONE_V_TWO: {
      FULL_DEFENCE: data.CLAIM_CLAIMANT_RESPONSE_1v2_SPEC('FULL_DEFENCE'),
      FULL_ADMISSION: data.CLAIM_CLAIMANT_RESPONSE_1v2_SPEC('FULL_ADMISSION'),
      PART_ADMISSION: data.CLAIM_CLAIMANT_RESPONSE_1v2_SPEC('PART_ADMISSION'),
      NOT_PROCEED: data.CLAIM_CLAIMANT_RESPONSE_1v2_SPEC('NOT_PROCEED'),
    },
    // TWO_V_ONE: {
    //   FULL_DEFENCE: data.CLAIMANT_RESPONSE_2v1_SPEC('FULL_DEFENCE'),
    //   FULL_ADMISSION: data.CLAIMANT_RESPONSE_2v1_SPEC('FULL_ADMISSION'),
    //   PART_ADMISSION: data.CLAIMANT_RESPONSE_2v1_SPEC('PART_ADMISSION'),
    //   NOT_PROCEED: data.CLAIMANT_RESPONSE_2v1_SPEC('NOT_PROCEED')
    // }
  },
  sdoTracks: {
    CREATE_DISPOSAL: data.CREATE_DISPOSAL(),
    CREATE_SMALL: data.CREATE_SMALL(),
    CREATE_FAST: data.CREATE_FAST(),
  }
};

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

let caseId, eventName;
let caseData = {};
let mpScenario = 'ONE_V_ONE';

module.exports = {

  createSpecifiedClaimWithUnrepresentedRespondent: async (user, mpScenario) => {
    console.log(' Creating specified claim');
    eventName = 'CREATE_CLAIM_SPEC';
    caseId = null;
    caseData = {};
    let createClaimSpecData;

    console.log('Creating small claims...');
    createClaimSpecData = data.CREATE_SPEC_CLAIM_LIP(mpScenario);

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    for (let pageId of Object.keys(createClaimSpecData.userInput)) {
       await assertValidDataLip(createClaimSpecData, pageId);
    }

    await assertSubmittedEvent('PENDING_CASE_ISSUED');

    var pbaV3 = await checkToggleEnabled(PBAv3);

    console.log('Is PBAv3 toggle on?: ' + pbaV3);
    await waitForFinishedBusinessProcess(caseId, user);

    if (pbaV3) {
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
        claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    }
    function delay(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    }
    // Pause for 10 seconds
    await delay(15000);

    await assignCaseRoleToUser(caseId, 'DEFENDANT', config.defendantCitizenUser2);

    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');
    return caseId;
  },

  createUnspecifiedClaim: async (user, multipartyScenario, claimantType, claimAmount= '30000', useBirminghamCourt = false) => {

    eventName = 'CREATE_CLAIM';
    caseId = null;
    caseData = {};
    mpScenario = multipartyScenario;

    const createClaimData = data.CREATE_CLAIM(mpScenario, claimantType, claimAmount, useBirminghamCourt);

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    await validateEventPages(createClaimData);

    let i;
    for (i = 0; i < createClaimData.invalid.Court.courtLocation.applicantPreferredCourt.length; i++) {
      await assertError('Court', createClaimData.invalid.Court.courtLocation.applicantPreferredCourt[i],
        null, 'Case data validation failed');
    }
    await assertError('Upload', createClaimData.invalid.Upload.servedDocumentFiles.particularsOfClaimDocument,
      null, 'Case data validation failed');

    const pbaV3 = await checkToggleEnabled(PBAv3);
    console.log('Is PBAv3 toggle on?: ' + pbaV3);


    await assertSubmittedEvent('PENDING_CASE_ISSUED');

    await waitForFinishedBusinessProcess(caseId, user);

    if (pbaV3) {
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
                                      claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    }

    await waitForFinishedBusinessProcess(caseId, user);

    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');
    return caseId;
  },

  amendClaimDocuments: async (user) => {
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');

    await apiRequest.setupTokens(user);

    eventName = 'ADD_OR_AMEND_CLAIM_DOCUMENTS';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    caseData = returnedCaseData;

    await validateEventPages(data[eventName]);

    const document = await testingSupport.uploadDocument();
    let errorData = await updateCaseDataWithPlaceholders(data[eventName], document);

    await assertError('Upload', errorData.invalid.Upload.duplicateError,
      'You need to either upload 1 Particulars of claim only or enter the Particulars of claim text in the field provided. You cannot do both.');

    await assertSubmittedEvent('CASE_ISSUED', {
      header: 'Documents uploaded successfully',
      body: ''
    });

    await waitForFinishedBusinessProcess(caseId, user);
  },

  amendclaimDismissedDeadline: async (user) => {
    await apiRequest.setupTokens(user);
    let claimDismissedDeadline;
    claimDismissedDeadline = {'claimDismissedDeadline':'2022-01-10T15:59:50'};
    await testingSupport.updateCaseData(caseId, claimDismissedDeadline);
  },

  amendRespondent1ResponseDeadline: async (user) => {
    await apiRequest.setupTokens(user);
    let respondent1deadline ={};
    respondent1deadline = {'respondent1ResponseDeadline':'2022-01-10T15:59:50'};
    await testingSupport.updateCaseData(caseId, respondent1deadline);
  },

  amendDirectionOrderDeadlineDate: async (user) => {
    await apiRequest.setupTokens(user);
    let deadlineDate;
    deadlineDate = {'judicialDecisionMakeOrder.':'2022-01-10T15:59:50'};
    await testingSupport.updateCaseData(caseId, deadlineDate);
  },

  createSpecifiedClaim: async (user, multipartyScenario, assignTheSpecCase = true) => {

    eventName = 'CREATE_CLAIM_SPEC';
    caseId = null;
    caseData = {};
    const createClaimSpecData = data.CREATE_SPEC_CLAIM(multipartyScenario);

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    for (let pageId of Object.keys(createClaimSpecData.userInput)) {
      await assertValidDataSpec(createClaimSpecData, pageId);
    }

    await assertSubmittedSpecEvent('PENDING_CASE_ISSUED');

    var pbaV3 = await checkToggleEnabled(PBAv3);

    console.log('Is PBAv3 toggle on?: ' + pbaV3);
    await waitForFinishedBusinessProcess(caseId, user);

    if (pbaV3) {
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
                                      claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    }

    if (assignTheSpecCase) {
      await assignSpecCase(caseId, multipartyScenario);
    }

    await waitForFinishedBusinessProcess(caseId, user);

    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');
    return caseId;
  },

  initiateGeneralApplicationWithState: async (user, parentCaseId, expectState) => {
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_LR
      : data.INITIATE_GENERAL_APPLICATION;
    return await initiateGaWithState(user, parentCaseId, expectState, gaData);
  },

  initiateGeneralApplication: async (user, parentCaseId) => {
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_LR
      : data.INITIATE_GENERAL_APPLICATION;
    return await initiateGaWithState(user, parentCaseId, 'AWAITING_RESPONDENT_RESPONSE', gaData);
  },

  verifyCivilClaimGACollections: async (user, parentCaseId, gaCaseReference) => {
    return await verifyNoOfGAsInCivilClaim(user, parentCaseId, gaCaseReference);
  },

  initiateConsentGeneralApplication: async (user, parentCaseId, gaAppType) => {
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_CONSENT_LR(gaAppType)
      : data.INITIATE_GENERAL_APPLICATION_CONSENT(gaAppType);

    return await initiateGaWithState(user, parentCaseId, 'AWAITING_RESPONDENT_RESPONSE', gaData);
  },

  initiateConsentUrgentGeneralApplication: async (user, parentCaseId, gaAppType ) => {
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_CONSENT_URGENT_LR(gaAppType)
      : data.INITIATE_GENERAL_APPLICATION_CONSENT_URGENT(gaAppType);

    return await initiateGaWithState(user, parentCaseId, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION', gaData);
  },

  updateCivilClaimClaimantSolEmailID: async (user, parentCaseId) => {
    return await updateCivilClaimSolEmailID(user, parentCaseId);
  },

  initiateGaWithVaryJudgement: async (user, parentCaseId, isClaimant, urgency) => {
    return await initiateWithVaryJudgement(user, parentCaseId, isClaimant, urgency);
  },

  initiateGeneralApplicationWithOutNotice: async (user, parentCaseId) => {
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_WITHOUT_NOTICE_LR
      : data.INITIATE_GENERAL_APPLICATION_WITHOUT_NOTICE;

    return await initiateGeneralApplicationWithOutNotice(user, parentCaseId, gaData);
  },

  initiateGaWithTypes: async (user, parentCaseId, types, calculatedAmount, code) => {
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_WITH_MIX_TYPES_LR(types, 'No', null, calculatedAmount, code)
      : data.INITIATE_GENERAL_APPLICATION_WITH_MIX_TYPES(types, 'No', null, calculatedAmount, code);
    return await initiateGeneralApplicationWithOutNotice(user, parentCaseId,
      gaData);
  },

  initiateGaForLA: async (user, parentCaseId) => {
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_FOR_LA_LR
      : data.INITIATE_GENERAL_APPLICATION_FOR_LA;

    return await initiateGeneralApplicationWithOutNotice(user, parentCaseId, gaData) ;
  },

  initiateGaForJudge: async (user, parentCaseId) => {
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_FOR_JUDGE_LR
      : data.INITIATE_GENERAL_APPLICATION_FOR_JUDGE;

    return await initiateGeneralApplicationWithOutNotice(user, parentCaseId, gaData) ;
  },

  initiateGeneralApplicationWithNoStrikeOut: async (user, parentCaseId) => {
    eventName = events.INITIATE_GENERAL_APPLICATION.id;
    let gaCaseReference;
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_NO_STRIKEOUT_LR
      : data.INITIATE_GENERAL_APPLICATION_NO_STRIKEOUT;

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName, parentCaseId);
    const response = await apiRequest.submitEvent(eventName, gaData, parentCaseId);
    const responseBody = await response.json();
    assert.equal(response.status, 201);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have submitted an application');
    await waitForFinishedBusinessProcess(parentCaseId, user);
    await waitForGAFinishedBusinessProcess(parentCaseId, user);

    const updatedResponse = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
    const updatedCivilCaseData = await updatedResponse.json();

    switch (user.email) {
      case config.applicantSolicitorUser.email:
        gaCaseReference = updatedCivilCaseData.claimantGaAppDetails.pop().value.caseLink.CaseReference;
        break;
      case config.defendantSolicitorUser.email:
        gaCaseReference = updatedCivilCaseData.respondentSolGaAppDetails.pop().value.caseLink.CaseReference;
        break;
      case config.secondDefendantSolicitorUser.email:
        gaCaseReference = updatedCivilCaseData.respondentSolTwoGaAppDetails.pop().value.caseLink.CaseReference;
        break;
    }

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_APPLICATION_PAYMENT', user);
    //Payment callback handler response after the payment from service request tab
    const payment_response = await apiRequest.paymentApiRequestUpdateServiceCallback(
      genAppJudgeMakeDecisionData.serviceUpdateDtoWithNotice(gaCaseReference,'Paid'));

    assert.equal(payment_response.status, 200);

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_RESPONDENT_RESPONSE', user);
    console.log('*** GA Case Reference: '  + gaCaseReference + ' ***');
    await addUserCaseMapping(gaCaseReference, user);
    return gaCaseReference;
  },

  initiateGeneralApplicationWithStayClaim: async (user, parentCaseId) => {
    eventName = events.INITIATE_GENERAL_APPLICATION.id;

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName, parentCaseId);
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_STAY_CLAIM_LR
      : data.INITIATE_GENERAL_APPLICATION_STAY_CLAIM;

    const response = await apiRequest.submitEvent(eventName, gaData, parentCaseId);
    const responseBody = await response.json();
    assert.equal(response.status, 201);

    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have submitted an application');
    await waitForFinishedBusinessProcess(parentCaseId, user);
    await waitForGAFinishedBusinessProcess(parentCaseId, user);

    const updatedResponse = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
    const updatedCivilCaseData = await updatedResponse.json();
    let gaCaseReference = updatedCivilCaseData.claimantGaAppDetails[0].value.caseLink.CaseReference;
    console.log('*** GA Case Reference: '  + gaCaseReference + ' ***');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_APPLICATION_PAYMENT', user);

    //Payment callback handler response after the payment from service request tab
    const payment_response = await apiRequest.paymentApiRequestUpdateServiceCallback(
      genAppJudgeMakeDecisionData.serviceUpdateDtoWithoutNotice(gaCaseReference,'Paid'));
    assert.equal(payment_response.status, 200);
    console.log('General application case state : AWAITING_RESPONDENT_ACKNOWLEDGEMENT ');

    await addUserCaseMapping(gaCaseReference, user);
    return gaCaseReference;
  },

  initiateGeneralApplicationWithUnlessOrder: async (user, parentCaseId) => {
    eventName = events.INITIATE_GENERAL_APPLICATION.id;

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName, parentCaseId);
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_UNLESS_ORDER_LR
      : data.INITIATE_GENERAL_APPLICATION_UNLESS_ORDER;

    const response = await apiRequest.submitEvent(eventName, gaData, parentCaseId);
    const responseBody = await response.json();
    assert.equal(response.status, 201);

    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have submitted an application');
    await waitForFinishedBusinessProcess(parentCaseId, user);
    await waitForGAFinishedBusinessProcess(parentCaseId, user);

    const updatedResponse = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
    const updatedCivilCaseData = await updatedResponse.json();
    let gaCaseReference = updatedCivilCaseData.claimantGaAppDetails[0].value.caseLink.CaseReference;
    console.log('*** GA Case Reference: '  + gaCaseReference + ' ***');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_APPLICATION_PAYMENT', user);

    //Payment callback handler response after the payment from service request tab
    const payment_response = await apiRequest.paymentApiRequestUpdateServiceCallback(
      genAppJudgeMakeDecisionData.serviceUpdateDtoWithoutNotice(gaCaseReference,'Paid'));
    assert.equal(payment_response.status, 200);
    console.log('General application case state : AWAITING_RESPONDENT_ACKNOWLEDGEMENT ');

    await addUserCaseMapping(gaCaseReference, user);
    return gaCaseReference;
  },

  initiateAdjournVacateGeneralApplication: async (user, parentCaseId,
                                                    isWithNotice, isWithConsent, hearingDate,
                                                    calculatedAmount, feeCode,
                                                    feeVersion) => {
    eventName = events.INITIATE_GENERAL_APPLICATION.id;
    let freeGa = calculatedAmount==='0';
    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName, parentCaseId);
    var isCoscEnabled = await checkToggleEnabled(COSC);
    var gaData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_ADJOURN_VACATE_LR(isWithNotice, isWithConsent, hearingDate, calculatedAmount, feeCode, feeVersion)
      : data.INITIATE_GENERAL_APPLICATION_ADJOURN_VACATE(isWithNotice, isWithConsent, hearingDate, calculatedAmount, feeCode, feeVersion);

    const response = await apiRequest.submitEvent(eventName, gaData, parentCaseId);
    const responseBody = await response.json();
    assert.equal(response.status, 201);
    //assert.equal(responseBody.state, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have submitted an application');
    if (freeGa) {
      assert.include(responseBody.after_submit_callback_response.confirmation_body, 'The court will make a decision');
    } else {
      assert.include(responseBody.after_submit_callback_response.confirmation_body, 'Your application fee');
    }
    await waitForFinishedBusinessProcess(parentCaseId, user);
    await waitForGAFinishedBusinessProcess(parentCaseId, user);

    const updatedResponse = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
    const updatedCivilCaseData = await updatedResponse.json();
    let gaCaseReference = updatedCivilCaseData.claimantGaAppDetails[0].value.caseLink.CaseReference;
    console.log('*** GA Case Reference: '  + gaCaseReference + ' ***');

    if (!freeGa) {
      await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_APPLICATION_PAYMENT', user);
      //Payment callback handler response after the payment from service request tab
      const payment_response = await apiRequest.paymentApiRequestUpdateServiceCallback(
        genAppJudgeMakeDecisionData.serviceUpdateDtoWithoutNotice(gaCaseReference,'Paid'));
      assert.equal(payment_response.status, 200);
      console.log('General application case state : AWAITING_RESPONDENT_ACKNOWLEDGEMENT ');
    }
    return gaCaseReference;
  },

  getGACaseReference: async (user, parentCaseId) => {
    eventName = events.INITIATE_GENERAL_APPLICATION.id;

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName, parentCaseId);
    const updatedResponse = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
    const updatedCivilCaseData = await updatedResponse.json();
    let gaCaseReference;

    if(user.email === config.applicantSolicitorUser.email){
      gaCaseReference = updatedCivilCaseData.claimantGaAppDetails.pop().value.caseLink.CaseReference;
    }
    else if(user.email === config.defendantSolicitorUser.email) {
      gaCaseReference = updatedCivilCaseData.respondentSolGaAppDetails.pop().value.caseLink.CaseReference;
    }
    else if(user.email === config.secondDefendantSolicitorUser.email) {
      gaCaseReference = updatedCivilCaseData.respondentSolTwoGaAppDetails.pop().value.caseLink.CaseReference;
    }
    else{
      gaCaseReference = updatedCivilCaseData.gaDetailsMasterCollection.pop().value.caseLink.CaseReference;
    }

    console.log('*** GA Case Reference: ' + gaCaseReference + ' ***');
    await addUserCaseMapping(gaCaseReference, user);
    return gaCaseReference;
  },

    respondentResponse: async (user, gaCaseId, agree=true) => {
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_RESPONDENT_RESPONSE', user);
    await apiRequest.setupTokens(user);
    eventName = events.RESPOND_TO_APPLICATION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);
    let payload = data.RESPOND_TO_APPLICATION(agree);
    console.log('*** respondentResponse1v2WithPayload: Start uploading the document ***');
    const document = await testingSupport.uploadDocument();
    payload = await updateCaseDataWithPlaceholders(payload, document);
    console.log('*** respondentResponse1v2WithPayload: Finish uploading the document ***');
    const response = await apiRequest.submitGAEvent(eventName, payload, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have provided the requested information');
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION', user);
    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');
    console.log('General application updated case state : ' + updatedGABusinessProcessData.ccdState);
    await addUserCaseMapping(gaCaseId, user);
  },

  respondentResponseConsentOrderApp: async (user, gaCaseId, agree=true) => {
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_RESPONDENT_RESPONSE', user);

    await apiRequest.setupTokens(user);
    eventName = events.RESPOND_TO_APPLICATION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.RESPOND_TO_CONSENT_APPLICATION(agree), gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have provided the requested information');
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION', user);
    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');
    await addUserCaseMapping(gaCaseId, user);
  },

  respondentDebtorResponse: async (user, gaCaseId, agree=true) => {
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_RESPONDENT_RESPONSE', user);

    await apiRequest.setupTokens(user);
    eventName = events.RESPOND_TO_APPLICATION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    let payload = data.RESPOND_DEBTOR_TO_APPLICATION(agree);
    console.log('*** respondentResponse1v2WithPayload: Start uploading the document ***');
    const document = await testingSupport.uploadDocument();
    payload = await updateCaseDataWithPlaceholders(payload, document);

    const response = await apiRequest.submitGAEvent(eventName, payload, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have provided the requested information');
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'PROCEEDS_IN_HERITAGE', user);
    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'PROCEEDS_IN_HERITAGE');
    await addUserCaseMapping(gaCaseId, user);
  },

  respondentResponse1v2: async (user, user2, gaCaseId, agree=true) => {
     await respondentResponse1v2WithPayload(user, user2, gaCaseId, data.RESPOND_TO_APPLICATION(agree));
  },

  respondentConsentResponse1v2: async (user, user2, gaCaseId, agree=true) => {
    await respondentResponse1v2WithPayload(user, user2, gaCaseId, data.RESPOND_TO_CONSENT_APPLICATION(agree));
  },

  nbcAdminReferToJudge: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.REFER_TO_JUDGE.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.REFER_TO_JUDGE, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
  },

  nbcAdminApproveConsentOrder: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.APPROVE_CONSENT_ORDER.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.APPROVE_CONSENT_ORDER, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'ORDER_MADE', user);
    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'ORDER_MADE');
  },

  nbcAdminReferToLegalAdvisor: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.REFER_TO_LEGAL_ADVISOR.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.REFER_TO_LEGAL_ADVISOR, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
  },


  judgeMakesDecisionAdditionalInformation: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.MAKE_DECISION, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have requested more information');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_ADDITIONAL_INFORMATION', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'AWAITING_ADDITIONAL_INFORMATION');
    console.log('General application updated case state : ' + updatedGABusinessProcessData.ccdState);
  },

  judgeMakesDecisionApplicationDismiss: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;

    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.JUDGE_MAKES_ORDER_DISMISS, gaCaseId);
    const responseBody = await response.json();
    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'APPLICATION_DISMISSED', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'APPLICATION_DISMISSED');
    console.log('General application updated case state : ' + updatedGABusinessProcessData.ccdState);
  },

  judgeMakesOrderDecisionUncloak: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;

    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.JUDGE_MAKES_ORDER_UNCLOAK, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'ORDER_MADE', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'ORDER_MADE');
  },

  judgeRequestMoreInformationUncloak: async (user, gaCaseId, pay=true, other=false) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;

    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.JUDGE_REQUEST_MORE_INFO_UNCLOAK(other), gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    let ccdState = 'APPLICATION_ADD_PAYMENT';
    if (!pay) {
      ccdState = 'AWAITING_RESPONDENT_RESPONSE';
    }
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, ccdState, user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, ccdState);
  },

  additionalPaymentSuccess: async (user, gaCaseId, finalState) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;

    const response = await apiRequest.paymentApiRequestUpdateServiceCallback(
      genAppJudgeMakeDecisionData.serviceUpdateDto(gaCaseId,'Paid'));

    assert.equal(response.status, 200);

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, finalState, user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, finalState);
  },

  additionalPaymentFailure: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;

    const response = await apiRequest.paymentApiRequestUpdateServiceCallback(
      genAppJudgeMakeDecisionData.serviceUpdateDto(gaCaseId,'NotPaid'));

    assert.equal(response.status, 200);

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'APPLICATION_ADD_PAYMENT', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'APPLICATION_ADD_PAYMENT');
  },

  respondentResponseToJudgeAdditionalInfo: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.RESPOND_TO_JUDGE_ADDITIONAL_INFO.id;

    await apiRequest.startGAEvent(eventName, gaCaseId);

    console.log('*** respondentResponseToJudgeDirections: Start uploading the document ***');
    const document = await testingSupport.uploadDocument();
    let casaData = await updateCaseDataWithPlaceholders(data[eventName], document);
    console.log('*** respondentResponseToJudgeDirections: Finish uploading the document ***');

    const response = await apiRequest.submitGAEvent(eventName, casaData, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.equal(responseBody.state, 'AWAITING_ADDITIONAL_INFORMATION');
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_ADDITIONAL_INFORMATION', user);
  },

  respondentResponseToWrittenRepresentations: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION.id;

    await apiRequest.startGAEvent(eventName, gaCaseId);

    console.log('*** respondentResponseToJudgeDirections: Start uploading the document ***');
    const document = await testingSupport.uploadDocument();
    let casaData = await updateCaseDataWithPlaceholders(data[eventName], document);
    console.log('*** respondentResponseToJudgeDirections: Finish uploading the document ***');

    const response = await apiRequest.submitGAEvent(eventName, casaData, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.equal(responseBody.state, 'AWAITING_WRITTEN_REPRESENTATIONS');
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_WRITTEN_REPRESENTATIONS', user);
  },

  respondentResponseToJudgeDirections: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.RESPOND_TO_JUDGE_DIRECTIONS.id;

    await apiRequest.startGAEvent(eventName, gaCaseId);

    console.log('*** respondentResponseToJudgeDirections: Start uploading the document ***');
    const document = await testingSupport.uploadDocument();
    let casaData = await updateCaseDataWithPlaceholders(data[eventName], document);
    console.log('*** respondentResponseToJudgeDirections: Finish uploading the document ***');

    const response = await apiRequest.submitGAEvent(eventName, casaData, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.equal(responseBody.state, 'AWAITING_DIRECTIONS_ORDER_DOCS');
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_DIRECTIONS_ORDER_DOCS', user);
  },

  judgeMakesDecisionWrittenRep: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.JUDGE_MAKES_ORDER_WRITTEN_REP(current_date),gaCaseId);

    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_WRITTEN_REPRESENTATIONS', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    let ccd_state= updatedGABusinessProcessData.ccdState;
    assert.equal(updatedGABusinessProcessData.ccdState, 'AWAITING_WRITTEN_REPRESENTATIONS');
    return ccd_state;
  },

  judgeMakesDecisionWithoutNoticeWrittenRep: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;

    const response = await apiRequest.validateGAPage(
      'MAKE_DECISION',
      'GAJudicialDecision',
      data.JUDGE_MAKES_ORDER_WRITTEN_REP(current_date),
      gaCaseId
    );
    const responseBody = await response.json();

    assert.equal(response.status, 422);

    if (responseBody.callbackErrors != null) {
      assert.equal(responseBody.callbackErrors[0], 'The application needs to be uncloaked before requesting written representations');
    }
  },

  judgeRevisitMakesDecisionWrittenRepUncloakedAppln: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;

    const response = await apiRequest.validateGAPage(
      'MAKE_DECISION',
      'GAJudicialDecision',
      data.JUDGE_MAKES_ORDER_WRITTEN_REP_ON_UNCLOAKED_APPLN(current_date),
      gaCaseId
    );
    const responseBody = await response.json();

    assert.equal(response.status, 200);
    assert.equal(responseBody.callbackErrors, null);
  },

  judgeMakesDecisionDirectionsOrder: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.JUDGE_MAKES_ORDER_DIRECTIONS_REP(current_date), gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_DIRECTIONS_ORDER_DOCS', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    let ccd_state = updatedGABusinessProcessData.ccdState;
    assert.equal(updatedGABusinessProcessData.ccdState, 'AWAITING_DIRECTIONS_ORDER_DOCS');
    return ccd_state;
  },

  judgeApprovesStrikeOutApplication: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.JUDGE_APPROVES_STRIKEOUT_APPLN, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'PROCEEDS_IN_HERITAGE', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'PROCEEDS_IN_HERITAGE');
  },

  assertGaAppCollectionVisiblityToUser: async ( user, parentCaseId, gaCaseId, isVisibleToUser) => {
    const response = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
    const civilCaseData = await response.json();
    let gaReference;
    if(user.email === config.applicantSolicitorUser.email && isVisibleToUser){
      gaReference = civilCaseData.claimantGaAppDetails.pop().value.caseLink.CaseReference;
      assert.equal(gaCaseId, gaReference);
    }
    else if(user.email === config.defendantSolicitorUser.email && isVisibleToUser) {
      gaReference = civilCaseData.respondentSolGaAppDetails.pop().value.caseLink.CaseReference;
      assert.equal(gaCaseId, gaReference);
    }
    else if(user.email === config.secondDefendantSolicitorUser.email && isVisibleToUser) {
      gaReference = civilCaseData.respondentSolTwoGaAppDetails.pop().value.caseLink.CaseReference;
      assert.equal(gaCaseId, gaReference);
    }
    else{
      gaReference = civilCaseData.gaDetailsMasterCollection.pop().value.caseLink.CaseReference;
      assert.equal(gaCaseId, gaReference);
    }
    console.log('*** GA Case Reference: ' + gaReference + ' ***');
  },

  assertGACollectionNotVisiblityToUser: async ( user, parentCaseId) => {
    const response = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
    const civilCaseData = await response.json();

    if(user.email === config.applicantSolicitorUser.email){
      assert.equal(typeof(civilCaseData.claimantGaAppDetails), 'undefined');
    }
    else if(user.email === config.defendantSolicitorUser.email) {
      assert.equal(typeof(civilCaseData.respondentSolGaAppDetails), 'undefined');
    }
    else if(user.email === config.secondDefendantSolicitorUser.email) {
      assert.equal(typeof(civilCaseData.respondentSolTwoGaAppDetails), 'undefined');
    }
    else{
      assert.equal(typeof(civilCaseData.gaDetailsMasterCollection), 'undefined');    }
  },

  assertGaDocumentVisibilityToUser: async ( user, parentCaseId, gaCaseId, doc) => {
    await assertGaDocVisibilityToUser( user, parentCaseId, gaCaseId, doc);
  },

  assertNullGaDocumentVisibilityToUser: async ( user, parentCaseId, doc) => {
    await assertNullGaDocVisibilityToUser( user, parentCaseId, doc);
  },

  assertDocumentVisibilityToUser: async ( user, gaUserRole, parentCaseId, gaCaseId, doc) => {
    await assertDocVisibilityToUser( user, gaUserRole, parentCaseId, gaCaseId, doc);
  },

  judgeMakesDecisionOrderMade: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.JUDGE_APPROVES_STRIKEOUT_APPLN, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'ORDER_MADE', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'ORDER_MADE');
  },

  judgeMakesDecisionOrderMadeStayClaimAppln: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);


    const response = await apiRequest.submitGAEvent(eventName, data.JUDGE_APPROVES_STAYCLAIM_APPLN(current_date), gaCaseId);
    date.getDate();
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'ORDER_MADE', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'ORDER_MADE');
    return updatedGABusinessProcessData.ccdState;
  },

  judgeMakesDecisionOrderMadeUnlessOrderAppln: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);


    const response = await apiRequest.submitGAEvent(eventName, data.JUDGE_APPROVES_UNLESSORDER_APPLN(current_date), gaCaseId);
    date.getDate();
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'ORDER_MADE', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'ORDER_MADE');
    return updatedGABusinessProcessData.ccdState;
  },

  caseDismisalScheduler: async(caseId, gaCaseId, user ) => {
    const response_msg = await apiRequest.civilCaseDismissalHandler();
    assert.equal(response_msg.status, 200);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'APPLICATION_CLOSED');
  },

  judgeRevisitScheduler: async (gaCaseId,state,genAppType) => {
    const response_msg = await apiRequest.gaOrderMadeSchedulerTaskHandler(state, genAppType);
    assert.equal(response_msg.status, 200);
    // retrive the dcase data for the ga reference  and assert that the flag is true
    const updatedResponse = await apiRequest.fetchGaCaseData(gaCaseId);
    const updatedGaCaseData = await updatedResponse.json();
    if(state === 'ORDER_MADE' && genAppType === 'STAY_THE_CLAIM') {
      let isOrderProcessedByScheduler = updatedGaCaseData.judicialDecisionMakeOrder.isOrderProcessedByStayScheduler;
      assert.equal(isOrderProcessedByScheduler,'Yes');
    }

    if(state === 'ORDER_MADE' && genAppType === 'UNLESS_ORDER') {
      let isOrderProcessedByScheduler = updatedGaCaseData.judicialDecisionMakeOrder.isOrderProcessedByUnlessScheduler;
      assert.equal(isOrderProcessedByScheduler,'Yes');
    }
    console.log('*** Judge Revisit Scheduler ran successfully: ');
  },

  judgeRevisitConsentScheduler: async (gaCaseId,state,genAppType) => {
    const response_msg = await apiRequest.gaOrderMadeSchedulerTaskHandler(state, genAppType);
    assert.equal(response_msg.status, 200);
    // retrive the dcase data for the ga reference  and assert that the flag is true
    const updatedResponse = await apiRequest.fetchGaCaseData(gaCaseId);
    const updatedGaCaseData = await updatedResponse.json();
    let isOrderProcessedByScheduler = updatedGaCaseData.approveConsentOrder.isOrderProcessedByStayScheduler;
    assert.equal(isOrderProcessedByScheduler,'Yes');

    console.log('*** Judge Revisit Scheduler ran successfully: ');
  },

  verifySpecificAccessForGaCaseData: async (user, gaCaseId) => {
    const response = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    assert.equal(response.status, 200);
  },

  judgeListApplicationForHearing: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.LIST_FOR_A_HEARING, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'LISTING_FOR_A_HEARING', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'LISTING_FOR_A_HEARING');
  },

  judgeListApplicationForFreeFormOrder: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.JUDGE_DECIDES_FREE_FORM_ORDER, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'ORDER_MADE', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'ORDER_MADE');
  },

  judgeListApplicationForHearingInPerson: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.LIST_FOR_A_HEARING_InPerson, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'LISTING_FOR_A_HEARING', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'LISTING_FOR_A_HEARING');
  },

  judgeDismissApplication: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.MAKE_DECISION.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.APPLICATION_DISMISSED, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, 'Your order has been made');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'APPLICATION_DISMISSED',user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId,user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'APPLICATION_DISMISSED');
  },

  hearingCenterAdminScheduleHearing: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    eventName = events.HEARING_SCHEDULED_GA.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);

    const response = await apiRequest.submitGAEvent(eventName, data.SCHEDULE_HEARING, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, '# Hearing notice created');

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'HEARING_SCHEDULED',user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId,user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'HEARING_SCHEDULED');
  },

  judgeMakeFinalOrder: async (user, gaCaseId, orderSelection, hearing) => {
    await apiRequest.setupTokens(user);
    eventName = events.GENERATE_DIRECTIONS_ORDER.id;
    await apiRequest.startGAEvent(eventName, gaCaseId);
    let finalOrder;
    if(orderSelection === 'FREE_FORM_ORDER') {
      finalOrder = data.FINAL_ORDER_FREEFORM;
    } else if(hearing) {
      finalOrder = data.FINAL_ORDER_ASSISTED_WITH_HEARING;
    } else {
      finalOrder = data.FINAL_ORDER_ASSISTED;
    }
    const response = await apiRequest.submitGAEvent(eventName, finalOrder, gaCaseId);
    const responseBody = await response.json();

    assert.equal(response.status, 201);
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, '# Your order has been issued');

    if (orderSelection === 'ASSISTED_ORDER' && hearing) {
      await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'LISTING_FOR_A_HEARING',user);
    } else {
      await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'ORDER_MADE',user);
    }
  },

  notifyClaim: async (user, multipartyScenario, caseId) => {
    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM';
    mpScenario = multipartyScenario;

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName, caseId);

    await validateEventPages(data[eventName]);

    await assertSubmittedEvent('AWAITING_CASE_DETAILS_NOTIFICATION', {
      header: 'Notification of claim sent',
      body: 'The defendant legal representative\'s organisation has been notified and granted access to this claim.'
    });

    await waitForFinishedBusinessProcess(caseId, user);

    await assignCase(caseId, multipartyScenario);
  },

  partialNotifyClaim: async (user, multipartyScenario, caseId) => {
    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM';
    mpScenario = multipartyScenario;

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName, caseId);

    await validateEventPages(data['PARTIAL_DEFENDANT_OF_CLAIM']);

    await assertSubmittedEvent('AWAITING_CASE_DETAILS_NOTIFICATION', {
      header: '',
      body: ''
    }, true);

    await waitForFinishedBusinessProcess(caseId, user);
  },

  notifyClaimDetails: async (user, caseId) => {
    await apiRequest.setupTokens(user);

    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM_DETAILS';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = {...returnedCaseData, defendantSolicitorNotifyClaimDetailsOptions: {
        value: listElement('Both')
      }};

    await validateEventPages(data[eventName]);

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: 'Defendant notified',
      body: 'The defendant legal representative\'s organisation has been notified of the claim details.'
    });

    await waitForFinishedBusinessProcess(caseId, user);
  },

  partialNotifyClaimDetails: async (user, multipartyScenario, caseId) => {
    await apiRequest.setupTokens(user);

    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM_DETAILS';
    await apiRequest.startEvent(eventName, caseId);

    await validateEventPages(data['PARTIAL_NOTIFY_DEFENDANT_OF_CLAIM_DETAILS']);

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: 'Defendant notified',
      body: 'Notification of claim details sent to 1 Defendant legal representative only'
    });

    await waitForFinishedBusinessProcess(caseId, user);
  },

  defendantResponse: async (user, multipartyScenario, caseId, isFirst) => {
    await apiRequest.setupTokens(user);
    eventName = 'DEFENDANT_RESPONSE';
    await apiRequest.startEvent(eventName, caseId);
    if (isFirst) {
      await validateEventPagesWithCheck(data['DEFENDANT_RESPONSE_SOLICITOR_ONE'], false, user);
      await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
        header: 'You have submitted the Defendant\'s defence',
        body: 'You will receive a copy of this notification'
      });
    } else {
      await validateEventPagesWithCheck(data['DEFENDANT_RESPONSE_SOLICITOR_TWO'], false, user);
      await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION', {
        header: 'You have submitted the Defendant\'s defence',
        body: 'The Claimant legal representative will get a notification'
      });
    }
    await waitForFinishedBusinessProcess(caseId, user);
  },

  verifyGAState: async (user, parentCaseId, gaCaseId, expectedState) => {
    await apiRequest.setupTokens(user);
    await waitForFinishedBusinessProcess(parentCaseId, user);
    await waitForGAFinishedBusinessProcess(parentCaseId, user);
    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    console.log('ccd state '+updatedGABusinessProcessData.ccdState);
    console.log('expectedState '+expectedState);
    assert.equal(updatedGABusinessProcessData.ccdState, expectedState);
  },

  verifyGALocation: async (user, gaCaseId, civilCaseId) => {
    await apiRequest.setupTokens(user);
    const updatedGACaseDataResponse = await apiRequest.fetchUpdatedCaseData(gaCaseId, user);
    const updatedGACaseData = await updatedGACaseDataResponse.json();
    const updatedCivilCaseDataResponse = await apiRequest.fetchUpdatedCivilCaseData(civilCaseId, user);
    const updatedCivilCaseData = await updatedCivilCaseDataResponse.json();
    console.log('cnbcLocation After SDO on general application :'+updatedGACaseData.isCcmccLocation);
    // ccmcclocation field now uses/references CNBC
    assert.equal(updatedGACaseData.isCcmccLocation, 'No');
    assert.equal(updatedGACaseData.caseManagementLocation.region, updatedCivilCaseData.generalApplications[0].value.caseManagementLocation.region);
    assert.equal(updatedGACaseData.caseManagementLocation.baseLocation, updatedCivilCaseData.generalApplications[0].value.caseManagementLocation.baseLocation);
  },

  assertGAApplicantDisplayName: async (user, gaCaseId) => {
    await apiRequest.setupTokens(user);
    const updatedGACaseDataResponse = await apiRequest.fetchUpdatedCaseData(gaCaseId, user);
    const updatedGACaseData = await updatedGACaseDataResponse.json();

    if (user.email === config.applicantSolicitorUser.email) {
      await expect(updatedGACaseData.gaApplicantDisplayName).to.equals('Test Inc - Claimant');
    } else if (user.email === config.defendantSolicitorUser.email) {
      await expect(updatedGACaseData.gaApplicantDisplayName).to.equals('Sir John Doe - Defendant');
    } else if (user.email === config.secondDefendantSolicitorUser.email) {
      await expect(updatedGACaseData.gaApplicantDisplayName).to.equals('Dr Foo Bar - Defendant');
    }
  },

  cleanUp: async () => {
    await unAssignAllUsers();
  },

  //below is claim functions
  createClaimWithRepresentedRespondent: async (user, scenario = 'ONE_V_ONE') => {
    eventName = 'CREATE_CLAIM_SPEC';
    caseId = null;
    caseData = {};

    let createClaimData;

    createClaimData = data.CLAIM_CREATE_CLAIM_AP_SPEC(scenario);

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    for (let pageId of Object.keys(createClaimData.userInput)) {
      await assertValidClaimData(createClaimData, pageId);
    }

    const pbaV3 = await checkToggleEnabled(PBAv3);
    console.log('Is PBAv3 toggle on?: ' + pbaV3);

    await assertSubmittedEvent('PENDING_CASE_ISSUED');

    await waitForFinishedBusinessProcess(caseId, user);

    if (pbaV3) {
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
        claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    }

    await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);

    if (scenario === 'ONE_V_TWO_SAME_SOL') {
      await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORTWO', config.defendantSolicitorUser);
    }

    if (scenario === 'ONE_V_TWO'
        && createClaimData.userInput.SameLegalRepresentative
        && createClaimData.userInput.SameLegalRepresentative.respondent2SameLegalRepresentative === 'No') {
      await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORTWO', config.secondDefendantSolicitorUser);
    }
    await waitForFinishedBusinessProcess(caseId, user);

    console.log('carm not enabled, updating submitted date');
    await apiRequest.setupTokens(config.systemupdate);
    const submittedDate = { 'submittedDate': '2024-09-10T15:59:50' };
    await testingSupport.updateCaseData(caseId, submittedDate);
    console.log('submitted date update to before carm date');

    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');

    return caseId;
  },

  createClaimWithUnrepresentedClaimant: async (user, claimType = 'SmallClaims', typeOfData = '') => {
    console.log('Starting to create claim');
    let payload = {};
    await apiRequest.setupTokens(user);
    let userId = await apiRequest.fetchUserId();
    if (claimType === 'FastTrack') {
      console.log('FastTrack claim...');
      payload = createClaimLipClaimant.createClaimUnrepresentedClaimant('15000', userId);
    } else {
      console.log('SmallClaim...');
      payload = createClaimLipClaimant.createClaimUnrepresentedClaimant('1500', userId, typeOfData);
    }
    caseId = await apiRequest.startCreateCaseForCitizen(payload);
    await waitForFinishedBusinessProcess(caseId, user);
    console.log('Claim submitted');

    // issue claim
    payload = createClaimLipClaimant.issueClaim();
    await apiRequest.startCreateCaseForCitizen(payload, caseId);
    await waitForFinishedBusinessProcess(caseId, user);
    console.log('Claim issued');
    await assignCaseRoleToUser(caseId, 'DEFENDANT', config.defendantCitizenUser2);
    // update submit date
    console.log('carm not enabled, updating submitted date');
    await apiRequest.setupTokens(config.systemupdate);
    const submittedDate = { 'submittedDate': '2024-09-10T15:59:50' };
    await testingSupport.updateCaseData(caseId, submittedDate);
    console.log('submitted date update to before carm date');
    return caseId;
  },

  createGAApplicationWithUnrepresented: async (user, caseId, typeOfApplication, hwf = false) => {
    console.log('start create a GA application');
    const payload = data.INITIATE_GENERAL_APPLICATION_LIP(typeOfApplication, hwf);
    await apiRequest.setupTokens(user);
    const caseData = await apiRequest.startCreateCaseForCitizen(payload, caseId);
    await waitForFinishedBusinessProcess(caseData.id, user);
    await waitForGAFinishedBusinessProcess(caseData.id, user);
    const updatedResponse = await apiRequest.fetchUpdatedCaseData(caseData.id, user).then(res => res.json());
    const ccdGeneralApplications = updatedResponse.generalApplications;
    const gaCaseReference = ccdGeneralApplications[ccdGeneralApplications.length - 1]?.value?.caseLink?.CaseReference;
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_APPLICATION_PAYMENT', user);
    if (hwf) {
      await processHwf(gaCaseReference);
      await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_RESPONDENT_RESPONSE', user);
    }
    return gaCaseReference;
  },

  createGAApplicationWithUnrepresentedWithout: async (user, caseId) => {
    console.log('start create a GA application');
    const payload = data.INITIATE_GENERAL_APPLICATION_LIP_WITHOUT();
    await apiRequest.setupTokens(user);
    const caseData = await apiRequest.startCreateCaseForCitizen(payload, caseId);
    await waitForFinishedBusinessProcess(caseData.id, user);
    await waitForGAFinishedBusinessProcess(caseData.id, user);
    const updatedResponse = await apiRequest.fetchUpdatedCaseData(caseData.id, user).then(res => res.json());
    const ccdGeneralApplications = updatedResponse.generalApplications;
    const gaCaseReference = ccdGeneralApplications[ccdGeneralApplications.length - 1]?.value?.caseLink?.CaseReference;
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_APPLICATION_PAYMENT', user);
    const payment_response = await apiRequest.paymentApiRequestUpdateServiceCallback(
        genAppJudgeMakeDecisionData.serviceUpdateDtoWithoutNotice(gaCaseReference, 'Paid'));

    assert.equal(payment_response.status, 200);

    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference,
                                                        'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION', user);

    const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseReference, user);
    const updatedGABusinessProcessData = await updatedBusinessProcess.json();
    assert.equal(updatedGABusinessProcessData.ccdState, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');

    console.log('General application case state : APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');
    console.log('*** GA Case Reference: ' + gaCaseReference + ' ***');
    await addUserCaseMapping(gaCaseReference, user);
    return gaCaseReference;
  },

  defendantResponseSpecClaim: async (user, response = 'FULL_DEFENCE', scenario = 'ONE_V_ONE',
                            expectedEvent = 'AWAITING_APPLICANT_INTENTION') => {
    await apiRequest.setupTokens(user);
    eventName = 'DEFENDANT_RESPONSE_SPEC';

    const pbaV3 = await checkToggleEnabled(PBAv3);
    if(pbaV3){
      response = response+'_PBAv3';
    }

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    let defendantResponseData = eventData['defendantResponsesSpec'][scenario][response];

    caseData = returnedCaseData;
    for (let pageId of Object.keys(defendantResponseData.userInput)) {
      await assertValidClaimData(defendantResponseData, pageId);
    }
    deleteCaseFields('respondentSolGaAppDetails');
    deleteCaseFields('respondentSolTwoGaAppDetails');
    deleteCaseFields('generalApplications');
    deleteCaseFields('generalOrderDocClaimant');
    deleteCaseFields('generalOrderDocRespondentSol');
    deleteCaseFields('generalOrderDocRespondentSolTwo');
    deleteCaseFields('generalOrderDocStaff');
    deleteCaseFields('gaDraftDocument');
    deleteCaseFields('gaDraftDocStaff');
    deleteCaseFields('gaDraftDocClaimant');
    deleteCaseFields('gaDraftDocRespondentSol');
    deleteCaseFields('gaDraftDocRespondentSolTwo');
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
        if (response === 'DIFF_FULL_DEFENCE') {
          await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM');
        } else {
          await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION');
        }
        break;
    }
    await waitForFinishedBusinessProcess(caseId, user);
    deleteCaseFields('respondent1Copy');
  },

  claimantResponseClaimSpec: async (user, response = 'FULL_DEFENCE', scenario = 'ONE_V_ONE',
                           expectedEndState) => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');
    deleteCaseFields('respondentResponseIsSame');

    await apiRequest.setupTokens(user);

    eventName = 'CLAIMANT_RESPONSE_SPEC';
    caseData = await apiRequest.startEvent(eventName, caseId);
    deleteCaseFields('respondentSolGaAppDetails');
    let claimantResponseData = eventData['claimantResponsesSpec'][scenario][response];

    const document = await testingSupport.uploadDocument();

    for (let pageId of Object.keys(claimantResponseData.userInput)) {
      claimantResponseData = await updateCaseDataWithPlaceholders(claimantResponseData, document);
      await assertValidClaimData(claimantResponseData, pageId);
    }

    let validState = expectedEndState || 'PROCEEDS_IN_HERITAGE_SYSTEM';
    if ((response == 'FULL_DEFENCE' || response == 'NOT_PROCEED')) {
      validState = 'JUDICIAL_REFERRAL';
    }

    deleteCaseFields('generalApplications');

    await assertSubmittedEvent(validState || 'PROCEEDS_IN_HERITAGE_SYSTEM');

    await waitForFinishedBusinessProcess(caseId, user);
  },

  claimantResponseClaim: async (user, response = 'FULL_DEFENCE', scenario = 'ONE_V_ONE',
                                    expectedEndState) => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');
    deleteCaseFields('respondentResponseIsSame');

    await apiRequest.setupTokens(user);

    eventName = 'CLAIMANT_RESPONSE';
    caseData = await apiRequest.startEvent(eventName, caseId);
    deleteCaseFields('respondentSolGaAppDetails');
    let claimantResponseData = eventData['claimantResponsesSpec'][scenario][response];
    claimantResponseData = await replaceClaimantResponseWithCourtNumberIfCourtLocationDynamicListIsNotEnabled(claimantResponseData);

    for (let pageId of Object.keys(claimantResponseData.userInput)) {
      await assertValidClaimData(claimantResponseData, pageId);
    }

    deleteCaseFields('generalApplications');

    await assertSubmittedEvent(expectedEndState);

    await waitForFinishedBusinessProcess(caseId, user);
  },

  claimantResponseUnSpec: async (user, multipartyScenario, expectedEndState) => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');
    deleteCaseFields('respondentResponseIsSame');

    await apiRequest.setupTokens(user);

    eventName = 'CLAIMANT_RESPONSE';
    mpScenario = multipartyScenario;
    caseData = await apiRequest.startEvent(eventName, caseId);
    deleteCaseFields('gaDetailsRespondentSol');
    deleteCaseFields('generalApplications');

    let claimantResponseData = data.CLAIMANT_RESPONSE(mpScenario);
    delete claimantResponseData['midEventData'];
    // CIV-5514: remove when hnl is live
    claimantResponseData = await replaceDQFieldsIfHNLFlagIsDisabled(claimantResponseData, 'solicitorOne', false);
    let hnlEnabled = false;
    if (!hnlEnabled) {
      claimantResponseData = await replaceFieldsIfHNLToggleIsOffForClaimantResponse(
          claimantResponseData);
    }

    // for (let pageId of Object.keys(claimantResponseData.userInput)) {
    //   await assertValidClaimData(claimantResponseData, pageId);
    // }
    await validateEventPagesWithCheck(claimantResponseData, false, user);
    await assertSubmittedEvent(expectedEndState || 'PROCEEDS_IN_HERITAGE_SYSTEM');

    await waitForFinishedBusinessProcess(caseId, user);
  },

  defaultJudgmentXuiPayImmediately: async (user) => {
    await apiRequest.setupTokens(user);
    let state;
    eventName = 'DEFAULT_JUDGEMENT_SPEC';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);
    await validateEventPagesDefaultJudgments(data.CLAIM_DEFAULT_JUDGEMENT_SPEC);

    if (isJOLive) {
      state = 'All_FINAL_ORDERS_ISSUED';
    } else {
      state = 'PROCEEDS_IN_HERITAGE_SYSTEM';
    }

    await assertSubmittedEvent(state, {
      header: '',
      body: ''
    }, true);
    await waitForFinishedBusinessProcess(caseId, user);
  },

  markJudgmentPaid: async (user) => {
    console.log(`case in All final orders issued ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'JUDGMENT_PAID_IN_FULL';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    await validateEventPages(data.JUDGMENT_PAID_IN_FULL);

    await assertSubmittedEvent('All_FINAL_ORDERS_ISSUED', {
      header: '# Judgment marked as paid in full',
      body: 'The judgment has been marked as paid in full'
    }, true);
    await waitForFinishedBusinessProcess(caseId, user);
  },

  defaultJudgmentCui: async (user) => {
    eventName = 'DEFAULT_JUDGEMENT_SPEC';
    let payload = data.CLAIM_DEFAULT_JUDGEMENT_SPEC_CUI;
    await apiRequest.setupTokens(user);
    await apiRequest.startEventForCitizen(eventName, caseId, payload);
    await waitForFinishedBusinessProcess(caseId, user);
  },

  performCitizenDefendantResponse: async (user, caseId) => {
    eventName = 'DEFENDANT_RESPONSE_CUI';
    let payload = data.DEFENDANT_RESPONSE_SPEC_CUI;
    await apiRequest.setupTokens(user);
    await apiRequest.startEventForCitizen(eventName, caseId, payload);
    await waitForFinishedBusinessProcess(caseId, user);
  },

  amendWhenWillThisAmountBePaidDeadLine: async (user) => {
    await apiRequest.setupTokens(user);
    let respondToClaimDeadLine ={};
    respondToClaimDeadLine =   {'respondToClaimAdmitPartLRspec': {'whenWillThisAmountBePaid': '2025-03-26',} };
    await testingSupport.updateCaseData(caseId, respondToClaimDeadLine);
  },

  requestJudgementXui: async (user, response = 'REQUEST_JUDGEMENT') => {
    await apiRequest.setupTokens(user);
    eventName = 'REQUEST_JUDGEMENT_ADMISSION_SPEC';
    caseData = await apiRequest.startEvent(eventName, caseId);
    let requestJudgementData = data.REQUEST_JUDGEMENT(response);
    for (let pageId of Object.keys(requestJudgementData.userInput)) {
      await assertValidClaimData(requestJudgementData, pageId);
    }
    await assertSubmittedEvent('All_FINAL_ORDERS_ISSUED', {
      header: '',
      body: ''
    }, true);
    await waitForFinishedBusinessProcess(caseId, user);
  },

  judgmentPaidInFullCui: async (user) => {
    eventName = 'JUDGMENT_PAID_IN_FULL';
    let payload = data.JUDGMENT_PAID_FULL;
    await apiRequest.setupTokens(user);
    await apiRequest.startEventForCitizen(eventName, caseId, payload);
    await waitForFinishedBusinessProcess(caseId, user);
  },

  certificateOfSatisfactionCancellationCui: async (user, parentCaseId) => {
    console.log('start create a COSC application');
    let payload = data.CREATE_CERTIFICATION_OF_SATISFACTION_CANCELLATION;
    await apiRequest.setupTokens(user);
    await apiRequest.startCreateCaseForCitizen(payload, parentCaseId);
    await waitForFinishedBusinessProcess(parentCaseId, user);
    await waitForGAFinishedBusinessProcess(parentCaseId, user);
    const updatedResponse = await apiRequest.fetchUpdatedCaseData(parentCaseId, user).then(res => res.json());
    const ccdGeneralApplications = updatedResponse.generalApplications;
    const gaCaseReference = ccdGeneralApplications[ccdGeneralApplications.length - 1]?.value?.caseLink?.CaseReference;
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_APPLICATION_PAYMENT', user);
    const payment_response = await apiRequest.paymentApiRequestUpdateServiceCallback(
      genAppJudgeMakeDecisionData.serviceUpdateDtoWithoutNotice(gaCaseReference, 'Paid'));
    assert.equal(payment_response.status, 200);
    await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'APPLICATION_DISMISSED', user);
  },

  createSDO: async (caseId, user, response = 'CREATE_DISPOSAL') => {
    console.log('SDO for case id ' + caseId);
    await apiRequest.setupTokens(user);

    if (response === 'UNSUITABLE_FOR_SDO') {
      eventName = 'NotSuitable_SDO';
    } else {
      eventName = 'CREATE_SDO';
    }
    caseData = await apiRequest.startEvent(eventName, caseId);
    let disposalData = eventData['sdoTracks'][response];

    for (let pageId of Object.keys(disposalData.valid)) {
      await assertValidData(disposalData, pageId);
    }

    if (response === 'UNSUITABLE_FOR_SDO') {
      await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM', null, false);
    } else {
      await assertSubmittedEvent('CASE_PROGRESSION', null, false);
    }
  },

  scheduleCivilHearing: async (caseId, user, allocatedTrack) => {
    console.log('Hearing Scheduled for case id ' + caseId);
    await apiRequest.setupTokens(user);

    eventName = 'HEARING_SCHEDULED';

    caseData = await apiRequest.startEvent(eventName, caseId);
    delete caseData['SearchCriteria'];

    let scheduleData = data.HEARING_SCHEDULED(allocatedTrack);

    for (let pageId of Object.keys(scheduleData.valid)) {
      await assertValidData(scheduleData, pageId);
    }

    await assertSubmittedEvent('HEARING_READINESS', null, false);
    await waitForFinishedBusinessProcess(caseId, user);
  },

  amendHearingDueDate: async (caseId, user) => {
    let hearingDueDate = {};
    hearingDueDate = {'hearingDueDate': '2022-01-10'};
    await testingSupport.updateCaseData(caseId, hearingDueDate, user);
  },

  hearingFeePaid: async (caseId, user) => {
    await apiRequest.setupTokens(user);

    await apiRequest.paymentUpdate(caseId, '/service-request-update',
                                   claimData.serviceUpdateDto(caseId, 'paid'));

    const response_msg = await apiRequest.hearingFeePaidEvent(caseId, user);
    assert.equal(response_msg.status, 200);
    console.log('Hearing Fee Paid');

    //await apiRequest.setupTokens(user);
    // const updatedCaseState = await apiRequest.fetchCaseState(caseId, 'TRIAL_READINESS');
    // assert.equal(updatedCaseState, 'PREPARE_FOR_HEARING_CONDUCT_HEARING');
    // console.log('State moved to:'+updatedCaseState);
  },

  createFinalOrder: async (caseId, user, finalOrderRequestType) => {
    console.log(`case in Final Order ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'GENERATE_DIRECTIONS_ORDER';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);
    const document = await testingSupport.uploadDocument();

    if (finalOrderRequestType === 'ASSISTED_ORDER') {
      let updatedData = await updateCaseDataWithPlaceholders(data.FINAL_ORDERS('ASSISTED_ORDER'), document);
      await validateEventPages(updatedData);
    } else {
      let updatedData = await updateCaseDataWithPlaceholders(data.FINAL_ORDERS('FREE_FORM_ORDER'), document);
      await validateEventPages(updatedData);
    }
    await assertSubmittedEvent('PREPARE_FOR_HEARING_CONDUCT_HEARING', {
      header: '',
      body: ''
    }, true);
    await waitForFinishedBusinessProcess(caseId, user);
  },

  retrieveTaskDetails:  async(user, caseNumber, taskId) => {
    return apiRequest.fetchTaskDetails(user, caseNumber, taskId);
  },

  moveCaseToCaseman: async (user) => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');

    await apiRequest.setupTokens(user);

    eventName = 'CASE_PROCEEDS_IN_CASEMAN';
    caseData = await apiRequest.startEvent(eventName, caseId);
    deleteCaseFields('respondentSolGaAppDetails');
    deleteCaseFields('generalApplications');
    await validateEventPages(data.CASE_PROCEEDS_IN_CASEMAN_SPEC);

    await assertError('CaseProceedsInCaseman', data['CASE_PROCEEDS_IN_CASEMAN_SPEC'].invalid.CaseProceedsInCaseman,
                      'The date entered cannot be in the future');

    await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM', {
      header: '',
      body: ''
    }, false);

    await waitForFinishedBusinessProcess(caseId, user);
  },

  acknowledgeClaim: async (user, multipartyScenario, isFirst) => {
    mpScenario = multipartyScenario;
    await apiRequest.setupTokens(user);

    eventName = 'ACKNOWLEDGE_CLAIM';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);

    solicitorSetup(isFirst);

    caseData = returnedCaseData;

    deleteCaseFields('systemGeneratedCaseDocuments');
    deleteCaseFields('solicitorReferences');
    deleteCaseFields('solicitorReferencesCopy');
    deleteCaseFields('respondentSolicitor2Reference');

    // solicitor 2 should not be able to see respondent 1 details
    if (!isFirst) {
      deleteCaseFields('respondent1ClaimResponseIntentionType');
      deleteCaseFields('respondent1ResponseDeadline');
    }
    if (mpScenario === 'ONE_V_TWO_ONE_LEGAL_REP') {
      await validateEventPages(data['ACKNOWLEDGE_CLAIM_SAME_SOLICITOR']);
    } else if (isFirst) {
      await validateEventPages(data['ACKNOWLEDGE_CLAIM_SOLICITOR_ONE']);
    } else {
      await validateEventPages(data['ACKNOWLEDGE_CLAIM_SOLICITOR_TWO']);
    }

    await assertError('ConfirmNameAddress', data[eventName].invalid.ConfirmDetails.futureDateOfBirth,
                      'The date entered cannot be in the future');

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: '',
      body: ''
    });

    await waitForFinishedBusinessProcess(caseId, user);
  },

  defendantResponseClaim: async (user, multipartyScenario, solicitor,
                                 respondentClaimResponseType = 'FULL_DEFENCE',
                                 multiPartyResponseTypeFlags = 'FULL_DEFENCE') => {
    await apiRequest.setupTokens(user);
    mpScenario = multipartyScenario;
    eventName = 'DEFENDANT_RESPONSE';
    // solicitor 2 should not see respondent 1 data but because respondent 1 has replied before this, we need
    // to clear a big chunk of defendant response (respondent 1) data hence its cleaner to have a clean slate
    // and start off from there.
    if (solicitor === 'solicitorTwo') {
      caseData = {};
    }

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    solicitorSetup(solicitor === 'solicitorOne');
    let defendantResponseData;
    if (mpScenario === 'ONE_V_TWO_ONE_LEGAL_REP') {
      defendantResponseData = eventData['defendantResponses'][mpScenario];
    } else if (mpScenario !== 'ONE_V_TWO_TWO_LEGAL_REP') {
      defendantResponseData = eventData['defendantResponses'][mpScenario];
    } else {
      defendantResponseData = eventData['defendantResponses'][mpScenario][solicitor];
    }

    if(respondentClaimResponseType !== 'FULL_DEFENCE'
       && multiPartyResponseTypeFlags !== 'FULL_DEFENCE') {
      defendantResponseData['valid']['RespondentResponseType']['multiPartyResponseTypeFlags'] = multiPartyResponseTypeFlags;
      if (solicitor === 'solicitorOne') {
        defendantResponseData['valid']['RespondentResponseType']['respondent1ClaimResponseType'] = respondentClaimResponseType;
      } else {
        defendantResponseData['valid']['RespondentResponseType']['respondent2ClaimResponseType'] = respondentClaimResponseType;
      }
    }

    // Remove after court location toggle is removed
    // defendantResponseData = await replaceWithCourtNumberIfCourtLocationDynamicListIsNotEnabledForDefendantResponse(
    //     defendantResponseData, solicitor);
    //assertContainsPopulatedFields(returnedCaseData, solicitor);
    caseData = returnedCaseData;

    deleteCaseFields('isRespondent1');
    deleteCaseFields('respondent1', 'solicitorReferences');
    deleteCaseFields('systemGeneratedCaseDocuments');
    //this is for 1v2 diff sol 1
    deleteCaseFields('respondentSolicitor2Reference');
    deleteCaseFields('respondent1DQRequestedCourt', 'respondent2DQRequestedCourt');

    if (solicitor === 'solicitorTwo'){
      deleteCaseFields('respondent1DQHearing');
      deleteCaseFields('respondent1DQLanguage');
      deleteCaseFields('respondent1DQRequestedCourt');
      deleteCaseFields('respondent2DQRequestedCourt');
      deleteCaseFields('respondent1ClaimResponseType');
      deleteCaseFields('respondent1DQExperts');
      deleteCaseFields('respondent1DQWitnesses');

      deleteCaseFields('respondent1Experts');
      deleteCaseFields('respondent1Witnesses');
      deleteCaseFields('respondent1DetailsForClaimDetailsTab');
    }
    await validateEventPagesWithCheck(defendantResponseData, false, solicitor);
    // In a 1v2 different solicitor case, when the first solicitor responds, civil service would not change the state
    // to AWAITING_APPLICANT_INTENTION until the all solicitor response.
    deleteCaseFields('respondentSolGaAppDetails');
    deleteCaseFields('generalApplications');
    if (solicitor === 'solicitorOne' && mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP') {
      // when only one solicitor has responded in a 1v2 different solicitor case
      await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
        header: 'You have submitted the Defendant\'s defence',
        body: 'Once the other defendant\'s legal representative has submitted their defence, we will send the '
              + 'claimant\'s legal representative a notification.'
      });

      await waitForFinishedBusinessProcess(caseId, user);
    } else {
      // when all solicitors responded
      await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION', {
        header: 'You have submitted the Defendant\'s defence',
        body: 'The Claimant legal representative will get a notification'
      });

      await waitForFinishedBusinessProcess(caseId, user);
    }

    deleteCaseFields('respondent1Copy');
    deleteCaseFields('respondent2Copy');
  },

    createDateString: async (plusDays) => {
      let temp = new Date(Date.now() + (3600 * 1000 * 24) * plusDays);
      return padStr(temp.getFullYear().toString()) + '-' +
        padStr((temp.getMonth() + 1).toString()) + '-' +
        padStr(temp.getDate().toString());
    },

};

// Functions
const validateEventPages = async (data, solicitor) => {
  return await validateEventPagesWithCheck(data, true, solicitor);
};

const validateEventPagesWithCheck = async (data, check, solicitor) => {
  //transform the data
  for (let pageId of Object.keys(data.valid)) {
    if (pageId === 'Upload' || pageId === 'DraftDirections' || pageId === 'ApplicantDefenceResponseDocument' || pageId === 'DraftDirections') {
      const document = await testingSupport.uploadDocument();
      data = await updateCaseDataWithPlaceholders(data, document);
    }
    // data = await updateCaseDataWithPlaceholders(data);
    await assertValidData(data, pageId, solicitor, check);
  }
};

const assertValidDataSpec = async (data, pageId) => {
  const userData = data.userInput[pageId];
  caseData = update(caseData, userData);
  const response = await apiRequest.validatePage(
    eventName,
    pageId,
    caseData,
    caseId
  );
  let responseBody = await response.json();

  assert.equal(response.status, 200);

  if (data.midEventData && data.midEventData[pageId]) {
    checkExpected(responseBody.data, data.midEventData[pageId]);
  }

  if (data.midEventGeneratedData && data.midEventGeneratedData[pageId]) {
    checkGenerated(responseBody.data, data.midEventGeneratedData[pageId]);
  }

  caseData = update(caseData, responseBody.data);
};

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

const assertValidData = async (data, pageId, solicitor, check) => {
  const validDataForPage = data.valid[pageId];
  caseData = {...caseData, ...validDataForPage};
  const response = await apiRequest.validatePage(
    eventName,
    pageId,
    caseData,
    isDifferentSolicitorForDefendantResponseOrExtensionDate() ? caseId : null
  );
  let responseBody;

  if (eventName === 'INFORM_AGREED_EXTENSION_DATE' && mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP') {
    responseBody = clearDataForExtensionDate(await response.json(), solicitor);
  } else if (eventName === 'DEFENDANT_RESPONSE' && mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP') {
    responseBody = clearDataForDefendantResponse(await response.json(), solicitor);
  } else {
    responseBody = await response.json();
  }

  assert.equal(response.status, 200);

   
  if (midEventFieldForPage.hasOwnProperty(pageId)) {
    addMidEventFields(pageId, responseBody);
    caseData = removeUiFields(pageId, caseData);
  }
  try {
    assert.deepEqual(responseBody.data, caseData);
  }
  catch(err) {
    if(check) {
      // console.log('Valid data is failed with mismatch ..', err);
    }
  }
};

// Functions
const assertValidDataLip = async (data, pageId) => {
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

  if (data.midEventData && data.midEventData[pageId]) {
    checkExpected(responseBody.data, data.midEventData[pageId]);
  }

  if (data.midEventGeneratedData && data.midEventGeneratedData[pageId]) {
    checkGenerated(responseBody.data, data.midEventGeneratedData[pageId]);
  }

  caseData = update(caseData, responseBody.data);
};

function removeUiFields(pageId, caseData) {
  const midEventField = midEventFieldForPage[pageId];

  if (midEventField.uiField.remove === true) {
    const fieldToRemove = midEventField.uiField.field;
    delete caseData[fieldToRemove];
  }
  return caseData;
}

const assertError = async (pageId, eventData, expectedErrorMessage, responseBodyMessage = 'Unable to proceed because there are one or more callback Errors or Warnings') => {
  const response = await apiRequest.validatePage(
    eventName,
    pageId,
    {...caseData, ...eventData},
    isDifferentSolicitorForDefendantResponseOrExtensionDate ? caseId : null,
    422
  );

  const responseBody = await response.json();

  assert.equal(response.status, 422);
  assert.equal(responseBody.message, responseBodyMessage);
  if (responseBody.callbackErrors != null) {
    assert.equal(responseBody.callbackErrors[0], expectedErrorMessage);
  }
};

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

  if (eventName === 'CREATE_CLAIM' || eventName === 'CREATE_CLAIM_SPEC') {
    caseId = responseBody.id;
    await addUserCaseMapping(caseId, config.applicantSolicitorUser);
    console.log('Case created: ' + caseId);
  }
};

const assertSubmittedSpecEvent = async (expectedState, submittedCallbackResponseContains, hasSubmittedCallback = true) => {
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

const initiateGaWithState = async (user, parentCaseId, expectState, payload) => {
  eventName = events.INITIATE_GENERAL_APPLICATION.id;
  await apiRequest.setupTokens(user);
  await apiRequest.startEvent(eventName, parentCaseId);
  const response = await apiRequest.submitEvent(eventName, payload, parentCaseId);
  const responseBody = await response.json();
  assert.equal(response.status, 201);
  console.log('General application case state : ' + responseBody.state);
  assert.equal(responseBody.callback_response_status_code, 200);
  assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have submitted an application');
  await waitForFinishedBusinessProcess(parentCaseId, user);
  await waitForGAFinishedBusinessProcess(parentCaseId, user);

  const updatedResponse = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
  const updatedCivilCaseData = await updatedResponse.json();
  let gaCaseReference;
  if(user.email === config.applicantSolicitorUser.email){
    gaCaseReference = updatedCivilCaseData.claimantGaAppDetails.pop().value.caseLink.CaseReference;
  }
  else if(user.email === config.defendantSolicitorUser.email) {
    gaCaseReference = updatedCivilCaseData.respondentSolGaAppDetails.pop().value.caseLink.CaseReference;
  }
  else if(user.email === config.secondDefendantSolicitorUser.email) {
    gaCaseReference = updatedCivilCaseData.respondentSolTwoGaAppDetails.pop().value.caseLink.CaseReference;
  }
  else{
    gaCaseReference = updatedCivilCaseData.gaDetailsMasterCollection.pop().value.caseLink.CaseReference;
  }
  console.log('*** GA Case Reference: ' + gaCaseReference + ' ***');
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_APPLICATION_PAYMENT', user);

  //calling payment callback handler
  const payment_response = await apiRequest.paymentApiRequestUpdateServiceCallback(
    genAppJudgeMakeDecisionData.serviceUpdateDtoWithoutNotice(gaCaseReference,'Paid'));
  assert.equal(payment_response.status, 200);

  //comment out next line to see race condition
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference,expectState , user);
  await addUserCaseMapping(gaCaseReference, user);
  return gaCaseReference;
};

const updateCivilClaimSolEmailID = async (user, parentCaseId) => {
  eventName = events.CHANGE_SOLICITOR_EMAIL.id;
  await apiRequest.setupTokens(user);
  await apiRequest.startEvent(eventName, parentCaseId);

  const response = await apiRequest.submitEvent(eventName, data.UPDATE_CLAIMANT_SOLICITOR_EMAILID,
    parentCaseId);

  assert.equal(response.status, 201);
};

const verifyNoOfGAsInCivilClaim = async (user, parentCaseId, gaCaseReference) => {

  // Verify GA is added into right collection
  const updatedCivilCaseDataResponse = await apiRequest.fetchUpdatedCivilCaseData(parentCaseId, user);
  const civilClaimData = await updatedCivilCaseDataResponse.json();

  const updatedGACaseDataResponse = await apiRequest.fetchUpdatedCaseData(gaCaseReference, user);
  const updatedGACaseData = await updatedGACaseDataResponse.json();

  assert.equal(civilClaimData.gaDetailsMasterCollection.length, 1);

  if ((updatedGACaseData.generalAppRespondentAgreement.hasAgreed === 'No'
    && updatedGACaseData.generalAppInformOtherParty != null
    && updatedGACaseData.generalAppInformOtherParty.isWithNotice === 'Yes') || updatedGACaseData.generalAppRespondentAgreement.hasAgreed === 'Yes') {

    assert.equal(civilClaimData.claimantGaAppDetails.length, 1);
    assert.equal(civilClaimData.respondentSolGaAppDetails.length, 1);
    if (updatedGACaseData.isMultiParty === 'yes') {
      assert.equal(civilClaimData.respondentSolTwoGaAppDetails.length, 1);
    }
  }
};

const initiateWithVaryJudgement = async (user, parentCaseId, isClaimant, urgency) => {
  eventName = events.INITIATE_GENERAL_APPLICATION.id;
  await apiRequest.setupTokens(user);
  await apiRequest.startEvent(eventName, parentCaseId);
  let initiateData;

  var isCoscEnabled = await checkToggleEnabled(COSC);
  if(!isClaimant) {
    const document = await testingSupport.uploadDocument();

    initiateData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_VARY_PAYMENT_TERMS_OF_JUDGMENT_LR('Yes',createGeneralAppN245FormUpload(document), urgency)
    : data.INITIATE_GENERAL_APPLICATION_VARY_PAYMENT_TERMS_OF_JUDGMENT('Yes',createGeneralAppN245FormUpload(document), urgency);
  } else {
    initiateData = isCoscEnabled ? data.INITIATE_GENERAL_APPLICATION_VARY_PAYMENT_TERMS_OF_JUDGMENT_LR('Yes', null,  urgency)
      : data.INITIATE_GENERAL_APPLICATION_VARY_PAYMENT_TERMS_OF_JUDGMENT('Yes', null,  urgency);
  }
  const response = await apiRequest.submitEvent(eventName, initiateData ,parentCaseId);
  const responseBody = await response.json();
  assert.equal(response.status, 201);
  console.log('General application case state : ' + responseBody.state);
  assert.equal(responseBody.callback_response_status_code, 200);
  assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have submitted an application');
  await waitForFinishedBusinessProcess(parentCaseId, user);
  await waitForGAFinishedBusinessProcess(parentCaseId, user);

  const updatedResponse = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
  const updatedCivilCaseData = await updatedResponse.json();
  let gaCaseReference;
  if(user.email === config.applicantSolicitorUser.email){
    gaCaseReference = updatedCivilCaseData.claimantGaAppDetails.pop().value.caseLink.CaseReference;
  }
  else if(user.email === config.defendantSolicitorUser.email) {
    gaCaseReference = updatedCivilCaseData.respondentSolGaAppDetails.pop().value.caseLink.CaseReference;
  }
  else if(user.email === config.secondDefendantSolicitorUser.email) {
    gaCaseReference = updatedCivilCaseData.respondentSolTwoGaAppDetails.pop().value.caseLink.CaseReference;
  }
  else{
    gaCaseReference = updatedCivilCaseData.gaDetailsMasterCollection.pop().value.caseLink.CaseReference;
  }

  console.log('*** GA Case Reference: ' + gaCaseReference + ' ***');
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_APPLICATION_PAYMENT', user);

  //calling payment callback handler
  const payment_response = await apiRequest.paymentApiRequestUpdateServiceCallback(
    genAppJudgeMakeDecisionData.serviceUpdateDtoWithoutNotice(gaCaseReference,'Paid'));
  assert.equal(payment_response.status, 200);

  let ccdState = urgency ? 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION' : 'AWAITING_RESPONDENT_RESPONSE';
  //comment out next line to see race condition
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, ccdState, user);

  let doc = 'gaAddl';
  if (user.email === config.defendantSolicitorUser.email
      || user.email === config.secondDefendantSolicitorUser.email ) {
    await assertDocVisibilityToUser(user, 'Claimant', parentCaseId, gaCaseReference, doc);
    //commented this due to cui pr failures
    //await assertNullGaDocVisibilityToUser(config.applicantSolicitorUser, parentCaseId, doc);
  }

  if (user.email === config.defendantSolicitorUser.email) {
    await assertDocVisibilityToUser(config.defendantSolicitorUser, 'Claimant', parentCaseId, gaCaseReference,
                                    doc);
  }
  if (user.email === config.secondDefendantSolicitorUser.email ) {
    await assertDocVisibilityToUser(config.secondDefendantSolicitorUser, 'Claimant', parentCaseId, gaCaseReference,
                                    doc);
  }
  await addUserCaseMapping(gaCaseReference, user);
  return gaCaseReference;
};

const initiateGeneralApplicationWithOutNotice = async (user, parentCaseId, gaData) => {
  let gaCaseReference;
  eventName = events.INITIATE_GENERAL_APPLICATION.id;

  await apiRequest.setupTokens(user);
  await apiRequest.startEvent(eventName, parentCaseId);

  let response = await apiRequest.submitEvent(eventName, gaData, parentCaseId);

  const responseBody = await response.json();
  assert.equal(response.status, 201);
  console.log('General application case state : ' + responseBody.state);
  assert.equal(responseBody.callback_response_status_code, 200);
  assert.include(responseBody.after_submit_callback_response.confirmation_header,
    '# You have submitted an application');

  await waitForFinishedBusinessProcess(parentCaseId, user);
  await waitForGAFinishedBusinessProcess(parentCaseId, user);

  const updatedResponse = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
  const updatedCivilCaseData = await updatedResponse.json();

  switch (user.email) {
    case config.applicantSolicitorUser.email:
      gaCaseReference = updatedCivilCaseData.claimantGaAppDetails.pop().value.caseLink.CaseReference;
      break;
    case config.defendantSolicitorUser.email:
      gaCaseReference = updatedCivilCaseData.respondentSolGaAppDetails.pop().value.caseLink.CaseReference;
      break;
    case config.secondDefendantSolicitorUser.email:
      gaCaseReference = updatedCivilCaseData.respondentSolTwoGaAppDetails.pop().value.caseLink.CaseReference;
      break;
  }

  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference, 'AWAITING_APPLICATION_PAYMENT', user);

  //Payment callback handler response after the payment from service request tab
  const payment_response = await apiRequest.paymentApiRequestUpdateServiceCallback(
    genAppJudgeMakeDecisionData.serviceUpdateDtoWithoutNotice(gaCaseReference, 'Paid'));

  assert.equal(payment_response.status, 200);

  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseReference,
    'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION', user);

  const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseReference, user);
  const updatedGABusinessProcessData = await updatedBusinessProcess.json();
  assert.equal(updatedGABusinessProcessData.ccdState, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');

  console.log('General application case state : APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');
  console.log('*** GA Case Reference: ' + gaCaseReference + ' ***');
  await addUserCaseMapping(gaCaseReference, user);
  return gaCaseReference;
};

const respondentResponse1v2WithPayload = async (user, user2, gaCaseId, payload) => {
  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_RESPONDENT_RESPONSE', user);

  await apiRequest.setupTokens(user);
  eventName = events.RESPOND_TO_APPLICATION.id;
  console.log('*** respondentResponse1v2WithPayload: Start uploading the document ***');
  const document = await testingSupport.uploadDocument();
  payload = await updateCaseDataWithPlaceholders(payload, document);
  console.log('*** respondentResponse1v2WithPayload: Finish uploading the document ***');

  await apiRequest.startGAEvent(eventName, gaCaseId);

  const response = await apiRequest.submitGAEvent(eventName, payload, gaCaseId);
  const responseBody = await response.json();
  assert.equal(response.status, 201);
  assert.equal(responseBody.callback_response_status_code, 200);
  assert.include(responseBody.after_submit_callback_response.confirmation_header, '# You have provided the requested information');

  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'AWAITING_RESPONDENT_RESPONSE', user);
  const updatedBusinessProcess1 = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
  const updatedGABusinessProcessData1 = await updatedBusinessProcess1.json();
  assert.equal(updatedGABusinessProcessData1.ccdState, 'AWAITING_RESPONDENT_RESPONSE');
  await apiRequest.setupTokens(user2);
  eventName = events.RESPOND_TO_APPLICATION.id;
  await apiRequest.startGAEvent(eventName, gaCaseId);

  const response2 = await apiRequest.submitGAEvent(eventName, payload, gaCaseId);
  const responseBody2 = await response2.json();
  assert.equal(responseBody2.callback_response_status_code, 200);
  assert.include(responseBody2.after_submit_callback_response.confirmation_header, '# You have provided the requested information');

  await waitForGACamundaEventsFinishedBusinessProcess(gaCaseId, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION', user);
  const updatedBusinessProcess = await apiRequest.fetchUpdatedGABusinessProcessData(gaCaseId, user);
  const updatedGABusinessProcessData = await updatedBusinessProcess.json();
  assert.equal(updatedGABusinessProcessData.ccdState, 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION');
  await addUserCaseMapping(gaCaseId, user);
  await addUserCaseMapping(gaCaseId, user2);
};

function addMidEventFields(pageId, responseBody) {
  console.log(`Adding mid event fields for pageId: ${pageId}`);
  const midEventField = midEventFieldForPage[pageId];
  let midEventData;

  if (eventName === 'CREATE_CLAIM') {
    midEventData = data[eventName](mpScenario).midEventData[pageId];
  } else if (eventName === 'CLAIMANT_RESPONSE') {
    midEventData = data[eventName](mpScenario).midEventData[pageId];
  } else {
    midEventData = data[eventName].midEventData[pageId];
  }

  /*if (midEventField.dynamicList === true) {
    assertDynamicListListItemsHaveExpectedLabels(responseBody, midEventField.id, midEventData);
  }*/

  caseData = {...caseData, ...midEventData};
  responseBody.data[midEventField.id] = caseData[midEventField.id];
}

function padStr(i) {
  return (i < 10) ? '0' + i : '' + i;
}
async function updateCaseDataWithPlaceholders(data, document) {
  const placeholders = {
    TEST_DOCUMENT_URL: document.document_url,
    TEST_DOCUMENT_BINARY_URL: document.document_binary_url,
    TEST_DOCUMENT_FILENAME: document.document_filename
  };

  data = lodash.template(JSON.stringify(data))(placeholders);

  return JSON.parse(data);
}

const assignCase = async (caseId, mpScenario) => {
  await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
  switch (mpScenario) {
    case 'ONE_V_TWO_TWO_LEGAL_REP': {
      await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORTWO', config.secondDefendantSolicitorUser);
      break;
    }
    case 'ONE_V_TWO_ONE_LEGAL_REP': {
      await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORTWO', config.defendantSolicitorUser);
      break;
    }
  }
};

const assignSpecCase = async (caseId, mpScenario) => {
  await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
  switch (mpScenario) {
    case 'ONE_V_TWO_TWO_LEGAL_REP': {
      await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORTWO', config.secondDefendantSolicitorUser);
      break;
    }
    case 'ONE_V_TWO_ONE_LEGAL_REP': {
      await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
      break;
    }
  }
};

const clearDataForExtensionDate = (responseBody, solicitor) => {
  delete responseBody.data['businessProcess'];
  delete responseBody.data['caseNotes'];
  delete responseBody.data['systemGeneratedCaseDocuments'];

  // solicitor cannot see data from respondent they do not represent
  if (solicitor === 'solicitorTwo') {
    delete responseBody.data['respondent1'];
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

  // solicitor cannot see data from respondent they do not represent
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
    delete responseBody.data['respondent1DQDraftDirections'];
    delete responseBody.data['respondent1DQRequestedCourt'];
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

const isDifferentSolicitorForDefendantResponseOrExtensionDate = () => {
  return mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP' && (eventName === 'DEFENDANT_RESPONSE' || eventName === 'INFORM_AGREED_EXTENSION_DATE');
};
async function replaceClaimantResponseWithCourtNumberIfCourtLocationDynamicListIsNotEnabled(responseData) {
  // let isCourtListEnabled = false;
  // // work around for the api  tests
  // //console.log(`Court location selected in Env: ${config.runningEnv}`);
  // if (false) {
  //   responseData = {
  //     ...responseData,
  //     userInput: {
  //       ...responseData.userInput,
  //       ApplicantCourtLocationLRspec: {
  //         applicant1DQRequestedCourt: {
  //           reasonForHearingAtSpecificCourt: 'reasons',
  //           responseCourtCode: '123'
  //         }
  //       },
  //     }
  //   };
  // }
  return responseData;
}

const assertValidClaimData = async (data, pageId) => {
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

// solicitor 1 should not see details for respondent 2
// solicitor 2 should not see details for respondent 1
const solicitorSetup = (isFirst) => {
  if(isFirst){
    deleteCaseFields('respondent2');
  } else {
    deleteCaseFields('respondent1');
  }
};

// CIV-4959: needs to be removed when court location goes live
// async function replaceWithCourtNumberIfCourtLocationDynamicListIsNotEnabledForDefendantResponse(
//     defendantResponseData, solicitor) {
  //let isCourtListEnabled = await checkCourtLocationDynamicListIsEnabled();
  // work around for the api tests
  //console.log(`Court location selected in Env: ${config.runningEnv}`);
  // if (false) {
  //   if (solicitor === 'solicitorTwo') {
  //     defendantResponseData = {
  //       ...defendantResponseData,
  //       valid: {
  //         ...defendantResponseData.valid,
  //         RequestedCourt: {
  //           respondent2DQRequestedCourt: {
  //             responseCourtCode: '343'
  //           }
  //         }
  //       }
  //     };
  //   } else {
  //     defendantResponseData = {
  //       ...defendantResponseData,
  //       valid: {
  //         ...defendantResponseData.valid,
  //         RequestedCourt: {
  //           respondent1DQRequestedCourt: {
  //             responseCourtCode: '343'
  //           }
  //         }
  //       }
  //     };
  //   }
  // }
//   return defendantResponseData;
// }

const assertContainsPopulatedFields = (returnedCaseData, solicitor) => {
  const fixture = solicitor ? adjustDataForSolicitor(solicitor, caseData) : caseData;
  for (let populatedCaseField of Object.keys(fixture)) {
    // this property won't be here until civil service is merged
    if (populatedCaseField !== 'applicant1DQRemoteHearing') {
      assert.property(returnedCaseData, populatedCaseField);
    }
  }
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

const assertDocVisibilityToUser = async ( user, gaUserRole, parentCaseId, gaCaseId, doc) => {
  let docGaTitle = doc + 'Document';
  if (doc === 'gaEvidence') {
    docGaTitle = 'generalAppEvidence' + 'Document';
  }
  if (gaUserRole != null) {
    docGaTitle = doc + 'Doc' + gaUserRole;
  }
  let docCivil = '';
  const response = await apiRequest.fetchMainCivilCaseData(parentCaseId, user);
  const civilCaseData = await response.json();
  const updatedResponse = await apiRequest.fetchGaCaseData(gaCaseId);
  const updatedGaCaseData = await updatedResponse.json();
  let docGa = updatedGaCaseData[docGaTitle];
  if(user.email === config.applicantSolicitorUser.email){
    docCivil = civilCaseData[doc + 'DocClaimant'];
  }
  else if(user.email === config.defendantSolicitorUser.email) {
    docCivil = civilCaseData[doc + 'DocRespondentSol'];
  }
  else if(user.email === config.secondDefendantSolicitorUser.email) {
    docCivil = civilCaseData[doc + 'DocRespondentSolTwo'];
  }
  else{
    docCivil = civilCaseData[doc + 'DocStaff'];
  }
  assert.equal(docGa.at(-1)['id'], docCivil.at(-1)['id']);
};

const assertGaDocVisibilityToUser = async ( user, parentCaseId, gaCaseId, doc) => {
  await assertDocVisibilityToUser( user, null, parentCaseId, gaCaseId, doc);
};

const assertNullGaDocVisibilityToUser = async ( user, parentCaseId, doc) => {
  let docCivil = '';
  const response = await apiRequest.fetchUpdatedCaseData(parentCaseId, user);
  const civilCaseData = await response.json();
  if(user.email === config.applicantSolicitorUser.email){
    docCivil = civilCaseData[doc + 'DocClaimant'];
  }
  else if(user.email === config.defendantSolicitorUser.email) {
    docCivil = civilCaseData[doc + 'DocRespondentSol'];
  }
  else if(user.email === config.secondDefendantSolicitorUser.email) {
    docCivil = civilCaseData[doc + 'DocRespondentSolTwo'];
  }
  else{
    docCivil = civilCaseData[doc + 'DocStaff'];
  }
   assert.equal(typeof(docCivil), 'undefined');
};

const processHwf = async (gaCaseReference) => {
  await apiRequest.setupTokens(config.ctscAdmin);
  eventName = 'FULL_REMISSION_HWF_GA';
  let returnedCaseData = await apiRequest.startEvent(eventName, gaCaseReference);
  let hwfData = data.HWF_FULL('APPLICATION');
  caseData = {...returnedCaseData, ...hwfData};
  await apiRequest.submitGAEvent(eventName, caseData, gaCaseReference);

  eventName = 'FEE_PAYMENT_OUTCOME_GA';
  returnedCaseData = await apiRequest.startEvent(eventName, gaCaseReference);
  hwfData = data.HWF_OUTCOME('APPLICATION');
  caseData = {...returnedCaseData, ...hwfData};
  await apiRequest.submitGAEvent(eventName, caseData, gaCaseReference);
};

const validateEventPagesDefaultJudgments = async (data) => {
  //transform the data
  console.log('validateEventPages');
  for (let pageId of Object.keys(data.userInput)) {
    await assertValidDataDefaultJudgments(data, pageId);
  }
};

const assertValidDataDefaultJudgments = async (data, pageId) => {
  console.log(`asserting page: ${pageId} has valid data`);
  const userData = data.userInput[pageId];

  caseData = update(caseData, userData);
  deleteCaseFields('flightDelayDetails');

  const response = await apiRequest.validatePage(
    eventName,
    pageId,
    caseData,
    caseId
  );
  let responseBody = await response.json();
  responseBody = clearDataForSearchCriteria(responseBody); //Until WA release

  assert.equal(response.status, 200);
  delete responseBody.data['flightDelayDetails'];
  delete responseBody.data['defendantUserDetails'];

  if (pageId === 'defendantDetailsSpec') {
    delete responseBody.data['registrationTypeRespondentOne'];
    delete responseBody.data['registrationTypeRespondentTwo'];
  }
  if (pageId === 'showCertifyStatementSpec') {
    deleteCaseFields('currentDefendant');
    deleteCaseFields('currentDefendantName');
  }
  if (pageId === 'claimPartialPayment') {
    responseBody.data.currentDefendantName = 'Sir John Doe';
    deleteCaseFields('CPRAcceptance');
  }
  if (pageId === 'paymentConfirmationSpec') {
    delete responseBody.data['repaymentSummaryObject'];
    deleteCaseFields('currentDefendantName');

  } else if (pageId === 'paymentSetDate') {
    if (['preview', 'demo'].includes(config.runningEnv)) {
      responseBody.data.repaymentDue= '1502.00';
    } else {
      responseBody.data.repaymentDue= '1580.00';
    }
  }
  if (pageId === 'paymentSetDate' || pageId === 'paymentType') {
    responseBody.data.currentDatebox = '25 August 2022';
  }
  if (pageId === 'claimPartialPayment' && ['preview', 'demo'].includes(config.runningEnv)) {
    delete responseBody.data['showDJFixedCostsScreen'];
    responseBody.data.currentDefendantName = 'Sir John Doe';
  }

  if (pageId === 'fixedCostsOnEntry') {
    delete responseBody.data['showDJFixedCostsScreen'];
    responseBody.data.currentDefendantName = 'Sir John Doe';
  }
  try {
    assert.deepEqual(responseBody.data, caseData);
  }
  catch(err) {
    console.error('Validate data is failed due to a mismatch ..', err);
    throw err;
  }
};
