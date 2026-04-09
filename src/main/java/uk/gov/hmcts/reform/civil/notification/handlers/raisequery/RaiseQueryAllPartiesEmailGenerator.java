package uk.gov.hmcts.reform.civil.notification.handlers.raisequery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class RaiseQueryAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public RaiseQueryAllPartiesEmailGenerator(
        RaiseQueryAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        RaiseQueryRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        RaiseQueryClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        RaiseQueryDefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(List.of(appSolOneEmailDTOGenerator,
                      respSolOneEmailDTOGenerator,
                      claimantEmailDTOGenerator,
                      defendantEmailDTOGenerator));
    }
}
