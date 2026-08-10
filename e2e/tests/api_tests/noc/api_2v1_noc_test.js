
const {
  applicantSolicitorUser,
  secondDefendantSolicitorUser,
  otherSolicitorUser1
} = require('../../../config');

Feature('2v1 unspec notice of change api journey').tag('@civil-service-nightly @api-noc');

Scenario.skip('2v1 unspec notice of change', async ({api, noc}) => {
  await api.createClaimWithRepresentedRespondent(applicantSolicitorUser, 'TWO_V_ONE');
  await api.notifyClaim(applicantSolicitorUser);
  await api.notifyClaimDetails(applicantSolicitorUser);

  let caseId = await api.getCaseId();

  await noc.requestNoticeOfChangeForApplicant1Solicitor(caseId, secondDefendantSolicitorUser);
  await api.checkUserCaseAccess(applicantSolicitorUser, false);
  await api.checkUserCaseAccess(secondDefendantSolicitorUser, true);

  await noc.requestNoticeOfChangeForApplicant2Solicitor(caseId, otherSolicitorUser1);
  await api.checkUserCaseAccess(secondDefendantSolicitorUser, false);
  await api.checkUserCaseAccess(otherSolicitorUser1, true);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});