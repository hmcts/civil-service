package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmsnottoproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;

@Component
public class ClaimantResponseConfirmsNotToProceedAllLegalRepsEmailGenerator extends AllLegalRepsEmailGenerator {

    public ClaimantResponseConfirmsNotToProceedAllLegalRepsEmailGenerator(
        ClaimantResponseConfirmsNotToProceedAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
        ClaimantResponseConfirmsNotToProceedRespSolOneEmailDTOGenerator respSolOneEmailGenerator,
        ClaimantResponseConfirmsNotToProceedRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator
    ) {
        super(appSolOneEmailGenerator, respSolOneEmailGenerator, respSolTwoEmailGenerator);
    }
}
