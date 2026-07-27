const restHelper = require('./restHelper');
const config = require('../config');
const {TOTP} = require('totp-generator');
const {s2sForXUI, s2s} = require('../config');
const NodeCache = require('node-cache');

// S2S tokens are typically valid for 1-2 hours
// stdTTL: 3600 seconds (1 hour), checkperiod: 300 seconds (5 minutes)
const s2sTokenCache = new NodeCache({ stdTTL: 3600, checkperiod: 300 });

const getS2sToken = async ({microservice, secret}) => {
  // Check cache first
  const cacheKey = microservice;
  const cachedToken = s2sTokenCache.get(cacheKey);
  if (cachedToken != null) {
    console.log(`S2S token for ${microservice} retrieved from cache`);
    return cachedToken;
  }

  // Token not in cache, fetch from auth provider
  console.log(`Fetching S2S token for ${microservice}...`);
  const token = await restHelper.retriedRequest(
    `${config.url.authProviderApi}/lease`,
    {'Content-Type': 'application/json'},
    {
      microservice: microservice,
      oneTimePassword: TOTP.generate(secret).otp
    })
    .then(response => response.text());
  
  // Cache the token for future use
  s2sTokenCache.set(cacheKey, token);
  console.log(`S2S token for ${microservice} fetched and cached`);
  
  return token;
};

module.exports = {
  civilServiceAuth: ()  => {
    return getS2sToken(s2s);
  },
  xuiAuth: ()  => {
    return getS2sToken(s2sForXUI);
  },
};
