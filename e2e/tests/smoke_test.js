/* global process */
const config = require('../config.js');
const baseUrl = process.env.URL || 'http://localhost:3333';

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as solicitor user', (I, loginPage) => {
  I.amOnPage(baseUrl);
  loginPage.signIn(config.solicitorUser);
  I.see('Case List');
});
