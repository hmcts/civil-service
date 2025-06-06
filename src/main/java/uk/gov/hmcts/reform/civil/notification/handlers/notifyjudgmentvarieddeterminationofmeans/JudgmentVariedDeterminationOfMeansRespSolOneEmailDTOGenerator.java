package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getRespondentLegalOrganizationName;

@Component
public class JudgmentVariedDeterminationOfMeansRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "defendant-judgment-varied-determination-of-means-%s";

    private final NotificationsProperties notificationsProperties;

    public JudgmentVariedDeterminationOfMeansRespSolOneEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDefendantJudgmentVariedDeterminationOfMeansTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(DEFENDANT_NAME_SPEC,
                getRespondentLegalOrganizationName(caseData.getRespondent1OrganisationPolicy(), organisationService));
        return properties;
    }
}
