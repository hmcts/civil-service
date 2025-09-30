package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DocUploadNotificationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.SolicitorEmailValidation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.CASE_TITLE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.GA_LIP_APPLICANT_NAME;
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.addAllFooterItems;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslatedDocumentUploadedApplicantNotificationHandler extends CallbackHandler
    implements NotificationDataGA {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final CaseDetailsConverter caseDetailsConverter;
    private final GaForLipService gaForLipService;
    private final CoreCaseDataService coreCaseDataService;
    private final SolicitorEmailValidation solicitorEmailValidation;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;

    private static final List<CaseEvent> EVENTS = List.of(CaseEvent.NOTIFY_APPLICANT_TRANSLATED_DOCUMENT_UPLOADED_GA);
    private static final String REFERENCE_TEMPLATE = "translated-document-uploaded-applicant-notification-%s";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::notifyApplicant
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }


    @Override
    public Map<String, String> addProperties(CaseData caseData, CaseData mainCaseData) {
        if (gaForLipService.isLipApp(caseData)) {
            String caseTitle = DocUploadNotificationService.getAllPartyNames(caseData);
            String isLipAppName = caseData.getApplicantPartyName();
            HashMap<String, String> properties = new HashMap<>(Map.of(
                CASE_TITLE, Objects.requireNonNull(caseTitle),
                GA_LIP_APPLICANT_NAME, Objects.requireNonNull(isLipAppName),
                CASE_REFERENCE, caseData.getParentCaseReference()
            ));
            addAllFooterItems(caseData, mainCaseData, properties, configuration,
                              featureToggleService.isPublicQueryManagementEnabled(caseData));
            return properties;
        }
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CASE_REFERENCE, caseData.getParentCaseReference(),
            CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData)
        ));
        addAllFooterItems(caseData, mainCaseData, properties, configuration,
                          featureToggleService.isPublicQueryManagementEnabled(mainCaseData));
        return properties;
    }

    private CallbackResponse notifyApplicant(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Translated document uploaded for applicant for case: {}", caseData.getCcdCaseReference());
        CaseData civilCaseData = caseDetailsConverter
            .toCaseDataGA(coreCaseDataService
                            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        caseData = solicitorEmailValidation.validateSolicitorEmail(civilCaseData, caseData);
        notificationService.sendMail(
            caseData.getGeneralAppApplnSolicitor().getEmail(),
            addTemplate(caseData),
            addProperties(caseData, civilCaseData),
            String.format(REFERENCE_TEMPLATE, caseData.getCcdCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String addTemplate(CaseData caseData) {
        if (gaForLipService.isLipApp(caseData)) {
            if (caseData.isApplicantBilingual()) {
                return notificationsProperties.getNotifyApplicantLiPTranslatedDocumentUploadedWhenParentCaseInBilingual();
            }
            return notificationsProperties.getLipGeneralAppApplicantEmailTemplate();
        }
        return notificationsProperties.getNotifyLRTranslatedDocumentUploaded();

    }

    public String getApplicantLegalOrganizationName(CaseData caseData) {
        var organisationId = caseData.getGeneralAppApplnSolicitor().getOrganisationIdentifier();
        return organisationService.findOrganisationById(organisationId)
                .map(Organisation::getName)
                .orElseThrow(() -> new RuntimeException("Organisation not found for id: " + organisationId));
    }

}
