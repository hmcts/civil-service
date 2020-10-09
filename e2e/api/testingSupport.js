const config = require('../config.js');
const restHelper = require('./restHelper');

module.exports =  {
  resetBusinessProcess: async caseId => {
    const authToken = await restHelper.request(
      `${config.url.idamApi}/loginUser?username=${config.solicitorUser.email}&password=${config.solicitorUser.password}`,
      {'Content-Type': 'application/x-www-form-urlencoded'})
      .then(response => response.json()).then(data => data.access_token);

    await restHelper.request(
      `${config.url.unspecService}/testing-support/case/${caseId}/business-process/reset`,
      {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`,
      });
  }
};
