const assert = require('assert').strict;

const request = require('./request.js');
const testingSupport = require('./testingSupport.js');

const createClaimData = require('../fixtures/createClaim.js');
const confirmServiceData = require('../fixtures/confirmService.js');

let caseId;
let caseData = {};

module.exports = {
  createClaim: async (user) => {
    await request.setupTokens(user);
    await request.startEvent('CREATE_CLAIM');

    await assertValidData('CREATE_CLAIM', 'References', createClaimData.valid.references);
    await assertValidData('CREATE_CLAIM', 'Court', createClaimData.valid.court);
    await assertValidData('CREATE_CLAIM', 'Claimant', createClaimData.valid.claimant);
    await assertValidData('CREATE_CLAIM', 'ClaimantLitigationFriend', createClaimData.valid.applicant1LitigationFriend);
    await assertValidData('CREATE_CLAIM', 'Defendant', createClaimData.valid.defendant);
    await assertValidData('CREATE_CLAIM', 'ClaimType', createClaimData.valid.claimType);
    await assertValidData('CREATE_CLAIM', 'PersonalInjuryType', createClaimData.valid.personalInjuryType);
    await assertValidData('CREATE_CLAIM', 'Upload', createClaimData.valid.upload);
    await assertCallbackError('CREATE_CLAIM', 'ClaimValue', createClaimData.invalid.claimValue,
      'CONTENT TBC: Higher value must not be lower than the lower value.');
    await assertValidData('CREATE_CLAIM', 'ClaimValue', createClaimData.valid.claimValue);
    await assertValidData('CREATE_CLAIM', 'StatementOfTruth', createClaimData.valid.statementOfTruth);

    await assertSubmittedEvent('CREATE_CLAIM', 'CREATED', {
      header: 'Your claim has been issued',
      body: 'Follow these steps to serve a claim'
    });
  },

  confirmService: async () => {
    await testingSupport.resetBusinessProcess(caseId);
    await request.startEvent('CONFIRM_SERVICE', caseId);

    delete caseData.servedDocumentFiles;
    await assertValidData('CONFIRM_SERVICE', 'ServedDocuments', confirmServiceData.valid.servedDocuments);
    await assertValidData('CONFIRM_SERVICE', 'Upload', confirmServiceData.valid.upload);
    await assertValidData('CONFIRM_SERVICE', 'Method', confirmServiceData.valid.method);
    await assertValidData('CONFIRM_SERVICE', 'Location', confirmServiceData.valid.location);
    await assertCallbackError('CONFIRM_SERVICE', 'Date', confirmServiceData.invalid.date.yesterday,
      'The date must not be before issue date of claim');
    await assertCallbackError('CONFIRM_SERVICE', 'Date', confirmServiceData.invalid.date.tomorrow,
      'The date must not be in the future');
    await assertValidData('CONFIRM_SERVICE', 'Date', confirmServiceData.valid.date);
    await assertValidData('CONFIRM_SERVICE', 'StatementOfTruth', confirmServiceData.valid.statementOfTruth);

    await assertSubmittedEvent('CONFIRM_SERVICE', 'CREATED', {
      header: 'You\'ve confirmed service',
      body: 'Deemed date of service'
    });
  }
};

const assertValidData = async (eventName, pageId, eventData, expectedDataSetByCallback = {}) => {
  caseData = Object.assign(caseData, eventData);
  const response = await request.validatePage(eventName, pageId, caseData);
  const responseBody = await response.json();
  caseData = Object.assign(caseData, expectedDataSetByCallback);

  assert.equal(response.status, 200);
  assert.deepEqual(responseBody.data, caseData);
};

const assertCallbackError = async (eventName, pageId, eventData, expectedErrorMessage) => {
  caseData = Object.assign(caseData, eventData);
  const response = await request.validatePage(eventName, pageId, caseData);
  const responseBody = await response.json();

  assert.equal(response.status, 422);
  assert.equal(responseBody.message, 'Unable to proceed because there are one or more callback Errors or Warnings');
  assert.equal(responseBody.callbackErrors[0], expectedErrorMessage);
};

const assertSubmittedEvent = async (eventName, expectedState, submittedCallbackResponse) => {
  const response = await request.submitEvent(eventName, caseData, caseId);
  const responseBody = await response.json();

  assert.equal(response.status, 201);
  assert.equal(responseBody.state, expectedState);
  assert.equal(responseBody.callback_response_status_code, 200);
  assert.equal(responseBody.after_submit_callback_response.confirmation_header.includes(submittedCallbackResponse.header), true);
  assert.equal(responseBody.after_submit_callback_response.confirmation_body.includes(submittedCallbackResponse.body), true);

  caseData = Object.assign(caseData, responseBody.case_data);
  if (eventName === 'CREATE_CLAIM') {
    caseId = responseBody.id;
    console.log('Case created: ' + caseId);
  }
};
