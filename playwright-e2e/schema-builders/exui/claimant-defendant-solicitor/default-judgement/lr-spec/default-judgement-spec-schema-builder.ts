import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import DJPaymentTypeSpec from '../../../../../constants/ccd-events/default-judgement/dj-payment-type-spec';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import defaultJudgementSpecSchemaComponents from './default-judgement-spec-schema-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class DefaultJudgementSpecSchemaBuilder extends BaseSchemaBuilder {
  async build(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission);
  }

  protected async buildSchema(
    caseDataBeforeSubmission?: CCDCaseData,
    {
      djPaymentTypeSpec = DJPaymentTypeSpec.IMMEDIATELY,
    }: {
      djPaymentTypeSpec?: DJPaymentTypeSpec;
    } = {},
  ): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...defaultJudgementSpecSchemaComponents.defendantDetailsSpec(),
      ...defaultJudgementSpecSchemaComponents.claimPartialPayment(),
      ...defaultJudgementSpecSchemaComponents.fixedCostsOnEntry(),
      ...defaultJudgementSpecSchemaComponents.paymentBreakdown(),
      ...defaultJudgementSpecSchemaComponents.paymentType(),
      ...defaultJudgementSpecSchemaComponents.paymentSetDate(djPaymentTypeSpec),
      ...defaultJudgementSpecSchemaComponents.repaymentInformation(djPaymentTypeSpec),
    });
  }
}
