 
const {
  applicantSolicitorUser,
  defendantSolicitorUser,
  secondDefendantSolicitorUser,
  otherSolicitorUser1, otherSolicitorUser2
} = require('../../../config');
const config = require('../../../config.js');

Feature('Notice of Change on Specified Claim API test @api-noc @api-noc-spec @api-nightly-prod');


Scenario('notice of change - 1v1 - represented defendant', async ({api_spec, noc}) => {
  await api_spec.createClaimWithRepresentedRespondent(applicantSolicitorUser);

  let caseId = await api_spec.getCaseId();

  await noc.requestNoticeOfChangeForApplicant1Solicitor(caseId, secondDefendantSolicitorUser);
  await api_spec.checkUserCaseAccess(applicantSolicitorUser, false);
  await api_spec.checkUserCaseAccess(secondDefendantSolicitorUser, true);

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, otherSolicitorUser1);
  await api_spec.checkUserCaseAccess(defendantSolicitorUser, false);
  await api_spec.checkUserCaseAccess(otherSolicitorUser1, true);
});

Scenario('notice of change - 1v2 - both defendants represented - same to different solicitor', async ({api_spec, noc}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');

  let caseId = await api_spec.getCaseId();

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, secondDefendantSolicitorUser);
  await api_spec.checkUserCaseAccess(secondDefendantSolicitorUser, true);

  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_APPLICANT_INTENTION');
});

Scenario('notice of change - 2v1', async ({api_spec, noc}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'TWO_V_ONE');

  let caseId = await api_spec.getCaseId();

  await noc.requestNoticeOfChangeForApplicant1Solicitor(caseId, secondDefendantSolicitorUser);
  await api_spec.checkUserCaseAccess(applicantSolicitorUser, false);
  await api_spec.checkUserCaseAccess(secondDefendantSolicitorUser, true);

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, otherSolicitorUser1);
  await api_spec.checkUserCaseAccess(defendantSolicitorUser, false);
  await api_spec.checkUserCaseAccess(otherSolicitorUser1, true);
});

