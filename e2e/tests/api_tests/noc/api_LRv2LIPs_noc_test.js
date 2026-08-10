
const {
  defendantSolicitorUser,
  secondDefendantSolicitorUser
} = require('../../../config');
const config = require('../../../config.js');

Feature('Unspecified Notice of Change on Unpecified Claim API test').tag('@civil-service-nightly @api-noc');

Scenario('notice of change - 1v2 - both respondents LiPs to same solicitor', async ({api, noc}) => {
  await api.createClaimWithRespondentLitigantInPerson(config.applicantSolicitorUser, 'ONE_V_TWO_LIPS');
  await api.notifyClaimLip(config.applicantSolicitorUser, 'ONE_V_TWO_LIPS');
  await api.notifyClaimDetailsLip(config.applicantSolicitorUser, 'ONE_V_TWO_LIPS');

  let caseId = await api.getCaseId();

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, defendantSolicitorUser);
  await noc.requestNoticeOfChangeForRespondent2Solicitor(caseId, defendantSolicitorUser);

  await api.checkUserCaseAccess(defendantSolicitorUser, true);

  await api.defendantResponse(config.defendantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP');
  await api.claimantResponse(config.applicantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP', 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO');
});

Scenario('notice of change - 1v2 - both respondents LiPs to diff solicitor', async ({api, noc}) => {
  await api.createClaimWithRespondentLitigantInPerson(config.applicantSolicitorUser, 'ONE_V_TWO_LIPS');
  await api.notifyClaimLip(config.applicantSolicitorUser, 'ONE_V_TWO_LIPS');
  await api.notifyClaimDetailsLip(config.applicantSolicitorUser, 'ONE_V_TWO_LIPS');

  let caseId = await api.getCaseId();

  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, defendantSolicitorUser);
  await noc.requestNoticeOfChangeForRespondent2Solicitor(caseId, secondDefendantSolicitorUser);

  await api.checkUserCaseAccess(defendantSolicitorUser, true);
  await api.checkUserCaseAccess(secondDefendantSolicitorUser, true);

  await api.defendantResponse(config.defendantSolicitorUser, 'ONE_V_TWO_TWO_LEGAL_REP', 'solicitorOne');
  await api.defendantResponse(config.secondDefendantSolicitorUser, 'ONE_V_TWO_TWO_LEGAL_REP', 'solicitorTwo');
  await api.claimantResponse(config.applicantSolicitorUser, 'ONE_V_TWO_TWO_LEGAL_REP', 'AWAITING_APPLICANT_INTENTION', 'FOR_SDO');
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});