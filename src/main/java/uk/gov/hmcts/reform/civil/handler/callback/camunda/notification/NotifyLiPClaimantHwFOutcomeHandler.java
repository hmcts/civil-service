package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
@RequiredArgsConstructor
public class NotifyLiPClaimantHwFOutcomeHandler extends CallbackHandler implements NotificationData {

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_LIP_CLAIMANT_HWF_OUTCOME);
    private static final String TASK_ID = "NotifyClaimantHwFOutcome";
    private static final String REFERENCE_TEMPLATE = "hwf-outcome-notification-%s";
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final Map<String, Callback> callBackMap = Map.of(
        callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicantForHwFOutcome
    );
    private Map<CaseEvent, String> emailTemplates;
    private Map<CaseEvent, String> emailTemplatesBilingual;

    @Override
    protected Map<String, Callback> callbacks() {
        return callBackMap;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse notifyApplicantForHwFOutcome(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseEvent hwfEvent = caseData.getHwFEvent();
        if (Objects.nonNull(caseData.getApplicant1Email())) {
            notificationService.sendMail(
                caseData.getApplicant1Email(),
                caseData.isBilingual() ? getTemplateBilingual(hwfEvent) : getTemplate(hwfEvent),
                addProperties(caseData),
                String.format(REFERENCE_TEMPLATE, caseData.getLegacyCaseReference())
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        Map<String, String> commonProperties = getCommonProperties(caseData);
        Map<String, String> furtherProperties = getFurtherProperties(caseData);
        return Collections.unmodifiableMap(
            Stream.concat(commonProperties.entrySet().stream(), furtherProperties.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private Map<String, String> getFurtherProperties(CaseData caseData) {
        return switch (caseData.getHwFEvent()) {
            case NO_REMISSION_HWF -> getNoRemissionProperties(caseData);
            case MORE_INFORMATION_HWF -> getMoreInformationProperties(caseData);
            case UPDATE_HELP_WITH_FEE_NUMBER, INVALID_HWF_REFERENCE, FEE_PAYMENT_OUTCOME -> Collections.emptyMap();
            case PARTIAL_REMISSION_HWF_GRANTED -> getPartialRemissionProperties(caseData);
            default -> throw new IllegalArgumentException("case event not found");
        };
    }

    private String getTemplate(CaseEvent hwfEvent) {
        if (emailTemplates == null) {
            emailTemplates = Map.of(
                CaseEvent.NO_REMISSION_HWF,
                notificationsProperties.getNotifyApplicantForHwfNoRemission(),
                CaseEvent.INVALID_HWF_REFERENCE,
                notificationsProperties.getNotifyApplicantForHwfInvalidRefNumber(),
                CaseEvent.MORE_INFORMATION_HWF,
                notificationsProperties.getNotifyApplicantForHwFMoreInformationNeeded(),
                CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER,
                notificationsProperties.getNotifyApplicantForHwfUpdateRefNumber(),
                CaseEvent.PARTIAL_REMISSION_HWF_GRANTED,
                notificationsProperties.getNotifyApplicantForHwfPartialRemission(),
                CaseEvent.FEE_PAYMENT_OUTCOME,
                notificationsProperties.getNotifyApplicantForHwfFeePaymentOutcome()
            );
        }
        return emailTemplates.get(hwfEvent);
    }

    private String getTemplateBilingual(CaseEvent hwfEvent) {
        if (emailTemplatesBilingual == null) {
            emailTemplatesBilingual = Map.of(
                CaseEvent.NO_REMISSION_HWF, notificationsProperties.getNotifyApplicantForHwfNoRemissionWelsh(),
                CaseEvent.MORE_INFORMATION_HWF, notificationsProperties.getNotifyApplicantForHwFMoreInformationNeededWelsh(),
                CaseEvent.FEE_PAYMENT_OUTCOME,
                notificationsProperties.getNotifyApplicantForHwfFeePaymentOutcomeInBilingual()
            );
        }
        return emailTemplatesBilingual.get(hwfEvent);
    }

    private Map<String, String> getNoRemissionProperties(CaseData caseData) {
        return Map.of(
            REASONS, getHwFNoRemissionReason(caseData),
            AMOUNT, caseData.getHwFFeeAmount().toString()
        );
    }

    private Map<String, String> getPartialRemissionProperties(CaseData caseData) {
        return Map.of(
            PART_AMOUNT, caseData.getRemissionAmount().toString(),
            REMAINING_AMOUNT, caseData.getOutstandingFeeInPounds().toString()
        );
    }

    private Map<String, String> getCommonProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            TYPE_OF_FEE, caseData.getHwfFeeType().getLabel(),
            HWF_REFERENCE_NUMBER, caseData.getHwFReferenceNumber()
        );
    }

    private Map<String, String> getMoreInformationProperties(CaseData caseData) {
        HelpWithFeesMoreInformation moreInformation =
            null != caseData.getHelpWithFeesMoreInformationClaimIssue()
                ? caseData.getHelpWithFeesMoreInformationClaimIssue()
                : caseData.getHelpWithFeesMoreInformationHearing();
        return Map.of(
            HWF_MORE_INFO_DATE, formatLocalDate(moreInformation.getHwFMoreInfoDocumentDate(), DATE),
            HWF_MORE_INFO_DOCUMENTS, getMoreInformationDocumentList(moreInformation.getHwFMoreInfoRequiredDocuments())
        );
    }

    private String getMoreInformationDocumentList(List<HwFMoreInfoRequiredDocuments> list) {
        StringBuilder documentList = new StringBuilder();
        for (HwFMoreInfoRequiredDocuments doc : list) {
            documentList.append(doc.name());
            documentList.append("\n");
        }
        return documentList.toString();
    }

    private String getHwFNoRemissionReason(CaseData caseData) {
        if (caseData.isHWFTypeHearing()) {
            return caseData.getHearingHwfDetails().getNoRemissionDetailsSummary().getLabel();
        }
        if (caseData.isHWFTypeClaimIssued()) {
            return caseData.getClaimIssuedHwfDetails().getNoRemissionDetailsSummary().getLabel();
        }
        return "";
    }
}
