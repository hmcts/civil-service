package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SealedClaimLipResponseForm implements MappableObject {

    private String claimReferenceNumber;
    private String ccdCaseReference;
    private String claimantReferenceNumber;
    private String defendantReferenceNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate generationDate;
    private LipFormPartyDefence claimant1;
    private LipFormPartyDefence defendant1;
    private LipFormPartyDefence defendant2;
    private AccommodationTemplate whereTheyLive;
    private PartnerAndDependentsLRspec partnerAndDependent;
    private List<EmployerDetailsLRspec> employerDetails;
    private Respondent1SelfEmploymentLRspec selfEmployment;
    private List<AccountSimpleTemplateData> bankAccountList;
    private List<Respondent1CourtOrderDetails> courtOrderDetails;
    private List<DebtTemplateData> debtList;
    private List<ReasonMoneyTemplateData> incomeList;
    private List<ReasonMoneyTemplateData> expenseList;
    //repayment details for repayment plan that are common between LR and LiP
    private ResponseRepaymentDetailsForm commonDetails;

    //CARM defendant Mediation Fields
    private String defendant1MediationContactNumber;
    private String defendant1MediationEmail;
    private String defendant1MediationCompanyName;
    private boolean defendant1MediationUnavailableDatesExists;
    private List<Element<UnavailableDate>> defendant1UnavailableDatesList;
    private boolean checkCarmToggle;
    private StatementOfTruth uiStatementOfTruth;
    private String faContent;

    public SealedClaimLipResponseForm copy() {
        return new SealedClaimLipResponseForm(
            claimReferenceNumber,
            ccdCaseReference,
            claimantReferenceNumber,
            defendantReferenceNumber,
            generationDate,
            claimant1,
            defendant1,
            defendant2,
            whereTheyLive,
            partnerAndDependent,
            employerDetails,
            selfEmployment,
            bankAccountList,
            courtOrderDetails,
            debtList,
            incomeList,
            expenseList,
            commonDetails,
            defendant1MediationContactNumber,
            defendant1MediationEmail,
            defendant1MediationCompanyName,
            defendant1MediationUnavailableDatesExists,
            defendant1UnavailableDatesList,
            checkCarmToggle,
            uiStatementOfTruth,
            faContent
        );
    }

    @JsonIgnore
    public static SealedClaimLipResponseForm toTemplate(CaseData caseData, BigDecimal admittedAmount) {
        SealedClaimLipResponseForm form = new SealedClaimLipResponseForm()
            .setGenerationDate(LocalDate.now())
            .setCcdCaseReference(formatCcdCaseReference(caseData))
            .setClaimReferenceNumber(caseData.getLegacyCaseReference())
            .setClaimant1(LipFormPartyDefence.toLipDefenceParty(caseData.getApplicant1()))
            .setDefendant1(LipFormPartyDefence.toLipDefenceParty(
                caseData.getRespondent1(),
                caseData.getRespondent1CorrespondanceAddress()
            ))
            .setDefendant2(LipFormPartyDefence.toLipDefenceParty(caseData.getRespondent2()))
            .setPartnerAndDependent(caseData.getRespondent1PartnerAndDependent())
            .setDebtList(mapToDebtList(caseData.getSpecDefendant1Debts()))
            .setCommonDetails(ResponseRepaymentDetailsForm.toSealedClaimResponseCommonContent(caseData, admittedAmount))
            .setFaContent(getAdditionContent(caseData))
            .setUiStatementOfTruth(caseData.getRespondent1LiPStatementOfTruth());
        addSolicitorDetails(caseData, form);
        addEmployeeDetails(caseData, form);
        addFinancialDetails(caseData, form);
        addSelfEmploymentDetails(caseData, form);
        addCourtOrderDetails(caseData, form);
        return form;

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

    private static void addSolicitorDetails(CaseData caseData, SealedClaimLipResponseForm form) {
        Optional.ofNullable(caseData.getSolicitorReferences())
            .ifPresent(references ->
                           form.setClaimantReferenceNumber(references.getApplicantSolicitor1Reference())
                               .setDefendantReferenceNumber(references.getRespondentSolicitor1Reference()));
    }

    private static void addEmployeeDetails(CaseData caseData, SealedClaimLipResponseForm form) {
        Optional.ofNullable(caseData.getResponseClaimAdmitPartEmployer())
            .map(Respondent1EmployerDetailsLRspec::getEmployerDetails)
            .map(ElementUtils::unwrapElements)
            .ifPresent(form::setEmployerDetails);

    }

    private static void addSelfEmploymentDetails(CaseData caseData, SealedClaimLipResponseForm form) {
        Optional.ofNullable(caseData.getSpecDefendant1SelfEmploymentDetails())
            .ifPresent(selfEmployDetails ->
                           form.setSelfEmployment(new Respondent1SelfEmploymentLRspec()
                                                     .setAmountOwed(selfEmployDetails.getAmountOwed() != null
                                                                     ? (MonetaryConversions.penniesToPounds(
                                                         selfEmployDetails.getAmountOwed())).setScale(2, RoundingMode.CEILING)
                                                                     : null)
                                                     .setAnnualTurnover(selfEmployDetails.getAnnualTurnover() != null
                                                                         ? (MonetaryConversions.penniesToPounds(
                                                         selfEmployDetails.getAnnualTurnover())).setScale(2, RoundingMode.CEILING)
                                                         : null)
                                                     .setJobTitle(selfEmployDetails.getJobTitle())
                                                     .setReason(selfEmployDetails.getReason())
                                                     )
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
    private static void addFinancialDetails(CaseData caseData, SealedClaimLipResponseForm form) {
        if (caseData.getRespondent1DQ() != null) {
            form.setWhereTheyLive(new AccommodationTemplate(caseData.getRespondent1DQ().getRespondent1DQHomeDetails()));
            Optional.ofNullable(caseData.getRespondent1DQ().getRespondent1BankAccountList())
                .map(ElementUtils::unwrapElements)
                .map(list -> IntStream.range(0, list.size()).mapToObj(i -> new AccountSimpleTemplateData(
                    list.get(i),
                    i + 1
                )).toList())
                .ifPresent(form::setBankAccountList);
            Optional.ofNullable(caseData.getRecurringIncomeForRespondent1())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream()
                    .map(ReasonMoneyTemplateData::toReasonMoneyTemplateData).collect(Collectors.toList()))
                .ifPresent(form::setIncomeList);
            Optional.ofNullable(caseData.getRecurringExpensesForRespondent1())
                .map(ElementUtils::unwrapElements)
                .map(list -> list.stream()
                    .map(ReasonMoneyTemplateData::toReasonMoneyTemplateData).toList())
                .ifPresent(form::setExpenseList);
        }
    }

    private static void addCourtOrderDetails(CaseData caseData, SealedClaimLipResponseForm form) {
        form.setCourtOrderDetails(
            Optional.ofNullable(caseData.getRespondent1CourtOrderDetails()).map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(courtOrderDetails -> new Respondent1CourtOrderDetails()
                    .setClaimNumberText(courtOrderDetails.getValue().getClaimNumberText())
                    .setAmountOwed(MonetaryConversions.penniesToPounds(courtOrderDetails.getValue().getAmountOwed()))
                    .setMonthlyInstalmentAmount(courtOrderDetails.getValue().getMonthlyInstalmentAmount() == null ? null
                        : MonetaryConversions.penniesToPounds(courtOrderDetails.getValue().getMonthlyInstalmentAmount())
                    )
                    )
                .toList()
        );
    }

}
