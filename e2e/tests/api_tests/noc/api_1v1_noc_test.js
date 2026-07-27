
const {
  applicantSolicitorUser,
  defendantSolicitorUser,
  secondDefendantSolicitorUser,
  otherSolicitorUser1,
} = require('../../../config');

Feature('1v1 unspec notice of change api journey').tag('@civil-service-nightly @api-noc');

Scenario('1v1 unspec notice of change', async ({api, noc}) => {
  await api.createClaimWithRepresentedRespondent(applicantSolicitorUser);
  await api.notifyClaim(applicantSolicitorUser);
  await api.notifyClaimDetails(applicantSolicitorUser);

  let caseId = await api.getCaseId();

  await noc.requestNoticeOfChangeForApplicant1Solicitor(caseId, secondDefendantSolicitorUser);
  await api.checkUserCaseAccess(applicantSolicitorUser, false);
  await api.checkUserCaseAccess(secondDefendantSolicitorUser, true);

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, otherSolicitorUser1);
  await api.checkUserCaseAccess(defendantSolicitorUser, false);
  await api.checkUserCaseAccess(otherSolicitorUser1, true);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});
