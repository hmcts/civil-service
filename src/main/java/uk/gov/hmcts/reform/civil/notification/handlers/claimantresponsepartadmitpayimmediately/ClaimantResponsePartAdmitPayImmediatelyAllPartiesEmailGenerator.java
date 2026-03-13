package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsepartadmitpayimmediately;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimantResponsePartAdmitPayImmediatelyAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantResponsePartAdmitPayImmediatelyAllPartiesEmailGenerator(ClaimantResponsePartAdmitPayImmediatelyRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
                                                                           ClaimantResponsePartAdmitPayImmediatelyAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator) {
        super(List.of(respSolOneEmailDTOGenerator, appSolOneEmailDTOGenerator));
    }
}
