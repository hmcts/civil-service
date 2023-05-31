package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DebtLRspec;
import uk.gov.hmcts.reform.civil.model.EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.Respondent1CourtOrderDetails;
import uk.gov.hmcts.reform.civil.model.Respondent1DebtLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1SelfEmploymentLRspec;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.LipDefenceFormParty;
import uk.gov.hmcts.reform.civil.model.docmosis.common.AccountSimpleTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.DebtTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.ReasonMoneyTemplateData;
import uk.gov.hmcts.reform.civil.model.dq.HomeDetails;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimLipResponseForm implements MappableObject {





    private final String claimReferenceNumber;
    private final String claimantReferenceNumber;
    private final String defendantReferenceNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate generationDate;
    private final String amountToPay;
    private final String howMuchWasPaid;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate paymentDate;
    private final String paymentHow;
    private final RespondentResponsePartAdmissionPaymentTimeLRspec howToPay;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate payBy;
    private final String whyNotPayImmediately;
    private final RepaymentPlanLRspec repaymentPlan;

    private final RespondentResponseTypeSpec responseType;
    // TODO enum, ALREADY_PAID, DISPUTE, COUNTER_CLAIM
    private final String whyReject;
    private final String freeTextWhyReject;
    private final LipDefenceFormParty claimant1;
    private final LipDefenceFormParty defendant1;
    private final LipDefenceFormParty defendant2;
    private final List<EventTemplateData> timelineEventList;
    private final String timelineComments;
    private final List<EvidenceTemplateData> evidenceList;
    private final String evidenceComments;
    private final boolean mediation;
    private final String whereTheyLive;
    private final PartnerAndDependentsLRspec partnerAndDependent;
    private final List<EmployerDetailsLRspec> employerDetails;
    private final Respondent1SelfEmploymentLRspec selfEmployment;
    private List<AccountSimpleTemplateData> bankAccountList;
    private final List<Respondent1CourtOrderDetails> courtOrderDetails;
    private final List<DebtTemplateData> debtList;
    private final List<ReasonMoneyTemplateData> incomeList;
    private final List<ReasonMoneyTemplateData> expenseList;
    private final int childrenMaintenance;

    public String getResponseTypeDisplay() {
        // TODO localization?
        return responseType.getDisplayedValue();
    }

    public boolean isCurrentlyWorking() {
        return (employerDetails != null && !employerDetails.isEmpty())
            || selfEmployment != null && selfEmployment.getAnnualTurnover() != null;
    }

    @JsonIgnore
    public static SealedClaimLipResponseForm toTemplate(final CaseData caseData) {
        SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder =  SealedClaimLipResponseForm.builder()
            .generationDate(LocalDate.now())
            .responseType(caseData.getRespondent1ClaimResponseTypeForSpec())
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimant1(LipDefenceFormParty.from(caseData.getApplicant1()))
            .defendant1(LipDefenceFormParty.from(caseData.getRespondent1()))
            .defendant2(LipDefenceFormParty.from(caseData.getRespondent2()))
            .mediation(caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES)
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
            .partnerAndDependent(caseData.getRespondent1PartnerAndDependent())
            .selfEmployment(caseData.getSpecDefendant1SelfEmploymentDetails())
            .debtList(mapToDebtList(caseData.getSpecDefendant1Debts()));
        addSolicitorDetails(caseData, builder);
        addEmployeeDetails(caseData, builder);
        addDQ(caseData, builder);
        return builder.build();

    }

    private static void addSolicitorDetails (final CaseData caseData, SealedClaimLipResponseFormBuilder builder) {
        Optional.ofNullable(caseData.getSolicitorReferences())
            .ifPresent(references ->
                           builder.claimantReferenceNumber(references.getApplicantSolicitor1Reference())
                               .defendantReferenceNumber(references.getRespondentSolicitor1Reference()));
    }

    private static void addEmployeeDetails(final CaseData caseData, SealedClaimLipResponseFormBuilder builder) {
        Optional.ofNullable(caseData.getResponseClaimAdmitPartEmployer())
            .map(Respondent1EmployerDetailsLRspec::getEmployerDetails)
            .map(ElementUtils::unwrapElements)
            .ifPresent(builder::employerDetails);

    }

    private static void addDQ(final CaseData caseData, SealedClaimLipResponseFormBuilder builder) {
        if (caseData.getRespondent1DQ() != null) {
            Optional.ofNullable(caseData.getRespondent1DQ().getRespondent1BankAccountList())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream().map(AccountSimpleTemplateData::new).collect(Collectors.toList()))
                .ifPresent(builder::bankAccountList);
            Optional.ofNullable(caseData.getRespondent1DQ().getRespondent1DQRecurringIncome())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream()
                    .map(item -> ReasonMoneyTemplateData.toReasonMoneyTemplateData(item)).collect(Collectors.toList()))
                .ifPresent(builder::incomeList);
            Optional.ofNullable(caseData.getRespondent1DQ().getRespondent1DQRecurringExpenses())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream()
                    .map(item ->
                             ReasonMoneyTemplateData.toReasonMoneyTemplateData(item)).collect(Collectors.toList()))
                .ifPresent(builder::expenseList);
        }

        Optional.ofNullable(caseData.getRespondent1CourtOrderDetails())
            .map(ElementUtils::unwrapElements)
            .ifPresent(builder::courtOrderDetails);

        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
            == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
            == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE) {
            Optional.ofNullable(caseData.getRespondent1DQ())
                .map(Respondent1DQ::getRespondent1DQHomeDetails)
                .map(HomeDetails::getType)
                .ifPresent(type -> {
                    switch (type) {
                        case OWNED_HOME:
                            builder.whereTheyLive("Home they own or pay a mortgage on");
                            break;
                        case PRIVATE_RENTAL:
                            builder.whereTheyLive("Private rental");
                            break;
                        case ASSOCIATION_HOME:
                            builder.whereTheyLive("Council or housing association home");
                            break;
                        case JOINTLY_OWNED_HOME:
                            builder.whereTheyLive("Jointly-owned home (or jointly mortgaged home)");
                            break;
                        default:
                            builder.whereTheyLive("Other");
                            break;
                    }
                });
        }

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION:
                    fullAdmissionData(caseData, builder, caseData.getTotalClaimAmount());
                    break;
                case PART_ADMISSION:
                    partAdmissionData(caseData, builder);
                    break;
                case FULL_DEFENCE:
                    fullDefenceData(caseData, builder);
                    break;
                default:
                    builder.whyReject("COUNTER_CLAIM");
                    break;
            }
        }

    }

    @JsonIgnore
    private static void fullAdmissionData(final CaseData caseData,
                                   SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder,
                                   BigDecimal totalClaimAmount) {
        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
            == RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY) {
            builder.payBy(LocalDate.now()
                              .plusDays(RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY))
                .amountToPay(totalClaimAmount + "");
        } else if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
            == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN) {
            builder.repaymentPlan(caseData.getRespondent1RepaymentPlan())
                .payBy(caseData.getRespondent1RepaymentPlan()
                           .finalPaymentBy(totalClaimAmount))
                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
        } else if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
            == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE) {
            builder.payBy(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())
                .amountToPay(totalClaimAmount + "")
                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
        }
    }


    @JsonIgnore
    private static void fullDefenceData(CaseData caseData, SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder) {
        builder.freeTextWhyReject(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .timelineEventList(caseData.getSpecResponseTimelineOfEvents().stream()
                                   .map(event ->
                                            EventTemplateData.builder()
                                                .date(event.getValue().getTimelineDate())
                                                .explanation(event.getValue().getTimelineDescription())
                                                .build()).collect(Collectors.toList()));
        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED
            .equals(caseData.getDefenceRouteRequired())) {
            RespondToClaim respondToClaim = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .orElse(caseData.getRespondToClaim());
            builder.whyReject("ALREADY_PAID")
                .howMuchWasPaid(respondToClaim.getHowMuchWasPaid() + "")
                .paymentDate(respondToClaim.getWhenWasThisAmountPaid())
                .paymentHow(respondToClaim.getHowWasThisAmountPaid() == PaymentMethod.OTHER
                                ? respondToClaim.getHowWasThisAmountPaidOther()
                                : respondToClaim.getHowWasThisAmountPaid()
                    .getHumanFriendly());
        } else if (SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
            .equals(caseData.getDefenceRouteRequired())) {
            builder.whyReject("DISPUTE");
        }
    }

    @JsonIgnore
    private static void partAdmissionData(CaseData caseData, SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder) {
        builder.freeTextWhyReject(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .timelineEventList(caseData.getSpecResponseTimelineOfEvents().stream()
                                   .map(event ->
                                            EventTemplateData.builder()
                                                .date(event.getValue().getTimelineDate())
                                                .explanation(event.getValue().getTimelineDescription())
                                                .build()).collect(Collectors.toList()));
        if (caseData.getSpecDefenceAdmittedRequired() == YesOrNo.YES) {
            RespondToClaim respondToClaim = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .orElse(caseData.getRespondToClaim());
            builder.whyReject("ALREADY_PAID")
                .howMuchWasPaid(respondToClaim.getHowMuchWasPaid() + "")
                .paymentDate(respondToClaim.getWhenWasThisAmountPaid())
                .paymentHow(respondToClaim.getHowWasThisAmountPaid() == PaymentMethod.OTHER
                                ? respondToClaim.getHowWasThisAmountPaidOther()
                                : respondToClaim.getHowWasThisAmountPaid()
                    .getHumanFriendly());
        } else {
            if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                == RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY) {
                builder.payBy(LocalDate.now()
                                  .plusDays(RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY))
                    .amountToPay(caseData.getRespondToAdmittedClaimOwingAmount() + "");
            } else if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN) {
                builder.repaymentPlan(caseData.getRespondent1RepaymentPlan())
                    .payBy(caseData.getRespondent1RepaymentPlan()
                               .finalPaymentBy(caseData.getRespondToAdmittedClaimOwingAmount()))
                    .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
            } else if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()
                == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE) {
                builder.payBy(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())
                    .amountToPay(caseData.getRespondToAdmittedClaimOwingAmount() + "")
                    .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
            }
        }
    }


    @JsonIgnore
    private static List<DebtTemplateData> mapToDebtList(Respondent1DebtLRspec debtLRspec) {
        if (debtLRspec != null) {
            List<DebtTemplateData> debtList = new ArrayList<>();
            Optional.ofNullable(debtLRspec.getDebtDetails())
                .map(ElementUtils::unwrapElements)
                .ifPresent(list -> list.stream()
                    .map(SealedClaimLipResponseForm::mapGeneralDebt)
                    .forEach(debtList::add));

            Optional.ofNullable(debtLRspec.getLoanCardDebtDetails())
                .ifPresent(list -> list.stream().map(e -> DebtTemplateData.loanDebtFrom(e.getValue()))
                    .forEach(debtList::add));
            return debtList;
        }
        return Collections.emptyList();
    }

    @JsonIgnore
    private static DebtTemplateData mapGeneralDebt(DebtLRspec debt) {
        return DebtTemplateData.generalDebtFrom(debt);
    }



}
