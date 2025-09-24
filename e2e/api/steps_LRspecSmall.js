const config = require('../config.js');
const deepEqualInAnyOrder = require('deep-equal-in-any-order');
const chai = require('chai');

chai.use(deepEqualInAnyOrder);
chai.config.truncateThreshold = 0;
const {expect, assert} = chai;

const {waitForFinishedBusinessProcess} = require('../api/testingSupport');
const {assignCaseRoleToUser, addUserCaseMapping, unAssignAllUsers} = require('./caseRoleAssignmentHelper');
const apiRequest = require('./apiRequest.js');
const claimData = require('../fixtures/events/createClaimSpecSmall.js');
const claimDataHearings = require('../fixtures/events/createClaimSpecSmallForHearings.js');
const expectedEvents = require('../fixtures/ccd/expectedEventsLRSpec.js');
const nonProdExpectedEvents = require('../fixtures/ccd/nonProdExpectedEventsLRSpec.js');
const {assertCaseFlags, assertFlagsInitialisedAfterCreateClaim} = require('../helpers/assertions/caseFlagsAssertions');
const {PBAv3} = require('../fixtures/featureKeys');
const {checkToggleEnabled, checkCaseFlagsEnabled, checkManageContactInformationEnabled} = require('./testingSupport');
const {addAndAssertCaseFlag, getPartyFlags, getDefinedCaseFlagLocations, updateAndAssertCaseFlag} = require('./caseFlagsHelper');
const {CASE_FLAGS} = require('../fixtures/caseFlags');
const {dateNoWeekends} = require('./dataHelper');
const sdoTracks = require('../fixtures/events/createSDO');
const {addFlagsToFixture} = require('../helpers/caseFlagsFeatureHelper');
const mediationDocuments = require('../fixtures/events/mediation/uploadMediationDocuments');
const testingSupport = require('./testingSupport');
const lodash = require('lodash');
const requestForReconsideration = require('../fixtures/events/requestForReconsideration');
const judgeDecisionToReconsiderationRequest = require('../fixtures/events/judgeDecisionOnReconsiderationRequest');
const {updateExpert} = require('./manageContactInformationHelper');
const manageContactInformation = require('../fixtures/events/manageContactInformation.js');
const {adjustCaseSubmittedDateForCarm} = require('../helpers/carmHelper');
const mediationUnsuccessful = require('../fixtures/events/cui/unsuccessfulMediationCui.js');
const transferOnlineCase = require('../fixtures/events/transferOnlineCase');
const {fetchCaseDetails} = require('./apiRequest');
const hearingScheduled = require('../fixtures/events/scheduleHearing');
const settleClaim1v1Spec = require('../fixtures/events/settleClaim1v1Spec');

let caseId, eventName;
let caseData = {};

const data = {
  CREATE_CLAIM: (scenario, pbaV3) => claimData.createClaim(scenario, pbaV3),
  CREATE_CLAIM_HEARINGS: (scenario, pbaV3) => claimDataHearings.createClaim(scenario, pbaV3),
  DEFENDANT_RESPONSE: (response, camundaEvent) => require('../fixtures/events/defendantResponseSpecSmall.js').respondToClaim(response, camundaEvent),
  DEFENDANT_RESPONSE_JUDICIAL_REFERRAL: () => require('../fixtures/events/defendantResponseSpecSmall.js').respondToClaimForJudicialReferral(),
  DEFENDANT_RESPONSE_FULL_DEFENCE_CARM: () => require('../fixtures/events/defendantResponseSpecSmall.js').respondToClaimForCarm(),
  DEFENDANT_RESPONSE_PART_ADMISSION_NOT_PAID_CARM: () => require('../fixtures/events/defendantResponseSpecSmall.js').respondToClaimForCarmPartAdmitNotPaid(),
  DEFENDANT_RESPONSE_PART_ADMISSION_STATES_PAID_CARM: () => require('../fixtures/events/defendantResponseSpecSmall.js').respondToClaimForCarmPartAdmitStatesPaid(),
  DEFENDANT_RESPONSE_1v2: (response, camundaEvent) => require('../fixtures/events/defendantResponseSpec1v2.js').respondToClaim(response, camundaEvent),
  DEFENDANT_RESPONSE2_1V2_2ND_DEF: (response) => require('../fixtures/events/defendantResponseSpecSmall.js').respondToClaim2(response),
  CLAIMANT_RESPONSE: (hasAgreedFreeMediation, carmEnabled) => require('../fixtures/events/claimantResponseSpecSmall.js').claimantResponse(hasAgreedFreeMediation, carmEnabled),
  CLAIMANT_RESPONSE_PART_ADMIT_REJECT: () => require('../fixtures/events/claimantResponseSpecSmall.js').claimantResponseRejectPartAdmit(),
  CLAIMANT_RESPONSE_PART_ADMIT_STATES_PAID: (claimantPaymentReceived) => require('../fixtures/events/claimantResponseSpecSmall.js').claimantResponsePAStatesPaid(claimantPaymentReceived),
  INFORM_AGREED_EXTENSION_DATE: async (camundaEvent) => require('../fixtures/events/informAgreeExtensionDateSpec.js').informExtension(camundaEvent),
  LA_CREATE_SDO: (userInput) => sdoTracks.createLASDO(userInput),
  CREATE_SDO: (userInput) => sdoTracks.createSDOSmallWODamageSumInPerson(userInput),
  CREATE_SDO_CARM: (userInput) => sdoTracks.createSDOSmallCarm(userInput),
  CREATE_SMALL_DRH_CARM: () => sdoTracks.createSDOSmallDRHCarm(),
  REQUEST_FOR_RECONSIDERATION: (userType) => requestForReconsideration.createRequestForReconsiderationSpec(userType),
  DECISION_ON_RECONSIDERATION_REQUEST: (decisionSelection)=> judgeDecisionToReconsiderationRequest.judgeDecisionOnReconsiderationRequestSpec(decisionSelection),
  MANAGE_DEFENDANT1_EXPERT_INFORMATION: (caseData) => manageContactInformation.manageDefendant1ExpertsInformation(caseData),
  NOT_SUITABLE_SDO: (option) => transferOnlineCase.notSuitableSDO(option),
  HEARING_SCHEDULED: (allocatedTrack) => hearingScheduled.scheduleHearing(allocatedTrack),
  SETTLE_CLAIM_MARK_PAID_FULL: () => settleClaim1v1Spec.settleClaim(),
};

const eventData = {
  defendantResponses: {
    ONE_V_ONE: {
      FULL_DEFENCE: data.DEFENDANT_RESPONSE('FULL_DEFENCE'),
      FULL_DEFENCE_PBAv3: data.DEFENDANT_RESPONSE('FULL_DEFENCE', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_ADMISSION: data.DEFENDANT_RESPONSE('FULL_ADMISSION'),
      FULL_ADMISSION_PBAv3: data.DEFENDANT_RESPONSE('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION: data.DEFENDANT_RESPONSE('PART_ADMISSION'),
      PART_ADMISSION_PBAv3: data.DEFENDANT_RESPONSE('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM: data.DEFENDANT_RESPONSE('COUNTER_CLAIM'),
      COUNTER_CLAIM_PBAv3: data.DEFENDANT_RESPONSE('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      FULL_DEFENCE_JUDICIAL_REFERRAL: data.DEFENDANT_RESPONSE_JUDICIAL_REFERRAL(),
      FULL_DEFENCE_CARM: data.DEFENDANT_RESPONSE_FULL_DEFENCE_CARM(),
      PART_ADMISSION_NOT_PAID_CARM: data.DEFENDANT_RESPONSE_PART_ADMISSION_NOT_PAID_CARM(),
      PART_ADMISSION_STATES_PAID_CARM: data.DEFENDANT_RESPONSE_PART_ADMISSION_STATES_PAID_CARM()
    },
    ONE_V_TWO: {
      FULL_ADMISSION: data.DEFENDANT_RESPONSE_1v2('FULL_ADMISSION'),
      FULL_ADMISSION_PBAv3: data.DEFENDANT_RESPONSE_1v2('FULL_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      PART_ADMISSION: data.DEFENDANT_RESPONSE_1v2('PART_ADMISSION'),
      PART_ADMISSION_PBAv3: data.DEFENDANT_RESPONSE_1v2('PART_ADMISSION', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT'),
      COUNTER_CLAIM: data.DEFENDANT_RESPONSE_1v2('COUNTER_CLAIM'),
      COUNTER_CLAIM_PBAv3: data.DEFENDANT_RESPONSE_1v2('COUNTER_CLAIM', 'CREATE_CLAIM_SPEC_AFTER_PAYMENT')
    },
    ONE_V_TWO_DIF_SOL: {
      FULL_DEFENCE1: data.DEFENDANT_RESPONSE_JUDICIAL_REFERRAL(),
      FULL_DEFENCE1_PBAv3:  data.DEFENDANT_RESPONSE_JUDICIAL_REFERRAL(),
      FULL_DEFENCE2: data.DEFENDANT_RESPONSE2_1V2_2ND_DEF('FULL_DEFENCE'),
      FULL_DEFENCE2_PBAv3:  data.DEFENDANT_RESPONSE2_1V2_2ND_DEF('FULL_DEFENCE')
    }
  }
};

module.exports = function (){
  return actor({

  /**
   * Creates a claim
   *
   * @param user user to create the claim
   * @param scenario
   * @param hearings
   * @return {Promise<void>}
   */
  createClaimWithRepresentedRespondent: async (user,scenario = 'ONE_V_ONE', hearings = false, carmEnabled = false) => {

    eventName = 'CREATE_CLAIM_SPEC';
    caseId = null;
    caseData = {};

    const pbaV3 = await checkToggleEnabled(PBAv3);
    let createClaimData  = {};

    if (!hearings) {
      createClaimData = data.CREATE_CLAIM(scenario, pbaV3);
    } else {
      createClaimData = data.CREATE_CLAIM_HEARINGS(scenario, pbaV3);
    }
    //==============================================================

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    for (let pageId of Object.keys(createClaimData.userInput)) {
      await assertValidData(createClaimData, pageId);
    }

    await assertSubmittedEvent('PENDING_CASE_ISSUED');

    await waitForFinishedBusinessProcess(caseId);

    console.log('Is PBAv3 toggle on?: ' + pbaV3);

    if (pbaV3) {
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
        claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    }

    await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
    if (scenario === 'ONE_V_TWO') {
      await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORTWO', config.secondDefendantSolicitorUser);
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

    let informAgreedExtensionData = await data.INFORM_AGREED_EXTENSION_DATE(pbaV3 ? 'CREATE_CLAIM_SPEC_AFTER_PAYMENT':'CREATE_CLAIM_SPEC');
    informAgreedExtensionData.userInput.ExtensionDate.respondentSolicitor1AgreedDeadlineExtension = await dateNoWeekends(40);

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

  retrieveTaskDetails: async (user, caseNumber, taskId) => {
    return apiRequest.fetchTaskDetails(user, caseNumber, taskId);
  },

  assignTaskToUser: async (user, taskId) => {
    return apiRequest.taskActionByUser(user, taskId, 'claim');
  },

  completeTaskByUser: async (user, taskId) => {
    return apiRequest.taskActionByUser(user, taskId, 'complete');
  },

  defendantResponse: async (user, response = 'FULL_DEFENCE', scenario = 'ONE_V_ONE', judicialReferral = false, carmEnabled = false) => {
    await apiRequest.setupTokens(user);

    const pbaV3 = await checkToggleEnabled(PBAv3);
    if(pbaV3){
      response = response+'_PBAv3';
    }

    eventName = 'DEFENDANT_RESPONSE_SPEC';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);

    let defendantResponseData;

    if (!judicialReferral) {
      defendantResponseData = eventData['defendantResponses'][scenario][response];
    } else {
      if (scenario === 'ONE_V_TWO_DIF_SOL') {
        defendantResponseData = eventData['defendantResponses'][scenario][response];
      } else {
        defendantResponseData = eventData['defendantResponses'][scenario]['FULL_DEFENCE_JUDICIAL_REFERRAL'];
      }
    }

    if (carmEnabled) {
      if (response === 'FULL_DEFENCE') {
        defendantResponseData = eventData['defendantResponses'][scenario]['FULL_DEFENCE_CARM'];
      } else if (response === 'PART_ADMISSION_NOT_PAID_PBAv3') {
        defendantResponseData = eventData['defendantResponses'][scenario]['PART_ADMISSION_NOT_PAID_CARM'];
      } else if (response === 'PART_ADMISSION_STATES_PAID_PBAv3') {
        defendantResponseData = eventData['defendantResponses'][scenario]['PART_ADMISSION_STATES_PAID_CARM'];
      }
    }

    caseData = returnedCaseData;

    caseData = await addFlagsToFixture(caseData);

    for (let pageId of Object.keys(defendantResponseData.userInput)) {
      await assertValidData(defendantResponseData, pageId);
    }

    if(scenario === 'ONE_V_ONE')
      await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION');
    else if(response === 'FULL_ADMISSION' && scenario === 'ONE_V_TWO')
      await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    else if(scenario === 'ONE_V_TWO_DIF_SOL') {
      if(response === 'FULL_DEFENCE1' || response === 'FULL_DEFENCE1_PBAv3')
        await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
      else if(response === 'FULL_DEFENCE2' || response === 'FULL_DEFENCE2_PBAv3')
        await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION');
    }

    await waitForFinishedBusinessProcess(caseId);

    const caseFlagsEnabled = await checkCaseFlagsEnabled();
    if (caseFlagsEnabled) {
      await assertCaseFlags(caseId, user, response);
    }

    deleteCaseFields('respondent1Copy');
  },

  claimantResponse: async (user, judicialReferral = false, hasAgreedFreeMediation = 'Yes', carmEnabled = false, partAdmitScenario = null) => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');
    deleteCaseFields('respondentResponseIsSame');

    await adjustCaseSubmittedDateForCarm(caseId, carmEnabled);

    await apiRequest.setupTokens(user);

    eventName = 'CLAIMANT_RESPONSE_SPEC';
    caseData = await apiRequest.startEvent(eventName, caseId);

    caseData = await addFlagsToFixture(caseData);

    let claimantResponseData = data.CLAIMANT_RESPONSE(hasAgreedFreeMediation, carmEnabled);
    if (carmEnabled) {
      if (partAdmitScenario === 'PART_ADMIT_REJECT') {
        claimantResponseData = data.CLAIMANT_RESPONSE_PART_ADMIT_REJECT();
      } else if (partAdmitScenario === 'PART_ADMIT_STATES_PAID_NOT_RECEIVED') {
        claimantResponseData = data.CLAIMANT_RESPONSE_PART_ADMIT_STATES_PAID(false);
      } else if (partAdmitScenario === 'PART_ADMIT_STATES_PAID_CLAIMANT_RECEIVED_REJECTSPA') {
        claimantResponseData = data.CLAIMANT_RESPONSE_PART_ADMIT_STATES_PAID(true);
      }
    }

    for (let pageId of Object.keys(claimantResponseData.userInput)) {
      await assertValidData(claimantResponseData, pageId);
    }

    let expectedEndState;

    carmEnabled ? expectedEndState = 'IN_MEDIATION' : judicialReferral ? expectedEndState = 'JUDICIAL_REFERRAL' : null;

    if (expectedEndState) {
      await assertSubmittedEvent(expectedEndState);
    }

    await waitForFinishedBusinessProcess(caseId);

    const caseFlagsEnabled = await checkCaseFlagsEnabled();
    if (caseFlagsEnabled) {
      await assertCaseFlags(caseId, user, 'FULL_DEFENCE');
    }
  },

  mediationUnsuccessful: async (user, carmEnabled = false) => {
    eventName = 'MEDIATION_UNSUCCESSFUL';

    caseData = await apiRequest.startEvent(eventName, caseId);
    caseData = {...caseData, ...mediationUnsuccessful.unsuccessfulMediation(carmEnabled)};
    await apiRequest.setupTokens(user);
    deleteCaseFields('showConditionFlags');
    await assertSubmittedEvent('JUDICIAL_REFERRAL');
    await waitForFinishedBusinessProcess(caseId);
    console.log('End of unsuccessful mediation');
  },

  uploadMediationDocuments: async (user) => {
    await apiRequest.setupTokens(user);

    let eventData;
    if (user === config.applicantSolicitorUser) {
      eventData = mediationDocuments.uploadMediationDocuments('claimant');
    } else {
      eventData = mediationDocuments.uploadMediationDocuments('defendant');
    }

    eventName = 'UPLOAD_MEDIATION_DOCUMENTS';
    caseData = await apiRequest.startEvent(eventName, caseId);

    await validateEventPages(eventData);

    await assertSubmittedEvent('JUDICIAL_REFERRAL');
  },

  createSDO: async (user, response = 'CREATE_DISPOSAL', carmEnabled = false) => {
    console.log('SDO for case id ' + caseId);
    await apiRequest.setupTokens(user);

    if (response === 'UNSUITABLE_FOR_SDO') {
      eventName = 'NotSuitable_SDO';
    } else {
      eventName = 'CREATE_SDO';
    }

    caseData = await apiRequest.startEvent(eventName, caseId);
    let disposalData = data.CREATE_SDO();

    if (carmEnabled) {
      disposalData = data.CREATE_SDO_CARM();
    } else {
      disposalData = data.CREATE_SDO();
    }

    for (let pageId of Object.keys(disposalData.valid)) {
      await assertValidData(disposalData, pageId);
    }
    
    delete caseData['showConditionFlags'];

    if (response === 'UNSUITABLE_FOR_SDO') {
      await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM', null, false);
    } else {
      await assertSubmittedEvent('CASE_PROGRESSION', null, false);
    }

    delete caseData['smallClaimsFlightDelay'];
    delete caseData['smallClaimsFlightDelayToggle'];
    //required to fix existing prod api tests for sdo
    clearWelshParaFromCaseData();

    await waitForFinishedBusinessProcess(caseId);
  },

  notSuitableSDO: async (user, option) => {
    console.log(`case in CASE PROGRESSION  ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'NotSuitable_SDO';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    let disposalData = data.NOT_SUITABLE_SDO(option);

    for (let pageId of Object.keys(disposalData.valid)) {
      await assertValidData(disposalData, pageId);
    }

    if (option === 'CHANGE_LOCATION') {
      await assertSubmittedEvent('CASE_PROGRESSION', {
        header: '',
        body: ''
      }, true);
      await waitForFinishedBusinessProcess(caseId);
    } else {
      await assertSubmittedEvent('CASE_PROGRESSION', {
        header: '',
        body: ''
      }, true);
      await waitForFinishedBusinessProcess(caseId);
      const caseData = await fetchCaseDetails(config.adminUser, caseId, 200);
      assert(caseData.state === 'PROCEEDS_IN_HERITAGE_SYSTEM');
    }
  },

    notSuitableSdoChangeLocation: async (user, option) => {
      console.log(`case in CASE PROGRESSION  ${caseId}`);
      await apiRequest.setupTokens(user);

      eventName = 'NotSuitable_SDO';
      let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
      delete returnedCaseData['SearchCriteria'];
      caseData = returnedCaseData;
      let disposalData = data.NOT_SUITABLE_SDO(option);

      for (let pageId of Object.keys(disposalData.valid)) {
        await assertNotValidData(disposalData, pageId);
      }

    },

    createLASDO: async (user, response = 'CREATE_DISPOSAL') => {
      console.log('SDO for case id ' + caseId);
      await apiRequest.setupTokens(user);

    if (response === 'UNSUITABLE_FOR_SDO') {
      eventName = 'NotSuitable_SDO';
    } else {
      eventName = 'CREATE_SDO';
    }

    caseData = await apiRequest.startEvent(eventName, caseId);
    let disposalData = data.LA_CREATE_SDO();

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

  scheduleHearing: async (user, allocatedTrack) => {
    console.log('Hearing Scheduled for case id ' + caseId);
    await apiRequest.setupTokens(user);

    eventName = 'HEARING_SCHEDULED';

    caseData = await apiRequest.startEvent(eventName, caseId);
    delete caseData['SearchCriteria'];

    let scheduleData = data.HEARING_SCHEDULED(allocatedTrack);

    for (let pageId of Object.keys(scheduleData.userInput)) {
      await assertValidData(scheduleData, pageId);
    }

    await assertSubmittedEvent('HEARING_READINESS', null, false);
    await waitForFinishedBusinessProcess(caseId);
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

  manageContactInformation : async (user) => {
    if(!(await checkManageContactInformationEnabled())) {
      return;
    }
    eventName = 'MANAGE_CONTACT_INFORMATION';
    await apiRequest.setupTokens(user);
    caseData = await apiRequest.startEvent(eventName, caseId);
    let manageContactInformationData = data.MANAGE_DEFENDANT1_EXPERT_INFORMATION(caseData);
    await updateExpert(caseId, manageContactInformationData);
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

  requestForReconsideration: async (user, userType) => {
    console.log('RequestForReconsideration for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'REQUEST_FOR_RECONSIDERATION';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    let disposalData = data.REQUEST_FOR_RECONSIDERATION(userType);
    for (let pageId of Object.keys(disposalData.userInput)) {
      await assertValidData(disposalData, pageId);
    }
    await assertSubmittedEvent('CASE_PROGRESSION', {
      header: '# Your request has been submitted',
      body: ''
    }, true);

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
    delete caseData['showConditionFlags'];
    await assertSubmittedEvent('CASE_PROGRESSION', {
      header: '# Response has been submitted',
      body: ''
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

    settleClaim: async (user) => {
      console.log('settleClaim for case id ' + caseId);
      await apiRequest.setupTokens(user);
      eventName = 'SETTLE_CLAIM_MARK_PAID_FULL';

      let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
      delete returnedCaseData['SearchCriteria'];
      caseData = returnedCaseData;
      let disposalData = data.SETTLE_CLAIM_MARK_PAID_FULL();
      for (let pageId of Object.keys(disposalData.userInput)) {
        await assertValidData(disposalData, pageId);
      }
      await assertSubmittedEvent('CASE_STAYED', {
        header: '# Response has been submitted',
        body: ''
      }, true);

      await waitForFinishedBusinessProcess(caseId);
    },

  getCaseId: async () => {
    console.log(`case created: ${caseId}`);
    return caseId;
  },
  });
};

// Functions
const assertValidData = async (data, pageId) => {
  console.log(`asserting page: ${pageId} has valid data`);
  let userData;

  if (eventName === 'CREATE_SDO' || eventName === 'NotSuitable_SDO' ) {
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

  if (eventName === 'CLAIMANT_RESPONSE_SPEC') {
    if (responseBody.data['applicant1PartAdmitConfirmAmountPaidSpec']
      || responseBody.data['applicant1PartAdmitIntentionToSettleClaimSpec']
      || responseBody.data['applicant1AcceptAdmitAmountPaidSpec']) {
      delete responseBody.data['applicant1ProceedWithClaim'];
    }
  }

  delete responseBody.data['smallClaimsFlightDelayToggle'];
  delete responseBody.data['smallClaimsFlightDelay'];
  //required to fix existing prod api tests for sdo
  delete responseBody.data['sdoR2SmallClaimsUseOfWelshLanguage'];
  delete responseBody.data['sdoR2NihlUseOfWelshLanguage'];
  delete responseBody.data['sdoR2FastTrackUseOfWelshLanguage'];
  delete responseBody.data['sdoR2DrhUseOfWelshLanguage'];
  delete responseBody.data['sdoR2DisposalHearingUseOfWelshLanguage'];

  if (pageId === 'SdoR2FastTrack') {
    clearWelshParaFromCaseData();
  }

  caseData = update(caseData, responseBody.data);
};

const assertNotValidData = async (data, pageId) => {
  console.log(`asserting page: ${pageId} has valid data`);

  let userData;

  if (eventName === 'CREATE_SDO' || eventName === 'NotSuitable_SDO') {
    userData = data.valid[pageId];
  } else {
    userData = data.userInput[pageId];
  }
  caseData = update(caseData, userData);
  const response = await apiRequest.validatePage(
    eventName,
    pageId,
    caseData,
    caseId,
    422
  );
  let responseBody = await response.json();
  if (responseBody.callbackErrors != null) {
    assert.equal(responseBody.callbackErrors[0], 'Unable to process this request. To transfer the case to another court you need to issue a General Order.');
  }

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

const validateEventPages = async (data, solicitor) => {
  //transform the data
  console.log('validateEventPages....');
  for (let pageId of Object.keys(data.userInput)) {
    if (pageId === 'DocumentUpload' || pageId === 'Upload' || pageId === 'DraftDirections'|| pageId === 'ApplicantDefenceResponseDocument' || pageId === 'DraftDirections' || pageId === 'FinalOrderPreview') {
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

const clearWelshParaFromCaseData= () => {
  delete caseData['sdoR2SmallClaimsUseOfWelshLanguage'];
  delete caseData['sdoR2NihlUseOfWelshLanguage'];
  delete caseData['sdoR2FastTrackUseOfWelshLanguage'];
  delete caseData['sdoR2DrhUseOfWelshLanguage'];
  delete caseData['sdoR2DisposalHearingUseOfWelshLanguage'];
};
