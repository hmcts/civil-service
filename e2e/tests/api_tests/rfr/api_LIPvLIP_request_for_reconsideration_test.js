/**
 * CUI/LiP Test Ownership:
 * civil-ccd-definition: Owns workflow and API-level integration tests.
 * civil-citizen-ui: Owns citizen-rendered UI and routing tests.
 */

const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

let caseId;

Feature('LIPvLIP spec request for reconsideration api journeys').tag('@civil-service-nightly');

Before(async () => {
  await createAccount(config.applicantCitizenUser.email, config.applicantCitizenUser.password);
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LiP v LiP Request for reconsideration', async ({api_spec_cui}) => {
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'Request for reconsideration track', false);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'Request for reconsideration track', false, 'FULL_DEFENCE');
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, 'JUDICIAL_REFERRAL', false);
  await api_spec_cui.createSDO(config.tribunalCaseworkerWithRegionId4);
  await api_spec_cui.requestForReconsiderationCitizen(config.applicantCitizenUser);
  await api_spec_cui.judgeDecisionOnReconsiderationRequest(config.judgeUserWithRegionId1, 'CREATE_SDO');
}).tag('@api-rfr');;

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.applicantCitizenUser.email);
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});