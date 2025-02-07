package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        String recipientEmail = caseData.isApplicantLiP()
            ? caseData.getApplicant1Email()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();

        //CIV-12798: bypassing sendMail for Full Remission Granted event.
        if (Objects.nonNull(recipientEmail) && CaseEvent.FULL_REMISSION_HWF != hwfEvent) {
            notificationService.sendMail(
                recipientEmail,
                caseData.isClaimantBilingual() ? getTemplateBilingual(hwfEvent) : getTemplate(hwfEvent),
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
            case PARTIAL_REMISSION_HWF_GRANTED -> getPartialRemissionProperties(caseData);
            case INVALID_HWF_REFERENCE, UPDATE_HELP_WITH_FEE_NUMBER -> Collections.emptyMap();
            default -> throw new IllegalArgumentException("case event not found");
        };
    }

    private String getTemplate(CaseEvent hwfEvent) {
        if (emailTemplates == null) {
            emailTemplates = Map.of(
                CaseEvent.INVALID_HWF_REFERENCE,
                notificationsProperties.getNotifyApplicantForHwfInvalidRefNumber(),
                CaseEvent.NO_REMISSION_HWF,
                notificationsProperties.getNotifyApplicantForHwfNoRemission(),
                CaseEvent.MORE_INFORMATION_HWF,
                notificationsProperties.getNotifyApplicantForHwFMoreInformationNeeded(),
                CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER,
                notificationsProperties.getNotifyApplicantForHwfUpdateRefNumber(),
                CaseEvent.PARTIAL_REMISSION_HWF_GRANTED,
                notificationsProperties.getNotifyApplicantForHwfPartialRemission()
            );
        }
        return emailTemplates.get(hwfEvent);
    }

    private String getTemplateBilingual(CaseEvent hwfEvent) {
        if (emailTemplatesBilingual == null) {
            emailTemplatesBilingual = Map.of(
                CaseEvent.INVALID_HWF_REFERENCE,
                notificationsProperties.getNotifyApplicantForHwfInvalidRefNumberBilingual(),
                CaseEvent.MORE_INFORMATION_HWF,
                notificationsProperties.getNotifyApplicantForHwFMoreInformationNeededWelsh(),
                CaseEvent.NO_REMISSION_HWF,
                notificationsProperties.getNotifyApplicantForHwfNoRemissionWelsh(),
                CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER,
                notificationsProperties.getNotifyApplicantForHwfUpdateRefNumberBilingual(),
                CaseEvent.PARTIAL_REMISSION_HWF_GRANTED,
                notificationsProperties.getNotifyApplicantForHwfPartialRemissionBilingual()
            );
        }
        return emailTemplatesBilingual.get(hwfEvent);
    }

    private Map<String, String> getNoRemissionProperties(CaseData caseData) {
        return Map.of(
            REASONS, getHwFNoRemissionReason(caseData),
            REASONS_WELSH, getHwFNoRemissionReasonWelsh(caseData),
            AMOUNT, caseData.getHwFFeeAmount().toString()
        );
    }

    private Map<String, String> getMoreInformationProperties(CaseData caseData) {
        HelpWithFeesMoreInformation moreInformation =
            null != caseData.getHelpWithFeesMoreInformationClaimIssue()
                ? caseData.getHelpWithFeesMoreInformationClaimIssue()
                : caseData.getHelpWithFeesMoreInformationHearing();
        return Map.of(
            HWF_MORE_INFO_DATE, formatLocalDate(moreInformation.getHwFMoreInfoDocumentDate(), DATE),
            HWF_MORE_INFO_DOCUMENTS, getMoreInformationDocumentList(
                moreInformation.getHwFMoreInfoRequiredDocuments()
            ),
            HWF_MORE_INFO_DOCUMENTS_WELSH, getMoreInformationDocumentListWelsh(
                moreInformation.getHwFMoreInfoRequiredDocuments())
        );
    }

    private String getMoreInformationDocumentList(List<HwFMoreInfoRequiredDocuments> list) {
        StringBuilder documentList = new StringBuilder();
        for (HwFMoreInfoRequiredDocuments doc : list) {
            documentList.append(doc.getName());
            if (!doc.getDescription().isEmpty()) {
                documentList.append(" - ");
                documentList.append(doc.getDescription());
            }
            documentList.append("\n");
            documentList.append("\n");
        }
        return documentList.toString();
    }

    private String getMoreInformationDocumentListWelsh(List<HwFMoreInfoRequiredDocuments> list) {
        StringBuilder documentList = new StringBuilder();
        for (HwFMoreInfoRequiredDocuments doc : list) {
            documentList.append(doc.getNameBilingual());
            if (!doc.getDescriptionBilingual().isEmpty()) {
                documentList.append(" - ");
                documentList.append(doc.getDescriptionBilingual());
            }
            documentList.append("\n");
            documentList.append("\n");
        }
        return documentList.toString();
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
            TYPE_OF_FEE_WELSH, caseData.getHwfFeeType().getLabelInWelsh(),
            HWF_REFERENCE_NUMBER, caseData.getHwFReferenceNumber()
        );
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

    private String getHwFNoRemissionReasonWelsh(CaseData caseData) {
        if (caseData.isHWFTypeHearing()) {
            return caseData.getHearingHwfDetails().getNoRemissionDetailsSummary().getLabelWelsh();
        }
        if (caseData.isHWFTypeClaimIssued()) {
            return caseData.getClaimIssuedHwfDetails().getNoRemissionDetailsSummary().getLabelWelsh();
        }
        return "";
    }
}
