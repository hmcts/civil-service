const {
  applicantSolicitorUser,
  defendantSolicitorUser,
  secondDefendantSolicitorUser,
  otherSolicitorUser1
} = require('../../../config');
const config = require('../../../config.js');

Feature('2v1 spec notice of change api journey').tag('@civil-service-nightly @api-noc');

Scenario('2v1 spec notice of change', async ({api_spec, noc}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');

  let caseId = await api_spec.getCaseId();

  await noc.requestNoticeOfChangeForApplicant1Solicitor(caseId, secondDefendantSolicitorUser);
  await api_spec.checkUserCaseAccess(applicantSolicitorUser, false);
  await api_spec.checkUserCaseAccess(secondDefendantSolicitorUser, true);

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, otherSolicitorUser1);
  await api_spec.checkUserCaseAccess(defendantSolicitorUser, false);
  await api_spec.checkUserCaseAccess(otherSolicitorUser1, true);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});