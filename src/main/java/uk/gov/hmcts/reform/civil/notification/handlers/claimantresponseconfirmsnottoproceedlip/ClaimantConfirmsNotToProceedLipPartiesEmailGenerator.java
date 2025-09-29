package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmsnottoproceedlip;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimantConfirmsNotToProceedLipPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantConfirmsNotToProceedLipPartiesEmailGenerator(ClaimantConfirmsNotToProceedLipDefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(List.of(defendantEmailDTOGenerator));
    }
}
