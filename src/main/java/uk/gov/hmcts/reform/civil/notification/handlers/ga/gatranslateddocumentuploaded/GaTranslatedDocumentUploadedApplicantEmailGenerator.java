package uk.gov.hmcts.reform.civil.notification.handlers.ga.gatranslateddocumentuploaded;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CASE_TITLE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.GA_LIP_APPLICANT_NAME;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.addAllFooterItems;

@Component
@RequiredArgsConstructor
public class GaTranslatedDocumentUploadedApplicantEmailGenerator extends GaEmailDTOGenerator {

    public static final String REFERENCE_TEMPLATE = "translated-document-uploaded-applicant-notification-%s";

    private final NotificationsProperties templates;
    private final GaForLipService gaForLipService;
    private final OrganisationService organisationService;
    private final FeatureToggleService featureToggleService;
    private final NotificationsSignatureConfiguration configuration;

    @Override
    public Boolean getShouldNotify(GaNotificationContext context) {
        return true;
    }

    @Override
    protected String getEmailAddress(GaNotificationContext context) {
        return context.gaCaseData().getGeneralAppApplnSolicitor().getEmail();
    }

    @Override
    protected String getEmailTemplateId(GaNotificationContext context) {
        var data = context.gaCaseData();
        if (gaForLipService.isLipApp(data)) {
            return data.isApplicantBilingual()
                ? templates.getNotifyApplicantLiPTranslatedDocumentUploadedWhenParentCaseInBilingual()
                : templates.getLipGeneralAppApplicantEmailTemplate();
        }
        return templates.getNotifyLRTranslatedDocumentUploaded();
    }

    @Override
    protected String buildEmailReference(GaNotificationContext context) {
        return String.format(REFERENCE_TEMPLATE, context.getCcdCaseReference());
    }

    @Override
    public Map<String, String> addProperties(GaNotificationContext context) {
        var data = context.gaCaseData();
        boolean lip = gaForLipService.isLipApp(data);
        Map<String, String> properties = new HashMap<>();
        properties.put(CASE_REFERENCE, Objects.requireNonNull(data.getParentCaseReference()));
        if (lip) {
            properties.put(CASE_TITLE, Objects.requireNonNull(DocUploadNotificationService.getAllPartyNames(data)));
            properties.put(GA_LIP_APPLICANT_NAME, Objects.requireNonNull(data.getApplicantPartyName()));
        } else {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, organisationService
                .findOrganisationById(data.getGeneralAppApplnSolicitor().getOrganisationIdentifier())
                .map(Organisation::getName).orElseThrow(RuntimeException::new));
        }
        // The legacy LR applicant email evaluates the query-management flag against the parent claim.
        addAllFooterItems(data, context.mainCaseData(), properties, configuration,
            featureToggleService.isPublicQueryManagementEnabledGa(lip ? data : context.mainCaseData()));
        return properties;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, GaNotificationContext context) {
        return properties;
    }
}
