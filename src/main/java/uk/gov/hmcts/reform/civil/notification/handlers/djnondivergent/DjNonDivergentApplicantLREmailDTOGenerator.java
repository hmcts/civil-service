package uk.gov.hmcts.reform.civil.notification.handlers.djnondivergent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;

@Component
public class DjNonDivergentApplicantLREmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "dj-non-divergent-applicant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public DjNonDivergentApplicantLREmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                       OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDJNonDivergentSpecClaimantTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        properties.put(DEFENDANT_NAME_INTERIM, getDefendantNameBasedOnCaseType(caseData));
        return properties;
    }

    // getShouldNotify() is inherited from AppSolOneEmailDTOGenerator
    // Returns TRUE when applicant is NOT LiP (i.e., legally represented)
}
