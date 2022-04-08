const config = require('../config.js');
const restHelper = require('./restHelper');
const NodeCache = require('node-cache');
//Idam access token expires for every 8 hrs
const idamTokenCache = new NodeCache({ stdTTL: 25200, checkperiod: 1800 });

const loginEndpoint = config.idamStub.enabled ? 'oauth2/token' : 'loginUser';
const idamUrl = config.idamStub.enabled ? config.idamStub.url : config.url.idamApi;

async function getAccessTokenFromIdam(user) {
    return restHelper.retriedRequest(
            `${idamUrl}/${loginEndpoint}?username=${encodeURIComponent(user.email)}&password=${user.password}`, { 'Content-Type': 'application/x-www-form-urlencoded' })
        .then(response => response.json()).then(data => data.access_token);
}

async function accessToken(user) {
    console.log('User logged in', user.email);
    if (idamTokenCache.get(user.email) != null) {
        console.log('User access token coming from cache', user.email);
        return idamTokenCache.get(user.email);
    } else {
        const accessToken = await getAccessTokenFromIdam(user);
        idamTokenCache.set(user.email, accessToken);
        console.log('user access token coming from idam', user.email);
        return accessToken;
    }
}

async function userId(authToken) {
    return restHelper.retriedRequest(
            `${idamUrl}/o/userinfo`, {
                'Content-Type': 'application/x-www-form-urlencoded',
                'Authorization': `Bearer ${authToken}`
            })
        .then(response => response.json()).then(data => data.uid);
}

module.exports = {
    accessToken,
    userId
};
