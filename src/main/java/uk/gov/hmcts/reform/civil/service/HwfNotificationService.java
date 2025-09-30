package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.handler.callback.user.JudicialFinalDecisionHandler;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.utils.HwFFeeTypeService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.addAllFooterItems;

@Slf4j
@Service
@RequiredArgsConstructor
public class HwfNotificationService implements NotificationDataGA {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;
    private final SolicitorEmailValidation solicitorEmailValidation;
    private Map<CaseEvent, String> emailTemplates;
    private Map<CaseEvent, String> emailTemplatesBilingual;
    private CaseEvent event;
    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;

    private static final String ERROR_HWF_EVENT = "Hwf Event not support";

    public void sendNotification(CaseData caseData) throws NotificationException {
        sendNotification(caseData, null);
    }

    public void sendNotification(CaseData caseData, CaseEvent event) throws NotificationException {
        CaseData civilCaseData = caseDetailsConverter
                .toCaseDataGA(coreCaseDataService
                        .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        caseData = solicitorEmailValidation.validateSolicitorEmail(civilCaseData, caseData);
        if (Objects.isNull(event)) {
            event = getEvent(caseData);
        }
        log.info("Sending help with fees notification for Case ID: {}", caseData.getCcdCaseReference());

        notificationService.sendMail(
                caseData.getGeneralAppApplnSolicitor().getEmail(),
                caseData.isApplicantBilingual() ? getTemplateBilingual(event) :
                    getTemplate(event),
                addAllProperties(caseData, civilCaseData, event),
                caseData.getGeneralAppParentCaseLink().getCaseReference()
        );
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData, CaseData mainCaseDate) {
        return getCommonProperties(caseData, mainCaseDate);
    }

    private Map<String, String> addAllProperties(CaseData caseData, CaseData mainCaseData, CaseEvent event) {
        Map<String, String> commonProperties = addProperties(caseData, mainCaseData);
        Map<String, String> furtherProperties = getFurtherProperties(caseData, event);
        return Collections.unmodifiableMap(
            Stream.concat(commonProperties.entrySet().stream(), furtherProperties.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private Map<String, String> getCommonProperties(CaseData caseData, CaseData mainCaseData) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
                CASE_REFERENCE, caseData.getParentCaseReference(),
                CLAIMANT_NAME, caseData.getApplicantPartyName(),
                TYPE_OF_FEE, caseData.getHwfFeeType().getLabel(),
                TYPE_OF_FEE_WELSH, caseData.getHwfFeeType().getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber()
        ));
        addAllFooterItems(caseData, mainCaseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(caseData));
        return properties;
    }

    private Map<String, String> getPartialRemissionProperties(CaseData caseData) {
        BigDecimal remission;
        BigDecimal outstanding;
        if (caseData.isHWFTypeApplication()) {
            remission = MonetaryConversions
                    .penniesToPounds(HwFFeeTypeService.getGaRemissionAmount(caseData));
            outstanding =  MonetaryConversions
                .penniesToPounds(caseData.getGaHwfDetails().getOutstandingFee());
        } else {
            remission = MonetaryConversions
                    .penniesToPounds(HwFFeeTypeService.getAdditionalRemissionAmount(caseData));
            outstanding = MonetaryConversions
                .penniesToPounds(caseData.getAdditionalHwfDetails().getOutstandingFee());
        }
        return Map.of(
            PART_AMOUNT, remission.toString(),
            REMAINING_AMOUNT, outstanding.toString()
        );
    }

    private Map<String, String> getFurtherProperties(CaseData caseData, CaseEvent event) {
        return switch (event) {
            case MORE_INFORMATION_HWF_GA -> getMoreInformationProperties(caseData);
            case PARTIAL_REMISSION_HWF_GA -> getPartialRemissionProperties(caseData);
            case INVALID_HWF_REFERENCE_GA, UPDATE_HELP_WITH_FEE_NUMBER_GA -> Collections.emptyMap();
            case NO_REMISSION_HWF_GA -> getNoRemissionProperties(caseData);
            case FEE_PAYMENT_OUTCOME_GA -> getPaymentOutcomeProperties(caseData);
            default -> throw new NotificationException(new Exception(ERROR_HWF_EVENT));
        };
    }

    private Map<String, String> getNoRemissionProperties(CaseData caseData) {
        String remission;
        BigDecimal outstanding;
        if (caseData.isHWFTypeApplication()) {
            remission = caseData.getGaHwfDetails().getNoRemissionDetailsSummary().getLabel();
            outstanding = MonetaryConversions
                .penniesToPounds(caseData.getGaHwfDetails().getOutstandingFee());
        } else {
            remission = caseData.getAdditionalHwfDetails().getNoRemissionDetailsSummary().getLabel();
            outstanding = MonetaryConversions
                .penniesToPounds(caseData.getAdditionalHwfDetails().getOutstandingFee());
        }
        return Map.of(
            FEE_AMOUNT, outstanding.toString(),
            NO_REMISSION_REASONS_WELSH, getHwFNoRemissionReasonWelsh(caseData),
            NO_REMISSION_REASONS, remission
        );
    }

    private String getHwFNoRemissionReasonWelsh(CaseData caseData) {
        if (caseData.isHWFTypeApplication()) {
            return caseData.getGaHwfDetails().getNoRemissionDetailsSummary().getLabelWelsh();
        } else {
            return caseData.getAdditionalHwfDetails().getNoRemissionDetailsSummary().getLabelWelsh();
        }
    }

    private CaseEvent getEvent(CaseData caseData) {
        if (caseData.getHwfFeeType().equals(FeeType.APPLICATION)) {
            return caseData.getGaHwfDetails().getHwfCaseEvent();
        } else if (caseData.getHwfFeeType().equals(FeeType.ADDITIONAL)) {
            return caseData.getAdditionalHwfDetails().getHwfCaseEvent();
        } else {
            throw new NotificationException(new Exception(ERROR_HWF_EVENT));
        }
    }

    private String getTemplate(CaseEvent hwfEvent) {
        if (emailTemplates == null) {
            emailTemplates = Map.of(
                    CaseEvent.INVALID_HWF_REFERENCE_GA,
                    notificationsProperties.getNotifyApplicantForHwfInvalidRefNumber(),
                    CaseEvent.MORE_INFORMATION_HWF_GA,
                    notificationsProperties.getNotifyApplicantForHwFMoreInformationNeeded(),
                    CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER_GA,
                    notificationsProperties.getNotifyApplicantForHwfUpdateRefNumber(),
                    CaseEvent.PARTIAL_REMISSION_HWF_GA,
                    notificationsProperties.getNotifyApplicantForHwfPartialRemission(),
                    CaseEvent.NO_REMISSION_HWF_GA,
                    notificationsProperties.getNotifyApplicantForNoRemission(),
                    CaseEvent.FEE_PAYMENT_OUTCOME_GA,
                    notificationsProperties.getNotifyApplicantForHwfPaymentOutcome()
            );
        }
        return emailTemplates.get(hwfEvent);
    }

    private String getTemplateBilingual(CaseEvent hwfEvent) {
        if (emailTemplatesBilingual == null) {
            emailTemplatesBilingual = Map.of(
                CaseEvent.INVALID_HWF_REFERENCE_GA,
                notificationsProperties.getNotifyApplicantForHwfInvalidRefNumberBilingual(),
                CaseEvent.MORE_INFORMATION_HWF_GA,
                notificationsProperties.getNotifyApplicantForHwFMoreInformationNeededWelsh(),
                CaseEvent.NO_REMISSION_HWF_GA,
                notificationsProperties.getNotifyApplicantForHwfNoRemissionWelsh(),
                CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER_GA,
                notificationsProperties.getNotifyApplicantForHwfUpdateRefNumberBilingual(),
                CaseEvent.PARTIAL_REMISSION_HWF_GA,
                notificationsProperties.getNotifyApplicantForHwfPartialRemissionBilingual(),
                CaseEvent.FEE_PAYMENT_OUTCOME_GA,
                notificationsProperties.getLipGeneralAppApplicantEmailTemplateInWelsh()
            );
        }
        return emailTemplatesBilingual.get(hwfEvent);
    }

    private Map<String, String> getMoreInformationProperties(CaseData caseData) {
        HelpWithFeesMoreInformation moreInformation =
                null != caseData.getHelpWithFeesMoreInformationGa()
                        ? caseData.getHelpWithFeesMoreInformationGa()
                        : caseData.getHelpWithFeesMoreInformationAdditional();
        return Map.of(
            HWF_MORE_INFO_DATE, formatLocalDate(moreInformation.getHwFMoreInfoDocumentDate(), DATE),
            HWF_MORE_INFO_DATE_IN_WELSH, formatDateInWelsh(moreInformation.getHwFMoreInfoDocumentDate()),
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

    private Map<String, String> getPaymentOutcomeProperties(CaseData caseData) {
        String caseTitle = JudicialFinalDecisionHandler.getAllPartyNames(caseData);
        return Map.of(
                CASE_TITLE, caseTitle,
                APPLICANT_NAME, caseData.getApplicantPartyName()
                );
    }
}
