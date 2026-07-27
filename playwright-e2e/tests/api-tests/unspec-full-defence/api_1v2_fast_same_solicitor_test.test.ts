import { test } from '../../../playwright-fixtures/index';

test.describe(
  '1v2 same solicitor unspec full defence api journey',
  { tag: '@civil-service-nightly' },
  async () => {
    test(
      '1v2 same solicitor unspec full defence',
      async ({
        ClaimantSolicitorApiSteps,
        CaseworkerApiSteps,
        CaseRoleAssignmentApiSteps,
        DefendantSolicitor1ApiSteps,
      }) => {
        await ClaimantSolicitorApiSteps.CreateClaimFast1v2SS();
        await ClaimantSolicitorApiSteps.MakePaymentForClaimIssue();
        await CaseworkerApiSteps.AddCaseNote();
        await ClaimantSolicitorApiSteps.AmendClaimDocuments();
        await ClaimantSolicitorApiSteps.NotifyClaim();
        await CaseRoleAssignmentApiSteps.AssignCaseRoleToDS1();
        await ClaimantSolicitorApiSteps.NotifyClaimDetails();
        await CaseworkerApiSteps.AmendPartyDetails();
        await DefendantSolicitor1ApiSteps.AcknowledgeClaimFullDefence1v2SS();
        await DefendantSolicitor1ApiSteps.InformAgreedExtensionDate();
        await DefendantSolicitor1ApiSteps.RespondFastFullDefence1v2SS();
        await ClaimantSolicitorApiSteps.RespondFastProceed1v2SS();
      },
    );
  },
);
