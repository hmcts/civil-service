const deepEqualInAnyOrder = require('deep-equal-in-any-order');
const chai = require('chai');

chai.use(deepEqualInAnyOrder);
const {expect, assert} = chai;

const apiRequest = require('./apiRequest.js');
const bulkClaimData = require('../fixtures/events/createBulkClaim.js');
const {waitForFinishedBusinessProcess} = require('./testingSupport');
const config = require('../config');
const {addUserCaseMapping} = require('./caseRoleAssignmentHelper');
const nonProdExpectedEvents = require('../fixtures/ccd/nonProdExpectedEventsLRSpec');
const claimDataBulk = require('../fixtures/events/createClaimSpecBulk');
const claimData = require('../fixtures/events/createClaim');

let caseId, eventName;

const data = {
  CREATE_BULK_CLAIM: (mpScenario, interest, customerId, amount, postcodeValidation) => bulkClaimData.bulkCreateClaimDto(mpScenario, interest, customerId, amount, postcodeValidation),
  CREATE_BULK_CLAIM_VIA_CIVILSERVICE: (scenario, withInterest) => claimDataBulk.createClaimBulk(scenario, withInterest),

};

module.exports = {
  /**
   * Creates a Bulk claim via SDT
   *
   * @param user user to create the claim
   * @param mpScenario
   * @param interest
   * @return {Promise<void>}
   */
  /*createClaimFromSDTRequest: async (user, mpScenario, interest) => {
    let createClaimData;

    createClaimData = data.CREATE_BULK_CLAIM(mpScenario, interest, '12345678', '87989');
    //==============================================================

    await apiRequest.setupTokens(user);
    const response_msg = await apiRequest.createBulkClaim('112345', createClaimData);
    assert.equal(response_msg.errorText, 'Unknown User, ');
    assert.equal(response_msg.errorCode, '001');
  },*/

  createNewClaimWithCaseworkerCivilService: async (user, scenario, withInterest) => {
    eventName = 'CREATE_CLAIM_SPEC';
    caseId = null;
    let createClaimData  = {};

    createClaimData = data.CREATE_BULK_CLAIM_VIA_CIVILSERVICE(scenario, withInterest);
    await apiRequest.setupTokens(user);
    await assertCaseworkerSubmittedNewClaim('PENDING_CASE_ISSUED', createClaimData);
    await waitForFinishedBusinessProcess(caseId);
    console.log('Bulk claim created with case id: ' + caseId);
    if (scenario === 'ONE_V_ONE') {
      await assertCorrectEventsAreAvailableToUserBulkClaims(config.bulkClaimSystemUser, 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
      await apiRequest.paymentUpdate(caseId, '/service-request-update-claim-issued',
          claimData.serviceUpdateDto(caseId, 'paid'));
      console.log('Service request update sent to callback URL');
    } else {
      // one v two/multiparty continuing online not currently supported for LiPs
      await assertCorrectEventsAreAvailableToUserBulkClaims(config.bulkClaimSystemUser, 'PROCEEDS_IN_HERITAGE_SYSTEM');
    }
  },

  createClaimFromSDTRequestForPostCodeNegative: async (user, mpScenario, interest) => {
    let createClaimData;

    createClaimData = data.CREATE_BULK_CLAIM(mpScenario, interest, '12345678', '87989', '4DA');
    //==============================================================

    await apiRequest.setupTokens(user);
    const response_msg = await apiRequest.createBulkClaim('32321323', createClaimData);
   assert.equal(response_msg.errorText, ' First defendant’s postcode is not in England or Wales, ');
   assert.equal(response_msg.errorCode, '008');
  },

  createClaimFromSDTRequestValidSuccessSyncResponse: async (user, mpScenario, interest) => {
    let createClaimData;
    createClaimData = data.CREATE_BULK_CLAIM(mpScenario, interest, '12345678', '87989', 'TW13 4DA');

    await apiRequest.setupTokens(user);
    const response_msg = await apiRequest.createBulkClaimForStatusCode201(Math.random().toString(36).slice(2), createClaimData);

    assert.equal(response_msg.issueDate, null);
    assert.equal(response_msg.serviceDate, null);
    assert.notEqual(response_msg.claimNumber, null);
  },

  createClaimFromSDTRequestForPostCodePositive: async (user, mpScenario, interest) => {
    let createClaimData;

    createClaimData = data.CREATE_BULK_CLAIM(mpScenario, interest, '12345678', '87989', 'TW13 4DA');

    await apiRequest.setupTokens(user);
    const response_msg = await apiRequest.createBulkClaimForStatusCode201(Math.random().toString(36).slice(2), createClaimData);
    assert.equal(response_msg.issueDate, null);
  },

  createClaimFromSDTRequestForDuplicateCaseCheckCall: async (user, mpScenario, interest) => {
    let createClaimData;

    createClaimData = data.CREATE_BULK_CLAIM(mpScenario, interest, '12345678', '87989', 'TW13 4DA');

    await apiRequest.setupTokens(user);
    const response_msg = await apiRequest.createBulkClaim('12345678', createClaimData);
    assert.equal(response_msg.errorText, 'Bad data, Request already processed');
    assert.equal(response_msg.errorCode, '000');
  },
};

const assertCaseworkerSubmittedNewClaim = async (expectedState, caseData) => {
  const response = await apiRequest.submitNewClaimAsCaseworker(eventName, caseData);
  const responseBody = await response.json();
  assert.equal(response.status, 201);
  assert.equal(responseBody.state, expectedState);

  if (eventName === 'CREATE_CLAIM_SPEC') {
    caseId = responseBody.id;
    await addUserCaseMapping(caseId, config.applicantSolicitorUser);
    console.log('Case created: ' + caseId);
  }
};

const assertCorrectEventsAreAvailableToUserBulkClaims = async (user, state) => {
  console.log(`Bulk claim: Asserting user ${user.type} in env ${config.runningEnv} has correct permissions`);
  const caseForDisplay = await apiRequest.fetchCaseForDisplay(user, caseId);
  if (['preview', 'demo'].includes(config.runningEnv)) {
    expect(caseForDisplay.triggers).to.deep.include.members(nonProdExpectedEvents[user.type][state],
        'Unexpected events for state ' + state + ' and user type ' + user.type);
  }
};
