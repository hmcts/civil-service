import {
  defendantSolicitor1User,
  defendantSolicitor2User,
} from '../../../../../config/users/exui-users';
import preferredCourts from '../../../../../config/preferred-courts';
import partys from '../../../../../constants/users/partys';
import DefendantResponseSpecType from '../../../../../constants/ccd-events/defendant-response/lr-spec/defendant-response-spec-type';
import CaseDataHelper from '../../../../../helpers/case-data-helper';
import DefenceRouteSpec from '../../../../../constants/ccd-events/defendant-response/lr-spec/defence-route-spec';
import DateHelper from '../../../../../helpers/date-helper';
import { UploadDocumentValue } from '../../../../../models/ccd-case-data';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import { Party } from '../../../../../models/users/partys';
import PaymentTypeSpec from '../../../../../constants/ccd-events/defendant-response/lr-spec/payment-type-spec';
import DefenceAdmittedPartRouteSpec from '../../../../../constants/ccd-events/defendant-response/lr-spec/defence-admitted-part-route-spec';

const defendantChecklist = {
  RespondentChecklist: {},
};

const responseConfirmNameAddress = (claimType: ClaimType, defendantSolictorParty: Party) => {
  if (defendantSolictorParty === partys.DEFENDANT_SOLICITOR_1) {
    if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
      return {
        ResponseConfirmNameAddress: {
          specAoSApplicantCorrespondenceAddressRequired: 'Yes',
          specAoSRespondent2HomeAddressRequired: 'Yes',
        },
      };
    }
    return {
      ResponseConfirmNameAddress: {
        specAoSApplicantCorrespondenceAddressRequired: 'Yes',
      },
    };
  } else if (defendantSolictorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      ResponseConfirmNameAddress: {
        specAoSRespondent2HomeAddressRequired: 'Yes',
      },
    };
  }

  return {};
};

const responseConfirmDetails = (defendantSolicitorParty: Party) => {
  return {
    ResponseConfirmDetails: {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'specAoSRespondentCorrespondenceAddressRequired'
        : 'specAoSRespondent2CorrespondenceAddressRequired']: 'Yes',
    },
  };
};

const singleResponse = (claimType: ClaimType) => {
  if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
    return {
      SingleResponse: {
        respondentResponseIsSame: 'Yes',
      },
    };
  }

  if (ClaimTypeHelper.isClaimant2(claimType)) {
    return {
      SingleResponse2v1: {
        defendantSingleResponseToBothClaimants: 'Yes',
      },
    };
  }

  return {};
};

const respondentResponseTypeSpec = (
  defendantResponseType: DefendantResponseSpecType,
  claimType: ClaimType,
  defendantSolicitorParty: Party,
) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    if (ClaimTypeHelper.isClaimant2(claimType)) {
      return {
        RespondentResponseTypeSpec: {
          respondent1ClaimResponseTypeForSpec: defendantResponseType,
          claimant1ClaimResponseTypeForSpec: defendantResponseType,
          claimant2ClaimResponseTypeForSpec: defendantResponseType,
        },
      };
    }

    return {
      RespondentResponseTypeSpec: {
        respondent1ClaimResponseTypeForSpec: defendantResponseType,
      },
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      RespondentResponseTypeSpec: {
        respondent2ClaimResponseTypeForSpec: defendantResponseType,
      },
    };
  }

  return {};
};

const defenceRoute = (
  defendantResponseType: DefendantResponseSpecType,
  defenceRoute: DefenceRouteSpec,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE) {
    if (defenceRoute === DefenceRouteSpec.DISPUTE) {
      return {
        defenceRoute: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'defenceRouteRequired'
            : 'defenceRouteRequired2']: defenceRoute,
        },
      };
    } else if (defenceRoute === DefenceRouteSpec.HAS_PAID) {
      return {
        defenceRoute: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'defenceRouteRequired'
            : 'defenceRouteRequired2']: defenceRoute,
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'respondToClaim'
            : 'respondToClaim2']: {
            howMuchWasPaid: CaseDataHelper.getClaimValue(claimTrack).toFixed().toString(),
            howWasThisAmountPaid: 'CREDIT_CARD',
            whenWasThisAmountPaid: DateHelper.formatDateToString(
              DateHelper.subtractFromToday({ days: 1 }),
              { outputFormat: 'YYYY-MM-DD' },
            ),
          },
        },
      };
    }
  }

  return {};
};

const defenceAdmittedPartRoute = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (defendantResponseType === DefendantResponseSpecType.PART_ADMISSION) {
    if (defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_PAID)
      return {
        defenceAdmittedPartRoute: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1 
            ? 'specDefenceAdmittedRequired' 
            : 'specDefenceAdmitted2Required']: 'Yes',
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'respondToAdmittedClaim'
            : 'respondToAdmittedClaim2']: {
            howMuchWasPaid: ((CaseDataHelper.getClaimValue(claimTrack)/2)*100).toFixed().toString(),
            whenWasThisAmountPaid: DateHelper.formatDateToString(DateHelper.subtractFromToday({days: 1}), {outputFormat: 'YYYY-MM-DD'}),
            howWasThisAmountPaid: 'CREDIT_CARD',
          },
        },
      };

    return {
      defenceAdmittedPartRoute: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'specDefenceAdmittedRequired'
          : 'specDefenceAdmitted2Required']: 'No',
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondToAdmittedClaimOwingAmount'
          : 'respondToAdmittedClaimOwingAmount2']: (
          (CaseDataHelper.getClaimValue(claimTrack) / 2) *
          100
        )
          .toFixed()
          .toString(),
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondToAdmittedClaimOwingAmountPounds'
          : 'respondToAdmittedClaimOwingAmountPounds2']: (
          CaseDataHelper.getClaimValue(claimTrack) / 2
        )
          .toFixed()
          .toString(),
      },
    };
  }

  return {};
};

const upload = (
  defendantResponseType: DefendantResponseSpecType,
  defenceResponseDocumentSpec: UploadDocumentValue,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    return {
      Upload: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'detailsOfWhyDoesYouDisputeTheClaim'
          : 'detailsOfWhyDoesYouDisputeTheClaim2']:
          `Dispute reason - ${defendantSolicitorParty.key}`,
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1SpecDefenceResponseDocument'
          : 'respondent2SpecDefenceResponseDocument']: { file: defenceResponseDocumentSpec },
      },
    };
  }

  return {};
};

const timeline = (
  defendantResponseType: DefendantResponseSpecType,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    return {
      HowToAddTimeline: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'specClaimResponseTimelineList'
          : 'specClaimResponseTimelineList2']: 'MANUAL',
      },
      HowToAddTimelineManual: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'specResponseTimelineOfEvents'
          : 'specResponseTimelineOfEvents2']: [
          {
            value: {
              timelineDate: DateHelper.formatDateToString(
                DateHelper.subtractFromToday({ days: 10 }),
                { outputFormat: 'YYYY-MM-DD' },
              ),
              timelineDescription: `Timeline event 1 - ${defendantSolicitorParty.key}`,
            },
          },
          {
            value: {
              timelineDate: DateHelper.formatDateToString(
                DateHelper.subtractFromToday({ days: 11 }),
                { outputFormat: 'YYYY-MM-DD' },
              ),
              timelineDescription: `Timeline event 2 - ${defendantSolicitorParty.key}`,
            },
          },
        ],
      },
    };
  }

  return {};
};

const mediationContactInformation = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM) {
      return {
        MediationContactInformation: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'resp1MediationContactInfo'
            : 'resp2MediationContactInfo']: CaseDataHelper.buildMediationData(
            defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
              ? partys.DEFENDANT_1_MEDIATION_FRIEND
              : partys.DEFENDANT_2_MEDIATION_FRIEND,
          ),
        },
      };
    }
  }
  return {};
};

const mediationAvailability = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM) {
      const unavailableDate = DateHelper.formatDateToString(DateHelper.addToToday({ months: 1 }), {
        outputFormat: 'YYYY-MM-DD',
      });
      return {
        MediationAvailability: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'resp1MediationAvailability'
            : 'resp2MediationAvailability']: {
            isMediationUnavailablityExists: 'Yes',
            unavailableDatesForMediation: [
              CaseDataHelper.setIdToData({
                unavailableDateType: 'SINGLE_DATE',
                date: unavailableDate,
              }),
            ],
          },
        },
      };
    }
  }

  return {};
};

const whenWillClaimBePaid = (
  defendantResponseType: DefendantResponseSpecType,
  paymentTypeSpec: PaymentTypeSpec,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (
    (defendantResponseType === DefendantResponseSpecType.FULL_ADMISSION ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION) 
    && defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_NOT_PAID
  ) {
    if (paymentTypeSpec === PaymentTypeSpec.BY_SET_DATE) {
      return {
        WhenWillClaimBePaid: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'defenceAdmitPartPaymentTimeRouteRequired'
            : 'defenceAdmitPartPaymentTimeRouteRequired2']: paymentTypeSpec,
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'respondToClaimAdmitPartLRspec'
            : 'respondToClaimAdmitPartLRspec2']: {
            whenWillThisAmountBePaid: DateHelper.formatDateToString(
              DateHelper.subtractFromToday({ months: 1 }),
              { outputFormat: 'YYYY-MM-DD' },
            ),
          },
        },
      };
    }
    return {
      WhenWillClaimBePaid: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'defenceAdmitPartPaymentTimeRouteRequired'
          : 'defenceAdmitPartPaymentTimeRouteRequired2']: paymentTypeSpec,
      },
    };
  }

  return {};
};

const defendant1FinancialDetails = (
  defendantResponseType: DefendantResponseSpecType,
  paymentTypeSpec: PaymentTypeSpec,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (
    (defendantResponseType === DefendantResponseSpecType.FULL_ADMISSION ||
      defendantResponseType === DefendantResponseSpecType.PART_ADMISSION) &&
    (paymentTypeSpec === PaymentTypeSpec.BY_SET_DATE ||
      paymentTypeSpec === PaymentTypeSpec.REPAYMENT_PLAN) &&
    defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_NOT_PAID &&
    defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
  ) {
    return {
      FinancialDetailsPurpose: {},
      DefendantBankAccounts: {
        respondent1BankAccountList: [
          {
            value: {
              accountType: 'CURRENT',
              jointAccount: 'Yes',
              balance: '1000',
            },
          },
        ],
      },
      DisabilityPremiumPayments: {
        disabilityPremiumPayments: 'Yes',
        severeDisabilityPremiumPayments: 'No',
      },
      defendantHomeOptions: {
        respondent1DQHomeDetails: {
          type: 'PRIVATE_RENTAL',
        },
      },
      DefendantPartnersAndDependents: {
        respondent1PartnerAndDependent: {
          liveWithPartnerRequired: 'Yes',
          partnerAgedOver: 'Yes',
          haveAnyChildrenRequired: 'Yes',
          receiveDisabilityPayments: 'No',
          supportedAnyoneFinancialRequired: 'Yes',
          supportPeopleNumber: '1',
          supportPeopleDetails: `financial support details - ${partys.DEFENDANT_SOLICITOR_1.key}`,
          howManyChildrenByAgeGroup: {
            numberOfUnderEleven: '0',
            numberOfElevenToFifteen: '1',
            numberOfSixteenToNineteen: '0',
          },
        },
      },
      EmploymentDeclaration: {
        defenceAdmitPartEmploymentTypeRequired: 'Yes',
        respondToClaimAdmitPartEmploymentTypeLRspec: ['EMPLOYED', 'SELF'],
      },
      HowToAddEmploymentDetails: {
        responseClaimAdmitPartEmployer: {
          employerDetails: [
            {
              value: {
                employerName: `Company - ${partys.DEFENDANT_SOLICITOR_1.key}`,
                jobTitle: `Job - ${partys.DEFENDANT_SOLICITOR_1.key}`,
              },
            },
          ],
        },
      },
      DefendantSelfEmployment: {
        specDefendant1SelfEmploymentDetails: {
          jobTitle: 'Self Employed',
          annualTurnover: '100000',
          isBehindOnTaxPayment: 'Yes',
          amountOwed: '000',
          reason: `Tax Payments Behind Reason - ${partys.DEFENDANT_SOLICITOR_1.key}`,
        },
      },
      DetailsOfPayingMoneyRepaymentPlan: {
        respondent1CourtOrderPaymentOption: 'Yes',
        respondent1CourtOrderDetails: [
          {
            value: {
              claimNumberText: '123456',
              amountOwed: '10000',
              monthlyInstalmentAmount: '200',
            },
          },
        ],
      },
      DefendantDebts: {
        respondent1LoanCreditOption: 'Yes',
        respondent1LoanCreditDetails: [
          {
            value: {
              loanCardDebtDetail: 'Amex',
              totalOwed: '10000',
              monthlyPayment: '200',
            },
          },
        ],
      },
      DefendantIncomeExpensesFullAdmission: {
        respondent1DQCarerAllowanceCreditFullAdmission: 'Yes',
        respondent1DQRecurringIncomeFA: [
          {
            value: {
              type: 'JOB',
              amount: '50000',
              frequency: 'ONCE_ONE_WEEK',
            },
          },
        ],
        respondent1DQRecurringExpensesFA: [
          {
            value: {
              type: 'RENT',
              amount: '10000',
              frequency: 'ONCE_ONE_MONTH',
            },
          },
          {
            value: {
              type: 'GAS',
              amount: '3000',
              frequency: 'ONCE_ONE_WEEK',
            },
          },
        ],
      },
      WhyDoesNotPayImmediately: {
        responseToClaimAdmitPartWhyNotPayLRspec: `Cannot pay immediately reason - ${partys.DEFENDANT_SOLICITOR_1.key}`,
      },
    };
  }
  return {};
};

const defendant2FinancialDetails = (
  defendantResponseType: DefendantResponseSpecType,
  paymentTypeSpec: PaymentTypeSpec,
  claimType: ClaimType,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (
    (defendantResponseType === DefendantResponseSpecType.FULL_ADMISSION ||
      defendantResponseType === DefendantResponseSpecType.PART_ADMISSION) &&
    (paymentTypeSpec === PaymentTypeSpec.BY_SET_DATE ||
      paymentTypeSpec === PaymentTypeSpec.REPAYMENT_PLAN) &&
    defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_NOT_PAID &&
    (claimType === ClaimType.ONE_VS_TWO_SAME_SOL ||
      defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2)
  ) {
    return {
      FinancialDetailsPurpose2: {},
      DefendantBankAccountsRespondent2: {
        respondent2BankAccountList: [
          {
            value: {
              accountType: 'SAVINGS',
              jointAccount: 'No',
              balance: '200',
            },
          },
        ],
      },
      DisabilityPremiumPaymentsRespondent2: {
        disabilityPremiumPaymentsRespondent2: 'No',
      },
      defendantHomeOptionsRespondent2: {
        respondent2DQHomeDetails: { type: 'OWNED_HOME' },
      },
      Defendant2PartnersAndDependents: {
        respondent2PartnerAndDependent: {
          haveAnyChildrenRequired: 'No',
          liveWithPartnerRequired: 'No',
          supportedAnyoneFinancialRequired: 'No',
        },
      },
      EmploymentDeclarationRespondent2: {
        defenceAdmitPartEmploymentTypeRequiredRespondent2: 'No',
        respondToClaimAdmitPartUnemployedLRspecRespondent2: {
          unemployedComplexTypeRequired: 'RETIRED',
        },
      },
      DetailsOfPayingMoneyRepaymentPlanRespondent2: {
        respondent2CourtOrderPaymentOption: 'No',
      },
      DefendantDebtsRespondent2: {
        respondent2LoanCreditOption: 'No',
      },
      DefendantIncomeExpensesRespondent2: {
        respondent2DQCarerAllowanceCredit: 'No',
      },
      WhyDoesNotPayImmediatelyRespondent2: {
        responseToClaimAdmitPartWhyNotPayLRspec2: `Cannot pay immediately reason - ${partys.DEFENDANT_SOLICITOR_2.key}`,
      },
    };
  }
  return {};
};

const defendant1RepaymentPlan = (
  defendantResponseType: DefendantResponseSpecType,
  paymentTypeSpec: PaymentTypeSpec,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (
    (defendantResponseType === DefendantResponseSpecType.FULL_ADMISSION ||
      defendantResponseType === DefendantResponseSpecType.PART_ADMISSION) &&
    paymentTypeSpec === PaymentTypeSpec.REPAYMENT_PLAN &&
    defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_NOT_PAID &&
    defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
  ) {
    return {
      RepaymentPlan: {
        respondent1RepaymentPlan: {
          firstRepaymentDate: DateHelper.formatDateToString(DateHelper.addToToday({ months: 1 }), {
            outputFormat: 'YYYY-MM-DD',
          }),
          paymentAmount: '5000',
          repaymentFrequency: 'ONCE_ONE_WEEK',
        },
      },
    };
  }
  return {};
};

const defendant2RepaymentPlan = (
  defendantResponseType: DefendantResponseSpecType,
  paymentTypeSpec: PaymentTypeSpec,
  claimType: ClaimType,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (
    (defendantResponseType === DefendantResponseSpecType.FULL_ADMISSION ||
      defendantResponseType === DefendantResponseSpecType.PART_ADMISSION) &&
    paymentTypeSpec === PaymentTypeSpec.REPAYMENT_PLAN &&
    defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_NOT_PAID &&
    (claimType === ClaimType.ONE_VS_TWO_SAME_SOL ||
      defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2)
  ) {
    return {
      RepaymentPlanRespondent2: {
        respondent2RepaymentPlan: {
          firstRepaymentDate: DateHelper.formatDateToString(DateHelper.addToToday({ months: 2 }), {
            outputFormat: 'YYYY-MM-DD',
          }),
          paymentAmount: '5000',
          repaymentFrequency: 'ONCE_ONE_MONTH',
        },
      },
    };
  }
  return {};
};

const fileDirectionsQuestionnaire = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (
      claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM
    ) {
      return {
        FileDirectionsQuestionnaire: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'respondent1DQFileDirectionsQuestionnaire'
            : 'respondent2DQFileDirectionsQuestionnaire']: {
            oneMonthStayRequested: 'Yes',
            reactionProtocolCompliedWith: 'No',
            reactionProtocolNotCompliedWithReason: `Reaction protocol not complied with reason - ${defendantSolicitorParty.key}`,
            explainedToClient: ['CONFIRM'],
          },
        },
      };
    }
  }

  return {};
};

const fixedRecoverableCosts = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (claimTrack === ClaimTrack.FAST_CLAIM) {
      return {
        FixedRecoverableCosts: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'respondent1DQFixedRecoverableCosts'
            : 'respondent2DQFixedRecoverableCosts']: {
            isSubjectToFixedRecoverableCostRegime: 'Yes',
            band: 'BAND_4',
            complexityBandingAgreed: 'Yes',
            reasons: `Recoverable costs reason - ${defendantSolicitorParty.key}`,
          },
        },
      };
    }
  }

  return {};
};

const fixedRecoverableCostsIntermediate = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
  frcSupportingDocument?: UploadDocumentValue,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
      return {
        FixedRecoverableCostsIntermediate: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'respondent1DQFixedRecoverableCostsIntermediate'
            : 'respondent2DQFixedRecoverableCostsIntermediate']: {
            isSubjectToFixedRecoverableCostRegime: 'Yes',
            band: 'BAND_2',
            complexityBandingAgreed: 'Yes',
            reasons: `Recoverable costs reason - ${defendantSolicitorParty.key}`,
            frcSupportingDocument,
          },
        },
      };
    }
  }

  return {};
};

const disclosureOfElectronicDocumentsLRspec = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (
      claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM
    ) {
      return {
        DisclosureOfElectronicDocumentsLRspec: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'specRespondent1DQDisclosureOfElectronicDocuments'
            : 'specRespondent2DQDisclosureOfElectronicDocuments']: {
            reachedAgreement: 'No',
            agreementLikely: 'No',
            reasonForNoAgreement: `Reason for no agreement - ${defendantSolicitorParty.key}`,
          },
        },
      };
    }
  }

  return {};
};

const disclosureOfNonElectronicDocumentsLRspec = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (
      claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM
    ) {
      return {
        DisclosureOfNonElectronicDocumentsLRspec: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'specRespondent1DQDisclosureOfNonElectronicDocuments'
            : 'specRespondent2DQDisclosureOfNonElectronicDocuments']: {
            bespokeDirections: `Directions are proposed for disclosure - ${defendantSolicitorParty.key}`,
          },
        },
      };
    }
  }

  return {};
};

const disclosureReport = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (
      claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM
    ) {
      return {
        DisclosureReport: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'respondent1DQDisclosureReport'
            : 'respondent2DQDisclosureReport']: {
            disclosureFormFiledAndServed: 'Yes',
            disclosureProposalAgreed: 'Yes',
            draftOrderNumber: '12345',
          },
        },
      };
    }
  }

  return {};
};

const deterWithoutHearing = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM) {
      return {
        DeterminationWithoutHearing: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'deterWithoutHearingRespondent1'
            : 'deterWithoutHearingRespondent2']: {
            deterWithoutHearingWhyNot: `Determination without hearing reason - ${defendantSolicitorParty.key}`,
            deterWithoutHearingYesNo: 'No',
          },
        },
      };
    }
  }

  return {};
};

const experts = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM) {
      const defendantExpert =
        defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? partys.DEFENDANT_1_EXPERT_1
          : partys.DEFENDANT_2_EXPERT_1;

      return {
        SmallClaimExperts: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'respondToClaimExperts'
            : 'respondToClaimExperts2']: {
            ...CaseDataHelper.buildExpertData(defendantExpert),
            partyName: undefined,
          },
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'responseClaimExpertSpecRequired'
            : 'responseClaimExpertSpecRequired2']: 'Yes',
        },
      };
    }
    const defendantExperts =
      defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? [partys.DEFENDANT_1_EXPERT_1, partys.DEFENDANT_1_EXPERT_2]
        : [partys.DEFENDANT_2_EXPERT_1, partys.DEFENDANT_2_EXPERT_2];

    return {
      Experts: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQExperts'
          : 'respondent2DQExperts']: {
          expertRequired: 'Yes',
          expertReportsSent: 'YES',
          jointExpertSuitable: 'Yes',
          details: defendantExperts.map((expertParty) =>
            CaseDataHelper.setIdToData({
              ...CaseDataHelper.buildExpertData(expertParty),
              partyName: undefined,
            }),
          ),
        },
      },
    };
  }

  return {};
};

const witnesses = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM) {
      const defendantWitnesses =
        defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? [partys.DEFENDANT_1_WITNESS_1, partys.DEFENDANT_1_WITNESS_2]
          : [partys.DEFENDANT_2_WITNESS_1, partys.DEFENDANT_2_WITNESS_2];

      return {
        SmallClaimWitnesses: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'respondent1DQWitnessesSmallClaim'
            : 'respondent2DQWitnessesSmallClaim']: {
            witnessesToAppear: 'Yes',
            details: defendantWitnesses.map((witnessParty) =>
              CaseDataHelper.setIdToData({
                ...CaseDataHelper.buildWitnessData(witnessParty),
                partyName: undefined,
              }),
            ),
          },
        },
      };
    }

    const defendantWitnesses =
      defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? [partys.DEFENDANT_1_WITNESS_1, partys.DEFENDANT_1_WITNESS_2]
        : [partys.DEFENDANT_2_WITNESS_1, partys.DEFENDANT_2_WITNESS_2];

    const witnessDetails = defendantWitnesses.map((witnessParty) =>
      CaseDataHelper.setIdToData({
        ...CaseDataHelper.buildWitnessData(witnessParty),
        partyName: undefined,
      }),
    );

    if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
      return {
        Witnesses: {
          respondent1DQWitnessesRequiredSpec: 'Yes',
          respondent1DQWitnessesDetailsSpec: witnessDetails,
        },
      };
    }

    return {
      Witnesses: {
        respondent2DQWitnesses: {
          witnessesToAppear: 'Yes',
          details: witnessDetails,
        },
      },
    };
  }

  return {};
};

const language = (
  defendantResponseType: DefendantResponseSpecType,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    return {
      Language: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQLanguage'
          : 'respondent2DQLanguage']: {
          court: 'BOTH',
          documents: 'BOTH',
        },
      },
    };
  }

  return {};
};

const hearing = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  const unavailableDate = DateHelper.formatDateToString(DateHelper.addToToday({ months: 1 }), {
    outputFormat: 'YYYY-MM-DD',
  });

  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM) {
      return {
        SmallClaimHearing: {
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'respondent1DQHearingSmallClaim'
            : 'respondent2DQHearingSmallClaim']: {
            unavailableDatesRequired: 'Yes',
            smallClaimUnavailableDate: [
              CaseDataHelper.setIdToData({
                unavailableDateType: 'SINGLE_DATE',
                date: unavailableDate,
              }),
            ],
          },
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'SmallClaimHearingInterpreterRequired'
            : 'SmallClaimHearingInterpreter2Required']: 'Yes',
          [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
            ? 'SmallClaimHearingInterpreterDescription'
            : 'smallClaimHearingInterpreterDescription2']:
            `Small claim hearing interpreter description - ${defendantSolicitorParty.key}`,
        },
      };
    }

    return {
      HearingLRspec: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQHearing'
          : 'respondent2DQHearing']: {
          unavailableDatesRequired: 'No',
        },
      },
    };
  }

  return {};
};

const requestedCourtLocation = (
  defendantResponseType: DefendantResponseSpecType,
  defendantSolicitorParty: Party,
) => {
  const preferredCourtParty =
    defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
      ? partys.DEFENDANT_1
      : partys.DEFENDANT_2;
  const preferredCourt = CaseDataHelper.setCodeToData(
    preferredCourts[preferredCourtParty.key].default,
  );

  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    return {
      RequestedCourtLocationLRspec: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondToCourtLocation'
          : 'respondToCourtLocation2']: {
          responseCourtLocations: {
            list_items: [preferredCourt],
            value: preferredCourt,
          },
          reasonForHearingAtSpecificCourt: `Court location reason - ${defendantSolicitorParty.key}`,
        },
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQRemoteHearingLRspec'
          : 'respondent2DQRemoteHearingLRspec']: {
          remoteHearingRequested: 'Yes',
          reasonForRemoteHearing: `Court location reason - ${defendantSolicitorParty.key}`,
        },
      },
    };
  }

  return {};
};

const hearingSupport = (
  defendantResponseType: DefendantResponseSpecType,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    return {
      HearingSupport: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQHearingSupport'
          : 'respondent2DQHearingSupport']: {
          supportRequirements: 'Yes',
          supportRequirementsAdditional: `Support requirements for ${defendantSolicitorParty.key}`,
        },
      },
    };
  }

  return {};
};

const vulnerabilityQuestions = (
  defendantResponseType: DefendantResponseSpecType,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    return {
      VulnerabilityQuestions: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQVulnerabilityQuestions'
          : 'respondent2DQVulnerabilityQuestions']: {
          vulnerabilityAdjustmentsRequired: 'Yes',
          vulnerabilityAdjustments: `Vulnerability adjustments - ${defendantSolicitorParty.key}`,
        },
      },
    };
  }

  return {};
};

const applications = (
  defendantResponseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    defendantResponseType === DefendantResponseSpecType.FULL_DEFENCE ||
    defendantResponseType === DefendantResponseSpecType.PART_ADMISSION
  ) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM) {
      return {};
    }

    return {
      Applications: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'additionalInformationForJudge'
          : 'additionalInformationForJudge2']:
          `Additional information - ${defendantSolicitorParty.key}`,
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQFutureApplications'
          : 'respondent2DQFutureApplications']: {
          intentionToMakeFutureApplications: 'Yes',
          whatWillFutureApplicationsBeMadeFor: `Reason - ${defendantSolicitorParty.key}`,
        },
      },
    };
  }

  return {};
};

const statementOfTruth = (defendantSolicitorParty: Party) => {
  const defendantSolicitorUser =
    defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
      ? defendantSolicitor2User
      : defendantSolicitor1User;

  return {
    StatementOfTruth: {
      uiStatementOfTruth: {
        name: defendantSolicitorUser.name,
        role: 'Solicitor',
      },
    },
  };
};

const undefine = (defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      Undefine: {
        respondent1DetailsForClaimDetailsTab: undefined,
      },
    };
  }

  return {};
};

const defendantResponseSpecData = {
  defendantChecklist,
  responseConfirmNameAddress,
  responseConfirmDetails,
  singleResponse,
  respondentResponseTypeSpec,
  defenceRoute,
  defenceAdmittedPartRoute,
  upload,
  timeline,
  whenWillClaimBePaid,
  defendant1FinancialDetails,
  defendant2FinancialDetails,
  defendant1RepaymentPlan,
  defendant2RepaymentPlan,
  mediationContactInformation,
  mediationAvailability,
  deterWithoutHearing,
  fileDirectionsQuestionnaire,
  fixedRecoverableCosts,
  fixedRecoverableCostsIntermediate,
  disclosureOfElectronicDocumentsLRspec,
  disclosureOfNonElectronicDocumentsLRspec,
  disclosureReport,
  experts,
  witnesses,
  language,
  hearing,
  requestedCourtLocation,
  hearingSupport,
  vulnerabilityQuestions,
  applications,
  statementOfTruth,
  undefine,
};

export default defendantResponseSpecData;
