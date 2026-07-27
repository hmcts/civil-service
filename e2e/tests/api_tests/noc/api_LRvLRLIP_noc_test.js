
const {
  applicantSolicitorUser,
  secondDefendantSolicitorUser,
} = require('../../../config');
const config = require('../../../config.js');

Feature('LR v LR LIP spec notice of change api journey').tag('@civil-service-nightly @api-noc');

Scenario('LR v LR LIP spec notice of change', async ({api, noc}) => {
  await api.createClaimWithRespondentLitigantInPerson(applicantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP');
  await api.notifyClaimLip(config.applicantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP');
  await api.notifyClaimDetailsLip(config.applicantSolicitorUser, 'ONE_V_TWO_ONE_LEGAL_REP_ONE_LIP');

  let caseId = await api.getCaseId();

  await noc.requestNoticeOfChangeForRespondent2Solicitor(caseId, secondDefendantSolicitorUser);

  await api.checkUserCaseAccess(secondDefendantSolicitorUser, true);
});

AfterSuite(async  ({api}) => {
  await api.cleanUp();
});