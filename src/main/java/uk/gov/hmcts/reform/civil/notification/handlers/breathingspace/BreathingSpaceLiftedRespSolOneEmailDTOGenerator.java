package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class BreathingSpaceLiftedRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final BreathingSpaceEmailHelper helper;
    private final NotificationsProperties notificationsProperties;

    public BreathingSpaceLiftedRespSolOneEmailDTOGenerator(OrganisationService organisationService,
        BreathingSpaceEmailHelper helper, NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.helper = helper;
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return !caseData.isRespondent1NotRepresented();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getBreathingSpaceLiftedRespondentEmailTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "breathing-space-lifted-notification-%s";
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return helper.addProperties(caseData);
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(Map.of(
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, helper.getLiftedOrganisationName(caseData.getRespondent1OrganisationPolicy(), null)
        ));
        return properties;
    }
}
