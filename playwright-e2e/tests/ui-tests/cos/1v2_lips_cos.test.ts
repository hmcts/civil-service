import { test } from '../../../playwright-fixtures/index';

test.describe('1v2 LIPs COS notify claim journey', { tag: '@civil-ccd-nightly' }, () => {
  test('1v2 LIPs - notify and notify claim details', async ({
    ClaimantSolicitorSteps,
    ClaimantSolicitorApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimSmallTrack1v2LIPs();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.NotifyClaim1v2LIPS();
    await ClaimantSolicitorSteps.NotifyClaimDetails1v2LIPS();
  });
});
