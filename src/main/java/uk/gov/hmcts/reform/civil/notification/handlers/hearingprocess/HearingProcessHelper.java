package uk.gov.hmcts.reform.civil.notification.handlers.hearingprocess;

import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;

class HearingProcessHelper {

    private HearingProcessHelper() {

    }

    protected static boolean isNoFeeDue(CaseData caseData) {
        return (caseData.getHearingFeePaymentDetails() != null
            && SUCCESS.equals(caseData.getHearingFeePaymentDetails().getStatus()))
            || HearingNoticeList.OTHER.equals(caseData.getHearingNoticeList())
            || ListingOrRelisting.RELISTING.equals(caseData.getListingOrRelisting());
    }

    protected static Map<String, String> getHearingFeePropertiesIfNotPaid(CaseData caseData) {
        Map<String, String> properties = new HashMap<>();

        if (caseData.getHearingFeePaymentDetails() == null
            || !SUCCESS.equals(caseData.getHearingFeePaymentDetails().getStatus())) {

            properties.put(
                HEARING_FEE, caseData.getHearingFee() == null
                    ? "£0.00" : String.valueOf(caseData.getHearingFee().formData())
            );

            properties.put(
                HEARING_DUE_DATE, caseData.getHearingDueDate() == null
                    ? "" : NotificationUtils.getFormattedHearingDate(caseData.getHearingDueDate())
            );
        }
        return properties;
    }

    protected static String getAppSolReference(CaseData caseData) {
        if (nonNull(caseData.getSolicitorReferences())
            && nonNull(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())) {
            return caseData.getSolicitorReferences().getApplicantSolicitor1Reference();
        }
        return "";
    }

    protected static String getRespSolOneReference(CaseData caseData) {
        if (nonNull(caseData.getSolicitorReferences())
            && nonNull(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())) {
            return caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
        }
        return "";
    }

    protected static String getRespSolTwoReference(CaseData caseData) {
        return caseData.getRespondentSolicitor2Reference() == null ? "" :
            caseData.getRespondentSolicitor2Reference();
    }
}
