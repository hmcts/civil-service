package uk.gov.hmcts.reform.civil.notification.handlers.defresponse.unspec.online.fulldefence;

import org.springframework.stereotype.Component;

@Component
public class DefRespAllLegalRepsEmailGenerator extends uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator {

    public DefRespAllLegalRepsEmailGenerator(
        DefRespAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        DefRespRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        DefRespRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(appSolOneEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator);
    }
}
