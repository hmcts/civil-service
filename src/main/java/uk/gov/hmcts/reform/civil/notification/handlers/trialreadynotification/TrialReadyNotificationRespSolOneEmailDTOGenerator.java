package uk.gov.hmcts.reform.civil.notification.handlers.trialreadynotification;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class TrialReadyNotificationRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_RESPONDENT_1 = "trial-ready-respondent-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected TrialReadyNotificationRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getSolicitorTrialReady();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_RESPONDENT_1;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData, true, organisationService));
        properties.put(HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE));
        return properties;
    }
}
