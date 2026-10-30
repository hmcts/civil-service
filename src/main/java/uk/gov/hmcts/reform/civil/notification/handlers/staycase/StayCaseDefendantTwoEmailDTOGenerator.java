package uk.gov.hmcts.reform.civil.notification.handlers.staycase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.ONE_RESPONDENT_REPRESENTATIVE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_TWO;

@Component
public class StayCaseDefendantTwoEmailDTOGenerator extends StayCaseEmailDTOGenerator {

    private final SimpleStateFlowEngine stateFlowEngine;

    public StayCaseDefendantTwoEmailDTOGenerator(NotificationsProperties notificationsProperties,
        NotificationsSignatureConfiguration configuration, FeatureToggleService featureToggleService,
        SimpleStateFlowEngine stateFlowEngine) {
        super(notificationsProperties, configuration, featureToggleService);
        this.stateFlowEngine = stateFlowEngine;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        var flow = stateFlowEngine.evaluate(caseData);
        // Preserve the first matching branch of the former exclusive gateway.
        return !flow.isFlagSet(ONE_RESPONDENT_REPRESENTATIVE)
            && !flow.isFlagSet(UNREPRESENTED_DEFENDANT_ONE)
            && (flow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES) || flow.isFlagSet(UNREPRESENTED_DEFENDANT_TWO))
            && super.getShouldNotify(caseData);
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.isRespondent1LiP() ? caseData.getRespondent1().getPartyEmail()
            : caseData.getRespondentSolicitor2EmailAddress();
    }

    @Override
    protected boolean isLiP(CaseData caseData) {
        return caseData.isRespondent1LiP();
    }

    @Override
    protected boolean isBilingual(CaseData caseData) {
        return caseData.isRespondentResponseBilingual();
    }

    @Override
    protected String getPartyName(CaseData caseData) {
        return caseData.getRespondent2().getPartyName();
    }

    @Override
    protected String getReferenceTemplate() {
        return "case-stayed-defendant-notification-%s";
    }
}
