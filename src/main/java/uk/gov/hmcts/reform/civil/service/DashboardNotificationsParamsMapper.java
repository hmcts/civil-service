package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState.ISSUED;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_IN_INSTALMENTS;
import static uk.gov.hmcts.reform.civil.utils.AmountFormatter.formatAmount;
import static uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils.getDefendantAdmittedAmount;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    public static final String CLAIMANT1_ACCEPTED_REPAYMENT_PLAN = "accepted";
    public static final String CLAIMANT1_REJECTED_REPAYMENT_PLAN = "rejected";
    public static final String CLAIMANT1_ACCEPTED_REPAYMENT_PLAN_WELSH = "derbyn";
    public static final String CLAIMANT1_REJECTED_REPAYMENT_PLAN_WELSH = "gwrthod";
    public static final String ORDER_DOCUMENT = "orderDocument";
    private final FeatureToggleService featureToggleService;

    public HashMap<String, Object> mapCaseDataToParams(CaseData caseData) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put("legacyCaseReference", caseData.getLegacyCaseReference());
        params.put("defaultRespondTime", "4pm");
        params.put("respondent1PartyName", caseData.getRespondent1().getPartyName());
        params.put("applicant1PartyName", caseData.getApplicant1().getPartyName());

        if (featureToggleService.isGeneralApplicationsEnabled()) {
            params.put("djClaimantNotificationMessage", "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to vary the judgment</a>");
            params.put("djDefendantNotificationMessage", "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to set aside (remove) or vary the judgment</a>");
        } else {
            params.put("djClaimantNotificationMessage", "<u>make an application to vary the judgment</u>");
            params.put("djDefendantNotificationMessage", "<u>make an application to set aside (remove) or vary the judgment</u>");
        }

        if (caseData.getJoJudgmentRecordReason() != null && caseData.getJoJudgmentRecordReason().equals(JudgmentRecordedReason.DETERMINATION_OF_MEANS)) {
            params.put("paymentFrequencyMessage", getPaymentFrequencyMessage(caseData).toString());
        }

        if (nonNull(caseData.getApplicant1ResponseDeadline())) {
            LocalDateTime applicant1ResponseDeadline = caseData.getApplicant1ResponseDeadline();
            params.put("applicant1ResponseDeadlineEn", DateUtils.formatDate(applicant1ResponseDeadline));
            params.put("applicant1ResponseDeadlineCy",
                       DateUtils.formatDateInWelsh(applicant1ResponseDeadline.toLocalDate()));
        }

        if (featureToggleService.isJudgmentOnlineLive()
            && nonNull(caseData.getActiveJudgment())
            && caseData.getActiveJudgment().getState().equals(ISSUED)
            && nonNull(caseData.getActiveJudgment().getPaymentPlan())
            && caseData.getActiveJudgment().getPaymentPlan().getType().equals(PAY_IN_INSTALMENTS)) {

            JudgmentDetails judgmentDetails = caseData.getActiveJudgment();
            JudgmentInstalmentDetails instalmentDetails = judgmentDetails.getInstalmentDetails();

            params.put("ccjDefendantAdmittedAmount", MonetaryConversions.penniesToPounds(new BigDecimal(judgmentDetails.getOrderedAmount())));
            params.put("ccjPaymentFrequency", getStringPaymentFrequency(instalmentDetails.getPaymentFrequency()));
            params.put("ccjInstallmentAmount", MonetaryConversions.penniesToPounds(new BigDecimal(instalmentDetails.getAmount())));
            params.put("ccjFirstRepaymentDateEn", DateUtils.formatDate(instalmentDetails.getStartDate()));
        }

        if (nonNull(getDefendantAdmittedAmount(caseData))) {
            params.put(
                "defendantAdmittedAmount",
                "£" + this.removeDoubleZeros(formatAmount(getDefendantAdmittedAmount(caseData)))
            );
        }
        if (nonNull(caseData.getRespondToClaimAdmitPartLRspec())) {
            LocalDate whenWillThisAmountBePaid = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
            params.put("respondent1AdmittedAmountPaymentDeadlineEn", DateUtils.formatDate(whenWillThisAmountBePaid));
            params.put(
                "respondent1AdmittedAmountPaymentDeadlineCy",
                DateUtils.formatDateInWelsh(whenWillThisAmountBePaid)
            );
        }
        if (nonNull(caseData.getClaimFee())) {
            params.put(
                "claimFee",
                "£" + this.removeDoubleZeros(caseData.getClaimFee().toPounds().toPlainString())
            );
        }

        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            LocalDate respondentResponseDeadline = caseData.getRespondent1ResponseDeadline().toLocalDate();
            params.put("respondent1ResponseDeadlineEn", DateUtils.formatDate(respondentResponseDeadline));
            params.put("respondent1ResponseDeadlineCy", DateUtils.formatDateInWelsh(respondentResponseDeadline));
        }

        if (caseData.getClaimIssueRemissionAmount() != null) {
            params.put(
                "claimIssueRemissionAmount",
                "£" + this.removeDoubleZeros(MonetaryConversions.penniesToPounds(
                    caseData.getClaimIssueRemissionAmount()).toPlainString())
            );
        }
        if (caseData.getOutstandingFeeInPounds() != null) {
            params.put(
                "claimIssueOutStandingAmount",
                "£" + this.removeDoubleZeros(caseData.getOutstandingFeeInPounds().toPlainString())
            );
        }
        if (caseData.getHwfFeeType() != null) {
            params.put("typeOfFee", caseData.getHwfFeeType().getLabel());
        }

        getAlreadyPaidAmount(caseData).ifPresent(amount -> params.put("admissionPaidAmount", amount));

        getClaimSettledAmount(caseData).ifPresent(amount -> params.put("claimSettledAmount", amount));

        getClaimSettleDate(caseData).ifPresent(date -> {
            params.put("claimSettledDateEn", DateUtils.formatDate(date));
            params.put("claimSettledDateCy", DateUtils.formatDateInWelsh(date));
        });

        getRespondToSettlementAgreementDeadline(caseData).ifPresent(date -> {
            params.put("respondent1SettlementAgreementDeadlineEn", DateUtils.formatDate(date));
            params.put("respondent1SettlementAgreementDeadlineCy", DateUtils.formatDateInWelsh(date));
            params.put("claimantSettlementAgreementEn", getClaimantRepaymentPlanDecision(caseData));
            params.put("claimantSettlementAgreementCy", getClaimantRepaymentPlanDecisionCy(caseData));
        });

        LocalDate claimSettleDate = caseData.getApplicant1ClaimSettleDate();
        if (nonNull(claimSettleDate)) {
            params.put("applicant1ClaimSettledDateEn", DateUtils.formatDate(claimSettleDate));
            params.put("applicant1ClaimSettledDateCy", DateUtils.formatDateInWelsh(claimSettleDate));
        }

        if (nonNull(caseData.getRespondent1RepaymentPlan())) {
            getInstalmentAmount(caseData).ifPresent(amount -> params.put("instalmentAmount", amount));
            getInstalmentStartDate(caseData).ifPresent(date -> {
                params.put("instalmentStartDateEn", DateUtils.formatDate(date));
                params.put("instalmentStartDateCy", DateUtils.formatDateInWelsh(date));
            });
            params.put(
                "instalmentTimePeriodEn",
                getInstalmentTimePeriod(caseData.getRespondent1RepaymentPlan().getRepaymentFrequency())
            );
            params.put(
                "instalmentTimePeriodCy",
                getInstalmentTimePeriod(caseData.getRespondent1RepaymentPlan().getRepaymentFrequency())
            );
        }

        if (nonNull(caseData.getRespondent1RepaymentPlan())) {
            params.put("installmentAmount", "£" + this.removeDoubleZeros(MonetaryConversions.penniesToPounds(
                caseData.getRespondent1RepaymentPlan().getPaymentAmount()).toPlainString()));

            params.put(
                "paymentFrequency",
                caseData.getRespondent1RepaymentPlan().getRepaymentFrequency().getDashboardLabel()
            );
            getFirstRepaymentDate(caseData).ifPresent(date -> {
                params.put("firstRepaymentDateEn", DateUtils.formatDate(date));
                params.put("firstRepaymentDateCy", DateUtils.formatDateInWelsh(date));
            });
        }

        if (nonNull(caseData.getHearingDueDate())) {
            LocalDate date = caseData.getHearingDueDate();
            params.put("hearingDueDateEn", DateUtils.formatDate(date));
            params.put("hearingDueDateCy", DateUtils.formatDateInWelsh(date));
        }

        Optional<LocalDate> hearingDocumentDeadline = getHearingDocumentDeadline(caseData);
        hearingDocumentDeadline.ifPresent(date -> {
            params.put("sdoDocumentUploadRequestedDateEn", DateUtils.formatDate(date));
            params.put("sdoDocumentUploadRequestedDateCy", DateUtils.formatDateInWelsh(date));
        });

        params.put("claimantRepaymentPlanDecision", getClaimantRepaymentPlanDecision(caseData));
        params.put("claimantRepaymentPlanDecisionCy", getClaimantRepaymentPlanDecisionCy(caseData));

        if (nonNull(caseData.getHearingDate())) {
            LocalDate date = caseData.getHearingDate();
            params.put("hearingDateEn", DateUtils.formatDate(date));
            params.put("hearingDateCy", DateUtils.formatDateInWelsh(date));
        }

        if (nonNull(caseData.getHearingLocation())) {
            params.put("hearingCourtEn", caseData.getHearingLocationCourtName());
            params.put("hearingCourtCy", caseData.getHearingLocationCourtName());
        }

        if (nonNull(caseData.getHearingDate())) {
            LocalDate date = caseData.getHearingDate().minusWeeks(3);
            params.put("trialArrangementDeadlineEn", DateUtils.formatDate(date));
            params.put("trialArrangementDeadlineCy", DateUtils.formatDateInWelsh(date));
        }
        if (nonNull(caseData.getHearingFee())) {
            params.put(
                "hearingFee",
                "£" + this.removeDoubleZeros(caseData.getHearingFee().toPounds().toPlainString())
            );
        }

        if (caseData.getHearingRemissionAmount() != null) {
            params.put(
                "hearingFeeRemissionAmount",
                "£" + this.removeDoubleZeros(MonetaryConversions.penniesToPounds(
                    caseData.getHearingRemissionAmount()).toPlainString())
            );
        }
        if (caseData.getOutstandingFeeInPounds() != null) {
            params.put(
                "hearingFeeOutStandingAmount",
                "£" + this.removeDoubleZeros(caseData.getOutstandingFeeInPounds().toPlainString())
            );
        }

        return params;
    }

    public Map<String, Object> mapCaseDataToParams(CaseData caseData, CaseEvent caseEvent) {

        Map<String, Object> params = mapCaseDataToParams(caseData);
        String orderDocumentUrl = addToMapDocumentInfo(caseData, caseEvent);
        if (nonNull(orderDocumentUrl)) {
            params.put(ORDER_DOCUMENT, orderDocumentUrl);
        }

        if (CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT.equals(caseEvent)
            || CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT.equals(caseEvent)) {
            params.put("requestForReconsiderationDeadlineEn", DateUtils.formatDate(LocalDate.now().plusDays(7)));
            params.put("requestForReconsiderationDeadlineCy", DateUtils.formatDateInWelsh(LocalDate.now().plusDays(7)));
        }

        return params;
    }

    private static String getStringPaymentFrequency(PaymentFrequency paymentFrequency) {
        return switch (paymentFrequency) {
            case WEEKLY -> "weekly";
            case EVERY_TWO_WEEKS -> "biweekly";
            case MONTHLY -> "monthly";
            default -> "";
        };
    }

    private Optional<LocalDate> getHearingDocumentDeadline(CaseData caseData) {
        if (SdoHelper.isSmallClaimsTrack(caseData)) {
            // TODO currently, user can edit the date description
            return Optional.empty();
        } else if (SdoHelper.isFastTrack(caseData)) {
            return Optional.ofNullable(caseData.getFastTrackDisclosureOfDocuments())
                .map(FastTrackDisclosureOfDocuments::getDate3);
        } else {
            return Optional.ofNullable(caseData.getDisposalHearingDisclosureOfDocuments())
                .map(DisposalHearingDisclosureOfDocuments::getDate2);
        }
    }

    private Optional<LocalDate> getFirstRepaymentDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RepaymentPlan())
            .map(RepaymentPlanLRspec::getFirstRepaymentDate);
    }

    private Optional<String> getClaimSettledAmount(CaseData caseData) {
        return Optional.ofNullable(getRespondToClaim(caseData))
            .map(RespondToClaim::getHowMuchWasPaid)
            .map(MonetaryConversions::penniesToPounds)
            .map(amount -> amount.setScale(2))
            .map(BigDecimal::toPlainString)
            .map(this::removeDoubleZeros)
            .map(amount -> "£" + amount);
    }

    private String removeDoubleZeros(String input) {
        return input.replace(".00", "");
    }

    private Optional<LocalDate> getClaimSettleDate(CaseData caseData) {
        return Optional.ofNullable(getRespondToClaim(caseData))
            .map(RespondToClaim::getWhenWasThisAmountPaid);
    }

    private RespondToClaim getRespondToClaim(CaseData caseData) {
        RespondToClaim respondToClaim = null;
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            respondToClaim = caseData.getRespondToClaim();
        } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            respondToClaim = caseData.getRespondToAdmittedClaim();
        }

        return respondToClaim;
    }

    private Optional<LocalDate> getRespondToSettlementAgreementDeadline(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RespondToSettlementAgreementDeadline())
            .map(LocalDateTime::toLocalDate);
    }

    private Optional<String> getAlreadyPaidAmount(CaseData caseData) {
        return Optional.ofNullable(getRespondToClaim(caseData)).map(RespondToClaim::getHowMuchWasPaid).map(
                MonetaryConversions::penniesToPounds).map(
                BigDecimal::stripTrailingZeros)
            .map(amount -> amount.setScale(2))
            .map(BigDecimal::toPlainString)
            .map(this::removeDoubleZeros)
            .map(amount -> "£" + amount);
    }

    private String getClaimantRepaymentPlanDecision(CaseData caseData) {
        if (caseData.hasApplicantAcceptedRepaymentPlan()) {
            return CLAIMANT1_ACCEPTED_REPAYMENT_PLAN;
        }
        return CLAIMANT1_REJECTED_REPAYMENT_PLAN;
    }

    private String getClaimantRepaymentPlanDecisionCy(CaseData caseData) {
        if (caseData.hasApplicantAcceptedRepaymentPlan()) {
            return CLAIMANT1_ACCEPTED_REPAYMENT_PLAN_WELSH;
        }
        return CLAIMANT1_REJECTED_REPAYMENT_PLAN_WELSH;
    }

    private String getInstalmentTimePeriod(PaymentFrequencyLRspec repaymentFrequency) {
        return switch (repaymentFrequency) {
            case ONCE_ONE_WEEK -> "week";
            case ONCE_TWO_WEEKS -> "2 weeks";
            case ONCE_THREE_WEEKS -> "3 weeks";
            case ONCE_FOUR_WEEKS -> "4 weeks";
            case ONCE_ONE_MONTH -> "month";
            default -> null;
        };
    }

    private Optional<LocalDate> getInstalmentStartDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RepaymentPlan().getFirstRepaymentDate());
    }

    private Optional<String> getInstalmentAmount(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RepaymentPlan())
            .map(RepaymentPlanLRspec::getPaymentAmount)
            .map(MonetaryConversions::penniesToPounds)
            .map(amount -> amount.setScale(2))
            .map(BigDecimal::toPlainString)
            .map(this::removeDoubleZeros)
            .map(amount -> "£" + amount);
    }

    private String addToMapDocumentInfo(CaseData caseData, CaseEvent caseEvent) {

        if (nonNull(caseEvent)) {
            switch (caseEvent) {
                case CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT -> {
                    return caseData.getFinalOrderDocumentCollection()
                        .get(0).getValue().getDocumentLink().getDocumentBinaryUrl();
                }
                case CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT -> {
                    return caseData.getOrderSDODocumentDJCollection()
                        .get(0).getValue().getDocumentLink().getDocumentBinaryUrl();
                }
                case CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT -> {
                    Optional<Element<CaseDocument>> sdoDocument = caseData.getSDODocument();
                    if (sdoDocument.isPresent()) {
                        return sdoDocument.get().getValue().getDocumentLink().getDocumentBinaryUrl();
                    }
                }
                default -> {
                    return null;
                }
            }
        }
        return null;
    }

    private static StringBuilder getPaymentFrequencyMessage(CaseData caseData) {
        PaymentPlanSelection paymentPlanType = caseData.getJoPaymentPlan().getType();
        StringBuilder paymentFrequencyMessage = new StringBuilder();
        BigDecimal totalAmount = new BigDecimal(caseData.getJoAmountOrdered());

        if ((caseData.getJoAmountCostOrdered() != null) && !caseData.getJoAmountCostOrdered().isEmpty()) {
            totalAmount.add(new BigDecimal(caseData.getJoAmountCostOrdered()));
        }

        JudgmentInstalmentDetails instalmentDetails = caseData.getJoInstalmentDetails();
        String paymentFrecuencyString = getStringPaymentFrequency(instalmentDetails.getPaymentFrequency());

        if (PaymentPlanSelection.PAY_IN_INSTALMENTS.equals(paymentPlanType)) {
            paymentFrequencyMessage.append("You must pay the claim amount of £ ")
                .append(MonetaryConversions.penniesToPounds(totalAmount).toString())
                .append(" in ")
                .append(paymentFrecuencyString)
                .append(" instalments of £ ")
                .append(MonetaryConversions.penniesToPounds((new BigDecimal(instalmentDetails.getAmount()))).toString())
                .append(" The first payment is due on ")
                .append(instalmentDetails.getStartDate())
                .append(".");
        }
        return paymentFrequencyMessage;
    }
}
