import { test } from '../../../playwright-fixtures/index';

test.describe('1v2 LIP LR COS notify claim journey', { tag: '@civil-ccd-nightly' }, () => {
  test('1v2 LIP LR - notify and notify claim details', async ({
    ClaimantSolicitorSteps,
    ClaimantSolicitorApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimSmallTrack1v2LRLIP();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorSteps.Login();
    await ClaimantSolicitorSteps.NotifyClaim1v1LIP1LR();
    await ClaimantSolicitorSteps.NotifyClaimDetails1v2LIPLR();
  });
});
