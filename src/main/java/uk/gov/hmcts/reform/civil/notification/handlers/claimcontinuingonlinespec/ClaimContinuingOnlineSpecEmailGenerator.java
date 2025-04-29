package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

import java.util.HashSet;
import java.util.Set;

@Component
public class ClaimContinuingOnlineSpecEmailGenerator implements PartiesEmailGenerator {

    private final ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator appGen;
    private final ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator respOneGen;
    private final ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator respTwoGen;
    private final SimpleStateFlowEngine stateFlowEngine;

    public ClaimContinuingOnlineSpecEmailGenerator(
            ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator appGen,
            ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator respOneGen,
            ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator respTwoGen,
            SimpleStateFlowEngine stateFlowEngine
    ) {
        this.appGen = appGen;
        this.respOneGen = respOneGen;
        this.respTwoGen = respTwoGen;
        this.stateFlowEngine = stateFlowEngine;
    }

    @Override
    public Set<EmailDTO> getPartiesToNotify(CaseData caseData) {
        Set<EmailDTO> set = new HashSet<>();

        if (!caseData.isApplicantNotRepresented()) {
            set.add(appGen.buildEmailDTO(caseData));
        }
        set.add(respOneGen.buildEmailDTO(caseData));
        if (stateFlowEngine.evaluate(caseData)
                .isFlagSet(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES)) {
            set.add(respTwoGen.buildEmailDTO(caseData));
        }
        return set;
    }
}
