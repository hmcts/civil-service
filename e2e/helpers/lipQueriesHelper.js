const apiRequest = require('./../api/apiRequest.js');
const config = require('../config.js');
const testingSupport = require('./../api/testingSupport');

module.exports = {
  adjustCaseSubmittedDateForPublicQueries: async (caseId, publicQueriesEnabled = false) => {
    if (publicQueriesEnabled) {
      console.log('public queries enabled, updating submitted date');
      await apiRequest.setupTokens(config.systemupdate);
      const submittedDate = {'submittedDate':'2025-09-18T00:12:50'};
      await testingSupport.updateCaseData(caseId, submittedDate);
      console.log('submitted date update to after qm date');
    }
  },
};
