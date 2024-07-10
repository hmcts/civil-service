package uk.gov.hmcts.reform.civil.service.defendantresponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.DirectionsQuestionnairePreparer;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

@Component
@RequiredArgsConstructor
public class DefendantResponseNonCamundaWorkFlow {

    private final DirectionsQuestionnairePreparer directionsQuestionnairePreparer;
}
