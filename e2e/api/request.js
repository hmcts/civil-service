const config = require('../config.js');

const restHelper = require('../api/restHelper.js');
const totp = require('totp-generator');

const tokens = {};
const getCcdDataStoreBaseUrl = () => `${config.url.ccdDataStore}/caseworkers/${tokens.userId}/jurisdictions/${config.definition.jurisdiction}/case-types/${config.definition.caseType}`;
const getRequestHeaders = () => {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${tokens.userAuth}`,
    'ServiceAuthorization': tokens.s2sAuth
  };
};

module.exports = {
  setupTokens: async (user) => {
    tokens.userAuth = await restHelper.request(
      `${config.url.idamApi}/loginUser?username=${user.email}&password=${user.password}`,
      {'Content-Type': 'application/x-www-form-urlencoded'})
      .then(response => response.json()).then(data => data.access_token);

    tokens.userId = await restHelper.request(
      `${config.url.idamApi}/o/userinfo`,
      {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': `Bearer ${tokens.userAuth}`
      })
      .then(response => response.json()).then(data => data.uid);

    tokens.s2sAuth = await restHelper.request(
      `${config.url.authProviderApi}/lease`,
      {'Content-Type': 'application/json'},
      {
        microservice: config.s2s.microservice,
        oneTimePassword: totp(config.s2s.secret)
      })
      .then(response => response.text());
  },

  startEvent: async (eventName, caseId) => {
    let url = getCcdDataStoreBaseUrl();
    if (caseId) {
      url += `/cases/${caseId}`;
    }
    url += `/event-triggers/${eventName}/token`;

    tokens.ccdEvent = await restHelper.request(url, getRequestHeaders(), null, 'GET')
      .then(response => response.json()).then(data => data.token);
  },

  validatePage: async (eventName, pageId, caseData) => {
    return restHelper.request(`${getCcdDataStoreBaseUrl()}/validate?pageId=${eventName}${pageId}`, getRequestHeaders(),
      {
        data: caseData,
        event: {id: eventName},
        event_data: caseData,
        event_token: tokens.ccdEvent
      }
    );
  },

  submitEvent: async (eventName, caseData, caseId) => {
    let url = `${getCcdDataStoreBaseUrl()}/cases`;
    if (caseId) {
      url += `/${caseId}/events`;
    }

    return restHelper.request(url, getRequestHeaders(),
      {
        data: caseData,
        event: {id: eventName},
        event_data: caseData,
        event_token: tokens.ccdEvent
      });
  }
};
