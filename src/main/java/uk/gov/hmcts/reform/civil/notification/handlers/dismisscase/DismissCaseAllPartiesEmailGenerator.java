package uk.gov.hmcts.reform.civil.notification.handlers.dismisscase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class DismissCaseAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public DismissCaseAllPartiesEmailGenerator(
        DismissCaseAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
        DismissCaseRespSolOneEmailDTOGenerator respSolOneEmailGenerator,
        DismissCaseRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator,
        DismissCaseClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        DismissCaseDefendantEmailDTOGenerator defendantEmailDTOGenerator) {

        super(List.of(appSolOneEmailGenerator,
                      respSolOneEmailGenerator,
                      respSolTwoEmailGenerator,
                      claimantEmailDTOGenerator,
                      defendantEmailDTOGenerator));
    }
}
