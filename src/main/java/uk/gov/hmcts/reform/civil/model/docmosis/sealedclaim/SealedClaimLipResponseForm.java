package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChildrenByAgeGroupLRspec;
import uk.gov.hmcts.reform.civil.model.EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.Respondent1CourtOrderDetails;
import uk.gov.hmcts.reform.civil.model.Respondent1DebtLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1SelfEmploymentLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.LipFormParty;
import uk.gov.hmcts.reform.civil.model.docmosis.common.AccommodationTemplate;
import uk.gov.hmcts.reform.civil.model.docmosis.common.AccountSimpleTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.DebtTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.ReasonMoneyTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;

@Getter
@Builder
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
    private final RepaymentPlanTemplateData repaymentPlan;
    private final RespondentResponseTypeSpec responseType;
    private final String whyReject;
    private final String freeTextWhyReject;
    private final LipFormParty claimant1;
    private final LipFormParty defendant1;
    private final LipFormParty defendant2;
    private final List<EventTemplateData> timelineEventList;
    private final String timelineComments;
    private final List<EvidenceTemplateData> evidenceList;
    private final String evidenceComments;
    private final boolean mediation;
    private final AccommodationTemplate whereTheyLive;
    private final PartnerAndDependentsLRspec partnerAndDependent;
    private final List<EmployerDetailsLRspec> employerDetails;
    private final Respondent1SelfEmploymentLRspec selfEmployment;
    private final List<AccountSimpleTemplateData> bankAccountList;
    private final List<Respondent1CourtOrderDetails> courtOrderDetails;
    private final List<DebtTemplateData> debtList;
    private final List<ReasonMoneyTemplateData> incomeList;
    private final List<ReasonMoneyTemplateData> expenseList;

    public String getResponseTypeDisplay() {
        return responseType.getDisplayedValue();
    }

    public boolean isCurrentlyWorking() {
        return (employerDetails != null && !employerDetails.isEmpty())
            || selfEmployment != null && selfEmployment.getAnnualTurnover() != null;
    }

    public int getNumberOfChildren() {
        return Optional.ofNullable(partnerAndDependent).map(PartnerAndDependentsLRspec::getHowManyChildrenByAgeGroup)
            .map(ChildrenByAgeGroupLRspec::getTotalChildren).orElse(0);
    }

    @JsonIgnore
    public static SealedClaimLipResponseForm toTemplate(final CaseData caseData) {
        SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder = SealedClaimLipResponseForm.builder()
            .generationDate(LocalDate.now())
            .responseType(caseData.getRespondent1ClaimResponseTypeForSpec())
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimant1(LipFormParty.toLipDefenceParty(caseData.getApplicant1()))
            .defendant1(LipFormParty.toLipDefenceParty(
                caseData.getRespondent1(),
                caseData.getRespondent1CorrespondanceAddress()
            ))
            .defendant2(LipFormParty.toLipDefenceParty(caseData.getRespondent2()))
            .mediation(caseData.getResponseClaimMediationSpecRequired() == YesOrNo.YES)
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec())
            .partnerAndDependent(caseData.getRespondent1PartnerAndDependent())
            .debtList(mapToDebtList(caseData.getSpecDefendant1Debts()));
        addSolicitorDetails(caseData, builder);
        addEmployeeDetails(caseData, builder);
        addFinancialDetails(caseData, builder);
        addSelfEmploymentDetails(caseData, builder);
        addCourtOrderDetails(caseData, builder);

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            builder.howToPay(caseData.getDefenceAdmitPartPaymentTimeRouteRequired());
            switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_ADMISSION -> addRepaymentMethod(caseData, builder, caseData.getTotalClaimAmount());
                case PART_ADMISSION -> partAdmissionData(caseData, builder);
                case FULL_DEFENCE -> fullDefenceData(caseData, builder);
                case COUNTER_CLAIM -> builder.whyReject(COUNTER_CLAIM.name());
                default -> builder.whyReject(null);
            }
        }
        return builder.build();

    }

    private static void addSolicitorDetails(final CaseData caseData, SealedClaimLipResponseFormBuilder builder) {
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

    private static void addPayBySetDate(CaseData caseData, SealedClaimLipResponseFormBuilder builder, BigDecimal totalClaimAmount) {
        builder.payBy(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())
            .amountToPay(totalClaimAmount + "")
            .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
    }

    private static void addPayByDatePayImmediately(SealedClaimLipResponseFormBuilder builder, BigDecimal totalClaimAmount) {
        builder.payBy(LocalDate.now()
                          .plusDays(RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY))
            .amountToPay(totalClaimAmount + "");
    }

    private static void addRepaymentPlan(CaseData caseData, SealedClaimLipResponseFormBuilder builder, BigDecimal totalClaimAmount) {
        if (caseData.getRespondent1RepaymentPlan() != null) {
            builder.repaymentPlan(RepaymentPlanTemplateData.builder()
                                      .paymentFrequencyDisplay(caseData.getRespondent1RepaymentPlan().getPaymentFrequencyDisplay())
                                      .firstRepaymentDate(caseData.getRespondent1RepaymentPlan().getFirstRepaymentDate())
                                      .paymentAmount(MonetaryConversions.penniesToPounds(caseData.getRespondent1RepaymentPlan().getPaymentAmount()))
                                      .build())
                .payBy(caseData.getRespondent1RepaymentPlan()
                           .finalPaymentBy(totalClaimAmount))
                .whyNotPayImmediately(caseData.getResponseToClaimAdmitPartWhyNotPayLRspec());
        }
    }

    private static void alreadyPaid(CaseData caseData, SealedClaimLipResponseFormBuilder builder) {
        RespondToClaim respondToClaim = caseData.getResponseToClaim();
        builder.whyReject("ALREADY_PAID")
            .howMuchWasPaid(MonetaryConversions.penniesToPounds(respondToClaim.getHowMuchWasPaid()) + "")
            .paymentDate(respondToClaim.getWhenWasThisAmountPaid())
            .paymentHow(respondToClaim.getHowWasThisAmountPaid() == PaymentMethod.OTHER
                            ? respondToClaim.getHowWasThisAmountPaidOther()
                            : respondToClaim.getHowWasThisAmountPaid()
                .getHumanFriendly());
    }

    private static void addDetailsOnWhyClaimIsRejected(CaseData caseData, SealedClaimLipResponseFormBuilder builder) {
        builder.freeTextWhyReject(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .timelineComments(Optional.ofNullable(caseData.getCaseDataLiP()).map(CaseDataLiP::getTimeLineComment).orElse(
                ""))
            .timelineEventList(Optional.ofNullable(caseData.getSpecResponseTimelineOfEvents()).map(Collection::stream)
                                   .orElseGet(Stream::empty)
                                   .map(event ->
                                            EventTemplateData.builder()
                                                .date(event.getValue().getTimelineDate())
                                                .explanation(event.getValue().getTimelineDescription())
                                                .build()).collect(Collectors.toList()))
            .evidenceComments(Optional.ofNullable(caseData.getCaseDataLiP()).map(CaseDataLiP::getEvidenceComment).orElse(
                ""))
            .evidenceList(Optional.ofNullable(caseData.getSpecResponselistYourEvidenceList()).map(Collection::stream)
                              .orElseGet(Stream::empty)
                              .map(evidence -> EvidenceTemplateData.toEvidenceTemplateData(evidence))
                              .toList());
    }

    private static void fullDefenceData(CaseData caseData, SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder) {
        addDetailsOnWhyClaimIsRejected(caseData, builder);
        if (caseData.hasDefendantPayedTheAmountClaimed()) {
            alreadyPaid(caseData, builder);
        } else if (caseData.isClaimBeingDisputed()) {
            builder.whyReject("DISPUTE");
        }
    }

    @JsonIgnore
    private static void partAdmissionData(CaseData caseData, SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder) {
        addDetailsOnWhyClaimIsRejected(caseData, builder);
        if (caseData.getSpecDefenceAdmittedRequired() == YesOrNo.YES) {
            alreadyPaid(caseData, builder);
        } else {
            addRepaymentMethod(
                caseData,
                builder,
                MonetaryConversions.penniesToPounds(caseData.getRespondToAdmittedClaimOwingAmount())
            );
        }
    }

    private static void addSelfEmploymentDetails(CaseData caseData, SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder) {
        Optional.ofNullable(caseData.getSpecDefendant1SelfEmploymentDetails())
            .ifPresent(selfEmployDetails ->
                           builder.selfEmployment(Respondent1SelfEmploymentLRspec.builder()
                                                      .amountOwed(selfEmployDetails.getAmountOwed() != null
                                                                      ? MonetaryConversions.penniesToPounds(
                                                          selfEmployDetails.getAmountOwed())
                                                                      : null)
                                                      .annualTurnover(MonetaryConversions.penniesToPounds(
                                                          selfEmployDetails.getAnnualTurnover()))
                                                      .jobTitle(selfEmployDetails.getJobTitle())
                                                      .reason(selfEmployDetails.getReason())
                                                      .build())
            );
    }

    private static void addRepaymentMethod(CaseData caseData, SealedClaimLipResponseFormBuilder builder, BigDecimal totalAmount) {
        if (caseData.isPayImmediately()) {
            addPayByDatePayImmediately(builder, totalAmount);
        } else if (caseData.isPayByInstallment()) {
            addRepaymentPlan(caseData, builder, totalAmount);
        } else if (caseData.isPayBySetDate()) {
            addPayBySetDate(caseData, builder, totalAmount);
        }
    }

    @JsonIgnore
    private static List<DebtTemplateData> mapToDebtList(Respondent1DebtLRspec debtLRspec) {
        return Optional.ofNullable(debtLRspec).map(debt -> createDebtList(debt)).orElse(Collections.emptyList());
    }

    @JsonIgnore
    private static List<DebtTemplateData> createDebtList(Respondent1DebtLRspec debtLRspec) {
        List<DebtTemplateData> debtList = new ArrayList<>();
        Optional.ofNullable(debtLRspec.getDebtDetails())
            .map(ElementUtils::unwrapElements)
            .ifPresent(list -> list.stream()
                .map(DebtTemplateData::generalDebtFrom)
                .forEach(debtList::add));

        Optional.ofNullable(debtLRspec.getLoanCardDebtDetails())
            .ifPresent(list -> list.stream().map(e -> DebtTemplateData.loanDebtFrom(e.getValue()))
                .forEach(debtList::add));
        return debtList;
    }

    @JsonIgnore
    private static void addFinancialDetails(CaseData caseData, SealedClaimLipResponseFormBuilder builder) {
        if (caseData.getRespondent1DQ() != null) {
            builder.whereTheyLive(new AccommodationTemplate(caseData.getRespondent1DQ().getRespondent1DQHomeDetails()));
            Optional.ofNullable(caseData.getRespondent1DQ().getRespondent1BankAccountList())
                .map(ElementUtils::unwrapElements)
                .map(list -> IntStream.range(0, list.size()).mapToObj(i -> new AccountSimpleTemplateData(
                    list.get(i),
                    i + 1
                )).collect(Collectors.toList()))
                .ifPresent(builder::bankAccountList);
            Optional.ofNullable(caseData.getRecurringIncomeForRespondent1())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream()
                    .map(item -> ReasonMoneyTemplateData.toReasonMoneyTemplateData(item)).collect(Collectors.toList()))
                .ifPresent(builder::incomeList);
            Optional.ofNullable(caseData.getRecurringExpensesForRespondent1())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream()
                    .map(item ->
                             ReasonMoneyTemplateData.toReasonMoneyTemplateData(item)).collect(Collectors.toList()))
                .ifPresent(builder::expenseList);
        }
    }

    private static void addCourtOrderDetails(final CaseData caseData, SealedClaimLipResponseFormBuilder builder) {
        builder.courtOrderDetails(
            Optional.ofNullable(caseData.getRespondent1CourtOrderDetails()).map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(courtOrderDetails -> Respondent1CourtOrderDetails.builder()
                    .claimNumberText(courtOrderDetails.getValue().getClaimNumberText())
                    .amountOwed(MonetaryConversions.penniesToPounds(courtOrderDetails.getValue().getAmountOwed()))
                    .monthlyInstalmentAmount(MonetaryConversions.penniesToPounds(courtOrderDetails.getValue().getMonthlyInstalmentAmount()))
                    .build())
                .toList()
        );
    }

}
