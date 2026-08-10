

const config = require('../../../config.js');

Feature('1v1 spec default judgment')
  .tag('@civil-ccd-master @civil-ccd-pr @civil-ccd-nightly @ui-dj');

Scenario('1v1 create spec claim request default judgment', async ({I, api_spec}) => {

  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser);
  let caseid = await api_spec.getCaseId();
  await api_spec.amendRespondent1ResponseDeadline(config.systemupdate);

  await I.login(config.applicantSolicitorUser);
  await I.initiateDJSpec(caseid, 'ONE_V_ONE', 'SPEC');

}).retry(2);

AfterSuite(async  ({api_spec}) => {
  await api_spec.cleanUp();
});
