import BaseDataBuilder from '../../../../../base/base-data-builder.ts';
import ClaimType from '../../../../../constants/cases/claim-type.ts';
import DJPaymentTypeSpec from '../../../../../constants/ccd-events/default-judgement/dj-payment-type-spec.ts';
import DJSpecType from '../../../../../constants/ccd-events/default-judgement/dj-spec-type.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import requestDefaultJudgementSpecBuilderComponents from './default-judgement-spec-data-builder-components.ts';

@AllMethodsStep()
export default class DefaultJudgementSpecDataBuilder extends BaseDataBuilder {
  async build() {
    return this.buildData();
  }

  async build1v2SSNonDivergent() {
    return this.buildData({
      claimType: ClaimType.ONE_VS_TWO_SAME_SOL,
      djSpecType: DJSpecType.NON_DIVERGENT
    });
  }

  protected async buildData({
    claimType = ClaimType.ONE_VS_ONE,
    djSpecType = DJSpecType.NON_DIVERGENT,
    djPaymentTypeSpec = DJPaymentTypeSpec.IMMEDIATELY,
  }: {
    claimType?: ClaimType;
    djSpecType?: DJSpecType;
    djPaymentTypeSpec?: DJPaymentTypeSpec;
  } = {}) {
    return {
      ...requestDefaultJudgementSpecBuilderComponents.defendantDetailsSpec(
        claimType,
        djSpecType,
        this.defendant1PartyType!,
      ),
      ...requestDefaultJudgementSpecBuilderComponents.showCertifyStatementSpec(),
      ...requestDefaultJudgementSpecBuilderComponents.claimPartialPayment(),
      ...requestDefaultJudgementSpecBuilderComponents.fixedCostsOnEntry(),
      ...requestDefaultJudgementSpecBuilderComponents.paymentBreakdown(
        this.ccdCaseData.totalClaimAmount,
        this.ccdCaseData.claimFee?.calculatedAmountInPence,
        this.ccdCaseData.fixedCosts?.fixedCostAmount,
      ),
      ...requestDefaultJudgementSpecBuilderComponents.paymentType(djPaymentTypeSpec),
      ...requestDefaultJudgementSpecBuilderComponents.paymentSetDate(djPaymentTypeSpec),
      ...requestDefaultJudgementSpecBuilderComponents.repaymentInformation(djPaymentTypeSpec),
    };
  }
}
