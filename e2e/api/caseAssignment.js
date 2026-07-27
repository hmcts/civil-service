const idamHelper = require('./idamHelper');
const config = require('../config.js');
const restHelper = require('./restHelper');
const {TOTP} = require('totp-generator');

let serviceAuth;

const authToken = async(user) => {
  return await idamHelper.accessToken(user);
};

const serviceToken = async() => {
  // Should be able to utilise the same service token
  // so storing it
  if(!serviceAuth) {
    serviceAuth = await restHelper.retriedRequest(
      `${config.url.authProviderApi}/lease`,
      {'Content-Type': 'application/json'},
      {
        microservice: config.s2s.microservice,
        oneTimePassword: TOTP.generate(config.s2s.secret).otp
      })
      .then(response => response.text());
  }

  return serviceAuth;
};

const getRequestHeaders = async (user) => {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${await authToken(user)}`,
    'ServiceAuthorization': `Bearer ${await serviceToken()}`
  };
};

module.exports = {
  getNocQuestions: async (caseId, user) => {
    const headers = await getRequestHeaders(user);
    return restHelper.request(
      `${config.url.caseAssignmentService}/noc/noc-questions?case_id=${caseId}`, headers, null, 'GET')
      .then(async response => ({status: response.status, body: response.json()}));
  },
  validateNocAnswers: async (caseId, answers, user) => {
    const headers = await getRequestHeaders(user);
    return restHelper.request(
      `${config.url.caseAssignmentService}/noc/verify-noc-answers`, headers, {case_id: caseId, answers}, 'POST')
      .then(async response => ({status: response.status, body: response.json()}));
  },
  submitNocRequest: async (caseId, answers, user) => {
   const headers = await getRequestHeaders(user);
    return restHelper.request(
      `${config.url.caseAssignmentService}/noc/noc-requests`, headers, {case_id: caseId, answers}, 'POST')
      .then(async response => ({status: response.status, body: response.json()}));
  }
};


