package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondate;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class InformAgreedExtensionDateAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public InformAgreedExtensionDateAllPartiesEmailGenerator(
        InformAgreedExtensionDateAppSolOneEmailDTOGenerator applicantGenerator,
        InformAgreedExtensionDateRespSolOneEmailDTOGenerator respondentOneGenerator,
        InformAgreedExtensionDateRespSolTwoEmailDTOGenerator respondentTwoGenerator
    ) {
        super(List.of(
            applicantGenerator,
            respondentOneGenerator,
            respondentTwoGenerator
        ));
    }
}
