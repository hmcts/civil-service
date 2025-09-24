import { test } from '../../playwright-fixtures/index';

test.describe('Unspecified Small Track 1v2LRLIP', async () => {
  test('Create Claim, Notify Claim and Notify Claim Details', async ({
    ClaimantSolicitorSteps,
    ClaimantSolicitorApiSteps,
    CaseRoleAssignmentApiSteps,
  }) => {
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.CreateClaimSmallTrack1v2LRLIP();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorSteps.NotifyClaim1v1LIP1LR();
    await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
    await ClaimantSolicitorSteps.NotifyClaimDetails1v2LIPLR();
  });
});
