const config = require('../../../config.js');

Feature('Smoke tests @smoke-tests-spec');

Scenario('Sign in as solicitor user', async ({LRspec}) => {
  await LRspec.retry(5).login(config.applicantSolicitorUser);
  await LRspec.retry(5).see('Case list');
});
