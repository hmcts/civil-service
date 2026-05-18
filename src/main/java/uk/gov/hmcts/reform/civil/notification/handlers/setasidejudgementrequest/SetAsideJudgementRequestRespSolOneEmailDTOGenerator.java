package uk.gov.hmcts.reform.civil.notification.handlers.setasidejudgementrequest;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;

@Component
public class SetAsideJudgementRequestRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "set-aside-judgment-defendant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected SetAsideJudgementRequestRespSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                                  OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifySetAsideJudgmentTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        properties.put(LEGAL_ORG_NAME, properties.get(CLAIM_LEGAL_ORG_NAME_SPEC));
        properties.put(DEFENDANT_NAME_INTERIM, getDefendantNameBasedOnCaseType(caseData));
        properties.put(REASON_FROM_CASEWORKER, caseData.getJoSetAsideJudgmentErrorText());
        return properties;
    }
}
