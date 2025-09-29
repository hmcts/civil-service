package uk.gov.hmcts.reform.civil.notification.handlers.amendrestitchbundle;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class AmendRestitchBundleAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public AmendRestitchBundleAllPartiesEmailGenerator(
        AmendRestitchBundleAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
        AmendRestitchBundleRespSolOneEmailDTOGenerator respSolOneEmailGenerator,
        AmendRestitchBundleRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator,
        AmendRestitchBundleClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        AmendRestitchBundleDefendantEmailDTOGenerator defendantEmailDTOGenerator
    ) {
        super(List.of(appSolOneEmailGenerator,
            respSolOneEmailGenerator,
            respSolTwoEmailGenerator,
            claimantEmailDTOGenerator,
            defendantEmailDTOGenerator));
    }
}
