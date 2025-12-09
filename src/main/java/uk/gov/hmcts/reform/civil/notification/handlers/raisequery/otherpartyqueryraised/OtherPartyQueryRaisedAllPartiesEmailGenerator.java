package uk.gov.hmcts.reform.civil.notification.handlers.raisequery.otherpartyqueryraised;

import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

public class OtherPartyQueryRaisedAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public OtherPartyQueryRaisedAllPartiesEmailGenerator(
        OtherPartyQueryRaisedAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        OtherPartyQueryRaisedRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        OtherPartyQueryRaisedClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        OtherPartyQueryRaisedDefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(List.of(appSolOneEmailDTOGenerator,
                      respSolOneEmailDTOGenerator,
                      claimantEmailDTOGenerator,
                      defendantEmailDTOGenerator));
    }
}
