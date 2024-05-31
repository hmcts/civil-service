package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.common.Element;
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
import static uk.gov.hmcts.reform.civil.utils.AmountFormatter.formatAmount;
import static uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils.getDefendantAdmittedAmount;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    public static final String CLAIMANT1_ACCEPTED_REPAYMENT_PLAN = "accepted";
    public static final String CLAIMANT1_REJECTED_REPAYMENT_PLAN = "rejected";
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

        if (nonNull(caseData.getApplicant1ResponseDeadline())) {
            LocalDateTime applicant1ResponseDeadline = caseData.getApplicant1ResponseDeadline();
            params.put("applicant1ResponseDeadlineEn", DateUtils.formatDate(applicant1ResponseDeadline));
            params.put("applicant1ResponseDeadlineCy",
                       DateUtils.formatDateInWelsh(applicant1ResponseDeadline.toLocalDate()));
        }

        if (featureToggleService.isJudgmentOnlineLive() &&
            caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN &&
            nonNull(caseData.getCcjPaymentDetails().getCcjJudgmentTotalStillOwed())) {

            params.put("paymentFrequencyMessage", getPaymentAgreementMessage(caseData));
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
            params.put("claimantSettlementAgreement", getClaimantRepaymentPlanDecision(caseData));
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

        return params;
    }

    private String getPaymentAgreementMessage(CaseData caseData) {
        RepaymentPlanLRspec paymentPlanDetails = caseData.getRespondent1RepaymentPlan();

        return "You’ve agreed to pay the claim amount of £" +
            MonetaryConversions.penniesToPounds(caseData.getCcjPaymentDetails().getCcjJudgmentTotalStillOwed()) +
            " in " +
            getStringPaymentFrecuency(paymentPlanDetails.getRepaymentFrequency()) +
            " instalments of £" +
            MonetaryConversions.penniesToPounds(paymentPlanDetails.getPaymentAmount()) +
            ". The first payment is due on " +
            caseData.getRespondent1PaymentDateToStringSpec();
    }

    private String getStringPaymentFrecuency(PaymentFrequencyLRspec repaymentFrequency) {
        return switch (repaymentFrequency) {
            case ONCE_ONE_WEEK -> "weekly";
            case ONCE_TWO_WEEKS -> "biweekly";
            case ONCE_ONE_MONTH -> "monthly";
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
}
