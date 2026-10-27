package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class BreathingSpaceLiftedAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public BreathingSpaceLiftedAllPartiesEmailGenerator(
        BreathingSpaceLiftedAppSolOneEmailDTOGenerator appSolOne,
        BreathingSpaceLiftedRespSolOneEmailDTOGenerator respSolOne) {
        super(List.of(appSolOne, respSolOne));
    }
}
