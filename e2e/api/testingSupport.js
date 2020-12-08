const config = require('../config.js');
const restHelper = require('./restHelper');

const {retry} = require('./retryHelper');
let incidentMessage;

const MAX_RETRIES = 300;
const RETRY_TIMEOUT_MS = 1000;

module.exports =  {
  waitForFinishedBusinessProcess: async caseId => {
    const authToken = await restHelper.retriedRequest(
      `${config.url.idamApi}/loginUser?username=${config.solicitorUser.email}&password=${config.solicitorUser.password}`,
      {'Content-Type': 'application/x-www-form-urlencoded'})
      .then(response => response.json()).then(data => data.access_token);

    await retry(() => {
      return restHelper.request(
        `${config.url.unspecService}/testing-support/case/${caseId}/business-process`,
        {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authToken}`,
        }, null, 'GET')
        .then(async response => await response.json()).then(response => {
          let businessProcess = response.businessProcess;
          if (response.incidentMessage) {
            incidentMessage = response.incidentMessage;
          } else if (businessProcess.status !== 'FINISHED') {
            throw new Error(`Ongoing business process: ${businessProcess.camundaEvent}, case id: ${caseId}, status: ${businessProcess.status},`
              + ` process instance: ${businessProcess.processInstanceId}, last finished activity: ${businessProcess.activityId}`);
          }
      });
    }, MAX_RETRIES, RETRY_TIMEOUT_MS);
    if (incidentMessage)
      throw new Error(`Business process failed for case: ${caseId}, incident message: ${incidentMessage}`);
  }
};
