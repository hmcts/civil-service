package uk.gov.hmcts.reform.civil.notification.handlers.trialreadynotification;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class TrialReadyNotificationAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_APPLICANT = "trial-ready-applicant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected TrialReadyNotificationAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getSolicitorTrialReady();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_APPLICANT;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE));
        return properties;
    }
}
