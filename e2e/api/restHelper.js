const config = require('../config.js');

const fetch = require('node-fetch');
const HttpProxyAgent = require('http-proxy-agent');
const HttpsProxyAgent = require('https-proxy-agent');

const PROXY_SERVER = `http://${config.proxyServer}`;
const PROXY_AGENT = url => config.proxyServer
  ? url.startsWith('http:')
    ? new HttpProxyAgent(PROXY_SERVER)
    : new HttpsProxyAgent(PROXY_SERVER)
  : null;

module.exports = {
  request: async (url, headers, body, method = 'POST') => {
    return fetch(url, {
      method: method,
      body: body ? JSON.stringify(body) : undefined,
      headers: headers,
      agent: PROXY_AGENT(url)
    });
  }
};
