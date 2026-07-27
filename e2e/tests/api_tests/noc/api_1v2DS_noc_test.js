const {
  applicantSolicitorUser,
  defendantSolicitorUser,
  secondDefendantSolicitorUser, 
  otherSolicitorUser2,
} = require('../../../config');

Feature('1vDS spec notice of change api journey').tag('@civil-service-nightly @api-noc');

Scenario('1vDS spec notice of change', async ({api, noc}) => {
  await api.createClaimWithRepresentedRespondent(applicantSolicitorUser, 'ONE_V_TWO_TWO_LEGAL_REP');
  await api.notifyClaim(applicantSolicitorUser);
  await api.notifyClaimDetails(applicantSolicitorUser);

  let caseId = await api.getCaseId();

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, secondDefendantSolicitorUser);
  await api.checkUserCaseAccess(defendantSolicitorUser, false);
  await api.checkUserCaseAccess(secondDefendantSolicitorUser, true);

  await noc.requestNoticeOfChangeForRespondent2Solicitor(caseId, otherSolicitorUser2);
  await api.checkUserCaseAccess(otherSolicitorUser2, true);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});