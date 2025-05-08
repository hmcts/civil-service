package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Respondent1CourtOrderDetails;
import uk.gov.hmcts.reform.civil.model.Respondent1DebtLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1SelfEmploymentLRspec;
import uk.gov.hmcts.reform.civil.model.docmosis.common.AccommodationTemplate;
import uk.gov.hmcts.reform.civil.model.docmosis.common.AccountSimpleTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.DebtTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.ReasonMoneyTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormPartyDefence;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

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

import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.formatCcdCaseReference;

@Component
@RequiredArgsConstructor
public class SealedClaimLipResponseFormMapper {

    private final ResponseRepaymentDetailsFormMapper responseRepaymentDetailsFormMapper;

    public SealedClaimLipResponseForm toTemplate(final CaseData caseData) {
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
            .commonDetails(responseRepaymentDetailsFormMapper.toResponsePaymentDetails(caseData));
        addSolicitorDetails(caseData, builder);
        addEmployeeDetails(caseData, builder);
        addFinancialDetails(caseData, builder);
        addSelfEmploymentDetails(caseData, builder);
        addCourtOrderDetails(caseData, builder);
        return builder.build();

    }

    private static void addSolicitorDetails(final CaseData caseData, SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder) {
        Optional.ofNullable(caseData.getSolicitorReferences())
            .ifPresent(references ->
                builder.claimantReferenceNumber(references.getApplicantSolicitor1Reference())
                    .defendantReferenceNumber(references.getRespondentSolicitor1Reference()));
    }

    private static void addEmployeeDetails(final CaseData caseData, SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder) {
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
    private static void addFinancialDetails(CaseData caseData, SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder) {
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

    private static void addCourtOrderDetails(final CaseData caseData, SealedClaimLipResponseForm.SealedClaimLipResponseFormBuilder builder) {
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
