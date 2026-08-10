/**
 * CUI/LiP Test Ownership:
 * civil-ccd-definition: Owns workflow and API-level integration tests.
 * civil-citizen-ui: Owns citizen-rendered UI and routing tests.
 */

const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

Feature('LIP v LIP spec stay case api journey').tag('@civil-service-nightly @api-stay-case');

Before(async () => {
  await createAccount(config.applicantCitizenUser.email, config.applicantCitizenUser.password);
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 JUDICIAL_REFERRAL Lip v Lip stay case dismiss case', async ({api_spec_cui}) => {
  let caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'FastTrack', false);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'FastTrack', false);
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, 'JUDICIAL_REFERRAL', false);
  await api_spec_cui.stayCase(config.hearingCenterAdminWithRegionId1);
  await api_spec_cui.manageStay(config.hearingCenterAdminWithRegionId1, true);
  await api_spec_cui.manageStay(config.hearingCenterAdminWithRegionId1, false);
  await api_spec_cui.createSDO(config.judgeUserWithRegionId1, 'CREATE_SMALL');
  await api_spec_cui.dismissCase(config.hearingCenterAdminWithRegionId1);
}).retry(1);

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.applicantCitizenUser.email);
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});