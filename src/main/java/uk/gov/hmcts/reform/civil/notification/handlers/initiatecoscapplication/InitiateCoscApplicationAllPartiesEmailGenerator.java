package uk.gov.hmcts.reform.civil.notification.handlers.initiatecoscapplication;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class InitiateCoscApplicationAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public InitiateCoscApplicationAllPartiesEmailGenerator(
        InitiateCoscApplicationAppSolEmailDTOGenerator appSolEmailDTOGenerator) {
        super(List.of(appSolEmailDTOGenerator));
    }
}
