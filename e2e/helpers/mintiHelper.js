const apiRequest = require('./../api/apiRequest.js');
const config = require('../config.js');
const testingSupport = require('./../api/testingSupport');
const chai = require('chai');
const {assert} = chai;

const MaxTrackAmounts = {
  SMALL_CLAIM:  10000,
  FAST_CLAIM: 25000,
  MULTI_CLAIM: 100000
};

const MintiMaxTrackAmounts = {
  SMALL_CLAIM:  10000,
  FAST_CLAIM: 25000,
  INTERMEDIATE_CLAIM: 100000,
  MULTI_CLAIM: 10000000 // infinity
};

function getTrackByClaimAmount(claimAmount) {
  if (claimAmount <= MaxTrackAmounts.SMALL_CLAIM) {
    return 'SMALL_CLAIM';
  } else if (claimAmount > MaxTrackAmounts.SMALL_CLAIM && claimAmount <= MaxTrackAmounts.FAST_CLAIM) {
    return 'FAST_CLAIM';
  } else {
    return 'MULTI_CLAIM';
  }
}

function getMintiTrackByClaimAmount(claimAmount) {
  if (claimAmount <= MintiMaxTrackAmounts.SMALL_CLAIM) {
    return 'SMALL_CLAIM';
  } else if (claimAmount > MintiMaxTrackAmounts.SMALL_CLAIM && claimAmount <= MintiMaxTrackAmounts.FAST_CLAIM) {
    return 'FAST_CLAIM';
  } else if (claimAmount > MintiMaxTrackAmounts.FAST_CLAIM && claimAmount <= MintiMaxTrackAmounts.INTERMEDIATE_CLAIM) {
    return 'INTERMEDIATE_CLAIM';
  } else {
    return 'MULTI_CLAIM';
  }
}

function getCaseAllocatedTrack(case_data, isSpecCase) {
  if (isSpecCase) {
    return case_data.responseClaimTrack;
  } else {
    return case_data.allocatedTrack;
  }
}

module.exports = {
  addSubmittedDateInCaseData: (caseData) => {
    caseData.valid.References.submittedDate = '2025-06-20T15:59:50';
    return caseData;
  },
  adjustCaseSubmittedDateForMinti: async (caseId, isMintiEnabled = false, isCarmEnabled = false) => {
    if (isMintiEnabled) {
      console.log('multi Intermediate track is enabled');
      await apiRequest.setupTokens(config.systemupdate);
      const submittedDate = {'submittedDate':'2025-06-20T15:59:50'};
      await testingSupport.updateCaseData(caseId, submittedDate);
      console.log('submitted date update to after multi Intermediate track live date');
    } else if (!isMintiEnabled && !isCarmEnabled) {
      console.log('multi Intermediate track not enabled, updating submitted date');
      await apiRequest.setupTokens(config.systemupdate);
      const submittedDate = {'submittedDate':'2024-10-28T15:59:50'};
      await testingSupport.updateCaseData(caseId, submittedDate);
      console.log('submitted date update to before multi Intermediate track live date');
    }
  },
  getMintiTrackByClaimAmount(claimAmount) {
    if (claimAmount <= MintiMaxTrackAmounts.SMALL_CLAIM) {
      return 'SMALL_CLAIM';
    } else if (claimAmount > MintiMaxTrackAmounts.SMALL_CLAIM && claimAmount <= MintiMaxTrackAmounts.FAST_CLAIM) {
      return 'FAST_CLAIM';
    } else if (claimAmount > MintiMaxTrackAmounts.FAST_CLAIM && claimAmount <= MintiMaxTrackAmounts.INTERMEDIATE_CLAIM) {
      return 'INTERMEDIATE_CLAIM';
    } else {
      return 'MULTI_CLAIM';
    }
  },

  assertTrackAfterClaimCreation: async (user, caseId, claimAmount, isMintiEnabled, isSpecCase = false) => {
    const {case_data} = await apiRequest.fetchCaseDetails(user, caseId);
    let caseAllocatedTrack = getCaseAllocatedTrack(case_data, isSpecCase);
    assert.equal(caseAllocatedTrack, getMintiTrackByClaimAmount(claimAmount));
    console.log('Allocated track is ' + caseAllocatedTrack);
  }
};
