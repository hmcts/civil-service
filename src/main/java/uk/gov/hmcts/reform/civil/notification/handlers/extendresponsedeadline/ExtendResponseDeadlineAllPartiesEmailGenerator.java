package uk.gov.hmcts.reform.civil.notification.handlers.extendresponsedeadline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ExtendResponseDeadlineAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ExtendResponseDeadlineAllPartiesEmailGenerator(ExtendResponseDeadlineAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
                                                          ExtendResponseDeadlineClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                                          ExtendResponseDeadlineDefendantEmailDTOGenerator defendantEmailDTOGenerator) {

        super(List.of(appSolOneEmailGenerator,
                      claimantEmailDTOGenerator,
                      defendantEmailDTOGenerator));
    }
}
