import { test } from '../../playwright-fixtures/index';

test.describe('Unspecified Small Track 1vLIP', async () => {
  test('Create Claim, Notify Claim and Notify Claim Details', async ({
    ClaimantSolicitorSteps,
    ClaimantSolicitorApiSteps,
  }) => {
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.CreateClaimSmallTrack1vLIP();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorSteps.NotifyClaim1v1LIP();
    await ClaimantSolicitorSteps.NotifyClaimDetails1v1LIP();
  });
});
