package uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

@Component
public class AddDefLitFriendAllLegalRepsEmailGenerator extends AllLegalRepsEmailGenerator {

    public AddDefLitFriendAllLegalRepsEmailGenerator(
        AddDefLitFriendAppSolOneEmailDTOGenerator appSolOneAddDefLitFriendEmailGenerator,
        AddDefLitFriendRespSolOneEmailDTOGenerator addDefLitFriendRespSolOneEmailGenerator,
        AddDefLitFriendRespSolTwoEmailDTOGenerator addDefLitFriendRespSolTwoEmailGenerator,
        SimpleStateFlowEngine stateFlowEngine
    ) {
        super(appSolOneAddDefLitFriendEmailGenerator,
            addDefLitFriendRespSolOneEmailGenerator,
            addDefLitFriendRespSolTwoEmailGenerator,
            stateFlowEngine);
    }

}
