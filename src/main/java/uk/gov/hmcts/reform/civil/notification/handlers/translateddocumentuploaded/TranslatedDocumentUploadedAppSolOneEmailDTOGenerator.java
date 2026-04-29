package uk.gov.hmcts.reform.civil.notification.handlers.translateddocumentuploaded;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class TranslatedDocumentUploadedAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "translated-document-uploaded-claimant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected TranslatedDocumentUploadedAppSolOneEmailDTOGenerator(
        OrganisationService organisationService,
        NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyClaimantTranslatedDocumentUploaded();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}
