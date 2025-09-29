package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.online.fulldefence;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class DefendantResponseAllLegalRepsEmailGenerator extends AllPartiesEmailGenerator {

    public DefendantResponseAllLegalRepsEmailGenerator(
        DefendantResponseAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        DefendantResponseRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        DefendantResponseRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(List.of(appSolOneEmailDTOGenerator,
                      respSolOneEmailDTOGenerator,
                      respSolTwoEmailDTOGenerator));
    }
}
