import { test } from '../../../playwright-fixtures/index';

test.describe('Unspec 1v2lips api journey', { tag: '@civil-service-nightly' }, async () => {
  test('Create claim where one respondent is LIP one is LR and notify/notify details', async ({
    ClaimantSolicitorApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimSmallTrack1v2LRLIP();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim1v2LRLIP();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails1v2LRLIP();
  });

  test('Create claim where two respondents are LIP and notify/notify details', async ({
    ClaimantSolicitorApiSteps,
  }) => {
    await ClaimantSolicitorApiSteps.CreateClaimSmallTrack1v2LIPs();
    await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
    await ClaimantSolicitorApiSteps.NotifyClaim1v2LIPS();
    await ClaimantSolicitorApiSteps.NotifyClaimDetails1v2LIPS();
  });
});
