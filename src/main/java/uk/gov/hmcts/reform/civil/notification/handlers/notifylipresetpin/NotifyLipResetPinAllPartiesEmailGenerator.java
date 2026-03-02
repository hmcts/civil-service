package uk.gov.hmcts.reform.civil.notification.handlers.notifylipresetpin;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class NotifyLipResetPinAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public NotifyLipResetPinAllPartiesEmailGenerator(NotifyLipResetPinDefendantEmailDTOGenerator defendantGenerator) {
        super(List.of(defendantGenerator));
    }
}
