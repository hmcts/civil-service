package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.ONE_RESPONDENT_REPRESENTATIVE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
public class BreathingSpaceEnterRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final BreathingSpaceEmailHelper helper;
    private final NotificationsProperties notificationsProperties;
    private final SimpleStateFlowEngine stateFlowEngine;

    public BreathingSpaceEnterRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
        BreathingSpaceEmailHelper helper, NotificationsProperties notificationsProperties, SimpleStateFlowEngine stateFlowEngine) {
        super(organisationService);
        this.helper = helper;
        this.notificationsProperties = notificationsProperties;
        this.stateFlowEngine = stateFlowEngine;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        var flow = stateFlowEngine.evaluate(caseData);
        return !flow.isFlagSet(ONE_RESPONDENT_REPRESENTATIVE) && flow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES);
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        String email = caseData.getRespondentSolicitor2EmailAddress();
        return email == null && caseData.getRespondent2SameLegalRepresentative() == YES
            ? caseData.getRespondentSolicitor1EmailAddress() : email;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getBreathingSpaceEnterDefendantEmailTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-confirms-not-to-proceed-respondent-notification-%s";
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return helper.addProperties(caseData);
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_DEFENDANT_LEGAL_ORG_NAME_SPEC, helper.getEnterOrganisationName(caseData.getRespondent2OrganisationPolicy(), null));
        properties.put("defendantName", caseData.getRespondent2().getPartyName());
        return properties;
    }
}
