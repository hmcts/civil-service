package uk.gov.hmcts.reform.civil.notification.handlers.stayupdaterequested;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.ONE_RESPONDENT_REPRESENTATIVE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_TWO;

@Component
@RequiredArgsConstructor
public class StayUpdateRequestedDefendantTwoEmailDTOGenerator extends DefendantTwoEmailDTOGenerator {

    private final StayUpdateRequestedEmailHelper helper;
    private final SimpleStateFlowEngine stateFlowEngine;

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        var flow = stateFlowEngine.evaluate(caseData);
        return !flow.isFlagSet(ONE_RESPONDENT_REPRESENTATIVE)
            && !flow.isFlagSet(UNREPRESENTED_DEFENDANT_ONE)
            && (flow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES) || flow.isFlagSet(UNREPRESENTED_DEFENDANT_TWO));
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.isRespondent1LiP() && caseData.getRespondent1().getPartyEmail() != null
            ? caseData.getRespondent1().getPartyEmail()
            : caseData.getRespondentSolicitor2EmailAddress();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return helper.getTemplate(caseData.isRespondent1LiP(), caseData.isRespondentResponseBilingual());
    }

    @Override
    protected String getReferenceTemplate() {
        return "stay-update-requested-defendant-notification-%s";
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return helper.addProperties(caseData, caseData.isRespondent1LiP());
    }

    @Override
    public EmailDTO buildEmailDTO(CaseData caseData, String taskId) {
        return helper.withCcdReference(super.buildEmailDTO(caseData, taskId), getReferenceTemplate(), caseData);
    }
}
