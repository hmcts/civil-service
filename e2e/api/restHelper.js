


const {retry} = require('./retryHelper');

const request = (url, headers, body, method = 'POST') =>  fetch(url, {
    method: method,
    body: body ? JSON.stringify(body) : undefined,
    // native fetch (undici) throws on `headers: null`; node-fetch tolerated it. Normalise null -> undefined.
    headers: headers || undefined,
    keepalive: true
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

const retriedRequestFor201 = async (url, headers, body, method = 'POST', expectedStatus = 201) => {
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

const retriedRequestFor400 = async (url, headers, body, method = 'POST', expectedStatus = 400) => {
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

const retriedJsonRequest = async (url, headers, body, method = 'POST', expectedStatus = 200) => {
  return retry(() => {
    return request(url, headers, body, method).then(async response => {
      if (response.status !== expectedStatus) {
        throw new Error(`Expected status: ${expectedStatus}, actual status: ${response.status}, `
          + `message: ${response.statusText}, url: ${response.url}`);
      }

      const responseText = await response.text();
      let responseBody;

      try {
        responseBody = JSON.parse(responseText);
      } catch (err) {
        throw new Error(`Failed to parse JSON response from ${response.url}: ${err.message}`);
      }

      return {
        status: response.status,
        statusText: response.statusText,
        url: response.url,
        headers: response.headers,
        json: async () => responseBody,
        text: async () => JSON.stringify(responseBody)
      };
    });
  });
};

module.exports = {request, retriedRequest, retriedRequestFor400, retriedRequestFor201, retriedJsonRequest};
