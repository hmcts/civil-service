package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimantConfirmProceedAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantConfirmProceedAllPartiesEmailGenerator(ClaimantConfirmProceedClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                                          ClaimantConfirmProceedDefendantEmailDTOGenerator defendantEmailDTOGenerator,
                                                          ClaimantConfirmProceedRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator) {
        super(List.of(claimantEmailDTOGenerator,
                      defendantEmailDTOGenerator,
                      respSolOneEmailDTOGenerator));
    }
}
