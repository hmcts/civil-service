package uk.gov.hmcts.reform.civil.notification.handlers.bundlecreation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class BundleCreationAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public BundleCreationAllPartiesEmailGenerator(BundleCreationAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
                                                  BundleCreationRespSolOneEmailDTOGenerator respSolOneEmailGenerator,
                                                  BundleCreationRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator,
                                                  BundleCreationClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                                  BundleCreationDefendantEmailDTOGenerator defendantEmailDTOGenerator
    ) {
        super(List.of(appSolOneEmailGenerator, respSolOneEmailGenerator, respSolTwoEmailGenerator,
                      claimantEmailDTOGenerator, defendantEmailDTOGenerator));
    }
}
