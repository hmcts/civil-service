package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import java.util.List;

@Component
public class ClaimantDefendantAgreedMediationPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantDefendantAgreedMediationPartiesEmailGenerator(ClaimantDefendantAgreedMediationAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
                                                                 ClaimantDefendantAgreedMediationRespSolOneEmailDTOGenerator respSolOneEmailGenerator,
                                                                 ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator,
                                                                 ClaimantDefendantAgreedMediationDefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(List.of(appSolOneEmailGenerator, respSolOneEmailGenerator, respSolTwoEmailGenerator, defendantEmailDTOGenerator));
    }
}
