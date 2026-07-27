
const {
  applicantSolicitorUser,
  defendantSolicitorUser,
  secondDefendantSolicitorUser,
  otherSolicitorUser1
} = require('../../../config');

Feature('Unspecified Notice of Change on Unpecified Claim API test').tag('@civil-service-nightly @api-noc');

Scenario.skip('notice of change - 1v2 - same solicitor to diff solicitor', async ({api, noc}) => {
  await api.createClaimWithRepresentedRespondent(applicantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP');
  await api.notifyClaim(applicantSolicitorUser);
  await api.notifyClaimDetails(applicantSolicitorUser);

  let caseId = await api.getCaseId();

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, secondDefendantSolicitorUser);
  await api.checkUserCaseAccess(defendantSolicitorUser, true);
  await api.checkUserCaseAccess(secondDefendantSolicitorUser, true);

  await noc.requestNoticeOfChangeForRespondent2Solicitor(caseId, otherSolicitorUser1);
  await api.checkUserCaseAccess(secondDefendantSolicitorUser, true);
  await api.checkUserCaseAccess(otherSolicitorUser1, true);
  await api.checkUserCaseAccess(defendantSolicitorUser, false);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});