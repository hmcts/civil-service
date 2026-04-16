package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DirectionQuestionnaireLipGeneratorFactory {

    private final DirectionQuestionnaireLipResponseGenerator directionQuestionnaireLipResponseGenerator;

    public DirectionsQuestionnaireGenerator getDirectionQuestionnaire() {
        return directionQuestionnaireLipResponseGenerator;
    }
}
