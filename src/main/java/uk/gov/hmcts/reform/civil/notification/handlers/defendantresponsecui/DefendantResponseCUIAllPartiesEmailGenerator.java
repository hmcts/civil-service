package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecui;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class DefendantResponseCUIAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public DefendantResponseCUIAllPartiesEmailGenerator(DefendantResponseCUIAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
                                                        DefendantResponseCUIClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                                        DefendantResponseCUIDefendantEmailDTOGenerator defendantEmailDTOGenerator,
                                                        DefendantChangeOfAddressAppSolOneEmailDTOGenerator defendantChangeOfAddressAppSolOneEmailDTOGenerator,
                                                        DefendantChangeOfAddressClaimantEmailDTOGenerator defendantChangeOfAddressClaimantEmailDTOGenerator) {
        super(List.of(appSolOneEmailDTOGenerator, claimantEmailDTOGenerator, defendantEmailDTOGenerator,
                      defendantChangeOfAddressClaimantEmailDTOGenerator, defendantChangeOfAddressAppSolOneEmailDTOGenerator));
    }
}
