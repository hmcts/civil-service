package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@Service
@RequiredArgsConstructor
public class GaDashboardNotificationsParamsMapper {

    private static Optional<LocalDate> getGeneralAppListingForHearingDate(GeneralApplicationCaseData caseData) {
        return Optional.ofNullable(caseData.getGaHearingNoticeDetail())
            .map(GAHearingNoticeDetail::getHearingDate);
    }

    private static Optional<LocalDate> getGeneralAppNotificationDeadlineDate(GeneralApplicationCaseData caseData) {
        return Optional.ofNullable(caseData.getGeneralAppNotificationDeadlineDate())
            .map(LocalDateTime::toLocalDate);
    }

    private static Optional<LocalDate> getJudgeRequestMoreInfoByDate(GeneralApplicationCaseData caseData) {
        return Optional.ofNullable(caseData.getJudicialDecisionRequestMoreInfo())
            .map(GAJudicialRequestMoreInfo::getJudgeRequestMoreInfoByDate);
    }

    public HashMap<String, Object> mapCaseDataToParams(GeneralApplicationCaseData caseData) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        addNotificationDeadlineDate(params, caseData);
        addJudgeRequestMoreInfoDate(params, caseData);
        addHearingScheduledDate(params, caseData);
        addApplicationFee(params, caseData);
        addRemissionAmounts(params, caseData);
        addFeeTypeLabels(params, caseData);
        addWrittenRepresentationDeadlines(params, caseData);
        params.put("testRef", "string");
        return params;
    }

    private void addNotificationDeadlineDate(HashMap<String, Object> params, GeneralApplicationCaseData caseData) {
        getGeneralAppNotificationDeadlineDate(caseData)
            .ifPresent(date -> putDateTimeVariants(params, "generalAppNotificationDeadlineDate", date));
    }

    private void addJudgeRequestMoreInfoDate(HashMap<String, Object> params, GeneralApplicationCaseData caseData) {
        getJudgeRequestMoreInfoByDate(caseData)
            .ifPresent(date -> putDateTimeVariants(params, "judgeRequestMoreInfoByDate", date));
    }

    private void addHearingScheduledDate(HashMap<String, Object> params, GeneralApplicationCaseData caseData) {
        if (!CaseState.LISTING_FOR_A_HEARING.equals(caseData.getCcdState())) {
            return;
        }

        getGeneralAppListingForHearingDate(caseData)
            .ifPresent(date -> putDateVariants(params, date));
    }

    private void addApplicationFee(HashMap<String, Object> params, GeneralApplicationCaseData caseData) {
        if (caseData.getGeneralAppPBADetails() != null) {
            params.put(
                "applicationFee",
                "£" + MonetaryConversions.penniesToPounds(
                    caseData.getGeneralAppPBADetails().getFee().getCalculatedAmountInPence()
                )
            );
        }
    }

    private void addRemissionAmounts(HashMap<String, Object> params, GeneralApplicationCaseData caseData) {
        if (hasPartialApplicationRemission(caseData)) {
            putRemissionAmounts(
                params,
                caseData.getGaHwfDetails().getRemissionAmount(),
                caseData.getGaHwfDetails().getOutstandingFee()
            );
            return;
        }

        if (hasPartialAdditionalRemission(caseData)) {
            putRemissionAmounts(
                params,
                caseData.getAdditionalHwfDetails().getRemissionAmount(),
                caseData.getAdditionalHwfDetails().getOutstandingFee()
            );
        }
    }

    private boolean hasPartialApplicationRemission(GeneralApplicationCaseData caseData) {
        return caseData.getGaHwfDetails() != null
            && caseData.getHwfFeeType() != null
            && FeeType.APPLICATION == caseData.getHwfFeeType()
            && CaseEvent.PARTIAL_REMISSION_HWF_GA.equals(caseData.getGaHwfDetails().getHwfCaseEvent());
    }

    private boolean hasPartialAdditionalRemission(GeneralApplicationCaseData caseData) {
        return caseData.getAdditionalHwfDetails() != null
            && caseData.getHwfFeeType() != null
            && FeeType.ADDITIONAL == caseData.getHwfFeeType()
            && CaseEvent.PARTIAL_REMISSION_HWF_GA.equals(caseData.getAdditionalHwfDetails().getHwfCaseEvent());
    }

    private void putRemissionAmounts(HashMap<String, Object> params,
                                     java.math.BigDecimal remissionAmount,
                                     java.math.BigDecimal outstandingFee) {
        params.put("remissionAmount", "£" + MonetaryConversions.penniesToPounds(remissionAmount));
        params.put("outstandingFeeInPounds", "£" + MonetaryConversions.penniesToPounds(outstandingFee));
    }

    private void addFeeTypeLabels(HashMap<String, Object> params, GeneralApplicationCaseData caseData) {
        if (Objects.isNull(caseData.getGeneralAppHelpWithFees())) {
            return;
        }

        if (CaseState.AWAITING_APPLICATION_PAYMENT.equals(caseData.getCcdState())) {
            params.put("applicationFeeTypeEn", "application");
            params.put("applicationFeeTypeCy", "gwneud cais");
        } else if (CaseState.APPLICATION_ADD_PAYMENT.equals(caseData.getCcdState())) {
            params.put("applicationFeeTypeEn", "additional application");
            params.put("applicationFeeTypeCy", "ychwanegol i wneud cais");
        }
    }

    private void addWrittenRepresentationDeadlines(HashMap<String, Object> params, GeneralApplicationCaseData caseData) {
        if (Objects.isNull(caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations())) {
            return;
        }

        LocalDate applicantDeadlineDate = getApplicantDeadline(caseData);
        LocalDate respondentDeadlineDate = getRespondentDeadline(caseData);
        putWrittenRepresentationDeadline(params, "writtenRepApplicantDeadline", applicantDeadlineDate);
        putWrittenRepresentationDeadline(params, "writtenRepRespondentDeadline", respondentDeadlineDate);
    }

    private LocalDate getApplicantDeadline(GeneralApplicationCaseData caseData) {
        if (caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenOption()
            != SEQUENTIAL_REPRESENTATIONS) {
            return caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenConcurrentRepresentationsBy();
        }

        return caseData.getParentClaimantIsApplicant() == YesOrNo.YES
            ? caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getSequentialApplicantMustRespondWithin()
            : caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenSequentailRepresentationsBy();
    }

    private LocalDate getRespondentDeadline(GeneralApplicationCaseData caseData) {
        if (caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenOption()
            != SEQUENTIAL_REPRESENTATIONS) {
            return caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenConcurrentRepresentationsBy();
        }

        return caseData.getParentClaimantIsApplicant() == YesOrNo.YES
            ? caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenSequentailRepresentationsBy()
            : caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getSequentialApplicantMustRespondWithin();
    }

    private void putDateTimeVariants(HashMap<String, Object> params, String key, LocalDate date) {
        params.put(key, date.atTime(END_OF_BUSINESS_DAY));
        params.put(key + "En", DateUtils.formatDate(date));
        params.put(key + "Cy", DateUtils.formatDateInWelsh(date, false));
    }

    private void putDateVariants(HashMap<String, Object> params, LocalDate date) {
        params.put("hearingNoticeApplicationDate" + "En", DateUtils.formatDate(date));
        params.put("hearingNoticeApplicationDate" + "Cy", DateUtils.formatDateInWelsh(date, false));
    }

    private void putWrittenRepresentationDeadline(HashMap<String, Object> params, String key, LocalDate date) {
        params.put(key, date.atTime(END_OF_BUSINESS_DAY));
        params.put(key + "DateEn", DateUtils.formatDate(date));
        params.put(key + "DateCy", DateUtils.formatDateInWelsh(date, false));
    }

}
