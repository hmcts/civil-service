package uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;

@Component
public class AddDefLitFriendAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public AddDefLitFriendAllPartiesEmailGenerator(
        AddDefLitFriendAppSolOneEmailDTOGenerator appSolOneAddDefLitFriendEmailGenerator,
        AddDefLitFriendRespSolOneEmailDTOGenerator addDefLitFriendRespSolOneEmailGenerator,
        AddDefLitFriendRespSolTwoEmailDTOGenerator addDefLitFriendRespSolTwoEmailGenerator,
        SimpleStateFlowEngine stateFlowEngine
    ) {
        super(appSolOneAddDefLitFriendEmailGenerator,
            addDefLitFriendRespSolOneEmailGenerator,
            addDefLitFriendRespSolTwoEmailGenerator,
            null,
            null,
            null,
            stateFlowEngine);
    }

}
