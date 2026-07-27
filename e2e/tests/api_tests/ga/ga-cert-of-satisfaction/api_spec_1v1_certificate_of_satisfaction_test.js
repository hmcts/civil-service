const { createAccount, deleteCitizenAccount} = require('../../../../api/idamHelper.js');
const config = require('../../../../config.js');

let civilCaseReference;

Feature('GA SPEC Claim 1v1 Certification of Satisfaction/Cancellation').tag('@civil-service-nightly @api-ga-cert-of-satisfaction');

Before(async () => {
  await createAccount(config.applicantCitizenUser.email, config.applicantCitizenUser.password);
  await createAccount(config.defendantCitizenUser2.email, config.defendantCitizenUser2.password);
});

Scenario('1v1 LR v LIP Spec case marked paid in full', async ({api_ga}) => {
  civilCaseReference = await api_ga.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  await api_ga.amendRespondent1ResponseDeadline(config.systemupdate);
  await api_ga.defaultJudgmentXuiPayImmediately(config.applicantSolicitorUser);
  await api_ga.markJudgmentPaid(config.applicantSolicitorUser);
  await api_ga.certificateOfSatisfactionCancellationCui(config.defendantCitizenUser2, civilCaseReference);
}).retry(1);

Scenario('1v1 LIP v LIP Spec Case marked paid in full', async ({api_ga}) => {
  civilCaseReference = await api_ga.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'SmallClaims', 'INDIVIDUAL');
  await api_ga.amendRespondent1ResponseDeadline(config.systemupdate);
  await api_ga.defaultJudgmentCui(config.applicantCitizenUser);
  await api_ga.certificateOfSatisfactionCancellationCui(config.defendantCitizenUser2, civilCaseReference);
}).retry(1);

Scenario('1v1 LIP v LIP Spec Case not marked paid in full', async ({api_ga}) => {
  civilCaseReference = await api_ga.createClaimWithUnrepresentedClaimant(config.applicantCitizenUser, 'SmallClaims', 'INDIVIDUAL');
  await api_ga.amendRespondent1ResponseDeadline(config.systemupdate);
  await api_ga.defaultJudgmentCui(config.applicantCitizenUser);
  await api_ga.judgmentPaidInFullCui(config.applicantCitizenUser);
  await api_ga.certificateOfSatisfactionCancellationCui(config.defendantCitizenUser2, civilCaseReference);
}).retry(1);

Scenario('1v1 LR v LIP Spec case JBA marked paid in full', async ({api_ga}) => {
  civilCaseReference = await api_ga.createSpecifiedClaimWithUnrepresentedRespondent(config.applicantSolicitorUser, 'ONE_V_ONE');
  await api_ga.performCitizenDefendantResponse(config.defendantCitizenUser2, civilCaseReference);
  await api_ga.claimantResponseClaimSpec(config.applicantSolicitorUser, 'PART_ADMISSION_IMMEDIATELY', 'ONE_V_ONE',
    'AWAITING_APPLICANT_INTENTION');
  await api_ga.amendWhenWillThisAmountBePaidDeadLine(config.systemupdate);
  await api_ga.requestJudgementXui(config.applicantSolicitorUser, 'REQUEST_JUDGEMENT');
  await api_ga.markJudgmentPaid(config.applicantSolicitorUser);
  await api_ga.certificateOfSatisfactionCancellationCui(config.defendantCitizenUser2, civilCaseReference);
}).retry(1);

AfterSuite(async ({ api_ga }) => {
  await api_ga.cleanUp();
  await deleteCitizenAccount(config.applicantCitizenUser.email);
  await deleteCitizenAccount(config.defendantCitizenUser2.email);
});
