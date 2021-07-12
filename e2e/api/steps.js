const config = require('../config.js');

const deepEqualInAnyOrder = require('deep-equal-in-any-order');
const chai = require('chai');

chai.use(deepEqualInAnyOrder);
chai.config.truncateThreshold = 0;
const {expect, assert} = chai;

const {waitForFinishedBusinessProcess, assignCaseToDefendant} = require('../api/testingSupport');
const apiRequest = require('./apiRequest.js');
const claimData = require('../fixtures/events/createClaim.js');
const expectedEvents = require('../fixtures/ccd/expectedEvents.js');

const data = {
  CREATE_CLAIM: claimData.createClaim,
  CREATE_CLAIM_RESPONDENT_LIP: claimData.createClaimLitigantInPerson,
  CREATE_CLAIM_TERMINATED_PBA: claimData.createClaimWithTerminatedPBAAccount,
  RESUBMIT_CLAIM: require('../fixtures/events/resubmitClaim.js'),
  ADD_OR_AMEND_CLAIM_DOCUMENTS: require('../fixtures/events/addOrAmendClaimDocuments.js'),
  CREATE_CLAIM_RESPONDENT_SOLICITOR_FIRM_NOT_IN_MY_HMCTS: claimData.createClaimRespondentSolFirmNotInMyHmcts,
  ACKNOWLEDGE_CLAIM: require('../fixtures/events/acknowledgeClaim.js'),
  INFORM_AGREED_EXTENSION_DATE: require('../fixtures/events/informAgreeExtensionDate.js'),
  DEFENDANT_RESPONSE: require('../fixtures/events/defendantResponse.js'),
  CLAIMANT_RESPONSE: require('../fixtures/events/claimantResponse.js'),
  ADD_DEFENDANT_LITIGATION_FRIEND: require('../fixtures/events/addDefendantLitigationFriend.js'),
  CASE_PROCEEDS_IN_CASEMAN: require('../fixtures/events/caseProceedsInCaseman.js'),
  AMEND_PARTY_DETAILS: require('../fixtures/events/amendPartyDetails.js'),
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

module.exports = {
  createClaimWithRepresentedRespondent: async (user) => {
    eventName = 'CREATE_CLAIM';
    caseId = null;
    caseData = {};
    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    await validateEventPages(data.CREATE_CLAIM);

    let i;
    for (i = 0; i < data[eventName].invalid.Court.courtLocation.applicantPreferredCourt.length; i++) {
      await assertError('Court', data[eventName].invalid.Court.courtLocation.applicantPreferredCourt[i],
        null, 'Case data validation failed');
    }
    await assertError('Upload', data[eventName].invalid.Upload.servedDocumentFiles.particularsOfClaimDocument,
      null, 'Case data validation failed');

    await assertSubmittedEvent('PENDING_CASE_ISSUED', {
      header: 'Your claim has been received',
      body: 'Your claim will not be issued until payment is confirmed.'
    });

    await assignCaseToDefendant(caseId);
    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'CASE_ISSUED');
    // await assertCaseNotAvailableToUser(config.defendantSolicitorUser);

    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');
  },

  createClaimWithRespondentLitigantInPerson: async (user) => {
    eventName = 'CREATE_CLAIM';
    caseId = null;
    caseData = {};
    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    await validateEventPages(data.CREATE_CLAIM_RESPONDENT_LIP);

    await assertSubmittedEvent('PENDING_CASE_ISSUED', {
      header: 'Your claim has been received and will progress offline',
      body: 'Your claim will not be issued until payment is confirmed. Once payment is confirmed you will receive an email. The claim will then progress offline.'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
  },

  createClaimWithRespondentSolicitorFirmNotInMyHmcts: async (user) => {
    eventName = 'CREATE_CLAIM';
    caseId = null;
    caseData = {};
    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    await validateEventPages(data.CREATE_CLAIM_RESPONDENT_SOLICITOR_FIRM_NOT_IN_MY_HMCTS);

    await assertSubmittedEvent('PENDING_CASE_ISSUED', {
      header: 'Your claim has been received and will progress offline',
      body: 'Your claim will not be issued until payment is confirmed. Once payment is confirmed you will receive an email. The claim will then progress offline.'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');
  },

  createClaimWithFailingPBAAccount: async (user) => {
    eventName = 'CREATE_CLAIM';
    caseId = null;
    caseData = {};
    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    await validateEventPages(data.CREATE_CLAIM_TERMINATED_PBA);
    await assertSubmittedEvent('PENDING_CASE_ISSUED', {
      header: 'Your claim has been received',
      body: 'You have until DATE to notify the defendant of the claim and claim details.'
    });

    await assignCaseToDefendant(caseId);
    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'PENDING_CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'PENDING_CASE_ISSUED');
    // await assertCaseNotAvailableToUser(config.defendantSolicitorUser);
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
    // await assertCaseNotAvailableToUser(config.defendantSolicitorUser);
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

    await validateEventPages(data[eventName]);

    await assertError('Upload', data[eventName].invalid.Upload.duplicateError,
      'You need to either upload 1 Particulars of claim only or enter the Particulars of claim text in the field provided. You cannot do both.');

    await assertSubmittedEvent('CASE_ISSUED', {
      header: 'Documents uploaded successfully',
      body: ''
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'CASE_ISSUED');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'CASE_ISSUED');
    // await assertCaseNotAvailableToUser(config.defendantSolicitorUser);
  },

  notifyClaim: async (user) => {
    await apiRequest.setupTokens(user);

    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);

    await assertSubmittedEvent('AWAITING_CASE_DETAILS_NOTIFICATION', {
      header: 'Notification of claim sent',
      body: 'The defendant legal representative\'s organisation has been notified and granted access to this claim.'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_CASE_DETAILS_NOTIFICATION');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_CASE_DETAILS_NOTIFICATION');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_CASE_DETAILS_NOTIFICATION');
  },

  notifyClaimDetails: async (user) => {
    await apiRequest.setupTokens(user);

    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM_DETAILS';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);

    await validateEventPages(data.ADD_OR_AMEND_CLAIM_DOCUMENTS);

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: 'Defendant notified',
      body: 'The defendant legal representative\'s organisation has been notified of the claim details.'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  },

  amendPartyDetails: async (user) => {
    await apiRequest.setupTokens(user);

    eventName = 'AMEND_PARTY_DETAILS';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);

    await validateEventPages(data[eventName]);

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: 'You have updated a legal representative\'s email address',
      body: ' '
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  },

  acknowledgeClaim: async (user) => {
    await apiRequest.setupTokens(user);

    eventName = 'ACKNOWLEDGE_CLAIM';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;
    deleteCaseFields('systemGeneratedCaseDocuments');

    await validateEventPages(data.ACKNOWLEDGE_CLAIM);

    await assertError('ConfirmDetails', data[eventName].invalid.ConfirmDetails.futureDateOfBirth,
      'The date entered cannot be in the future');

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: '',
      body: ''
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    deleteCaseFields('respondent1Copy');
  },

  informAgreedExtension: async (user) => {
    await apiRequest.setupTokens(user);

    eventName = 'INFORM_AGREED_EXTENSION_DATE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;
    deleteCaseFields('systemGeneratedCaseDocuments');

    await validateEventPages(data[eventName]);

    await assertError('ExtensionDate', data[eventName].invalid.ExtensionDate.past,
      'The agreed extension date must be a date in the future');
    await assertError('ExtensionDate', data[eventName].invalid.ExtensionDate.beforeCurrentDeadline,
      'The agreed extension date must be after the current deadline');

    await assertSubmittedEvent('AWAITING_RESPONDENT_ACKNOWLEDGEMENT', {
      header: 'Extension deadline submitted',
      body: 'You must respond to the claimant by'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  },

  defendantResponse: async (user) => {
    await apiRequest.setupTokens(user);

    eventName = 'DEFENDANT_RESPONSE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;
    deleteCaseFields('respondent1', 'solicitorReferences');

    await validateEventPages(data.DEFENDANT_RESPONSE);

    await assertError('ConfirmDetails', data[eventName].invalid.ConfirmDetails.futureDateOfBirth,
      'The date entered cannot be in the future');
    await assertError('Experts', data[eventName].invalid.Experts.emptyDetails, 'Expert details required');
    await assertError('Hearing', data[eventName].invalid.Hearing.past,
      'The date cannot be in the past and must not be more than a year in the future');
    await assertError('Hearing', data[eventName].invalid.Hearing.moreThanYear,
      'The date cannot be in the past and must not be more than a year in the future');

    //TODO: update when service repo has new content (CMC-1265)
    await assertSubmittedEvent('AWAITING_APPLICANT_INTENTION', {
      header: 'You have submitted the Defendant\'s defence',
      body: 'The Claimant legal representative will get a notification'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'AWAITING_APPLICANT_INTENTION');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_APPLICANT_INTENTION');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'AWAITING_APPLICANT_INTENTION');
    deleteCaseFields('respondent1Copy');
  },

  claimantResponse: async (user) => {
    // workaround
    deleteCaseFields('applicantSolicitor1ClaimStatementOfTruth');

    await apiRequest.setupTokens(user);

    eventName = 'CLAIMANT_RESPONSE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    await validateEventPages(data.CLAIMANT_RESPONSE);

    await assertError('Experts', data[eventName].invalid.Experts.emptyDetails, 'Expert details required');
    await assertError('Hearing', data[eventName].invalid.Hearing.past,
      'The date cannot be in the past and must not be more than a year in the future');
    await assertError('Hearing', data[eventName].invalid.Hearing.moreThanYear,
      'The date cannot be in the past and must not be more than a year in the future');

    await assertSubmittedEvent('PROCEEDS_IN_HERITAGE_SYSTEM', {
      header: 'You have chosen to proceed with the claim',
      body: '>We will review the case and contact you to tell you what to do next.'
    });

    await waitForFinishedBusinessProcess(caseId);
    await assertCorrectEventsAreAvailableToUser(config.applicantSolicitorUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
    await assertCorrectEventsAreAvailableToUser(config.adminUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
  },

  //TODO this method is not used in api tests
  addDefendantLitigationFriend: async () => {
    eventName = 'ADD_DEFENDANT_LITIGATION_FRIEND';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    await validateEventPages(data.ADD_DEFENDANT_LITIGATION_FRIEND);
    await assertSubmittedEvent('ADD_DEFENDANT_LITIGATION_FRIEND', {
      header: 'You have added litigation friend details',
      body: '<br />'
    });
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
  }
};

const validateEventPages = async (data) => {
  for (let pageId of Object.keys(data.valid)) {
    await assertValidData(data, pageId);
  }
};

const assertValidData = async (data, pageId) => {
  console.log(`asserting page: ${pageId} has valid data`);
  const validDataForPage = data.valid[pageId];
  caseData = {...caseData, ...validDataForPage};

  const response = await apiRequest.validatePage(eventName, pageId, caseData);
  const responseBody = await response.json();

  assert.equal(response.status, 200);

  // eslint-disable-next-line no-prototype-builtins
  if (midEventFieldForPage.hasOwnProperty(pageId)) {
    addMidEventFields(pageId, responseBody);
    caseData = removeUiFields(pageId, caseData);
  }

  assert.deepEqual(responseBody.data, caseData);
};

function removeUiFields(pageId, caseData) {
  console.log(`Removing ui fields for pageId: ${pageId}`);
  const midEventField = midEventFieldForPage[pageId];

  if (midEventField.uiField.remove === true) {
    const fieldToRemove = midEventField.uiField.field;
    delete caseData[fieldToRemove];
  }
  return caseData;
}

const assertError = async (pageId, eventData, expectedErrorMessage, responseBodyMessage = 'Unable to proceed because there are one or more callback Errors or Warnings') => {
  const response = await apiRequest.validatePage(eventName, pageId, {...caseData, ...eventData}, 422);
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
  if (hasSubmittedCallback) {
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.include(responseBody.after_submit_callback_response.confirmation_header, submittedCallbackResponseContains.header);
    assert.include(responseBody.after_submit_callback_response.confirmation_body, submittedCallbackResponseContains.body);
  }

  if (eventName === 'CREATE_CLAIM') {
    caseId = responseBody.id;
    console.log('Case created: ' + caseId);
  }
};

const assertContainsPopulatedFields = returnedCaseData => {
  for (let populatedCaseField of Object.keys(caseData)) {
    assert.property(returnedCaseData,  populatedCaseField);
  }
};

// Mid event will not return case fields that were already filled in another event if they're present on currently processed event.
// This happens until these case fields are set again as a part of current event (note that this data is not removed from the case).
// Therefore these case fields need to be removed from caseData, as caseData object is used to make assertions
const deleteCaseFields = (...caseFields) => {
  caseFields.forEach(caseField => delete caseData[caseField]);
};

const assertCorrectEventsAreAvailableToUser = async (user, state) => {
  console.log(`Asserting user ${user.type} has correct permissions`);
  const caseForDisplay = await apiRequest.fetchCaseForDisplay(user, caseId);
  expect(caseForDisplay.triggers).to.deep.equalInAnyOrder(expectedEvents[user.type][state]);
};

// const assertCaseNotAvailableToUser = async (user) => {
//   console.log(`Asserting user ${user.type} does not have permission to case`);
//   const caseForDisplay = await apiRequest.fetchCaseForDisplay(user, caseId, 404);
//   assert.equal(caseForDisplay.message, `No case found for reference: ${caseId}`);
// };

function addMidEventFields(pageId, responseBody) {
  console.log(`Adding mid event fields for pageId: ${pageId}`);
  const midEventData = data[eventName].midEventData[pageId];
  const midEventField = midEventFieldForPage[pageId];

  if (midEventField.dynamicList === true) {
    assertDynamicListListItemsHaveExpectedLabels(responseBody, midEventField.id, midEventData);
  }

  caseData = {...caseData, ...midEventData};
  responseBody.data[midEventField.id] = caseData[midEventField.id];
}

function assertDynamicListListItemsHaveExpectedLabels(responseBody, dynamicListFieldName, midEventData) {
  const actualDynamicElementLabels = removeUuidsFromDynamicList(responseBody.data, dynamicListFieldName);
  const expectedDynamicElementLabels = removeUuidsFromDynamicList(midEventData, dynamicListFieldName);

  expect(actualDynamicElementLabels).to.deep.equalInAnyOrder(expectedDynamicElementLabels);
}

function removeUuidsFromDynamicList(data, dynamicListField) {
  const dynamicElements = data[dynamicListField].list_items;
  // eslint-disable-next-line no-unused-vars
  return dynamicElements.map(({code, ...item}) => item);
}
