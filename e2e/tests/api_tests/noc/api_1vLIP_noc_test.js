const {
  applicantSolicitorUser, 
  otherSolicitorUser2,
} = require('../../../config');
const config = require('../../../config.js');

Feature('1vLIP unspec notice of change api journey').tag('@civil-service-nightly @api-noc');

Scenario('1vLIP unspec notice of change', async ({api, noc}) => {
  await api.createClaimWithRespondentLitigantInPerson(applicantSolicitorUser, 'ONE_V_ONE');

  let caseId = await api.getCaseId();

  await api.notifyClaimLip(config.applicantSolicitorUser, 'ONE_V_ONE');
  await api.notifyClaimDetailsLip(config.applicantSolicitorUser, 'ONE_V_ONE');

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, otherSolicitorUser2);

  await api.checkUserCaseAccess(otherSolicitorUser2, true);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
