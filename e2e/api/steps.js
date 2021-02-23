const assert = require('assert').strict;
const config = require('../config.js');

const deepEqualInAnyOrder = require('deep-equal-in-any-order');
const chai = require('chai');

chai.use(deepEqualInAnyOrder);

const { expect } = chai;

const {waitForFinishedBusinessProcess, assignCaseToDefendant} = require('../api/testingSupport');
const apiRequest = require('./apiRequest.js');
const claimData = require('../fixtures/events/createClaim.js');
const expectedEvents =  require('../fixtures/ccd/expectedEvents.js');

const data = {
  CREATE_CLAIM: claimData.createClaim,
  CREATE_CLAIM_RESPONDENT_LIP: claimData.createClaimLitigantInPerson,
  ADD_OR_AMEND_CLAIM_DOCUMENTS: require('../fixtures/events/addOrAmendClaimDocuments.js'),
  ACKNOWLEDGE_SERVICE: require('../fixtures/events/acknowledgeService.js'),
  DEFENDANT_RESPONSE: require('../fixtures/events/defendantResponse.js'),
  CLAIMANT_RESPONSE: require('../fixtures/events/claimantResponse.js'),
  ADD_DEFENDANT_LITIGATION_FRIEND: require('../fixtures/events/addDefendantLitigationFriend.js'),
  CASE_PROCEEDS_IN_CASEMAN: require('../fixtures/events/caseProceedsInCaseman.js'),
};

const midEventFieldForPage = {
  ClaimValue: {
    id: 'applicantSolicitor1PbaAccounts',
    dynamicList: true
  },
  ClaimantLitigationFriend: {
    id: 'applicantSolicitor1CheckEmail',
    dynamicList: false
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

    await assertSubmittedEvent('PENDING_CASE_ISSUED', {
      header: 'Your claim has been issued',
      body: 'Follow these steps to serve a claim'
    }, true);
    await assignCaseToDefendant(caseId);

    await assertCorrectEventsAreAvailableToUser(config.solicitorUser, 'AWAITING_CASE_NOTIFICATION');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_CASE_NOTIFICATION');
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

    await assertSubmittedEvent('PROCEEDS_WITH_OFFLINE_JOURNEY', {
        header: 'Your claim will now progress offline',
        body: 'You do not need to do anything'
      }, true);

    await assignCaseToDefendant(caseId);
    await assertCorrectEventsAreAvailableToUser(config.solicitorUser, 'PROCEEDS_WITH_OFFLINE_JOURNEY');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'PROCEEDS_WITH_OFFLINE_JOURNEY');
    //field is deleted in about to submit callback
    deleteCaseFields('applicantSolicitor1CheckEmail');
  },

  amendClaimDocuments: async () => {
    eventName = 'ADD_OR_AMEND_CLAIM_DOCUMENTS';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    await validateEventPages(data[eventName]);

    await assertCallbackError('Upload', data[eventName].invalid.Upload.duplicateError,
      'More than one particular of claim added');

    await assertCallbackError('Upload', data[eventName].invalid.Upload.nullError,
      'One particular of claim is required');

    await assertSubmittedEvent('AWAITING_CASE_NOTIFICATION', {
      header: 'Documents uploaded successfully',
      body: '<br />'
    }, true);

    await assertCorrectEventsAreAvailableToUser(config.solicitorUser, 'AWAITING_CASE_NOTIFICATION');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_CASE_NOTIFICATION');
  },

  notifyClaim: async() => {
    eventName = 'NOTIFY_DEFENDANT_OF_CLAIM';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);

    await assertSubmittedEvent('CREATED', {
      header: 'Notification of claim sent',
      body: 'What happens next'
    });

    await assertCorrectEventsAreAvailableToUser(config.solicitorUser, 'CREATED');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'CREATED');
  },

  acknowledgeService: async () => {
    eventName = 'ACKNOWLEDGE_SERVICE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;
    deleteCaseFields('systemGeneratedCaseDocuments');

    await validateEventPages(data.ACKNOWLEDGE_SERVICE);

    await assertCallbackError('ConfirmDetails', data[eventName].invalid.ConfirmDetails.futureDateOfBirth,
      'The date entered cannot be in the future');

    await assertSubmittedEvent('CREATED', {
      header: 'You\'ve acknowledged service',
      body: 'You need to respond before'
    }, true);

    await assertCorrectEventsAreAvailableToUser(config.solicitorUser, 'CREATED');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'CREATED');
  },

  defendantResponse: async () => {
    eventName = 'DEFENDANT_RESPONSE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;
    deleteCaseFields('respondent1', 'solicitorReferences');

    await validateEventPages(data.DEFENDANT_RESPONSE);

    await assertCallbackError('ConfirmDetails', data[eventName].invalid.ConfirmDetails.futureDateOfBirth,
      'The date entered cannot be in the future');
    await assertCallbackError('Hearing', data[eventName].invalid.Hearing.past,
      'The date cannot be in the past and must not be more than a year in the future');
    await assertCallbackError('Hearing', data[eventName].invalid.Hearing.moreThanYear,
      'The date cannot be in the past and must not be more than a year in the future');

    await assertSubmittedEvent('AWAITING_CLAIMANT_INTENTION', {
      header: 'You\'ve submitted your response',
      body: 'We will let you know when they respond.'
    }, true);

    await assertCorrectEventsAreAvailableToUser(config.solicitorUser, 'AWAITING_CLAIMANT_INTENTION');
    await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'AWAITING_CLAIMANT_INTENTION');
  },

  claimantResponse: async () => {
    eventName = 'CLAIMANT_RESPONSE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    await validateEventPages(data.CLAIMANT_RESPONSE);

    await assertCallbackError('Hearing', data[eventName].invalid.Hearing.past,
      'The date cannot be in the past and must not be more than a year in the future');
    await assertCallbackError('Hearing', data[eventName].invalid.Hearing.moreThanYear,
      'The date cannot be in the past and must not be more than a year in the future');

    await assertSubmittedEvent('STAYED', {
      header: 'You\'ve decided to proceed with the claim',
      body: 'We\'ll review the case. We\'ll contact you to tell you what to do next.'
    }, true);
    await waitForFinishedBusinessProcess(caseId);

    //TODO: event currently puts claim into stayed state and users do no have permissions to see it.

    // await assertCorrectEventsAreAvailableToUser(config.solicitorUser, 'PROCEEDS_WITH_OFFLINE_JOURNEY');
    // await assertCorrectEventsAreAvailableToUser(config.defendantSolicitorUser, 'PROCEEDS_WITH_OFFLINE_JOURNEY');
  },

  //TODO this method is not used in api tests
  addDefendantLitigationFriend: async () => {
    eventName = 'ADD_DEFENDANT_LITIGATION_FRIEND';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    await validateEventPages(data.ADD_DEFENDANT_LITIGATION_FRIEND);
  },

  caseProceedsInCaseman: async () => {
    eventName = 'CASE_PROCEEDS_IN_CASEMAN';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    await validateEventPages(data.CASE_PROCEEDS_IN_CASEMAN);

    await assertCallbackError('CaseProceedsInCaseman', data[eventName].invalid.CaseProceedsInCaseman,
      'The date entered cannot be in the future');

    //TODO CMC-1245 confirmation page for event
    await assertSubmittedEvent('PROCEEDS_WITH_OFFLINE_JOURNEY', {
      header: '',
      body: ''
    }, false);
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
  }

  assert.deepEqual(responseBody.data, caseData);
};

const assertCallbackError = async (pageId, eventData, expectedErrorMessage) => {
  const response = await apiRequest.validatePage(eventName, pageId, {...caseData, ...eventData}, 422);
  const responseBody = await response.json();

  assert.equal(response.status, 422);
  assert.equal(responseBody.message, 'Unable to proceed because there are one or more callback Errors or Warnings');
  assert.equal(responseBody.callbackErrors[0], expectedErrorMessage);
};

const assertSubmittedEvent = async (expectedState, submittedCallbackResponseContains, hasSubmittedCallback) => {
  await apiRequest.startEvent(eventName, caseId);
  const response = await apiRequest.submitEvent(eventName, caseData, caseId);
  const responseBody = await response.json();

  assert.equal(response.status, 201);
  assert.equal(responseBody.state, expectedState);
  if (hasSubmittedCallback) {
    assert.equal(responseBody.callback_response_status_code, 200);
    assert.equal(responseBody.after_submit_callback_response.confirmation_header.includes(submittedCallbackResponseContains.header), true);
    assert.equal(responseBody.after_submit_callback_response.confirmation_body.includes(submittedCallbackResponseContains.body), true);
  }

  if (eventName === 'CREATE_CLAIM') {
    caseId = responseBody.id;
    console.log('Case created: ' + caseId);
  }
};

const assertContainsPopulatedFields = returnedCaseData => {
  for (let populatedCaseField of Object.keys(caseData)) {
    assert.equal(populatedCaseField in returnedCaseData, true,
      'Expected case data to contain field: ' + populatedCaseField);
  }
};

// Mid event will not return case fields that were already filled in another event if they're present on currently processed event.
// This happens until these case fields are set again as a part of current event (note that this data is not removed from the case).
// Therefore these case fields need to be removed from caseData, as caseData object is used to make assertions
const deleteCaseFields = (...caseFields) => {
  caseFields.forEach(caseField => delete caseData[caseField]);
};

const assertCorrectEventsAreAvailableToUser = async (user, state) => {
  await waitForFinishedBusinessProcess(caseId);
  const caseForDisplay = await apiRequest.fetchCaseForDisplay(user, caseId);
  expect(caseForDisplay.triggers).to.deep.equalInAnyOrder(expectedEvents[state]);
};

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
