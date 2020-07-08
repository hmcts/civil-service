const config = require('../config.js');

Feature('Claim creation @create-claim');

Scenario('Solicitor creates claim', async (I) => {
  await I.login(config.solicitorUser);
  await I.createCase();

  let caseNumber = await I.grabCaseNumber();
  await I.see('Case ' + caseNumber + ' has been created.');
});
