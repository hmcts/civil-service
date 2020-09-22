const config = require('../config.js');

Feature('CCD API tests @api-tests');

Scenario('Create claim', async (api) => {
  await api.createClaim(config.solicitorUser);
});

Scenario('Confirm service', async (api) => {
  await api.confirmService();
});
