package uk.gov.hmcts.reform.civil.notification.handlers.djnondivergent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;

@Component
public class DjNonDivergentDefendant1LREmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "dj-non-divergent-defendant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public DjNonDivergentDefendant1LREmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                        OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDJNonDivergentSpecDefendantTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        properties.put(DEFENDANT_NAME_INTERIM, getDefendantNameBasedOnCaseType(caseData));
        properties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
        return properties;
    }

    // getShouldNotify() is inherited from RespSolOneEmailDTOGenerator
    // Returns TRUE when respondent is NOT LiP (i.e., legally represented)
}
