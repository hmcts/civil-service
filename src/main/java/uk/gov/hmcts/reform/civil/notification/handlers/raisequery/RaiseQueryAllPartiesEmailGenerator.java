package uk.gov.hmcts.reform.civil.notification.handlers.raisequery;

import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

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
