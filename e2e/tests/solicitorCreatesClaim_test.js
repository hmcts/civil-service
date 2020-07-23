const config = require('../config.js');

Feature('Claim creation');

Scenario('Solicitor creates claim @create-claim', async (I) => {
  await I.login(config.solicitorUser);
  await I.createCase();

  let caseNumber = await I.grabCaseNumber();
  await I.see('Case ' + caseNumber + ' has been created.');
});

Scenario('Solicitor confirms service', async (I) => {
  await I.confirmService();
  await I.see('updated with event: Confirm service');
});

Scenario('Solicitor requests extension', async (I) => {
  await I.requestExtension();
  await I.see('updated with event: Request extension');
});
