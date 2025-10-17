package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.GaCallbackDataUtil;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DocUploadNotificationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.SolicitorEmailValidation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
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
    private final ObjectMapper objectMapper;

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
        GeneralApplicationCaseData gaCaseData = GaCallbackDataUtil.toGaCaseData(caseData, objectMapper);
        return addProperties(gaCaseData, mainCaseData);
    }

    private Map<String, String> addProperties(GeneralApplicationCaseData caseData, CaseData mainCaseData) {
        if (gaForLipService.isLipAppGa(caseData)) {
            String caseTitle = DocUploadNotificationService.getAllPartyNames(caseData);
            String isLipAppName = caseData.getApplicantPartyName();
            HashMap<String, String> properties = new HashMap<>(Map.of(
                CASE_TITLE, Objects.requireNonNull(caseTitle),
                GA_LIP_APPLICANT_NAME, Objects.requireNonNull(isLipAppName),
                CASE_REFERENCE, caseData.getParentCaseReference()
            ));
            addAllFooterItems(caseData, mainCaseData, properties, configuration,
                              featureToggleService.isPublicQueryManagementEnabledGa(caseData));
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
        GeneralApplicationCaseData gaCaseData = GaCallbackDataUtil.resolveGaCaseData(callbackParams, objectMapper);
        CaseData caseData = GaCallbackDataUtil.mergeToCaseData(gaCaseData, callbackParams.getCaseData(), objectMapper);
        log.info("Translated document uploaded for applicant for case: {}", caseData.getCcdCaseReference());
        CaseData civilCaseData = caseDetailsConverter.toCaseDataGA(
            coreCaseDataService.getCase(Long.parseLong(gaCaseData.getGeneralAppParentCaseLink().getCaseReference()))
        );

        GeneralApplicationCaseData updatedGaCaseData = solicitorEmailValidation.validateSolicitorEmail(
            civilCaseData,
            gaCaseData
        );
        notificationService.sendMail(
            updatedGaCaseData.getGeneralAppApplnSolicitor().getEmail(),
            addTemplate(updatedGaCaseData),
            addProperties(updatedGaCaseData, civilCaseData),
            String.format(REFERENCE_TEMPLATE, updatedGaCaseData.getCcdCaseReference())
        );
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    private String addTemplate(GeneralApplicationCaseData caseData) {
        if (gaForLipService.isLipAppGa(caseData)) {
            if (caseData.isApplicantBilingual()) {
                return notificationsProperties.getNotifyApplicantLiPTranslatedDocumentUploadedWhenParentCaseInBilingual();
            }
            return notificationsProperties.getLipGeneralAppApplicantEmailTemplate();
        }
        return notificationsProperties.getNotifyLRTranslatedDocumentUploaded();

    }

    public String getApplicantLegalOrganizationName(GeneralApplicationCaseData caseData) {
        var organisationId = caseData.getGeneralAppApplnSolicitor().getOrganisationIdentifier();
        return organisationService.findOrganisationById(organisationId)
                .map(Organisation::getName)
                .orElseThrow(() -> new RuntimeException("Organisation not found for id: " + organisationId));
    }

}
