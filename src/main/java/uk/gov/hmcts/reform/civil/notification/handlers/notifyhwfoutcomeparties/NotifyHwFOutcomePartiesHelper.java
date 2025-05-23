package uk.gov.hmcts.reform.civil.notification.handlers.notifyhwfoutcomeparties;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_MORE_INFO_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_MORE_INFO_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_MORE_INFO_DOCUMENTS_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PART_AMOUNT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REMAINING_AMOUNT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASONS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASONS_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.TYPE_OF_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.TYPE_OF_FEE_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AMOUNT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class NotifyHwFOutcomePartiesHelper {
    private final NotificationsProperties notificationsProperties;

    public NotifyHwFOutcomePartiesHelper(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    public String getTemplate(CaseEvent hwfEvent) {
        return switch (hwfEvent) {
            case INVALID_HWF_REFERENCE -> notificationsProperties.getNotifyApplicantForHwfInvalidRefNumber();
            case NO_REMISSION_HWF -> notificationsProperties.getNotifyApplicantForHwfNoRemission();
            case MORE_INFORMATION_HWF -> notificationsProperties.getNotifyApplicantForHwFMoreInformationNeeded();
            case UPDATE_HELP_WITH_FEE_NUMBER -> notificationsProperties.getNotifyApplicantForHwfUpdateRefNumber();
            case PARTIAL_REMISSION_HWF_GRANTED -> notificationsProperties.getNotifyApplicantForHwfPartialRemission();
            default -> null;
        };
    }

    public String getTemplateBilingual(CaseEvent hwfEvent) {
        return switch (hwfEvent) {
            case INVALID_HWF_REFERENCE -> notificationsProperties.getNotifyApplicantForHwfInvalidRefNumberBilingual();
            case MORE_INFORMATION_HWF -> notificationsProperties.getNotifyApplicantForHwFMoreInformationNeededWelsh();
            case NO_REMISSION_HWF -> notificationsProperties.getNotifyApplicantForHwfNoRemissionWelsh();
            case UPDATE_HELP_WITH_FEE_NUMBER -> notificationsProperties.getNotifyApplicantForHwfUpdateRefNumberBilingual();
            case PARTIAL_REMISSION_HWF_GRANTED -> notificationsProperties.getNotifyApplicantForHwfPartialRemissionBilingual();
            default -> null;
        };
    }

    public Map<String, String> getCommonProperties(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
            CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
            TYPE_OF_FEE, caseData.getHwfFeeType().getLabel(),
            TYPE_OF_FEE_WELSH, caseData.getHwfFeeType().getLabelInWelsh(),
            HWF_REFERENCE_NUMBER, caseData.getHwFReferenceNumber()
        );
    }

    public Map<String, String> getFurtherProperties(CaseData caseData) {
        if (caseData.getHwFEvent() == null) {
            throw new IllegalArgumentException("HwFEvent is null in CaseData");
        }
        return switch (caseData.getHwFEvent()) {
            case NO_REMISSION_HWF -> getNoRemissionProperties(caseData);
            case MORE_INFORMATION_HWF -> getMoreInformationProperties(caseData);
            case PARTIAL_REMISSION_HWF_GRANTED -> getPartialRemissionProperties(caseData);
            case INVALID_HWF_REFERENCE, UPDATE_HELP_WITH_FEE_NUMBER -> Collections.emptyMap();
            default -> throw new IllegalArgumentException("case event not found");
        };
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

    private Map<String, String> getPartialRemissionProperties(CaseData caseData) {
        return Map.of(
            PART_AMOUNT, caseData.getRemissionAmount().toString(),
            REMAINING_AMOUNT, caseData.getOutstandingFeeInPounds().toString()
        );
    }

    public String getHwFNoRemissionReason(CaseData caseData) {
        if (caseData.isHWFTypeHearing()) {
            return caseData.getHearingHwfDetails().getNoRemissionDetailsSummary().getLabel();
        }
        if (caseData.isHWFTypeClaimIssued()) {
            return caseData.getClaimIssuedHwfDetails().getNoRemissionDetailsSummary().getLabel();
        }
        return "";
    }

    public String getHwFNoRemissionReasonWelsh(CaseData caseData) {
        if (caseData.isHWFTypeHearing()) {
            return caseData.getHearingHwfDetails().getNoRemissionDetailsSummary().getLabelWelsh();
        }
        if (caseData.isHWFTypeClaimIssued()) {
            return caseData.getClaimIssuedHwfDetails().getNoRemissionDetailsSummary().getLabelWelsh();
        }
        return "";
    }

    public String getMoreInformationDocumentList(List<HwFMoreInfoRequiredDocuments> list) {
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

    public String getMoreInformationDocumentListWelsh(List<HwFMoreInfoRequiredDocuments> list) {
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
}
