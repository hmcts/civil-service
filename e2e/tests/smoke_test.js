/* global process */
const config = require('../config.js');
const baseUrl = process.env.URL || 'http://localhost:3333';

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as solicitor user', async (I, loginPage) => {
  I.amOnPage(baseUrl);
  loginPage.signIn(config.solicitorUser);
  await I.see('Case List');
});
