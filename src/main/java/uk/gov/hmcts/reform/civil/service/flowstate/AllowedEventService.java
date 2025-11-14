package uk.gov.hmcts.reform.civil.service.flowstate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.repository.AllowedEventRepository;
import uk.gov.hmcts.reform.civil.service.flowstate.scenario.AllowedEventScenario;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.List;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@RequiredArgsConstructor
public class AllowedEventService {

    private final AllowedEventRepository repo;
    private final IStateFlowEngine stateFlowEngine;
    private final CaseDetailsConverter caseDetailsConverter;
    private final List<AllowedEventScenario> scenarios;

    private static boolean isSpecOrLip(CaseData caseData, CaseEvent event) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            || CREATE_CLAIM_SPEC.equals(event)
            || CREATE_LIP_CLAIM.equals(event);
    }

    public boolean isAllowed(CaseDetails caseDetails, CaseEvent event) {

        if (repo.getWhitelist().contains(event)) {
            return true;
        }

        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        boolean specOrLip = isSpecOrLip(caseData, event);
        AllowedEventScenario scenario = scenarios.stream()
            .filter(s -> s.appliesTo(specOrLip))
            .findFirst()
            .orElse(null);

        if (scenario == null) {
            log.warn(
                "Scenario not found ({}:{})",
                MultiPartyScenario.getMultiPartyScenario(caseData),
                caseData.getCaseAccessCategory()
            );
            return false;
        }

        StateFlow stateFlow = specOrLip ? stateFlowEngine.evaluateSpec(caseDetails)
            : stateFlowEngine.evaluate(caseDetails);
        return scenario.loadBaseEvents(stateFlow.getState().getName()).contains(event);
    }

}
