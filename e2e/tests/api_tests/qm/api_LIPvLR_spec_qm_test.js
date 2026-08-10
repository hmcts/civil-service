const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');
const { PUBLIC_QUERY} = require('../../../fixtures/queryTypes');
const {respondToQueryCTSCTask } = require('../../../fixtures/wa/respondToQueryTasks');
const {adjustCaseSubmittedDateForPublicQueries} = require('../../../helpers/lipQueriesHelper');

const claimType = 'SmallClaims';
let caseId;

Feature('1v1 LIP v LIP and LR v LIP spec api journeys').tag('@civil-service-nightly');

Before(async () => {
  await createAccount(config.applicantCitizenUser.email, config.applicantCitizenUser.password);
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LiP v LR defendant and claimant response- CARM enabled', async ({noc, api_spec_cui, qmSteps}) => {
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, claimType, true);
  await noc.requestNoticeOfChangeForRespondent1Solicitor(caseId, config.defendantSolicitorUser);
  await api_spec_cui.checkUserCaseAccess(config.defendantCitizenUser2, false);
  await api_spec_cui.checkUserCaseAccess(config.defendantSolicitorUser, true);
  // After CIV-14085 case moves to PROCEEDS_IN_HERITAGE_SYSTEM so defendant/claimant responses are not required here
  await adjustCaseSubmittedDateForPublicQueries(caseId, true);
  let query = await qmSteps.raiseLipQuery(caseId, config.applicantCitizenUser, PUBLIC_QUERY, false);
  await qmSteps.validateQmResponseTask(caseId, config.ctscAdminUser, respondToQueryCTSCTask(query.id, true), query.id);
  await qmSteps.respondToQuery(caseId, config.ctscAdminUser, query, PUBLIC_QUERY);
  await qmSteps.followUpOnLipQuery(caseId, config.applicantCitizenUser, query, PUBLIC_QUERY);
  query = await qmSteps.raiseLRQuery(caseId, config.defendantSolicitorUser, PUBLIC_QUERY, false);
  await qmSteps.respondToQuery(caseId, config.ctscAdminUser, query, PUBLIC_QUERY);
  await qmSteps.followUpOnLRQuery(caseId, config.defendantSolicitorUser, query, PUBLIC_QUERY);
});

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.applicantCitizenUser.email);
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
