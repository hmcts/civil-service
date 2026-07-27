/**
 * CUI/LiP Test Ownership:
 * civil-ccd-definition: Owns workflow and API-level integration tests.
 * civil-citizen-ui: Owns citizen-rendered UI and routing tests.
 */

const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

let caseId;

Feature('1v1 LIP v LIP and LR v LIP spec api journeys').tag('@civil-service-nightly @api-case-progression');

Before(async () => {
  await createAccount(config.applicantCitizenUser.email, config.applicantCitizenUser.password);
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LiP v LiP Case Progression Journey', async ({ api_spec_cui }) => {
  caseId = await api_spec_cui.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'FastTrack', false);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'FastTrack', false);
  await api_spec_cui.performCitizenClaimantResponse(config.applicantCitizenUser, caseId, 'JUDICIAL_REFERRAL', false);
  await api_spec_cui.createSDO(config.judgeUserWithRegionId1, 'CREATE_FAST');
  await api_spec_cui.evidenceUploadApplicant(config.applicantCitizenUser);
  await api_spec_cui.evidenceUploadDefendant(config.defendantCitizenUser2);
  await api_spec_cui.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'FAST_TRACK_TRIAL', 'CUI', 'PREPARE_FOR_HEARING_CONDUCT_HEARING');
  await api_spec_cui.trialReadinessCitizen(config.applicantCitizenUser);
  await api_spec_cui.trialReadinessCitizen(config.defendantCitizenUser2);
  await api_spec_cui.createFinalOrder(config.judgeUserWithRegionId1, 'FREE_FORM_ORDER');
});

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.applicantCitizenUser.email);
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
