/**
 * CUI/LiP Test Ownership:
 * civil-ccd-definition: Owns workflow and API-level integration tests.
 * civil-citizen-ui: Owns citizen-rendered UI and routing tests.
 */

const config = require('../../../config.js');
const {createAccount, deleteCitizenAccount} = require('../../../api/idamHelper');

let claimRef;
const claimType = 'SmallClaims';
let carmEnabled = false;

Feature('LR v LIP spec stay case api journey').tag('@civil-service-nightly @api-stay-case');

Before(async () => {
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('LR v LIP spec stay case', async ({api_spec_cui}) => {
  claimRef = await api_spec_cui.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, '', claimType, carmEnabled);
  await api_spec_cui.performCitizenDefendantResponse(config.defendantCitizenUser2, claimRef, claimType, carmEnabled);
  await api_spec_cui.claimantResponse(config.applicantSolicitorUser, 'FULL_DEFENCE_CITIZEN_DEFENDANT', 'ONE_V_ONE', 'Yes', 'IN_MEDIATION', carmEnabled);
  await api_spec_cui.stayCase(config.hearingCenterAdminWithRegionId1);
  await api_spec_cui.manageStay(config.hearingCenterAdminWithRegionId1, true);
  await api_spec_cui.sendMessage(config.ctscAdminUser);
  await api_spec_cui.replyMessage(config.judgeUserWithRegionId1);
  await api_spec_cui.replyMessage(config.ctscAdminUser);
  await api_spec_cui.manageStay(config.hearingCenterAdminWithRegionId1, false);
  await api_spec_cui.createSDO(config.judgeUserWithRegionId1, 'CREATE_SMALL');
  await api_spec_cui.dismissCase(config.hearingCenterAdminWithRegionId1);
}).retry(1);


AfterSuite(async  ({api_spec_cui}) => {
  await api_spec_cui.cleanUp();
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});