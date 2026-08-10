import { z } from 'zod';
import DJPaymentTypeSpec from '../../../../../constants/ccd-events/default-judgement/dj-payment-type-spec';

const defendantDetailsSpec = () => ({
  defendantDetailsSpec: z.looseObject({
    list_items: z.array(
      z.looseObject({
        code: z.string(),
        label: z.string(),
      }),
    ),
    value: z.looseObject({
      code: z.string(),
      label: z.string(),
    }),
  }),
});

const claimPartialPayment = () => ({
  partialPayment: z.string(),
});

const fixedCostsOnEntry = () => ({
  claimFixedCostsOnEntryDJ: z.string(),
});

const paymentBreakdown = () => ({
  defaultJudgementOverallTotal: z.number(),
});

const paymentType = () => ({
  paymentTypeSelection: z.string(),
});

const paymentSetDate = (djPaymentTypeSpec: DJPaymentTypeSpec) => {
  if (djPaymentTypeSpec === DJPaymentTypeSpec.SET_DATE) {
    return {
      paymentSetDate: z.string(),
    };
  }

  return {};
};

const repaymentInformation = (djPaymentTypeSpec: DJPaymentTypeSpec) => {
  if (djPaymentTypeSpec === DJPaymentTypeSpec.REPAYMENT_PLAN) {
    return {
      repaymentDate: z.string(),
      repaymentFrequency: z.string(),
      repaymentSuggestion: z.string(),
    };
  }

  return {};
};

const defaultJudgementSpecSchemaComponents = {
  defendantDetailsSpec,
  claimPartialPayment,
  fixedCostsOnEntry,
  paymentBreakdown,
  paymentType,
  paymentSetDate,
  repaymentInformation,
};

export default defaultJudgementSpecSchemaComponents;
