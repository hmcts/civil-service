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
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LR v LiP case progression', async ({ api_spec_cui }) => {
  caseId = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE', 'FastTrack', false);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, caseId, 'FastTrack', false);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'No', 'JUDICIAL_REFERRAL', false);
  await api_spec_cui.createSDO(config.judgeUserWithRegionId1, 'CREATE_FAST');
  await api_spec_cui.evidenceUploadDefendant(config.defendantCitizenUser2);
  await api_spec_cui.scheduleHearing(config.hearingCenterAdminWithRegionId1, 'FAST_TRACK_TRIAL');
  await api_spec_cui.amendHearingDueDate(config.systemupdate);
  await api_spec_cui.hearingFeePaid(config.hearingCenterAdminWithRegionId1);
  await api_spec_cui.trialReadinessCitizen(config.defendantCitizenUser2);
  await api_spec_cui.createFinalOrder(config.judgeUserWithRegionId1, 'FREE_FORM_ORDER');
});

AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
