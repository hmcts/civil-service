package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Service
@RequiredArgsConstructor
public class NotifyClaimantMoreInformationNeededNotificationHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(NOTIFY_APPLICANT_MORE_INFORMATION_NEEDED);

    private static final String REFERENCE_TEMPLATE = "notify-claimant-hwf-more-information-needed-notification-%s";
    public static final String TASK_ID = "NotifyClaimantHwfMoreInformationNeeded";

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyClaimantLipHelpWithFees
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyClaimantLipHelpWithFees(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        notificationService.sendMail(
            getRecipientEmail(caseData),
            getNotificationTemplate(),
            addProperties(caseData),
            String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
        );

        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    public Map<String, String> addProperties(CaseData caseData) {
        String typeOfFee = "";
        HelpWithFeesMoreInformation moreInformation = new HelpWithFeesMoreInformation();
        if (null != caseData.getHelpWithFeesMoreInformationClaimIssue()) {
            typeOfFee = FeeType.CLAIMISSUED.name();
            moreInformation = caseData.getHelpWithFeesMoreInformationClaimIssue();
        } else if (null != caseData.getHelpWithFeesMoreInformationHearing()) {
            typeOfFee = FeeType.HEARING.name();
            moreInformation = caseData.getHelpWithFeesMoreInformationHearing();
        }
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            HwF_MORE_INFO_DATE, formatLocalDate(moreInformation.getHwFMoreInfoDocumentDate(), DATE),
            HwF_MORE_INFO_FEE_TYPE, typeOfFee,
            HwF_MORE_INFO_DOCUMENTS, getDocumentList(moreInformation.getHwFMoreInfoRequiredDocuments())
        );
    }

    private String getNotificationTemplate() {
        return notificationsProperties.getNotifyApplicantForHwFMoreInformationNeeded();
    }

    private String getRecipientEmail(CaseData caseData) {
        return caseData.getClaimantUserDetails().getEmail();
    }

    private String getDocumentList(List<HwFMoreInfoRequiredDocuments> list) {
        StringBuilder documentList = new StringBuilder();
        for (HwFMoreInfoRequiredDocuments doc : list) {
            documentList.append(doc.name());
            documentList.append("\n");
        }
        return documentList.toString();
    }

}
