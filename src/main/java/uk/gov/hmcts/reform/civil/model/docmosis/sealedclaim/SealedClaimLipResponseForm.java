package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1CourtOrderDetails;
import uk.gov.hmcts.reform.civil.model.Respondent1DebtLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1SelfEmploymentLRspec;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.AccommodationTemplate;
import uk.gov.hmcts.reform.civil.model.docmosis.common.AccountSimpleTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.DebtTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.ReasonMoneyTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormPartyDefence;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.formatCcdCaseReference;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimLipResponseForm implements MappableObject {

    private final String claimReferenceNumber;
    private final String ccdCaseReference;
    private final String claimantReferenceNumber;
    private final String defendantReferenceNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate generationDate;
    private final LipFormPartyDefence claimant1;
    private final LipFormPartyDefence defendant1;
    private final LipFormPartyDefence defendant2;
    private final AccommodationTemplate whereTheyLive;
    private final PartnerAndDependentsLRspec partnerAndDependent;
    private final List<EmployerDetailsLRspec> employerDetails;
    private final Respondent1SelfEmploymentLRspec selfEmployment;
    private final List<AccountSimpleTemplateData> bankAccountList;
    private final List<Respondent1CourtOrderDetails> courtOrderDetails;
    private final List<DebtTemplateData> debtList;
    private final List<ReasonMoneyTemplateData> incomeList;
    private final List<ReasonMoneyTemplateData> expenseList;
    //repayment details for repayment plan that are common between LR and LiP
    private final ResponseRepaymentDetailsForm commonDetails;

    //CARM defendant Mediation Fields
    private final String defendant1MediationContactNumber;
    private final String defendant1MediationEmail;
    private final String defendant1MediationCompanyName;
    private final boolean defendant1MediationUnavailableDatesExists;
    private final List<Element<UnavailableDate>> defendant1UnavailableDatesList;
    private final boolean checkCarmToggle;
    private final StatementOfTruth uiStatementOfTruth;
    private final String faContent;

    @JsonIgnore
    public static SealedClaimLipResponseForm toTemplate(final CaseData caseData, BigDecimal admittedAmount) {
        SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder = SealedClaimLipResponseForm.builder()
            .generationDate(LocalDate.now())
            .ccdCaseReference(formatCcdCaseReference(caseData))
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimant1(LipFormPartyDefence.toLipDefenceParty(caseData.getApplicant1()))
            .defendant1(LipFormPartyDefence.toLipDefenceParty(
                caseData.getRespondent1(),
                caseData.getRespondent1CorrespondanceAddress()
            ))
            .defendant2(LipFormPartyDefence.toLipDefenceParty(caseData.getRespondent2()))
            .partnerAndDependent(caseData.getRespondent1PartnerAndDependent())
            .debtList(mapToDebtList(caseData.getSpecDefendant1Debts()))
            .commonDetails(ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData, admittedAmount, false))
            .faContent(getAdditionContent(caseData))
            .uiStatementOfTruth(caseData.getRespondent1LiPStatementOfTruth());
        addSolicitorDetails(caseData, builder);
        addEmployeeDetails(caseData, builder);
        addFinancialDetails(caseData, builder);
        addSelfEmploymentDetails(caseData, builder);
        addCourtOrderDetails(caseData, builder);
        return builder.build();

    }

    private static String getAdditionContent(CaseData caseData) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() != null) {
            if (FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                if (caseData.isPayImmediately() || caseData.isPayBySetDate()) {
                    return "This amount includes interest if it has been claimed which may continue to accrue on the" +
                        " amount outstanding up to the date of Judgment, settlement agreement or earlier payment." +
                        "\n" +
                        "The amount does not include the claim fee and any fixed costs which are payable in addition.";

                } else if (caseData.isPayByInstallment()) {
                    return "The final payment date may be later to reflect any additional interest, " +
                        "any fixed costs and claim fee added to the judgment, settlement agreement or earlier payment amount";
                }
            } else if (PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && caseData.isPayImmediately() || caseData.isPayBySetDate()) {
                return "The claim fee and any fixed costs claimed are not included in this figure but are payable in addition and if judgment," +
                    " settlement agreement or earlier payment is entered on an admission will be included in the total amount.";
            }
        }
        return "";
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

    private static void addSelfEmploymentDetails(CaseData caseData, SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder) {
        Optional.ofNullable(caseData.getSpecDefendant1SelfEmploymentDetails())
            .ifPresent(selfEmployDetails ->
                           builder.selfEmployment(Respondent1SelfEmploymentLRspec.builder()
                                                      .amountOwed(selfEmployDetails.getAmountOwed() != null
                                                                      ? (MonetaryConversions.penniesToPounds(
                                                          selfEmployDetails.getAmountOwed())).setScale(2, RoundingMode.CEILING)
                                                                      : null)
                                                      .annualTurnover(selfEmployDetails.getAnnualTurnover() != null
                                                                          ? (MonetaryConversions.penniesToPounds(
                                                          selfEmployDetails.getAnnualTurnover())).setScale(2, RoundingMode.CEILING)
                                                          : null)
                                                      .jobTitle(selfEmployDetails.getJobTitle())
                                                      .reason(selfEmployDetails.getReason())
                                                      .build())
            );
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
                )).toList())
                .ifPresent(builder::bankAccountList);
            Optional.ofNullable(caseData.getRecurringIncomeForRespondent1())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream()
                    .map(ReasonMoneyTemplateData::toReasonMoneyTemplateData).collect(Collectors.toList()))
                .ifPresent(builder::incomeList);
            Optional.ofNullable(caseData.getRecurringExpensesForRespondent1())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream()
                    .map(ReasonMoneyTemplateData::toReasonMoneyTemplateData).toList())
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
