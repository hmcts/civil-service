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

        getGeneralAppNotificationDeadlineDate(caseData).ifPresent(date -> {
            params.put("generalAppNotificationDeadlineDate", date.atTime(END_OF_BUSINESS_DAY));
            params.put("generalAppNotificationDeadlineDateEn", DateUtils.formatDate(date));
            params.put("generalAppNotificationDeadlineDateCy", DateUtils.formatDateInWelsh(date, false));
        });

        getJudgeRequestMoreInfoByDate(caseData).ifPresent(date -> {
            params.put("judgeRequestMoreInfoByDate", date.atTime(END_OF_BUSINESS_DAY));
            params.put("judgeRequestMoreInfoByDateEn", DateUtils.formatDate(date));
            params.put("judgeRequestMoreInfoByDateCy", DateUtils.formatDateInWelsh(date, false));
        });

        if (caseData.getCcdState().equals(CaseState.LISTING_FOR_A_HEARING)) {

            getGeneralAppListingForHearingDate(caseData).ifPresent(date -> {
                params.put("hearingNoticeApplicationDateEn", DateUtils.formatDate(date));
                params.put(
                    "hearingNoticeApplicationDateCy",
                    DateUtils.formatDateInWelsh(date, false)
                );
            });

        }

        if (caseData.getGeneralAppPBADetails() != null) {
            params.put(
                "applicationFee",
                "£" + MonetaryConversions.penniesToPounds(caseData.getGeneralAppPBADetails().getFee().getCalculatedAmountInPence())
            );
        }

        if (caseData.getGaHwfDetails() != null && (caseData.getHwfFeeType() != null && FeeType.APPLICATION == caseData.getHwfFeeType())) {
            if (caseData.getGaHwfDetails().getHwfCaseEvent() != null
                && caseData.getGaHwfDetails().getHwfCaseEvent().equals(CaseEvent.PARTIAL_REMISSION_HWF_GA)) {
                params.put(
                    "remissionAmount",
                    "£" + MonetaryConversions.penniesToPounds(caseData.getGaHwfDetails().getRemissionAmount())
                );
                params.put(
                    "outstandingFeeInPounds", "£" + MonetaryConversions
                        .penniesToPounds(caseData.getGaHwfDetails().getOutstandingFee())
                );
            }
        } else if (caseData.getAdditionalHwfDetails() != null && (caseData.getHwfFeeType() != null
            && FeeType.ADDITIONAL == caseData.getHwfFeeType())) {
            if (caseData.getAdditionalHwfDetails().getHwfCaseEvent() != null
                && caseData.getAdditionalHwfDetails().getHwfCaseEvent().equals(CaseEvent.PARTIAL_REMISSION_HWF_GA)) {
                params.put(
                    "remissionAmount", "£" + MonetaryConversions.penniesToPounds(caseData.getAdditionalHwfDetails()
                                                                                     .getRemissionAmount())
                );
                params.put(
                    "outstandingFeeInPounds", "£" + MonetaryConversions
                        .penniesToPounds(caseData.getAdditionalHwfDetails().getOutstandingFee())
                );
            }
        }

        if (Objects.nonNull(caseData.getJudicialDecisionRequestMoreInfo())) {
            params.put(
                "judgeRequestMoreInfoByDate",
                caseData.getJudicialDecisionRequestMoreInfo().getJudgeRequestMoreInfoByDate().atTime(
                    END_OF_BUSINESS_DAY)
            );
            params.put(
                "judgeRequestMoreInfoByDateEn",
                DateUtils.formatDate(caseData.getJudicialDecisionRequestMoreInfo().getJudgeRequestMoreInfoByDate())
            );
            params.put(
                "judgeRequestMoreInfoByDateCy",
                DateUtils.formatDateInWelsh(
                    caseData.getJudicialDecisionRequestMoreInfo().getJudgeRequestMoreInfoByDate(),
                    false
                )
            );
        }

        if (Objects.nonNull(caseData.getGeneralAppHelpWithFees())) {
            if (caseData.getCcdState().equals(CaseState.AWAITING_APPLICATION_PAYMENT)) {
                params.put("applicationFeeTypeEn", "application");
                params.put("applicationFeeTypeCy", "gwneud cais");
            } else if (caseData.getCcdState().equals(CaseState.APPLICATION_ADD_PAYMENT)) {
                params.put("applicationFeeTypeEn", "additional application");
                params.put("applicationFeeTypeCy", "ychwanegol i wneud cais");
            }
        }

        if (Objects.nonNull(caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations())) {
            LocalDate applicantDeadlineDate;
            LocalDate respondentDeadlineDate;
            if (caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenOption() == SEQUENTIAL_REPRESENTATIONS) {
                applicantDeadlineDate = caseData.getParentClaimantIsApplicant() == YesOrNo.YES
                    ? caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getSequentialApplicantMustRespondWithin()
                    : caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenSequentailRepresentationsBy();
                respondentDeadlineDate = caseData.getParentClaimantIsApplicant() == YesOrNo.YES
                    ? caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenSequentailRepresentationsBy()
                    : caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getSequentialApplicantMustRespondWithin();
            } else {
                applicantDeadlineDate = caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenConcurrentRepresentationsBy();
                respondentDeadlineDate = caseData.getJudicialDecisionMakeAnOrderForWrittenRepresentations().getWrittenConcurrentRepresentationsBy();
            }
            params.put("writtenRepApplicantDeadline", applicantDeadlineDate.atTime(END_OF_BUSINESS_DAY));
            params.put("writtenRepApplicantDeadlineDateEn", DateUtils.formatDate(applicantDeadlineDate));
            params.put("writtenRepApplicantDeadlineDateCy", DateUtils.formatDateInWelsh(applicantDeadlineDate, false));
            params.put("writtenRepRespondentDeadline", respondentDeadlineDate.atTime(END_OF_BUSINESS_DAY));
            params.put("writtenRepRespondentDeadlineDateEn", DateUtils.formatDate(respondentDeadlineDate));
            params.put(
                "writtenRepRespondentDeadlineDateCy",
                DateUtils.formatDateInWelsh(respondentDeadlineDate, false)
            );
        }
        //ToDo: refactor below string to allow for notifications that do not require additional params
        params.put("testRef", "string");

        return params;
    }

}
