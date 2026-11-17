package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.ONE_RESPONDENT_REPRESENTATIVE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class BreathingSpaceLiftedRespSolTwoEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final BreathingSpaceEmailHelper helper;
    private final NotificationsProperties notificationsProperties;
    private final SimpleStateFlowEngine stateFlowEngine;

    public BreathingSpaceLiftedRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                                           BreathingSpaceEmailHelper helper, NotificationsProperties notificationsProperties,
                                                           SimpleStateFlowEngine stateFlowEngine) {
        super(organisationService);
        this.helper = helper;
        this.notificationsProperties = notificationsProperties;
        this.stateFlowEngine = stateFlowEngine;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        var flow = stateFlowEngine.evaluate(caseData);
        return !caseData.isRespondent2NotRepresented()
            && !flow.isFlagSet(ONE_RESPONDENT_REPRESENTATIVE) && flow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES);
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
            RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
            CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, helper.getLiftedOrganisationName(caseData.getRespondent2OrganisationPolicy(), null)
        ));
        return properties;
    }
}
