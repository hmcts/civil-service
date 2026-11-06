package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class BreathingSpaceEnterAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public BreathingSpaceEnterAllPartiesEmailGenerator(
        BreathingSpaceEnterAppSolOneEmailDTOGenerator appSolOne,
        BreathingSpaceEnterRespSolOneEmailDTOGenerator respSolOne,
        BreathingSpaceEnterRespSolTwoEmailDTOGenerator respSolTwo) {
        super(List.of(appSolOne, respSolOne, respSolTwo));
    }
}
