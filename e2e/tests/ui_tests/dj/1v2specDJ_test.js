

const config = require('../../../config.js');

Feature('1v2 spec defaultJudgement').tag('@ui-dj');

Scenario('1v2 create spec claim, request default judgment', async ({I, api_spec}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO');

  let caseid = await api_spec.getCaseId();

  await api_spec.amendRespondent1ResponseDeadline(config.systemupdate);
  await I.login(config.applicantSolicitorUser);
  await I.initiateDJSpec(caseid, 'ONE_V_TWO', 'SPEC');

}).retry(2);

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
