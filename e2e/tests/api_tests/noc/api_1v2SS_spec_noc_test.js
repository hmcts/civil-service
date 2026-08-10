 const {
  secondDefendantSolicitorUser,
} = require('../../../config');
const config = require('../../../config.js');

Feature('1vSS spec notice of change api journey').tag('@civil-service-nightly @api-noc');

Scenario('1vSS spec notice of change', async ({api_spec, noc}) => {
  await api_spec.createClaimWithRepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_TWO_SAME_SOL');

  let caseId = await api_spec.getCaseId();

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, secondDefendantSolicitorUser);
  await api_spec.checkUserCaseAccess(secondDefendantSolicitorUser, true);

  await api_spec.defendantResponse(config.secondDefendantSolicitorUser, 'FULL_DEFENCE1', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_RESPONDENT_ACKNOWLEDGEMENT');
  await api_spec.defendantResponse(config.defendantSolicitorUser, 'FULL_DEFENCE2', 'ONE_V_ONE_DIF_SOL',
    'AWAITING_APPLICANT_INTENTION');
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});