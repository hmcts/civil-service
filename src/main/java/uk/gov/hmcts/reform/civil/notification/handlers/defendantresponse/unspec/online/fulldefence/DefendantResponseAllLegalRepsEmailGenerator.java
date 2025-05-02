package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.online.fulldefence;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;

@Component
public class DefendantResponseAllLegalRepsEmailGenerator extends AllLegalRepsEmailGenerator {

    public DefendantResponseAllLegalRepsEmailGenerator(
        DefendantResponseAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        DefendantResponseRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        DefendantResponseRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(appSolOneEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator);
    }
}
