import { test } from '../../../playwright-fixtures/index';

test.describe('1v1 lip unspec api journey', { tag: '@civil-service-nightly' }, async () => {
  test('Create claim where respondent is litigant in person and notify/notify details', async ({
    ClaimantSolicitorApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimSmallTrack1vLIP();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim1vLIP();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails1vLIP();
  });
});
