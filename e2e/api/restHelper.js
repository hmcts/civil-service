const fetch = require('node-fetch');

const {retry} = require('./retryHelper');

const request = (url, headers, body, method = 'POST') => fetch(url, {
  method: method,
  body: body ? JSON.stringify(body) : undefined,
  headers: headers
});

const retriedRequest = async (url, headers, body, method = 'POST', expectedStatus = 200) => {
  return retry(() => {
    return request(url, headers, body, method).then(response => {
      if (response.status !== expectedStatus) {
        throw new Error(`Expected status: ${expectedStatus}, actual status: ${response.status}, `
          + `message: ${response.statusText}, url: ${response.url}`);
      }
      return response;
    });
  });
};

module.exports = {request, retriedRequest};
