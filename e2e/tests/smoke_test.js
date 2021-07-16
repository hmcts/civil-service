const config = require('../config.js');

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as solicitor user', async (I) => {
  await I.login(config.applicantSolicitorUser);
  await I.see('Case list');
});
