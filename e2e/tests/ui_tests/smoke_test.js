const config = require('../../config.js');

Feature('Smoke tests @smoke-tests-unspec');

Scenario('Sign in as solicitor user', async ({I}) => {
  await I.retry(5).login(config.applicantSolicitorUser);
  await I.retry(5).see('Case list');
}).retry(2);
