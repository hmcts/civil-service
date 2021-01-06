const assert = require('assert').strict;
const deepEqualInAnyOrder = require('deep-equal-in-any-order');
const chai = require('chai');

chai.use(deepEqualInAnyOrder);

const { expect } = chai;

const apiRequest = require('./apiRequest.js');
const {waitForFinishedBusinessProcess} = require('../api/testingSupport');

const data = {
  CREATE_CLAIM: require('../fixtures/events/createClaim.js'),
  CREATE_CLAIM_RESPONDENT_LIP: require('../fixtures/events/createClaimLitigantInPerson.js'),
  CONFIRM_SERVICE: require('../fixtures/events/confirmService.js'),
  ACKNOWLEDGE_SERVICE: require('../fixtures/events/acknowledgeService.js'),
  REQUEST_EXTENSION: require('../fixtures/events/requestExtension.js'),
  RESPOND_EXTENSION: require('../fixtures/events/respondExtension.js'),
  DEFENDANT_RESPONSE: require('../fixtures/events/defendantResponse.js'),
  CLAIMANT_RESPONSE: require('../fixtures/events/claimantResponse.js'),
  ADD_DEFENDANT_LITIGATION_FRIEND: require('../fixtures/events/addDefendantLitigationFriend.js'),
};

const midEventFieldForPage = {
  ClaimValue: {
    id: 'applicantSolicitor1PbaAccounts',
    dynamicList: true
  }
};

let caseId, eventName;
let caseData = {};

module.exports = {
  createClaimWithRepresentedRespondent: async (user) => {
    eventName = 'CREATE_CLAIM';
    await apiRequest.setupTokens(user);
    await apiRequest.startEvent(eventName);
    await validateEventPages(data.CREATE_CLAIM);

    await assertSubmittedEvent('PENDING_CASE_ISSUED', {
      header: 'Your claim has been issued',
      body: 'Follow these steps to serve a claim'
    });
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
    });
  },

  confirmService: async () => {
    eventName = 'CONFIRM_SERVICE';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;
    deleteCaseFields('servedDocumentFiles');

    await validateEventPages(data.CONFIRM_SERVICE);

    await assertCallbackError('ServedDocuments', data[eventName].invalid.ServedDocuments.blankOtherDocuments,
      'CONTENT TBC: please enter a valid value for other documents');
    await assertCallbackError('Date', data[eventName].invalid.Date.tomorrow,
      'The date must not be in the future');

    await assertSubmittedEvent('CREATED', {
      header: 'You\'ve confirmed service',
      body: 'Deemed date of service'
    });
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
    });
  },

  requestExtension: async () => {
    eventName = 'REQUEST_EXTENSION';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;
    deleteCaseFields('systemGeneratedCaseDocuments');

    await validateEventPages(data.REQUEST_EXTENSION);

    await assertCallbackError('ProposeDeadline', data[eventName].invalid.ProposeDeadline.past,
      'The proposed deadline must be a date in the future');
    await assertCallbackError('ProposeDeadline', data[eventName].invalid.ProposeDeadline.beforeCurrentDeadline,
      'The proposed deadline must be after the current deadline');

    await assertSubmittedEvent('CREATED', {
      header: 'You asked for extra time to respond',
      body: 'You asked if you can respond before 4pm on'
    });
  },

  respondExtension: async () => {
    eventName = 'RESPOND_EXTENSION';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    await validateEventPages(data.RESPOND_EXTENSION);

    await assertCallbackError('Counter', data[eventName].invalid.Counter.past,
      'The proposed deadline must be a date in the future');
    await assertCallbackError('Counter', data[eventName].invalid.Counter.beforeCurrentDeadline,
      'The proposed deadline must be after the current deadline');

    await assertSubmittedEvent('CREATED', {
      header: 'You\'ve responded to the request for more time',
      body: 'The defendant must respond before 4pm on'
    });
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
    });
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
    });
    await waitForFinishedBusinessProcess(caseId);
  },

  addDefendantLitigationFriend: async () => {
    eventName = 'ADD_DEFENDANT_LITIGATION_FRIEND';
    let returnedCaseData = await apiRequest.startEvent(eventName, caseId);
    assertContainsPopulatedFields(returnedCaseData);
    caseData = returnedCaseData;

    await validateEventPages(data.ADD_DEFENDANT_LITIGATION_FRIEND);
  }
};

const validateEventPages = async (data) => {
  for (let pageId of Object.keys(data.valid)) {
    await assertValidData(data, pageId);
  }
};

const assertValidData = async (data, pageId) => {
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

const assertSubmittedEvent = async (expectedState, submittedCallbackResponseContains) => {
  await apiRequest.startEvent(eventName, caseId);
  const response = await apiRequest.submitEvent(eventName, caseData, caseId);
  const responseBody = await response.json();

  assert.equal(response.status, 201);
  assert.equal(responseBody.state, expectedState);
  assert.equal(responseBody.callback_response_status_code, 200);
  assert.equal(responseBody.after_submit_callback_response.confirmation_header.includes(submittedCallbackResponseContains.header), true);
  assert.equal(responseBody.after_submit_callback_response.confirmation_body.includes(submittedCallbackResponseContains.body), true);

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

function addMidEventFields(pageId, responseBody) {
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

