import CaseDataHelper from '../../../../../helpers/case-data-helper.ts';
import {ClaimantDefendantPartyType} from "../../../../../models/users/claimant-defendant-party-types.ts";
import partys from "../../../../../constants/users/partys.ts";
import DJPaymentTypeSpec from '../../../../../constants/ccd-events/default-judgement/dj-payment-type-spec.ts';
import DateHelper from '../../../../../helpers/date-helper.ts';
import ClaimType from '../../../../../constants/cases/claim-type.ts';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper.ts';
import DJSpecType from '../../../../../constants/ccd-events/default-judgement/dj-spec-type.ts';

const formatDate = (date: Date) =>
  DateHelper.formatDateToString(date, { outputFormat: 'YYYY-MM-DD' });

const defendantDetailsSpec = (claimType: ClaimType, djSpecType: DJSpecType, defendant1Party: ClaimantDefendantPartyType) => {
  if (ClaimTypeHelper.isDefendant2(claimType)) {
    if (djSpecType === DJSpecType.NON_DIVERGENT) {
      return {
        defendantDetails: {
          defendantDetails: {
            list_items: [
              CaseDataHelper.setCodeToData(
                'Both Defendants'
              ),
            ],
            value: CaseDataHelper.setCodeToData(
              'Both Defendants'
            ),
          },
        },
      };
    } 
  }

  return {
    defendantDetails: {
      defendantDetails: {
        list_items: [
          CaseDataHelper.setCodeToData(
            CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_1, defendant1Party)
              .partyName,
          ),
        ],
        value: CaseDataHelper.setCodeToData(
          CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_1, defendant1Party).partyName,
        ),
      },
    },
  };
};

const showCertifyStatementSpec = () => ({
  showCertifyStatementSpec: {
    CPRAcceptance: {
      acceptance: ['CERTIFIED'],
    }
  },
});

const claimPartialPayment = () => ({
  claimPartialPayment: {
    partialPayment:  'No',
  },
});

const fixedCostsOnEntry = () => ({
  fixedCostsOnEntryDJ: {
    claimFixedCostsOnEntryDJ:  'Yes',
  },
});

const paymentBreakdown = (totalClaimAmount = 0, calculatedAmountInPence = 0, fixedCostAmount = 0) => ({
  paymentBreakdown: {
    defaultJudgementOverallTotal: `${totalClaimAmount + calculatedAmountInPence/100 + fixedCostAmount/100 + 30}`
  }
});

const paymentType = (djPaymentTypeSpec: DJPaymentTypeSpec) => ({
  paymentType: {
    paymentTypeSelection: djPaymentTypeSpec,
  },
});

const paymentSetDate = (djPaymentTypeSpec: DJPaymentTypeSpec) => {
  if (djPaymentTypeSpec === DJPaymentTypeSpec.SET_DATE) {
    return {
      paymentSetDate: {
        paymentSetDate: formatDate(DateHelper.addToToday({months: 1}))
      }
    }
  }

  return {};
}

const repaymentInformation = (djPaymentTypeSpec: DJPaymentTypeSpec) => {
  if (djPaymentTypeSpec === DJPaymentTypeSpec.REPAYMENT_PLAN) {
    return {
      repaymentInformation: {
        repaymentDate: formatDate(DateHelper.addToToday({months: 1})),
        repaymentFrequency: 'ONCE_TWO_WEEKS',
        repaymentSuggestion: '5000'
      }
    }
  }

  return {};
}

const requestDefaultJudgementSpecBuilderComponents = {
  defendantDetailsSpec,
  showCertifyStatementSpec,
  claimPartialPayment,
  fixedCostsOnEntry,
  paymentBreakdown,
  paymentType,
  paymentSetDate,
  repaymentInformation
};

export default requestDefaultJudgementSpecBuilderComponents;
