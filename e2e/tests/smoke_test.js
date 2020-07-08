const config = require('../config.js');

Feature('Smoke tests @smoke-tests');

Scenario('Sign in as solicitor user', async (I) => {
  await I.login(config.solicitorUser);
  await I.see('Case List');
});
