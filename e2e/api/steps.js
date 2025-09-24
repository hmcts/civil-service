const config = require('../config.js');
const lodash = require('lodash');
const deepEqualInAnyOrder = require('deep-equal-in-any-order');
const chai = require('chai');
const {listElement, dateNoWeekendsBankHolidayNextDay} = require('./dataHelper');

chai.use(deepEqualInAnyOrder);
chai.config.truncateThreshold = 0;
const {expect, assert} = chai;
const {waitForFinishedBusinessProcess} = require('../api/testingSupport');
const {assignCaseRoleToUser, addUserCaseMapping, unAssignAllUsers} = require('./caseRoleAssignmentHelper');
const apiRequest = require('./apiRequest.js');
const claimData = require('../fixtures/events/createClaim.js');
const createDJ = require('../fixtures/events/createDJ.js');
const createDJDirectionOrder = require('../fixtures/events/createDJDirectionOrder.js');
const expectedEvents = require('../fixtures/ccd/expectedEvents.js');
const nonProdExpectedEvents = require('../fixtures/ccd/nonProdExpectedEvents.js');
const testingSupport = require('./testingSupport');
const {PBAv3, COSC} = require('../fixtures/featureKeys');
const sdoTracks = require('../fixtures/events/createSDO.js');
const evidenceUploadApplicant = require('../fixtures/events/evidenceUploadApplicant.js');
const evidenceUploadRespondent = require('../fixtures/events/evidenceUploadRespondent.js');
const hearingScheduled = require('../fixtures/events/scheduleHearing.js');
const evidenceUploadJudge = require('../fixtures/events/evidenceUploadJudge.js');
const trialReadiness = require('../fixtures/events/trialReadiness.js');
const createFinalOrder = require('../fixtures/events/finalOrder.js');
const transferOnlineCase = require('../fixtures/events/transferOnlineCase.js');
const manageContactInformation = require('../fixtures/events/manageContactInformation.js');
const {checkToggleEnabled, checkCaseFlagsEnabled, checkFastTrackUpliftsEnabled, checkManageContactInformationEnabled,
  checkMintiToggleEnabled} = require('./testingSupport');
const {cloneDeep} = require('lodash');
const {assertCaseFlags, assertFlagsInitialisedAfterCreateClaim, assertFlagsInitialisedAfterAddLitigationFriend} = require('../helpers/assertions/caseFlagsAssertions');
const {CASE_FLAGS} = require('../fixtures/caseFlags');
const {addAndAssertCaseFlag, getDefinedCaseFlagLocations, getPartyFlags, updateAndAssertCaseFlag} = require('./caseFlagsHelper');
const {updateApplicant, updateLROrganisation} = require('./manageContactInformationHelper');
const {fetchCaseDetails} = require('./apiRequest');
const {removeFlagsFieldsFromFixture, addFlagsToFixture} = require('../helpers/caseFlagsFeatureHelper');
const {removeFixedRecoveryCostFieldsFromUnspecDefendantResponseData, removeFastTrackAllocationFromSdoData} = require('../helpers/fastTrackUpliftsHelper');
const {adjustCaseSubmittedDateForMinti, assertTrackAfterClaimCreation, addSubmittedDateInCaseData} = require('../helpers/mintiHelper');
const stayCase = require('../fixtures/events/stayCase');
const manageStay = require('../fixtures/events/manageStay');
const dismissCase = require('../fixtures/events/dismissCase');
const sendAndReplyMessage = require('../fixtures/events/sendAndReplyMessages');


const data = {
  CREATE_CLAIM: (mpScenario, claimAmount, pbaV3, hmcTest) => claimData.createClaim(mpScenario, claimAmount, pbaV3, hmcTest),
  CREATE_CLAIM_RESPONDENT_LIP: claimData.createClaimLitigantInPerson,
  CREATE_CLAIM_RESPONDENT_LR_LIP: claimData.createClaimLRLIP,
  CREATE_CLAIM_RESPONDENT_LIP_LIP: claimData.createClaimLIPLIP,
  COS_NOTIFY_CLAIM: (lip1, lip2) => claimData.cosNotifyClaim(lip1, lip2),
  COS_NOTIFY_CLAIM_DETAILS: (lip1, lip2) => claimData.cosNotifyClaimDetails(lip1, lip2),
  CREATE_CLAIM_TERMINATED_PBA: claimData.createClaimWithTerminatedPBAAccount,
  CREATE_CLAIM_RESPONDENT_SOLICITOR_FIRM_NOT_IN_MY_HMCTS: claimData.createClaimRespondentSolFirmNotInMyHmcts,
  RESUBMIT_CLAIM: require('../fixtures/events/resubmitClaim.js'),
  NOTIFY_DEFENDANT_OF_CLAIM: require('../fixtures/events/1v2DifferentSolicitorEvents/notifyClaim_1v2DiffSol.js'),
  NOTIFY_DEFENDANT_OF_CLAIM_DETAILS: require('../fixtures/events/1v2DifferentSolicitorEvents/notifyClaim_1v2DiffSol.js'),
  ADD_OR_AMEND_CLAIM_DOCUMENTS: require('../fixtures/events/addOrAmendClaimDocuments.js'),
  ACKNOWLEDGE_CLAIM: require('../fixtures/events/acknowledgeClaim.js'),
  ACKNOWLEDGE_CLAIM_SAME_SOLICITOR: require('../fixtures/events/1v2SameSolicitorEvents/acknowledgeClaim_sameSolicitor.js'),
  ACKNOWLEDGE_CLAIM_SOLICITOR_ONE: require('../fixtures/events/1v2DifferentSolicitorEvents/acknowledgeClaim_Solicitor1.js'),
  ACKNOWLEDGE_CLAIM_SOLICITOR_TWO: require('../fixtures/events/1v2DifferentSolicitorEvents/acknowledgeClaim_Solicitor2.js'),
  ACKNOWLEDGE_CLAIM_TWO_V_ONE: require('../fixtures/events/2v1Events/acknowledgeClaim_2v1.js'),
  INFORM_AGREED_EXTENSION_DATE: require('../fixtures/events/informAgreeExtensionDate.js'),
  INFORM_AGREED_EXTENSION_DATE_SOLICITOR_TWO: require('../fixtures/events/1v2DifferentSolicitorEvents/informAgreeExtensionDate_Solicitor2.js'),
  DEFENDANT_RESPONSE: (allocatedTrack) => require('../fixtures/events/defendantResponse.js').defendantResponse(allocatedTrack),
  DEFENDANT_RESPONSE_SAME_SOLICITOR: (allocatedTrack) => require('../fixtures/events/1v2SameSolicitorEvents/defendantResponse_sameSolicitor.js').defendantResponse(allocatedTrack),
  DEFENDANT_RESPONSE_SOLICITOR_ONE: (allocatedTrack) => require('../fixtures/events/1v2DifferentSolicitorEvents/defendantResponse_Solicitor1').defendantResponse(allocatedTrack),
  DEFENDANT_RESPONSE_SOLICITOR_TWO: (allocatedTrack) => require('../fixtures/events/1v2DifferentSolicitorEvents/defendantResponse_Solicitor2').defendantResponse(allocatedTrack),
  DEFENDANT_RESPONSE_TWO_APPLICANTS: (allocatedTrack) => require('../fixtures/events/2v1Events/defendantResponse_2v1').defendantResponse(allocatedTrack),
  CLAIMANT_RESPONSE: (mpScenario, allocatedTrack) => require('../fixtures/events/claimantResponse.js').claimantResponse(mpScenario, allocatedTrack),
  ADD_DEFENDANT_LITIGATION_FRIEND: require('../fixtures/events/addDefendantLitigationFriend.js'),
  CASE_PROCEEDS_IN_CASEMAN: require('../fixtures/events/caseProceedsInCaseman.js'),
  AMEND_PARTY_DETAILS: require('../fixtures/events/amendPartyDetails.js'),
  ADD_CASE_NOTE: require('../fixtures/events/addCaseNote.js'),
  REQUEST_DJ: (djRequestType, mpScenario) => createDJ.requestDJ(djRequestType, mpScenario),
  REQUEST_DJ_ORDER: (djOrderType, mpScenario) => createDJDirectionOrder.judgeCreateOrder(djOrderType, mpScenario),
  CREATE_DISPOSAL: (userInput) => sdoTracks.createSDODisposal(userInput),
  CREATE_FAST: (userInput) => sdoTracks.createSDOFast(userInput),
  CREATE_FAST_NIHL: (userInput) => sdoTracks.createSDOFastNIHL(userInput),
  CREATE_FAST_IN_PERSON: (userInput) => sdoTracks.createSDOFastInPerson(userInput),
  CREATE_SMALL: (userInput) => sdoTracks.createSDOSmall(userInput),
  CREATE_FAST_NO_SUM: (userInput) => sdoTracks.createSDOFastWODamageSum(userInput),
  CREATE_SMALL_NO_SUM: (userInput) => sdoTracks.createSDOSmallWODamageSum(userInput),
  UNSUITABLE_FOR_SDO: (userInput) => sdoTracks.createNotSuitableSDO(userInput),
  CREATE_SMALL_DRH: () => sdoTracks.createSDOSmallDRH(),
  HEARING_SCHEDULED: (allocatedTrack, isMinti) => hearingScheduled.scheduleHearing(allocatedTrack, isMinti),
  EVIDENCE_UPLOAD_JUDGE: (typeOfNote) => evidenceUploadJudge.upload(typeOfNote),
  TRIAL_READINESS: (user) => trialReadiness.confirmTrialReady(user),
  EVIDENCE_UPLOAD_APPLICANT_SMALL: (mpScenario) => evidenceUploadApplicant.createApplicantSmallClaimsEvidenceUpload(mpScenario),
  EVIDENCE_UPLOAD_APPLICANT_FAST: (mpScenario, claimTrack) => evidenceUploadApplicant.createApplicantFastClaimsEvidenceUpload(mpScenario, claimTrack),
  EVIDENCE_UPLOAD_RESPONDENT_SMALL: (mpScenario) => evidenceUploadRespondent.createRespondentSmallClaimsEvidenceUpload(mpScenario),
  EVIDENCE_UPLOAD_RESPONDENT_FAST: (mpScenario, claimTrack) => evidenceUploadRespondent.createRespondentFastClaimsEvidenceUpload(mpScenario, claimTrack),
  EVIDENCE_UPLOAD_APPLICANT_DRH: () => evidenceUploadApplicant.createApplicantEvidenceUploadDRH(),
  EVIDENCE_UPLOAD_RESPONDENT_DRH: () => evidenceUploadRespondent.createRespondentEvidenceUploadDRH(),
  FINAL_ORDERS: (finalOrdersRequestType, dayPlus0, dayPlus7, dayPlus14, dayPlus21, orderType) => createFinalOrder.requestFinalOrder(finalOrdersRequestType, dayPlus0, dayPlus7, dayPlus14, dayPlus21, orderType),
  NOT_SUITABLE_SDO: (option) => transferOnlineCase.notSuitableSDO(option),
  TRANSFER_CASE: () => transferOnlineCase.transferCase(),
  MANAGE_DEFENDANT1_INFORMATION: (caseData) => manageContactInformation.manageDefendant1Information(caseData),
  MANAGE_DEFENDANT1_LR_INDIVIDUALS_INFORMATION: (caseData) => manageContactInformation.manageDefendant1LROrganisationInformation(caseData),
  STAY_CASE: () => stayCase.stayCase(),
  MANAGE_STAY_UPDATE: () => manageStay.manageStayRequestUpdateDamages(),
  MANAGE_STAY_LIFT: () => manageStay.manageStayLiftStayDamages(),
  DISMISS_CASE: () => dismissCase.dismissCaseDamages(),
  SEND_MESSAGE: () => sendAndReplyMessage.sendMessage(),
  REPLY_MESSAGE: (messageCode, messageLabel) => sendAndReplyMessage.replyMessage(messageCode, messageLabel)
};
const calculatedClaimsTrackDRH = {
    disposalOrderWithoutHearing: (d) => typeof d.input === 'string',
    fastTrackOrderWithoutJudgement: (d) => typeof d.input === 'string',
    fastTrackHearingTime: (d) =>
      d.helpText1 === 'If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date of this order.',
    disposalHearingHearingTime: (d) =>
      d.input === 'This claim will be listed for final disposal before a judge on the first available date after'
      && d.dateTo,
    sdoR2SmallClaimsJudgesRecital: (data) => {
      return typeof data.input === 'string';
    },
    sdoR2SmallClaimsPPIToggle: (data) => Array.isArray(data),
    sdoR2SmallClaimsWitnessStatementsToggle: (data) => Array.isArray(data),
    sdoR2SmallClaimsUploadDocToggle: (data) => Array.isArray(data),
    sdoR2SmallClaimsHearingToggle: (data) => Array.isArray(data),
    sdoR2SmallClaimsWitnessStatements: (data) => {
      return typeof data.sdoStatementOfWitness === 'string'
        && typeof data.isRestrictWitness === 'string'
        && typeof data.isRestrictPages === 'string'
        && typeof data.text === 'string';
    },
    sdoR2SmallClaimsUploadDoc: (data) => {
      return typeof data.sdoUploadOfDocumentsTxt === 'string';
    },
    sdoR2DrhUseOfWelshIncludeInOrderToggle: (data) => Array.isArray(data),
    sdoR2DrhUseOfWelshLanguage: (data) => {
      return typeof data.description === 'string';
    },
    sdoR2SmallClaimsHearing: (data) => {
      return typeof data.trialOnOptions === 'string'
        && typeof data.trialOnOptions === 'string'
        && typeof data.hearingCourtLocationList === 'object'
        && typeof data.methodOfHearing === 'string'
        && typeof data.physicalBundleOptions === 'string'
        && typeof data.sdoR2SmallClaimsHearingFirstOpenDateAfter.listFrom.match(/\d{4}-\d{2}-\d{2}/);
    },
    sdoR2SmallClaimsImpNotes: (data) => {
      return typeof data.text === 'string'
        && typeof data.date.match(/\d{4}-\d{2}-\d{2}/);
    },
    sdoR2SmallClaimsPPI: (data) => {
      return typeof data.ppiDate.match(/\d{4}-\d{2}-\d{2}/)
        && typeof data.text === 'string';
    }
};
const eventData = {
  acknowledgeClaims: {
    ONE_V_ONE: data.ACKNOWLEDGE_CLAIM,
    ONE_V_TWO_ONE_LEGAL_REP: data.ACKNOWLEDGE_CLAIM_SAME_SOLICITOR,
    ONE_V_TWO_TWO_LEGAL_REP: {
      solicitorOne: data.ACKNOWLEDGE_CLAIM_SOLICITOR_ONE,
      solicitorTwo: data.ACKNOWLEDGE_CLAIM_SOLICITOR_TWO
    },
    TWO_V_ONE: data.ACKNOWLEDGE_CLAIM_TWO_V_ONE
  },
  informAgreedExtensionDates: {
    ONE_V_ONE: data.INFORM_AGREED_EXTENSION_DATE,
    ONE_V_TWO_ONE_LEGAL_REP: data.INFORM_AGREED_EXTENSION_DATE,
    ONE_V_TWO_TWO_LEGAL_REP: {
      solicitorOne: data.INFORM_AGREED_EXTENSION_DATE,
      solicitorTwo: data.INFORM_AGREED_EXTENSION_DATE_SOLICITOR_TWO
    },
    TWO_V_ONE: data.INFORM_AGREED_EXTENSION_DATE
  },
  defendantResponses: {
    ONE_V_ONE: (allocatedTrack) => data.DEFENDANT_RESPONSE(allocatedTrack),
    ONE_V_TWO_ONE_LEGAL_REP: (allocatedTrack) => data.DEFENDANT_RESPONSE_SAME_SOLICITOR(allocatedTrack),
    ONE_V_TWO_TWO_LEGAL_REP: {
      solicitorOne: (allocatedTrack) => data.DEFENDANT_RESPONSE_SOLICITOR_ONE(allocatedTrack),
      solicitorTwo: (allocatedTrack) => data.DEFENDANT_RESPONSE_SOLICITOR_TWO(allocatedTrack)
    },
    TWO_V_ONE: (allocatedTrack) => data.DEFENDANT_RESPONSE_TWO_APPLICANTS(allocatedTrack)
  },
  sdoTracks: {
    CREATE_DISPOSAL: data.CREATE_DISPOSAL(),
    CREATE_SMALL: data.CREATE_SMALL(),
    CREATE_FAST: data.CREATE_FAST(),
    CREATE_FAST_IN_PERSON: data.CREATE_FAST_IN_PERSON(),
    CREATE_SMALL_NO_SUM: data.CREATE_SMALL_NO_SUM(),
    CREATE_FAST_NO_SUM: data.CREATE_FAST_NO_SUM(),
    UNSUITABLE_FOR_SDO: data.UNSUITABLE_FOR_SDO(),
    CREATE_FAST_NIHL: data.CREATE_FAST_NIHL(),
    CREATE_SMALL_DRH: data.CREATE_SMALL_DRH(),
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

const newSdoR2FieldsFastTrack = {
  sdoR2FastTrackWitnessOfFact: (data) => {
    return typeof data.sdoStatementOfWitness === 'string'
      && typeof data.sdoWitnessDeadline === 'string'
      && typeof data.sdoWitnessDeadlineText === 'string';
  }
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

let caseId, eventName, legacyCaseReference;
let caseData = {};
let mpScenario = 'ONE_V_ONE';

module.exports = {

  createClaimWithRepresentedRespondent: async (user, multipartyScenario, claimAmount = '11000', isMintiCaseEnabled = false, hmcTest = false) => {
    eventName = 'CREATE_CLAIM';
    caseId = null;
    caseData = {};
    mpScenario = multipartyScenario;
    const pbaV3 = await checkToggleEnabled(PBAv3);

    let createClaimData = data.CREATE_CLAIM(mpScenario, claimAmount, pbaV3, hmcTest);

    // Workaround, toggle is active after 31/01/2025, based on either submittedDate, or current localdatetime
    const isMintiEnabled = await checkMintiToggleEnabled() && isMintiCaseEnabled;
    if (isMintiEnabled) {
      addSubmittedDateInCaseData(createClaimData);
    }

    //==============================================================

    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    await validateEventPages(createClaimData);

    let i;
    if (createClaimData.invalid) {
      for (i = 0; i < createClaimData.invalid.Court.courtLocation.applicantPreferredCourt.length; i++) {
        await assertError('Court', createClaimData.invalid.Court.courtLocation.applicantPreferredCourt[i],
          null, 'Case data validation failed');
      }
      await assertError('Upload', createClaimData.invalid.Upload.servedDocumentFiles.particularsOfClaimDocument,
        null, 'Case data validation failed');
    }

    console.log('Is PBAv3 toggle on?: ' + pbaV3);

    let bodyText = 'Your claim will not be issued until payment is confirmed.';
    let headerText = '# Please now pay your claim fee\n# using the link below';
    await assertSubmittedEvent('PENDING_CASE_ISSUED', {
      header: headerText,
      body: bodyText
    });

    await waitForFinishedBusinessProcess(caseId);

    if (pbaV3) {
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
        claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    }

    await assignCase();
    await waitForFinishedBusinessProcess(caseId);
    if(await checkCaseFlagsEnabled()) {
      await assertFlagsInitialisedAfterCreateClaim(config.adminUser, caseId);
    }
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'CASE_ISSUED');

    // field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');

    await adjustCaseSubmittedDateForMinti(caseId, isMintiEnabled);
    await assertTrackAfterClaimCreation(config.adminUser, caseId, claimAmount, isMintiEnabled);
    return caseId;
  },

  manageDefendant1Details: async (user) => {
    if(!(await checkManageContactInformationEnabled())) {
      return;
    }
    eventName = 'MANAGE_CONTACT_INFORMATION';
    await apiRequest.setupTokens(user);
    caseData = await apiRequest.startEvent(eventName, caseId);
    let manageContactInformationData = data.MANAGE_DEFENDANT1_INFORMATION(caseData);
    await expectedWarnings('Defendant1Party', manageContactInformationData, 'Check the litigation friend\'s details');
    await updateApplicant(caseId, manageContactInformationData);
  },

  manageDefendant1LROrgDetails: async (user) => {
    if(!(await checkManageContactInformationEnabled())) {
      return;
    }
    eventName = 'MANAGE_CONTACT_INFORMATION';
    await apiRequest.setupTokens(user);
    caseData = await apiRequest.startEvent(eventName, caseId);
    let manageContactInformationData = data.MANAGE_DEFENDANT1_LR_INDIVIDUALS_INFORMATION(caseData);
    await updateLROrganisation(caseId, manageContactInformationData);
  },

  createClaimWithRespondentLitigantInPerson: async (user, multipartyScenario) => {
    eventName = 'CREATE_CLAIM';
    caseId = null;
    caseData = {};
    mpScenario = multipartyScenario;
    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    const pbaV3 = await checkToggleEnabled(PBAv3);
    let createClaimData;
    switch (mpScenario){
      case 'ONE_V_ONE':
        createClaimData = data.CREATE_CLAIM_RESPONDENT_LIP;
        break;
      case 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP':
        createClaimData = data.CREATE_CLAIM_RESPONDENT_LR_LIP;
        break;
      case 'ONE_V_TWO_LIPS':
        createClaimData = data.CREATE_CLAIM_RESPONDENT_LIP_LIP;
        break;
    }

    //==============================================================
    if (pbaV3) {
      createClaimData.valid.ClaimValue.paymentTypePBA = 'PBAv3';
    }

    createClaimData.valid.ClaimTypeUnSpec = {
      claimTypeUnSpec: 'CONSUMER_CREDIT'
    };

    await validateEventPages(createClaimData);

    console.log('comparing assertSubmittedEvent');
    await assertSubmittedEvent('PENDING_CASE_ISSUED', {
      header: 'Please now pay your claim',
      body: 'Your claim will not be issued until payment is confirmed'
    });

    await waitForFinishedBusinessProcess(caseId);

    console.log('Is PBAv3 toggle on?: ' + pbaV3);

    if (pbaV3) {
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
        claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    }

    if (mpScenario === 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP') {
      await assignCaseRoleToUser(caseId, 'RESPONDENTSOLICITORONE', config.defendantSolicitorUser);
    }
    console.log('***waitForFinishedBusinessProcess');
    await waitForFinishedBusinessProcess(caseId);
    console.log('***assertCorrectEventsAreAvailableToUser');
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'CASE_ISSUED');
    console.log('***assertCorrectEventsAreAvailableToUser');
    await assertCorrectEventsAreAvailableToUser(config.adminUser,  'CASE_ISSUED');

    deleteCaseFields('applicantSolicitor1CheckEmail');
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');

    return caseId;
  },

  createClaimWithFailingPBAAccount: async (user) => {
    eventName = 'CREATE_CLAIM';
    caseId = null;
    caseData = {};
    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);

    let createClaimData = data.CREATE_CLAIM_TERMINATED_PBA;
    //==============================================================

    await validateEventPages(createClaimData);

    let bodyText = 'Your claim will not be issued until payment is confirmed.';
    let headerText = '# Please now pay your claim fee\n# using the link below';

    await assertSubmittedEvent('PENDING_CASE_ISSUED', {
      header: headerText,
      body: bodyText
    });

    await assignCase();
    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'PENDING_CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'PENDING_CASE_ISSUED');
  },

  resubmitClaim: async (user) => {
    eventName = 'RESUBMIT_CLAIM';
    caseData = {};
    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName, caseId);
    await validateEventPages(data.RESUBMIT_CLAIM);
    await assertSubmittedEvent('PENDING_CASE_ISSUED', {
      header: 'Claim pending',
      body: 'Your claim will be processed. Wait for us to contact you.'
    });
    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'PENDING_CASE_ISSUED');
  },

  amendClaimDocuments: async (user) => {
    // Temporary work around from CMC-1497 - statement of truth field is removed due to callback code in service repo.
    // Currently the mid event sets uiStatementOfTruth to null. When EXUI is involved this has the appearance of
    // resetting the field in the UI, most likely due to some caching mechanism, but the data is still available for the
    // about to submit. As these tests talk directly to the data store API the field is actually removed in the about
    // to submit callback. This gives the situation where uiStatementOfTruth is a defined field but with internal fields
    // set to null. In the about to submit callback this overwrites applicantSolicitor1ClaimStatementOfTruth with null
    // fields. When data is fetched here, the field does not exist.
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');

    await apiRequest.setupTokens(user);

    eventName = 'ADD_OR_AMEND_CLAIM_DOCUMENTS';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    caseData = await addFlagsToFixture(caseData);

    await validateEventPages(data[eventName]);

    const document = await testingSupport.uploadDocument();
    let errorData = await updateCaseDataWithPlaceholders(data[eventName], document);

    await assertError('Upload', errorData.invalid.Upload.duplicateError,
      'You need to either upload 1 Particulars of claim only or enter the Particulars of claim text in the field provided. You cannot do both.');

    await assertSubmittedEvent('CASE_ISSUED', {
      header: 'Documents uploaded successfully',
      body: ''
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'CASE_ISSUED');
  },

  notifyClaim: async (user, multipartyScenario) => {
    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM';
    mpScenario = multipartyScenario;

    await apiRequest.setupTokens(user);
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    legacyCaseReference = returnedCaseData['legacyCaseReference'];
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    caseData = await addFlagsToFixture(caseData);

    await validateEventPages(data[eventName]);

    await assertSubmittedEvent('AWAITING_CASE_DETAILS_NOTIFICATION', {
      header: 'Notification of claim sent',
      body: 'The defendant legal representative\'s organisation has been notified and granted access to this claim.'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_CASE_DETAILS_NOTIFICATION');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_CASE_DETAILS_NOTIFICATION');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_CASE_DETAILS_NOTIFICATION');
  },

  notifyClaimLip: async (user, multipartyScenario) => {

    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM';
    mpScenario = multipartyScenario;

    await apiRequest.setupTokens(user);

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    legacyCaseReference = returnedCaseData['legacyCaseReference'];
    // assertContainsPopulatedFields(returnedCaseData);

    await validateEventPages(data[eventName]);
    returnedCaseData.defendantSolicitorNotifyClaimOptions = null;

    if (mpScenario === 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP') {
      returnedCaseData = {...returnedCaseData, ...data.COS_NOTIFY_CLAIM(false, true)};
    } else if (mpScenario === 'ONE_V_TWO_LIPS') {
      returnedCaseData = {...returnedCaseData, ...data.COS_NOTIFY_CLAIM(false, true), ...data.COS_NOTIFY_CLAIM(true, false)};
    } else {
      returnedCaseData = {...returnedCaseData, ...data.COS_NOTIFY_CLAIM(true, false)};
    }
    await assertSubmittedEventWithCaseData(returnedCaseData, 'AWAITING_CASE_DETAILS_NOTIFICATION', {
      header: 'Certificate of Service',
      body: 'You must serve the claim details and'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_CASE_DETAILS_NOTIFICATION');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_CASE_DETAILS_NOTIFICATION');
  },

  notifyClaimDetails: async (user) => {
    await apiRequest.setupTokens(user);

    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM_DETAILS';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = {...returnedCaseData, defendantSolicitorNotifyClaimDetailsOptions: {
        value: listElement('Both')
      }};

    caseData = await addFlagsToFixture(caseData);

    await validateEventPages(data[eventName]);

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: 'Defendant notified',
      body: 'The defendant legal representative\'s organisation has been notified of the claim details.'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  },

  notifyClaimDetailsLip: async (user, multipartyScenario) => {

    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM_DETAILS';
    mpScenario = multipartyScenario;

    await apiRequest.setupTokens(user);

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    legacyCaseReference = returnedCaseData['legacyCaseReference'];
    // assertContainsPopulatedFields(returnedCaseData);

    await validateEventPages(data[eventName]);
    returnedCaseData.defendantSolicitorNotifyClaimOptions = null;
    if (mpScenario === 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP') {
      returnedCaseData = {};
      returnedCaseData = {...returnedCaseData, ...data.COS_NOTIFY_CLAIM_DETAILS(false, true)};
      const document = await testingSupport.uploadDocument();
      returnedCaseData = await updateCaseDataWithPlaceholders(returnedCaseData, document);
    } else if (mpScenario === 'ONE_V_TWO_LIPS') {
      returnedCaseData = {};
      returnedCaseData = {...returnedCaseData, ...data.COS_NOTIFY_CLAIM_DETAILS(false, true), ...data.COS_NOTIFY_CLAIM_DETAILS(true, false)};
      const document = await testingSupport.uploadDocument();
      returnedCaseData = await updateCaseDataWithPlaceholders(returnedCaseData, document);
    } else {
      returnedCaseData = {};
      returnedCaseData = {...returnedCaseData, ...data.COS_NOTIFY_CLAIM_DETAILS(true, false)};
      const document = await testingSupport.uploadDocument();
      returnedCaseData = await updateCaseDataWithPlaceholders(returnedCaseData, document);
    }
    await assertSubmittedEventWithCaseData(returnedCaseData, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: 'Certificate of Service',
      body: 'The defendant(s) must'
    });

    await waitForFinishedBusinessProcess(caseId);

    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  },

  amendPartyDetails: async (user) => {
    await apiRequest.setupTokens(user);
    caseData = {};

    eventName = 'AMEND_PARTY_DETAILS';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);

    await addFlagsToFixture(returnedCaseData);

    await validateEventPages(data[eventName]);

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: 'You have updated a',
      body: ' '
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  },

  acknowledgeClaim: async (user, multipartyScenario, solicitor) => {
    mpScenario = multipartyScenario;
    await apiRequest.setupTokens(user);

    eventName = 'ACKNOWLEDGE_CLAIM';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);

    solicitorSetup(solicitor);

    assertContainsPopulatedFields(returnedCaseData, solicitor);
    caseData = returnedCaseData;

    caseData = await addFlagsToFixture(caseData);

    deleteCaseFields('systemGeneratedCaseDocuments');
    deleteCaseFields('solicitorReferences');
    deleteCaseFields('solicitorReferencesCopy');
    deleteCaseFields('respondentSolicitor2Reference');

    // solicitor 2 should not be able to see respondent 1 details
    if (solicitor === 'solicitorTwo') {
      deleteCaseFields('respondent1ClaimResponseIntentionType');
      deleteCaseFields('respondent1ResponseDeadline');
    }

    const fixture = mpScenario !== 'ONE_V_TWO_TWO_LEGAL_REP' ?
      eventData['acknowledgeClaims'][mpScenario] : eventData['acknowledgeClaims'][mpScenario][solicitor];

    //Todo: Remove after caseflags release
    await removeFlagsFieldsFromFixture(fixture);

    await validateEventPages(fixture);

    await assertError('ConfirmNameAddress', data[eventName].invalid.ConfirmDetails.futureDateOfBirth,
      'The date entered cannot be in the future');

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: '',
      body: ''
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');

    //removed because it's not needed for the further tests
    deleteCaseFields('respondent1Copy');
    deleteCaseFields('respondent2Copy');
    deleteCaseFields('solicitorReferencesCopy');
  },

  informAgreedExtension: async (user, multipartyScenario, solicitor) => {
    mpScenario = multipartyScenario;
    await apiRequest.setupTokens(user);

    solicitorSetup(solicitor);

    eventName = 'INFORM_AGREED_EXTENSION_DATE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData, solicitor);
    caseData = returnedCaseData;
    deleteCaseFields('systemGeneratedCaseDocuments');
    if (solicitor === 'solicitorTwo') {
      deleteCaseFields('respondent1');
    }

    let informAgreedExtensionData;
    if (mpScenario !== 'ONE_V_TWO_TWO_LEGAL_REP') {
      informAgreedExtensionData = eventData['informAgreedExtensionDates'][mpScenario];
    } else {
      informAgreedExtensionData = eventData['informAgreedExtensionDates'][mpScenario][solicitor];
    }

    caseData = await addFlagsToFixture(caseData);

    await validateEventPages(informAgreedExtensionData, solicitor);

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: 'Extension deadline submitted',
      body: 'You must respond to the claimant by'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    deleteCaseFields('isRespondent1');
  },

  defendantResponse: async (user, multipartyScenario, solicitor, allocatedTrack) => {
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

    solicitorSetup(solicitor);

    let defendantResponseData;

    if (mpScenario !== 'ONE_V_TWO_TWO_LEGAL_REP') {
      defendantResponseData = eventData['defendantResponses'][mpScenario](allocatedTrack);
    } else {
      defendantResponseData = eventData['defendantResponses'][mpScenario][solicitor](allocatedTrack);
    }

    //Todo: Remove after fast track uplifts release
    if(!await checkFastTrackUpliftsEnabled()) {
      removeFixedRecoveryCostFieldsFromUnspecDefendantResponseData(defendantResponseData);
    }

    //Todo: Remove after caseflags release
    await removeFlagsFieldsFromFixture(defendantResponseData);

    assertContainsPopulatedFields(returnedCaseData, solicitor);
    caseData = returnedCaseData;
    caseData = await addFlagsToFixture(caseData);

    deleteCaseFields('isRespondent1');
    deleteCaseFields('respondent1', 'solicitorReferences');
    deleteCaseFields('systemGeneratedCaseDocuments');
    //this is for 1v2 diff sol 1
    deleteCaseFields('respondentSolicitor2Reference');
    deleteCaseFields('respondent1DQRequestedCourt', 'respondent2DQRequestedCourt');

    if (solicitor === 'solicitorTwo') {
      deleteCaseFields('respondent1DQHearing');
      deleteCaseFields('respondent1DQLanguage');
      deleteCaseFields('respondent1DQRequestedCourt');
      deleteCaseFields('respondent2DQRequestedCourt');
      deleteCaseFields('respondent1ClaimResponseType');
      deleteCaseFields('respondent1DQDisclosureReport');
      deleteCaseFields('respondent1DQExperts');
      deleteCaseFields('respondent1DQWitnesses');
      //delete case flags DQ party fields
      deleteCaseFields('respondent1Experts');
      deleteCaseFields('respondent1Witnesses');
      deleteCaseFields('respondent1DetailsForClaimDetailsTab');
    }
    await validateEventPages(defendantResponseData, solicitor);

    // need to add partyID back into respondent object
    if (solicitor === 'solicitorOne'){
      caseData.respondent1.partyID = caseData.respondent1Copy.partyID;
    }
    if (solicitor === 'solicitorTwo'){
      caseData.respondent2.partyID = caseData.respondent2Copy.partyID;
    }

    await assertError('ConfirmDetails', defendantResponseData.invalid.ConfirmDetails.futureDateOfBirth,
      'The date entered cannot be in the future');
    await assertError('Experts', defendantResponseData.invalid.Experts.emptyDetails, 'Expert details required');
    await assertError('Hearing', defendantResponseData.invalid.Hearing.past,
      'Unavailable Date cannot be past date');
    await assertError('Hearing', defendantResponseData.invalid.Hearing.moreThanYear,
      'Dates must be within the next 12 months.');

    await assertError('Hearing', defendantResponseData.invalid.Hearing.wrongDateRange,
      'From Date should be less than To Date');
    // In a 1v2 different solicitor case, when the first solicitor responds, civil service would not change the state
    // to AWAITING_APPLICANT_INTENTION until the all solicitor response.
   // console.log('Hearing>>>', caseData);
    if (solicitor === 'solicitorOne') {
      // when only one solicitor has responded in a 1v2 different solicitor case
      await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
        header: 'You have submitted the Defendant\'s defence',
        body: 'Once the other defendant\'s legal representative has submitted their defence, we will send the '
          + 'claimant\'s legal representative a notification.'
      });
      await waitForFinishedBusinessProcess(caseId);
      await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
      await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
      await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    } else {
      // when all solicitors responded
      await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION', {
        header: 'You have submitted the Defendant\'s defence',
        body: 'The Claimant legal representative will get a notification'
      });

      await waitForFinishedBusinessProcess(caseId);
      await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_APPLICANT_INTENTION');
      await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_APPLICANT_INTENTION');
      await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_APPLICANT_INTENTION');
    }

    deleteCaseFields('respondent1Copy');
    deleteCaseFields('respondent2Copy');

    const caseFlagsEnabled = await checkCaseFlagsEnabled();

    if (caseFlagsEnabled) {
      await assertCaseFlags(caseId, user, 'FULL_DEFENCE');
    }
  },

  claimantResponse: async (user, multipartyScenario, expectedCcdState, targetFlag, allocatedTrack) => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');
    deleteCaseFields('respondentResponseIsSame');

    await apiRequest.setupTokens(user);

    eventName = 'CLAIMANT_RESPONSE';
    mpScenario = multipartyScenario;
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    let claimantResponseData = data.CLAIMANT_RESPONSE(mpScenario, allocatedTrack);

    caseData = await addFlagsToFixture(caseData);

    await validateEventPages(claimantResponseData);

    await assertError('Experts', claimantResponseData.invalid.Experts.emptyDetails, 'Expert details required');
    await assertError('Hearing', claimantResponseData.invalid.Hearing.past,
      'Unavailable Date cannot be past date');
    await assertError('Hearing', claimantResponseData.invalid.Hearing.moreThanYear,
      'Dates must be within the next 12 months.');

    await assertError('Hearing', claimantResponseData.invalid.Hearing.wrongDateRange,
      'From Date should be less than To Date');

    if (targetFlag === 'FOR_SDO') {
      console.log('sdo test');
      await assertSubmittedEvent(
        'JUDICIAL_REFERRAL', {
          header: 'You have chosen to proceed with the claim',
          body: '>We will review the case and contact you to tell you what to do next.'
        });
    } else {
      await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM', {
        header: 'You have chosen to proceed with the claim',
        body: '>We will review the case and contact you to tell you what to do next.'
      });
    }

    await waitForFinishedBusinessProcess(caseId);
    if (!expectedCcdState) {
      await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
      await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
      await assertCorrectEventsAreAvailableToUser(config.adminUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
    }

    const caseFlagsEnabled = await checkCaseFlagsEnabled();

    if (caseFlagsEnabled) {
      await assertCaseFlags(caseId, user, 'FULL_DEFENCE');
    }
  },

  checkUserCaseAccess: async (user, shouldHaveAccess) => {
    console.log(`Checking ${user.email} ${shouldHaveAccess ? 'has' : 'does not have'} access to the case.`);
    const expectedStatus = shouldHaveAccess ? 200 : 404;
    return await fetchCaseDetails(user, caseId, expectedStatus);
  },

  addDefendantLitigationFriend: async (user, mpScenario, solicitor) => {
    eventName = 'ADD_DEFENDANT_LITIGATION_FRIEND';
    await apiRequest.setupTokens(user);
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    solicitorSetup(solicitor);
    assertContainsPopulatedFields(returnedCaseData, solicitor);
    caseData = returnedCaseData;

    caseData = await addFlagsToFixture(caseData);

    let fixture = data.ADD_DEFENDANT_LITIGATION_FRIEND[mpScenario];

    await validateEventPages(fixture);
    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: 'You have added litigation friend details'
    });

    await waitForFinishedBusinessProcess(caseId);

    if(await checkCaseFlagsEnabled()) {
      await assertFlagsInitialisedAfterAddLitigationFriend(config.hearingCenterAdminWithRegionId1, caseId);
    }
  },

  moveCaseToCaseman: async (user) => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');

    await apiRequest.setupTokens(user);

    eventName = 'CASE_PROCEEDS_IN_CASEMAN';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    await validateEventPages(data.CASE_PROCEEDS_IN_CASEMAN);

    await assertError('CaseProceedsInCaseman', data[eventName].invalid.CaseProceedsInCaseman,
      'The date entered cannot be in the future');

    //TODO CMC-1245 confirmation page for event
    await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM', {
      header: '',
      body: ''
    }, false);

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
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

  addCaseNote: async (user) => {
    await apiRequest.setupTokens(user);

    eventName = 'ADD_CASE_NOTE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    await validateEventPages(data.ADD_CASE_NOTE);

    await assertSubmittedEvent('CASE_ISSUED', {
      header: '',
      body: ''
    }, false);

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'CASE_ISSUED');

    // caseNote is set to null in service
    deleteCaseFields('caseNote');
  },

  amendRespondent1ResponseDeadline: async (user) => {
    await apiRequest.setupTokens(user);
    let respondent1deadline = {};
    respondent1deadline = {'respondent1ResponseDeadline': '2022-01-10T15:59:50'};
    testingSupport.updateCaseData(caseId, respondent1deadline);
  },

  amendRespondent2ResponseDeadline: async (user) => {
    await apiRequest.setupTokens(user);
    let respondent2deadline = {};
    respondent2deadline = {'respondent2ResponseDeadline': '2022-01-10T15:59:50'};
    testingSupport.updateCaseData(caseId, respondent2deadline);
  },

  amendHearingDueDate: async (user) => {
    let hearingDueDate = {};
    hearingDueDate = {'hearingDueDate': '2022-01-10'};
    await testingSupport.updateCaseData(caseId, hearingDueDate, user);
  },

  amendHearingDate: async (user, updatedDate) => {
    let hearingDate = {};
    hearingDate = {'hearingDate': updatedDate};
    await testingSupport.updateCaseData(caseId, hearingDate, user);
  },

  defaultJudgment: async (user, djRequestType = 'DISPOSAL_HEARING') => {
    await apiRequest.setupTokens(user);

    eventName = 'DEFAULT_JUDGEMENT';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);
    // workaround: caseManagementLocation shows in startevent api request but not in validate request
    deleteCaseFields('caseManagementLocation');
    if (djRequestType === 'DISPOSAL_HEARING') {
      await validateEventPages(data.REQUEST_DJ('DISPOSAL_HEARING', mpScenario));
    } else {
      await validateEventPages(data.REQUEST_DJ('TRIAL_HEARING', mpScenario));
    }

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: '',
      body: ''
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

  sdoDefaultJudgment: async (user, orderType = 'DISPOSAL_HEARING') => {
    await apiRequest.setupTokens(user);

    eventName = 'STANDARD_DIRECTION_ORDER_DJ';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);
    if (orderType === 'DISPOSAL_HEARING') {
      await validateEventPages(data.REQUEST_DJ_ORDER('DISPOSAL_HEARING', mpScenario));
    } else {
      await validateEventPages(data.REQUEST_DJ_ORDER('TRIAL_HEARING', mpScenario));
    }

    await assertSubmittedEvent('CASE_PROGRESSION', {
      header: '',
      body: ''
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

  getCaseId: async () => {
    console.log(`case created: ${caseId}`);
    return caseId;
  },

  getLegacyCaseReference: async () => {
    return legacyCaseReference;
  },

  cleanUp: async () => {
    await unAssignAllUsers();
  },

  createSDO: async (user, response = 'CREATE_DISPOSAL') => {
    console.log('SDO for case id ' + caseId);
    await apiRequest.setupTokens(user);
    if (response === 'UNSUITABLE_FOR_SDO') {
      eventName = 'NotSuitable_SDO';
    } else {
      eventName = 'CREATE_SDO';
    }

    caseData = await apiRequest.startEvent(eventName, caseId);
    // will be assigned on about to submit, based on judges decision
    delete caseData['allocatedTrack'];
    delete caseData['responseClaimTrack'];
    delete caseData['smallClaimsFlightDelay'];
    delete caseData['smallClaimsFlightDelayToggle'];
    //required to fix existing prod api tests for sdo
    clearWelshParaFromCaseData();
    delete caseData['sdoR2SmallClaimsWitnessStatementOther'];
    delete caseData['sdoR2FastTrackWitnessOfFact'];
    delete caseData['sdoR2FastTrackCreditHire'];
    delete caseData['sdoDJR2TrialCreditHire'];


    let disposalData = eventData['sdoTracks'][response];

    if (response === 'CREATE_FAST') {
      delete disposalData.calculated.ClaimsTrack.fastTrackWitnessOfFact;
      disposalData.calculated.ClaimsTrack = {...disposalData.calculated.ClaimsTrack, ...newSdoR2FieldsFastTrack};
      delete disposalData.calculated.FastTrack.fastTrackCreditHire;
      disposalData.calculated.FastTrack = {...disposalData.calculated.FastTrack, ...newSdoR2FastTrackCreditHireFields};
    }

    const fastTrackUpliftsEnabled = await checkFastTrackUpliftsEnabled();
    if (!fastTrackUpliftsEnabled) {
      removeFastTrackAllocationFromSdoData(disposalData);
    }

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

  createFinalOrder: async (user, finalOrderRequestType, orderTrack) => {
    console.log(`case in Final Order ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'GENERATE_DIRECTIONS_ORDER';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    delete returnedCaseData['hmcEaCourtLocation'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    const dayPlus0 = await dateNoWeekendsBankHolidayNextDay(0);
    const dayPlus7 = await dateNoWeekendsBankHolidayNextDay(7);
    const dayPlus14 = await dateNoWeekendsBankHolidayNextDay(14);
    const dayPlus21 = await dateNoWeekendsBankHolidayNextDay(21);

    if (finalOrderRequestType === 'ASSISTED_ORDER') {
      await validateEventPages(data.FINAL_ORDERS('ASSISTED_ORDER', dayPlus0, dayPlus7, dayPlus14, dayPlus21));
    }
    if (finalOrderRequestType === 'FREE_FORM_ORDER') {
      await validateEventPages(data.FINAL_ORDERS('FREE_FORM_ORDER', dayPlus0, dayPlus7, dayPlus14, dayPlus21));
    }
    if (finalOrderRequestType === 'DOWNLOAD_ORDER_TEMPLATE') {
      await validateEventPages(data.FINAL_ORDERS('DOWNLOAD_ORDER_TEMPLATE', dayPlus0, dayPlus7, dayPlus14, dayPlus21, orderTrack));
    }

    await apiRequest.startEvent(eventName, caseId);
    await apiRequest.submitEvent(eventName, caseData, caseId);

    await waitForFinishedBusinessProcess(caseId);
  },

  createFinalOrderJO: async (user, finalOrderRequestType) => {
    console.log(`case in Final Order ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'GENERATE_DIRECTIONS_ORDER';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    delete returnedCaseData['hmcEaCourtLocation'];
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

    await apiRequest.startEvent(eventName, caseId);
    await apiRequest.submitEvent(eventName, caseData, caseId);

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

    for(const [index, value] of caseFlagLocations.entries()) {
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

  scheduleHearing: async (user, allocatedTrack, isMinti = false) => {
    console.log('Hearing Scheduled for case id ' + caseId);
    await apiRequest.setupTokens(user);

    eventName = 'HEARING_SCHEDULED';

    caseData = await apiRequest.startEvent(eventName, caseId);
    delete caseData['SearchCriteria'];

    let scheduleData = data.HEARING_SCHEDULED(allocatedTrack, isMinti);

    for (let pageId of Object.keys(scheduleData.valid)) {
      await assertValidData(scheduleData, pageId);
    }
    let expectedState = 'HEARING_READINESS';
    if (allocatedTrack === 'OTHER') {
      expectedState = 'PREPARE_FOR_HEARING_CONDUCT_HEARING';
    }
    await assertSubmittedEvent(expectedState, null, false);
    await waitForFinishedBusinessProcess(caseId);
  },

  evidenceUploadJudge: async (user, typeOfNote, currentState) => {
    console.log('Evidence Upload of type:' + typeOfNote);
    await apiRequest.setupTokens(user);

    eventName = 'EVIDENCE_UPLOAD_JUDGE';

    caseData = await apiRequest.startEvent(eventName, caseId);
    delete caseData['SearchCriteria'];

    const document = await testingSupport.uploadDocument();
    let caseNoteData = await updateCaseDataWithPlaceholders(data.EVIDENCE_UPLOAD_JUDGE(typeOfNote), document);

    for (let pageId of Object.keys(caseNoteData.valid)) {
      await assertValidData(caseNoteData, pageId);
    }
    delete caseData['noteAdditionDateTime'];

    await assertSubmittedEvent(currentState, null, false);
    await waitForFinishedBusinessProcess(caseId);
  },

  hearingFeePaid: async (user) => {
    await apiRequest.setupTokens(user);

    await apiRequest.paymentUpdate(caseId, '/service-request-update',
      claimData.serviceUpdateDto(caseId, 'paid'));

    const response_msg = await apiRequest.hearingFeePaidEvent(caseId);
    assert.equal(response_msg.status, 200);
    console.log('Hearing Fee Paid');

    await apiRequest.setupTokens(config.applicantSolicitorUser);
    const updatedCaseState = await apiRequest.fetchCaseState(caseId, 'TRIAL_READINESS');
    assert.equal(updatedCaseState, 'PREPARE_FOR_HEARING_CONDUCT_HEARING');
    console.log('State moved to:'+updatedCaseState);
  },

  hearingFeeUnpaid: async (user) => {
    await apiRequest.setupTokens(user);

    const response_msg = await apiRequest.hearingFeeUnpaidEvent(caseId);
    assert.equal(response_msg.status, 200);
    console.log('Hearing Fee Unpaid');

    const updatedCaseState = await apiRequest.fetchCaseState(caseId, 'CASE_PROCEEDS_IN_CASEMAN', user);
    assert.equal(updatedCaseState, 'CASE_DISMISSED');
    console.log('State moved to:'+ updatedCaseState);
  },

  trialReadiness: async (user) => {
    await apiRequest.setupTokens(user);

    eventName = 'TRIAL_READINESS';
    caseData = await apiRequest.startEvent(eventName, caseId);

    let readinessData = data.TRIAL_READINESS(user);

    for (let pageId of Object.keys(readinessData.valid)) {
      await assertValidData(readinessData, pageId);
    }

    await assertSubmittedEvent('PREPARE_FOR_HEARING_CONDUCT_HEARING', null, false);
    await waitForFinishedBusinessProcess(caseId);
  },

  triggerBundle: async () => {
    const response_msg = await apiRequest.bundleTriggerEvent(caseId);
    const response = await response_msg.text();
    assert.equal(response, 'success');
  },

  evidenceUploadApplicant: async (user, mpScenario='', smallClaimType) => {
    await apiRequest.setupTokens(user);
    eventName = 'EVIDENCE_UPLOAD_APPLICANT';
    caseData = await apiRequest.startEvent(eventName, caseId);

    console.log('caseData.caseProgAllocatedTrack ..', caseData.caseProgAllocatedTrack );
    let ApplicantEvidenceSmallClaimData;
    if(caseData.caseProgAllocatedTrack === 'SMALL_CLAIM') {
      console.log('evidence upload small claim applicant for case id ' + caseId);
      if (smallClaimType === 'DRH') {
        ApplicantEvidenceSmallClaimData = data.EVIDENCE_UPLOAD_APPLICANT_DRH();
      } else {
        ApplicantEvidenceSmallClaimData = data.EVIDENCE_UPLOAD_APPLICANT_SMALL(mpScenario);
      }
      await validateEventPages(ApplicantEvidenceSmallClaimData);
    }
    if(caseData.caseProgAllocatedTrack === 'FAST_CLAIM' || caseData.caseProgAllocatedTrack === 'MULTI_CLAIM' || caseData.caseProgAllocatedTrack === 'INTERMEDIATE_CLAIM') {
      console.log('evidence upload applicant fast track for case id ' + caseId);
      let ApplicantEvidenceFastClaimData = data.EVIDENCE_UPLOAD_APPLICANT_FAST(mpScenario, caseData.caseProgAllocatedTrack);
      await validateEventPages(ApplicantEvidenceFastClaimData);
    }
    await assertSubmittedEvent('CASE_PROGRESSION', null, false);
    await waitForFinishedBusinessProcess(caseId);
  },

  evidenceUploadRespondent: async (user, multipartyScenario, smallClaimType) => {
    await apiRequest.setupTokens(user);
    eventName = 'EVIDENCE_UPLOAD_RESPONDENT';
    mpScenario = multipartyScenario;
    caseData = await apiRequest.startEvent(eventName, caseId);
    let RespondentEvidenceSmallClaimData;
    if(caseData.caseProgAllocatedTrack === 'SMALL_CLAIM') {
      console.log('evidence upload small claim respondent for case id ' + caseId);
      if (smallClaimType === 'DRH') {
        RespondentEvidenceSmallClaimData = data.EVIDENCE_UPLOAD_RESPONDENT_DRH();
      } else {
        RespondentEvidenceSmallClaimData = data.EVIDENCE_UPLOAD_RESPONDENT_SMALL(mpScenario);
      }
      await validateEventPages(RespondentEvidenceSmallClaimData);
    }
    if(caseData.caseProgAllocatedTrack === 'FAST_CLAIM' || caseData.caseProgAllocatedTrack === 'MULTI_CLAIM' || caseData.caseProgAllocatedTrack === 'INTERMEDIATE_CLAIM') {
      console.log('evidence upload fast claim respondent for case id ' + caseId);
      let RespondentEvidenceFastClaimData = data.EVIDENCE_UPLOAD_RESPONDENT_FAST(mpScenario, caseData.caseProgAllocatedTrack);
      await validateEventPages(RespondentEvidenceFastClaimData);
    }
    await assertSubmittedEvent('CASE_PROGRESSION', null, false);
    await waitForFinishedBusinessProcess(caseId);
  },

  hearingFeePaidDRH: async (user) => {
    await apiRequest.setupTokens(user);

    await apiRequest.paymentUpdate(caseId, '/service-request-update',
      claimData.serviceUpdateDto(caseId, 'paid'));

    const response_msg = await apiRequest.hearingFeePaidEvent(caseId);
    assert.equal(response_msg.status, 200);
    console.log('Hearing Fee Paid DRH');
  },

  notSuitableSDO: async (user, option) => {
    console.log(`case in Judicial Referral ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'NotSuitable_SDO';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    await validateEventPages(data.NOT_SUITABLE_SDO(option));

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

  transferCase: async (user) => {
    console.log(`case in Judicial Referral ${caseId}`);
    await apiRequest.setupTokens(user);

    eventName = 'TRANSFER_ONLINE_CASE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    assertContainsPopulatedFields(returnedCaseData);

    await validateEventPages(data.TRANSFER_CASE());

    await assertSubmittedEvent('JUDICIAL_REFERRAL', {
      header: '',
      body: ''
    }, true);
    await waitForFinishedBusinessProcess(caseId);
  },

  stayCase: async (user) => {
    console.log('Stay Case for case id ' + caseId);
    await apiRequest.setupTokens(user);
    eventName = 'STAY_CASE';

    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    delete returnedCaseData['SearchCriteria'];
    caseData = returnedCaseData;
    let disposalData = data.STAY_CASE();
    for (let pageId of Object.keys(disposalData.valid)) {
      await assertValidData(disposalData, pageId);
    }
    await assertSubmittedEvent('CASE_STAYED', {
      header: '# Stay added to the case \n\n ## All parties have been notified and any upcoming hearings must be cancelled',
      body: '&nbsp;'
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

  manageStay: async (user, requestUpdate, isJudicialReferral) => {
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
    for (let pageId of Object.keys(disposalData.valid)) {
      await assertValidData(disposalData, pageId);
    }
    if (requestUpdate) {
      await assertSubmittedEvent('CASE_STAYED', {
        header: header,
        body: '&nbsp;'
      }, true);
    } else {
      if (isJudicialReferral) {
        await assertSubmittedEvent('JUDICIAL_REFERRAL', {
          header: header,
          body: '&nbsp;'
        }, true);
      } else {
        await assertSubmittedEvent('CASE_PROGRESSION', {
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
    for (let pageId of Object.keys(disposalData.valid)) {
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
    for (let pageId of Object.keys(disposalData.valid)) {
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
    for (let pageId of Object.keys(disposalData.valid)) {
      await assertValidData(disposalData, pageId);
    }
    await assertSubmittedEvent('CASE_STAYED', {
      header: '# Reply sent',
      body: '<br /><h2 class="govuk-heading-m">What happens next</h2><br />A task has been created to review your reply.'
    }, true);

    await waitForFinishedBusinessProcess(caseId);
  },

};

// Functions
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
  for (let pageId of Object.keys(data.valid)) {
    if (pageId === 'DefendantLitigationFriend' || pageId === 'UploadOrder' || pageId === 'DocumentUpload' || pageId === 'Upload' || pageId === 'DraftDirections'|| pageId === 'ApplicantDefenceResponseDocument' || pageId === 'DraftDirections' || pageId === 'FinalOrderPreview' || pageId === 'FixedRecoverableCosts') {
      const document = await testingSupport.uploadDocument();
      data = await updateCaseDataWithPlaceholders(data, document);
    }
    await assertValidData(data, pageId, solicitor);
  }
};

const assertValidData = async (data, pageId, solicitor) => {
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
  if(pageId === 'SmallClaims' || pageId === 'SdoR2SmallClaims') {
    delete caseData.isSdoR2NewScreen;
  }

  let responseBody = await response.json();
  responseBody = clearDataForSearchCriteria(responseBody); //Until WA release
  if (eventName === 'INFORM_AGREED_EXTENSION_DATE' && mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP') {
    responseBody = clearDataForExtensionDate(responseBody, solicitor);
  } else if (eventName === 'DEFENDANT_RESPONSE' && mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP') {
    responseBody = clearDataForDefendantResponse(responseBody, solicitor);
  }
  if(eventName === 'EVIDENCE_UPLOAD_APPLICANT' || eventName === 'EVIDENCE_UPLOAD_RESPONDENT') {
    responseBody = clearDataForEvidenceUpload(responseBody, eventName);
    delete caseData['businessProcess'];
  }
  if(eventName === 'HEARING_SCHEDULED' && pageId === 'HearingNoticeSelect')
  {
    responseBody = clearHearingLocationData(responseBody);
    responseBody.data.allocatedTrack = caseData.allocatedTrack;
  }
  if(eventName === 'GENERATE_DIRECTIONS_ORDER') {
    responseBody = clearFinalOrderLocationData(responseBody);
    // After second minti release this is not needed. Track fields for GENERATE_DIRECTIONS_ORDER are currently linked
    // to a hidden wa page and do not appear in mid event handlers, which is fine as they are not currently used.
    // After minti release the fields are linked to a page and hidden via field show conditions and get returned correctly.
    responseBody.data.allocatedTrack = caseData.allocatedTrack;
    responseBody.data.respondent1Represented = caseData.respondent1Represented;
  }

  if(eventName === 'SEND_AND_REPLY') {
    if (pageId === 'sendAndReplyOption') {
      if (typeof caseData.lastMessage !== 'undefined') {
        responseBody.data.lastMessageJudgeLabel = caseData.lastMessageJudgeLabel;
        responseBody.data.lastMessage = caseData.lastMessage;
        responseBody.data.lastMessageAllocatedTrack = caseData.lastMessageAllocatedTrack;
      }

      delete responseBody.data['messageHistory'];
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
  delete responseBody.data['sdoR2SmallClaimsWitnessStatementOther'];
  delete responseBody.data['sdoR2FastTrackWitnessOfFact'];
  delete responseBody.data['sdoR2FastTrackCreditHire'];
  delete responseBody.data['sdoDJR2TrialCreditHire'];
  delete responseBody.data['gaEaCourtLocation'];
  delete responseBody.data['evidenceUploadNotificationSent'];


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
    responseBody.data.respondent1Represented = caseData.respondent1Represented;
    if (['ClaimsTrack', 'OrderType'].includes(pageId)) {
      delete caseData.hearingMethodValuesDisposalHearing;
      delete caseData.hearingMethodValuesFastTrack;
      delete caseData.hearingMethodValuesSmallClaims;
      clearNihlDataFromCaseData();
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
    if (pageId === 'ClaimsTrack'
      && !(responseBody.data.showCarmFields)) {
      // disposalHearingSchedulesOfLoss is populated on pageId SDO but then in pageId ClaimsTrack has been removed
      delete caseData.showCarmFields;
    }
  }
  if (pageId === 'Claimant') {
    delete caseData.applicant1OrganisationPolicy;
  }
  if (pageId === 'SdoR2FastTrack') {
    clearWelshParaFromCaseData();
    delete caseData['sdoR2FastTrackCreditHire'];
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

function appendToPath(path, key) {
  if (path) {
    return path.concat([key]);
  } else {
    return [key];
  }
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

const validateErrorOrWarning = async (pageId, eventData) => {
  const response = await apiRequest.validatePage(
    eventName,
    pageId,
    {...caseData, ...eventData},
    addCaseId(pageId) ? caseId : null,
    422
  );
  return response;
};

const assertError = async (pageId, eventData, expectedErrorMessage, responseBodyMessage = 'Unable to proceed because there are one or more callback Errors or Warnings') => {
  const response = await validateErrorOrWarning(pageId, eventData);
  const responseBody = await response.json();
  assert.equal(response.status, 422);
  assert.equal(responseBody.message, responseBodyMessage);
  if (responseBody.callbackErrors != null) {
    assert.equal(responseBody.callbackErrors[0], expectedErrorMessage);
  }
};

const expectedWarnings = async (pageId, eventData, expectedWarningMessages, responseBodyMessage = 'Unable to proceed because there are one or more callback Errors or Warnings') => {
  const response = await validateErrorOrWarning(pageId, eventData);
  const responseBody = await response.json();
  assert.equal(response.status, 422);
  assert.equal(responseBody.message, responseBodyMessage);
  if(responseBody.callbackWarnings != null ) {
    assert.equal(responseBody.callbackWarnings[0], expectedWarningMessages);
  }
};

const assertSubmittedEvent = async (expectedState, submittedCallbackResponseContains, hasSubmittedCallback = true) => {
  await apiRequest.startEvent(eventName, caseId);

  const response = await apiRequest.submitEvent(eventName, caseData, caseId);
  const responseBody = await response.json();
  assert.equal(response.status, 201);
  assert.equal(responseBody.state, expectedState);
  if (hasSubmittedCallback) {
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, submittedCallbackResponseContains.header);
    if(submittedCallbackResponseContains.body) {
      assert.include(responseBody.after_submit_callback_response.confirmation_body, submittedCallbackResponseContains.body);
    }
  }

  if (eventName === 'CREATE_CLAIM') {
    caseId = responseBody.id;
    await addUserCaseMapping(caseId, config.applicantSolicitorUser);
    console.log('Case created: ' + caseId);
  }
};

const assertSubmittedEventWithCaseData = async (updatedCaseData, expectedState, submittedCallbackResponseContains, hasSubmittedCallback = true) => {
  await apiRequest.startEvent(eventName, caseId);

  const response = await apiRequest.submitEvent(eventName, updatedCaseData, caseId);
  const responseBody = await response.json();
  assert.equal(response.status, 201);
  assert.equal(responseBody.state, expectedState);
  if (hasSubmittedCallback) {
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, submittedCallbackResponseContains.header);
    assert.include(responseBody.after_submit_callback_response.confirmation_body, submittedCallbackResponseContains.body);
  }
};
const assertContainsPopulatedFields = (returnedCaseData, solicitor) => {
  const fixture = solicitor ? adjustDataForSolicitor(solicitor, caseData) : caseData;
  for (let populatedCaseField of Object.keys(fixture)) {
    // this property won't be here until civil service is merged
    if (populatedCaseField !== 'applicant1DQRemoteHearing') {
      assert.property(returnedCaseData, populatedCaseField);
    }
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
    expect(caseForDisplay.triggers).to.deep.include.members(nonProdExpectedEvents[user.type][state]);
  } else {
    expect(caseForDisplay.triggers).to.deep.include.members(expectedEvents[user.type][state]);
  }
};

// const assertCaseNotAvailableToUser = async (user) => {
//   console.log(`Asserting user ${user.type} does not have permission to case`);
//   const caseForDisplay = await apiRequest.fetchCaseForDisplay(user, caseId, 404);
//   assert.equal(caseForDisplay.message, `No case found for reference: ${caseId}`);
// };

function addMidEventFields(pageId, responseBody, instanceData, claimAmount) {
  console.log(`Adding mid event fields for pageId: ${pageId}`);
  const midEventField = midEventFieldForPage[pageId];
  let midEventData;
  let calculated;

  if (instanceData && instanceData.calculated && instanceData.calculated[pageId]) {
    calculated = instanceData.calculated[pageId];
  }
  if(pageId === 'ClaimsTrack' || pageId === 'OrderType' || pageId === 'SmallClaims') {
    calculated = {...calculated, ...calculatedClaimsTrackDRH};
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

  if(pageId === 'OrderType') {
    let sdoR2Var = { ['isSdoR2NewScreen'] : 'No' };
    midEventData = {...midEventData, ...sdoR2Var};
  }

  caseData = {...caseData, ...midEventData};
  if (midEventField) {
    responseBody.data[midEventField.id] = caseData[midEventField.id];
  }
}

function assertDynamicListListItemsHaveExpectedLabels(responseBody, dynamicListFieldName, midEventData) {
  const actualDynamicElementLabels = removeUuidsFromDynamicList(responseBody.data, dynamicListFieldName);
  const expectedDynamicElementLabels = removeUuidsFromDynamicList(midEventData, dynamicListFieldName);

  expect(actualDynamicElementLabels).to.deep.equalInAnyOrder(expectedDynamicElementLabels);
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

function removeUuidsFromDynamicList(data, dynamicListField) {
  const dynamicElements = data[dynamicListField].list_items;

  return dynamicElements.map(({code, ...item}) => item);
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

const assignCase = async () => {
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

// solicitor 1 should not see details for respondent 2
// solicitor 2 should not see details for respondent 1
const solicitorSetup = (solicitor) => {
  if (solicitor === 'solicitorOne') {
    deleteCaseFields('respondent2');
  } else if (solicitor === 'solicitorTwo') {
    deleteCaseFields('respondent1');
  }
};

const clearDataForExtensionDate = (responseBody, solicitor) => {
//  delete responseBody.data['businessProcess'];
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

const clearDataForSearchCriteria = (responseBody) => {
  delete responseBody.data['SearchCriteria'];
  return responseBody;
};

const clearHearingLocationData = (responseBody) => {
  delete responseBody.data['hearingLocation'];
  return responseBody;
};

const clearDataForDefendantResponse = (responseBody, solicitor) => {

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
    delete responseBody.data['respondent1DQDisclosureReport'];
    delete responseBody.data['respondent1DQFixedRecoverableCosts'];
    delete responseBody.data['respondent1DQFixedRecoverableCostsIntermediate'];
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
  delete responseBody.data['businessProcess'];
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
  delete responseBody.data['isSdoR2NewScreen'];
  delete responseBody.data['fastClaims'];
  clearNihlDataFromResponse(responseBody);
  delete responseBody.data['sdoR2SmallClaimsJudgesRecital'];
  delete responseBody.data['sdoR2SmallClaimsUploadDocToggle'];
  delete responseBody.data['sdoR2SmallClaimsUploadDoc'];
  delete responseBody.data['sdoR2SmallClaimsWitnessStatements'];
  delete responseBody.data['sdoR2SmallClaimsImpNotes'];
  delete responseBody.data['sdoR2SmallClaimsPPI'];
  delete responseBody.data['sdoR2SmallClaimsHearing'];
  delete responseBody.data['sdoR2SmallClaimsWitnessStatementsToggle'];
  delete responseBody.data['sdoR2SmallClaimsHearingToggle'];
  delete responseBody.data['smallClaims'];

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

const clearNihlDataFromCaseData = () => {
  delete caseData['sdoFastTrackJudgesRecital'];
  delete caseData['sdoAltDisputeResolution'];
  delete caseData['sdoVariationOfDirections'];
  delete caseData['sdoR2Settlement'];
  delete caseData['sdoR2DisclosureOfDocumentsToggle'];
  delete caseData['sdoR2DisclosureOfDocuments'];
  delete caseData['sdoR2SeparatorWitnessesOfFactToggle'];
  delete caseData['sdoR2WitnessesOfFact'];
  delete caseData['sdoR2ScheduleOfLossToggle'];
  delete caseData['sdoR2ScheduleOfLoss'];
  delete caseData['sdoR2AddNewDirection'];
  delete caseData['sdoR2TrialToggle'];
  delete caseData['sdoR2Trial'];
  delete caseData['sdoR2ImportantNotesTxt'];
  delete caseData['sdoR2ImportantNotesDate'];
  delete caseData['sdoR2SeparatorExpertEvidenceToggle'];
  delete caseData['sdoR2ExpertEvidence'];
  delete caseData['sdoR2SeparatorAddendumReportToggle'];
  delete caseData['sdoR2AddendumReport'];
  delete caseData['sdoR2SeparatorFurtherAudiogramToggle'];
  delete caseData['sdoR2FurtherAudiogram'];
  delete caseData['sdoR2SeparatorQuestionsClaimantExpertToggle'];
  delete caseData['sdoR2QuestionsClaimantExpert'];
  delete caseData['sdoR2SeparatorPermissionToRelyOnExpertToggle'];
  delete caseData['sdoR2PermissionToRelyOnExpert'];
  delete caseData['sdoR2SeparatorEvidenceAcousticEngineerToggle'];
  delete caseData['sdoR2EvidenceAcousticEngineer'];
  delete caseData['sdoR2SeparatorQuestionsToEntExpertToggle'];
  delete caseData['sdoR2QuestionsToEntExpert'];
  delete caseData['sdoR2SeparatorUploadOfDocumentsToggle'];
  delete caseData['sdoR2UploadOfDocuments'];
  delete caseData['sdoR2NihlUseOfWelshLanguage'];
  delete caseData['sdoR2SmallClaimsHearing'];
};

const clearWelshParaFromCaseData= () => {
  delete caseData['sdoR2SmallClaimsUseOfWelshLanguage'];
  delete caseData['sdoR2NihlUseOfWelshLanguage'];
  delete caseData['sdoR2FastTrackUseOfWelshLanguage'];
  delete caseData['sdoR2DrhUseOfWelshLanguage'];
  delete caseData['sdoR2DisposalHearingUseOfWelshLanguage'];
};

const clearNihlDataFromResponse = (responseBody) => {
  delete responseBody.data['sdoFastTrackJudgesRecital'];
  delete responseBody.data['sdoAltDisputeResolution'];
  delete responseBody.data['sdoVariationOfDirections'];
  delete responseBody.data['sdoR2Settlement'];
  delete responseBody.data['sdoR2DisclosureOfDocumentsToggle'];
  delete responseBody.data['sdoR2DisclosureOfDocuments'];
  delete responseBody.data['sdoR2SeparatorWitnessesOfFactToggle'];
  delete responseBody.data['sdoR2WitnessesOfFact'];
  delete responseBody.data['sdoR2ScheduleOfLossToggle'];
  delete responseBody.data['sdoR2ScheduleOfLoss'];
  delete responseBody.data['sdoR2AddNewDirection'];
  delete responseBody.data['sdoR2TrialToggle'];
  delete responseBody.data['sdoR2Trial'];
  delete responseBody.data['sdoR2ImportantNotesTxt'];
  delete responseBody.data['sdoR2ImportantNotesDate'];
  delete responseBody.data['sdoR2SeparatorExpertEvidenceToggle'];
  delete responseBody.data['sdoR2ExpertEvidence'];
  delete responseBody.data['sdoR2SeparatorAddendumReportToggle'];
  delete responseBody.data['sdoR2AddendumReport'];
  delete responseBody.data['sdoR2SeparatorFurtherAudiogramToggle'];
  delete responseBody.data['sdoR2FurtherAudiogram'];
  delete responseBody.data['sdoR2SeparatorQuestionsClaimantExpertToggle'];
  delete responseBody.data['sdoR2QuestionsClaimantExpert'];
  delete responseBody.data['sdoR2SeparatorPermissionToRelyOnExpertToggle'];
  delete responseBody.data['sdoR2PermissionToRelyOnExpert'];
  delete responseBody.data['sdoR2SeparatorEvidenceAcousticEngineerToggle'];
  delete responseBody.data['sdoR2EvidenceAcousticEngineer'];
  delete responseBody.data['sdoR2SeparatorQuestionsToEntExpertToggle'];
  delete responseBody.data['sdoR2QuestionsToEntExpert'];
  delete responseBody.data['sdoR2SeparatorUploadOfDocumentsToggle'];
  delete responseBody.data['sdoR2UploadOfDocuments'];
};
