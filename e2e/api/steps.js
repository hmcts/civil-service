const assert = require('assert').strict;

const request = require('./request.js');

const data = {
  CREATE_CLAIM: require('../fixtures/events/createClaim.js'),
  CONFIRM_SERVICE: require('../fixtures/events/confirmService.js'),
  ACKNOWLEDGE_SERVICE: require('../fixtures/events/acknowledgeService.js'),
  REQUEST_EXTENSION: require('../fixtures/events/requestExtension.js'),
  RESPOND_EXTENSION: require('../fixtures/events/respondExtension.js'),
  DEFENDANT_RESPONSE: require('../fixtures/events/defendantResponse.js'),
  CLAIMANT_RESPONSE: require('../fixtures/events/claimantResponse.js'),
  ADD_DEFENDANT_LITIGATION_FRIEND: require('../fixtures/events/addDefendantLitigationFriend.js'),
};

let caseId, eventName;
let caseData = {};

module.exports = {
  createClaim: async (user) => {
    eventName = 'CREATE_CLAIM';
    await request.setupTokens(user);
    await request.startEvent(eventName);

    await validateEventPages();

    await assertSubmittedEvent('CREATED', {
      header: 'Your claim has been issued',
      body: 'Follow these steps to serve a claim'
    });
  },

  confirmService: async () => {
    eventName = 'CONFIRM_SERVICE';
    await request.startEvent(eventName, caseId);
    deleteCaseFields('servedDocumentFiles');

    await validateEventPages();

    await assertCallbackError('ServedDocuments', data[eventName].invalid.ServedDocuments.blankOtherDocuments,
      'CONTENT TBC: please enter a valid value for other documents');
    await assertCallbackError('Date', data[eventName].invalid.Date.threeDaysBeforeToday,
      'The date must not be before issue date of claim');
    await assertCallbackError('Date', data[eventName].invalid.Date.tomorrow,
      'The date must not be in the future');

    await assertSubmittedEvent('CREATED', {
      header: 'You\'ve confirmed service',
      body: 'Deemed date of service'
    });
  },

  acknowledgeService: async () => {
    eventName = 'ACKNOWLEDGE_SERVICE';
    deleteCaseFields('systemGeneratedCaseDocuments');
    await request.startEvent(eventName, caseId);

    await validateEventPages();

    await assertCallbackError('ConfirmDetails', data[eventName].invalid.ConfirmDetails.futureDateOfBirth,
      'The date entered cannot be in the future');

    await assertSubmittedEvent('CREATED', {
      header: 'You\'ve acknowledged service',
      body: 'You need to respond before'
    });
  },

  requestExtension: async () => {
    eventName = 'REQUEST_EXTENSION';
    await request.startEvent(eventName, caseId);

    await validateEventPages();

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
    await request.startEvent(eventName, caseId);

    await validateEventPages();

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
    await request.startEvent(eventName, caseId);
    deleteCaseFields('respondent1', 'solicitorReferences');

    await validateEventPages();

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
    await request.startEvent(eventName, caseId);

    await validateEventPages();

    await assertCallbackError('Hearing', data[eventName].invalid.Hearing.past,
      'The date cannot be in the past and must not be more than a year in the future');
    await assertCallbackError('Hearing', data[eventName].invalid.Hearing.moreThanYear,
      'The date cannot be in the past and must not be more than a year in the future');

    await assertSubmittedEvent('STAYED', {
      header: 'You\'ve decided to proceed with the claim',
      body: 'We\'ll review the case. We\'ll contact you to tell you what to do next.'
    });
  },

  addDefendantLitigationFriend: async () => {
    eventName = 'ADD_DEFENDANT_LITIGATION_FRIEND';
    await request.startEvent(eventName, caseId);

    await validateEventPages();
  }
};


const validateEventPages = async () => {
  for (let pageId of Object.keys(data[eventName].valid)) {
    await assertValidData(pageId);
  }
};

const assertValidData = async (pageId) => {
  const validDataForPage = data[eventName].valid[pageId];
  caseData = {...caseData, ...validDataForPage};

  const response = await request.validatePage(eventName, pageId, caseData);
  const responseBody = await response.json();

  if (response.status !== 200) {
    console.log(responseBody);
  }

  assert.equal(response.status, 200);
  assert.deepEqual(responseBody.data, caseData);
};

const assertCallbackError = async (pageId, eventData, expectedErrorMessage) => {
  const response = await request.validatePage(eventName, pageId, {...caseData, ...eventData}, 422);
  const responseBody = await response.json();

  assert.equal(response.status, 422);
  assert.equal(responseBody.message, 'Unable to proceed because there are one or more callback Errors or Warnings');
  assert.equal(responseBody.callbackErrors[0], expectedErrorMessage);
};

const assertSubmittedEvent = async (expectedState, submittedCallbackResponseContains) => {
  await request.startEvent(eventName, caseId);
  const response = await request.submitEvent(eventName, caseData, caseId);
  const responseBody = await response.json();

  if (response.status !== 201) {
    console.log(responseBody);
  }

  assert.equal(response.status, 201);
  assert.equal(responseBody.state, expectedState);
  assert.equal(responseBody.callback_response_status_code, 200);
  assert.equal(responseBody.after_submit_callback_response.confirmation_header.includes(submittedCallbackResponseContains.header), true);
  assert.equal(responseBody.after_submit_callback_response.confirmation_body.includes(submittedCallbackResponseContains.body), true);

  caseData = {...caseData, ...responseBody.case_data};
  if (eventName === 'CREATE_CLAIM') {
    caseId = responseBody.id;
    console.log('Case created: ' + caseId);
  }
};

// Mid event will not return case fields that were already filled in another event if they're present on currently processed event.
// This happens until these case fields are set again as a part of current event (note that this data is not removed from the case).
// Therefore these case fields need to be removed from caseData, as caseData object is used to make assertions
const deleteCaseFields = (...caseFields) => {
  caseFields.forEach(caseField => delete caseData[caseField]);
};
