/**
 * CUI/LiP Test Ownership:
 * civil-ccd-definition: Owns workflow and API-level integration tests.
 * civil-citizen-ui: Owns citizen-rendered UI and routing tests.
 */

const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

let caseId;

Feature('LRvLIP spec request for reconsideration api journeys').tag('@civil-service-nightly');

Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LR v LiP Request for reconsideration', async ({ api_spec_cui }) => {
  caseId = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', 'Request for reconsideration track', false);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'Request for reconsideration track', false);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'No', 'JUDICIAL_REFERRAL', false);
  await api_spec_cui.createSDO(config.tribunalCaseworkerWithRegionId4);
  await api_spec_cui.requestForReconsiderationCitizen(config.defendantCitizenUser2);
  await api_spec_cui.judgeDecisionOnReconsiderationRequest(config.judgeUserWithRegionId1, 'CREATE_SDO');
}).tag('@api-rfr');;

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
