package uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.AdditionalLipPartyDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.lip.LipFormParty;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getApplicant;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getApplicantSolicitorRef;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getOrgDetails;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getPartyDetails;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getRespondent1SolicitorRef;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getRespondent2SolicitorRef;

@Component
@RequiredArgsConstructor
public class JudgmentByAdmissionOrDeterminationMapper {

    private final DeadlineExtensionCalculatorService deadlineCalculatorService;
    private final JudgementService judgementService;
    private final OrganisationService organisationService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy");

    public JudgmentByAdmissionOrDetermination toClaimantResponseForm(CaseData caseData, CaseEvent caseEvent) {
        Optional<CaseDataLiP> caseDataLip = Optional.ofNullable(caseData.getCaseDataLiP());
        Optional<AdditionalLipPartyDetails> applicantDetails =
            caseDataLip.map(CaseDataLiP::getApplicant1AdditionalLipPartyDetails);
        Optional<AdditionalLipPartyDetails> defendantDetails =
            caseDataLip.map(CaseDataLiP::getRespondent1AdditionalLipPartyDetails);
        caseData.getApplicant1().setPartyEmail(caseData.getClaimantUserDetails() != null
                                                   ? caseData.getClaimantUserDetails().getEmail() : null);
        LipFormParty claimant = LipFormParty.toLipFormParty(
            caseData.getApplicant1(),
            getCorrespondenceAddress(applicantDetails),
            getContactPerson(applicantDetails)
        );

        LipFormParty defendant = LipFormParty.toLipFormParty(
            caseData.getRespondent1(),
            getCorrespondenceAddress(defendantDetails),
            getContactPerson(defendantDetails)
        );

        String totalClaimAmount = Optional.ofNullable(caseData.getTotalClaimAmount())
            .map(amount -> amount.setScale(2).toString())
            .orElse("0");

        String totalInterest = judgementService.ccjJudgmentInterest(caseData).setScale(2).toString();

        JudgmentByAdmissionOrDetermination.JudgmentByAdmissionOrDeterminationBuilder builder = new JudgmentByAdmissionOrDetermination.JudgmentByAdmissionOrDeterminationBuilder();
        LocalDateTime now = LocalDateTime.now();
        ApplicantResponsePaymentPlan paymentPlan = getPaymentType(caseData);
        return builder
            .formHeader(getFormHeader(caseData, caseEvent))
            .formName(getFormName(caseData))
            .claimant(claimant)
            .defendant(defendant)
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .totalClaimAmount(totalClaimAmount)
            .totalInterestAmount(totalInterest)
            .paymentType(paymentPlan)
            .paymentTypeDisplayValue(paymentPlan != null ? paymentPlan.getDisplayedValue() : null)
            .payBy(setPayByDate(caseData))
            .repaymentPlan(addRepaymentPlan(caseData))
            .ccjJudgmentAmount(judgementService.ccjJudgmentClaimAmount(caseData).setScale(2).toString())
            .ccjInterestToDate(totalInterest)
            .claimFee(getClaimFee(caseData))
            .ccjSubtotal(judgementService.ccjJudgementSubTotal(caseData).setScale(2).toString())
            .ccjAlreadyPaidAmount(getAlreadyPaidAmount(caseData))
            .ccjFinalTotal(judgementService.ccjJudgmentFinalTotal(caseData).setScale(2).toString())
            .defendantResponse(caseData.getRespondent1ClaimResponseTypeForSpec())
            .generationDate(now.toLocalDate())
            .generationDateTime(now.format(formatter))
            .build();
    }

    private String getClaimFee(CaseData caseData) {
        BigDecimal claimFee = judgementService.ccjJudgmentClaimFee(caseData);
        if (BigDecimal.ZERO.compareTo(claimFee) == 0) {
            return BigDecimal.ZERO.toString();
        }
        return claimFee.setScale(2).toString();
    }

    private String getAlreadyPaidAmount(CaseData caseData) {
        BigDecimal paidAmount = judgementService.ccjJudgmentPaidAmount(caseData);
        if (BigDecimal.ZERO.compareTo(paidAmount) == 0) {
            return BigDecimal.ZERO.toString();
        }
        return paidAmount.setScale(2).toString();
    }

    private LocalDate setPayByDate(CaseData caseData) {
        if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.SET_DATE)) {
            return caseData.getApplicant1RequestedPaymentDateForDefendantSpec().getPaymentSetDate();
        } else if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.IMMEDIATELY)) {
            return deadlineCalculatorService.calculateExtendedDeadline(
                LocalDate.now(),
                RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY);
        }

        return null;
    }

    private ApplicantResponsePaymentPlan getPaymentType(CaseData caseData) {
        if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.IMMEDIATELY)) {
            return ApplicantResponsePaymentPlan.IMMEDIATELY;
        } else if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.SET_DATE)) {
            return ApplicantResponsePaymentPlan.SET_DATE;
        } else if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.REPAYMENT_PLAN)) {
            return ApplicantResponsePaymentPlan.REPAYMENT_PLAN;
        }
        return null;
    }

    private Address getCorrespondenceAddress(Optional<AdditionalLipPartyDetails> partyDetails) {
        return partyDetails.map(AdditionalLipPartyDetails::getCorrespondenceAddress).orElse(null);
    }

    private String getContactPerson(Optional<AdditionalLipPartyDetails> partyDetails) {
        return partyDetails.map(AdditionalLipPartyDetails::getContactPerson).orElse(null);
    }

    private static RepaymentPlanTemplateData addRepaymentPlan(CaseData caseData) {
        RepaymentPlanTemplateData.RepaymentPlanTemplateDataBuilder builder = RepaymentPlanTemplateData.builder();
        if (caseData.getApplicant1RepaymentOptionForDefendantSpec().equals(PaymentType.REPAYMENT_PLAN)) {
            return builder
                .firstRepaymentDate(caseData.getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec())
                .paymentAmount(caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec().setScale(2))
                .paymentFrequencyDisplay(caseData.getApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec().getLabel())
                .build();
        }
        return null;
    }

    private String getFormHeader(CaseData caseData, CaseEvent caseEvent) {
        String formHeader = "Judgment by %s";
        String formType;
        if (YesOrNo.YES.equals(caseData.getApplicant1AcceptFullAdmitPaymentPlanSpec())
            || YesOrNo.YES.equals(caseData.getApplicant1AcceptPartAdmitPaymentPlanSpec())
            || caseEvent == CaseEvent.GENERATE_DEFAULT_JUDGMENT_BY_ADMISSION_RESPONSE_DOC) {
            formType = "admission";
        } else {
            formType = "determination";
        }
        return String.format(formHeader, formType);
    }

    private String getFormName(CaseData caseData) {
        return RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            ? "OCON225"
            : "OCON225a";
    }

    private Party getClaimantLipOrLRDetailsForPaymentAddress(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            return getPartyDetails(caseData.getApplicant1());
        } else {
            if (caseData.getApplicant1OrganisationPolicy() != null) {
                return getOrgDetails(caseData.getApplicant1OrganisationPolicy(), organisationService);
            } else {
                return null;
            }
        }
    }

    private Party getRespondentLROrLipDetails(CaseData caseData) {
        if (caseData.isRespondent1LiP()) {
            return getPartyDetails(caseData.getRespondent1());
        } else {
            if (caseData.getRespondent1OrganisationPolicy() != null) {
                return getOrgDetails(caseData.getRespondent1OrganisationPolicy(), organisationService);
            } else {
                return null;
            }
        }
    }

    public JudgmentByAdmissionOrDetermination toNonDivergentDocs(CaseData caseData) {
        String totalClaimAmount = Optional.ofNullable(caseData.getTotalClaimAmount())
            .map(amount -> amount.setScale(2).toString())
            .orElse("0");

        String totalInterest = judgementService.ccjJudgmentInterest(caseData).setScale(2).toString();

        JudgmentByAdmissionOrDetermination.JudgmentByAdmissionOrDeterminationBuilder builder = new JudgmentByAdmissionOrDetermination.JudgmentByAdmissionOrDeterminationBuilder();
        return builder
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .respondent1Name(caseData.getRespondent1().getPartyName())
            .respondent2Name(Objects.isNull(caseData.getRespondent2()) ? null : caseData.getRespondent2().getPartyName())
            .respondent1Ref(getRespondent1SolicitorRef(caseData))
            .respondent2Ref(getRespondent2SolicitorRef(caseData))
            .applicantReference(getApplicantSolicitorRef(caseData))
            .applicant(getClaimantLipOrLRDetailsForPaymentAddress(caseData))
            .applicants(getApplicant(caseData.getApplicant1(), caseData.getApplicant2()))
            .respondent(getRespondentLROrLipDetails(caseData))
            .totalClaimAmount(totalClaimAmount)
            .totalInterestAmount(totalInterest)
            .paymentPlan(getPaymentTypeForNonDivergent(caseData))
            .payByDate(caseData.getRespondToClaimAdmitPartLRspec() != null
                           ? DateFormatHelper.formatLocalDate(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(), DateFormatHelper.DATE) : null)
            .paymentStr(caseData.isPayByInstallment() ? getRepaymentString(caseData.getRespondent1RepaymentPlan().getRepaymentFrequency()) : null)
            .repaymentFrequency(caseData.isPayByInstallment()
                                    ? getRepaymentFrequency(caseData.getRespondent1RepaymentPlan().getRepaymentFrequency()) : null)
            .repaymentDate(caseData.isPayByInstallment()
                               ? DateFormatHelper.formatLocalDate(caseData.getRespondent1RepaymentPlan().getFirstRepaymentDate(), DateFormatHelper.DATE) : null)
            .installmentAmount(caseData.isPayByInstallment() ? String.valueOf(caseData.getRespondent1RepaymentPlan().getPaymentAmount().setScale(2)) : null)
            .ccjJudgmentAmount(judgementService.ccjJudgmentClaimAmount(caseData).setScale(2).toString())
            .ccjInterestToDate(totalInterest)
            .claimFee(getClaimFee(caseData))
            .ccjSubtotal(judgementService.ccjJudgementSubTotal(caseData).setScale(2).toString())
            .ccjFinalTotal(judgementService.ccjJudgmentFinalTotal(caseData).setScale(2).toString())
            .build();
    }

    public JudgmentByAdmissionOrDetermination toNonDivergentWelshDocs(CaseData caseData, JudgmentByAdmissionOrDetermination builder) {
        return builder.toBuilder()
            .welshDate(formatDateInWelsh(LocalDate.now()))
            .welshPayByDate(getWelshPayByDate(caseData))
            .welshRepaymentDate(getWelshRepaymentDate(caseData))
            .welshRepaymentFrequency(caseData.isPayByInstallment()
                                         ? getRepaymentFrequencyInWelsh(caseData.getRespondent1RepaymentPlan().getRepaymentFrequency()) : null)
            .welshPaymentStr(caseData.isPayByInstallment() ? getRepaymentWelshString(caseData.getRespondent1RepaymentPlan().getRepaymentFrequency()) : null)
            .build();
    }

    private String getWelshPayByDate(CaseData caseData) {
        return caseData.getRespondToClaimAdmitPartLRspec() != null
            ? formatDateInWelsh(LocalDate.parse(
            DateFormatHelper.formatLocalDate(
                caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid(),
                DateFormatHelper.DATE
            ), dateTimeFormatter)) : null;
    }

    private String getWelshRepaymentDate(CaseData caseData) {
        return caseData.isPayByInstallment()
            ? formatDateInWelsh(LocalDate.parse(DateFormatHelper.formatLocalDate(
            caseData.getRespondent1RepaymentPlan().getFirstRepaymentDate(),
            DateFormatHelper.DATE
        ), dateTimeFormatter)) : null;
    }

    private String getRepaymentString(PaymentFrequencyLRspec repaymentFrequency) {
        switch (repaymentFrequency) {
            case ONCE_ONE_WEEK : return "each week";
            case ONCE_ONE_MONTH: return "each month";
            case ONCE_TWO_WEEKS: return "every 2 weeks";
            default: return null;
        }
    }

    private String getRepaymentWelshString(PaymentFrequencyLRspec repaymentFrequency) {
        switch (repaymentFrequency) {
            case ONCE_ONE_WEEK : return "pob mis";
            case ONCE_ONE_MONTH: return "pob mis";
            case ONCE_TWO_WEEKS: return "pob 2 wythnos";
            default: return null;
        }
    }

    private String getRepaymentFrequency(PaymentFrequencyLRspec repaymentFrequency) {
        switch (repaymentFrequency) {
            case ONCE_ONE_WEEK : return "per week";
            case ONCE_ONE_MONTH: return "per month";
            case ONCE_TWO_WEEKS: return "every 2 weeks";
            default: return null;
        }
    }

    private String getRepaymentFrequencyInWelsh(PaymentFrequencyLRspec repaymentFrequency) {
        switch (repaymentFrequency) {
            case ONCE_ONE_WEEK : return "yr wythnos";
            case ONCE_ONE_MONTH: return "y mis";
            case ONCE_TWO_WEEKS: return "bob pythefnos";
            default: return null;
        }
    }

    private String getPaymentTypeForNonDivergent(CaseData caseData) {
        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY) {
            return "IMMEDIATELY";
        } else if (caseData.isPayByInstallment()) {
            return "REPAYMENT_PLAN";
        } else if (caseData.isPayBySetDate()) {
            return "SET_DATE";
        }
        return null;
    }
}
