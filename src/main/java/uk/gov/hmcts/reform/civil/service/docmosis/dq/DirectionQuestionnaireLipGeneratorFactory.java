package uk.gov.hmcts.reform.civil.service.docmosis.dq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DirectionQuestionnaireLipGeneratorFactory {

    private final DirectionsQuestionnaireLipGenerator directionsQuestionnaireLipGenerator;

    public DirectionsQuestionnaireGenerator getDirectionQuestionnaire() {
        log.info("Testing DTSCCI-2300");
        return directionsQuestionnaireLipGenerator;
    }
}
