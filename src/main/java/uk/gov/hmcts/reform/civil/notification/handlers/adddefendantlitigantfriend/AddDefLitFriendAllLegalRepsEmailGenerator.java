package uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;

@Component
public class AddDefLitFriendAllLegalRepsEmailGenerator extends AllLegalRepsEmailGenerator {

    public AddDefLitFriendAllLegalRepsEmailGenerator(
        AddDefLitFriendAppSolOneEmailDTOGenerator appSolOneAddDefLitFriendEmailGenerator,
        AddDefLitFriendRespSolOneEmailDTOGenerator addDefLitFriendRespSolOneEmailGenerator,
        AddDefLitFriendRespSolTwoEmailDTOGenerator addDefLitFriendRespSolTwoEmailGenerator
    ) {
        super(appSolOneAddDefLitFriendEmailGenerator,
            addDefLitFriendRespSolOneEmailGenerator,
            addDefLitFriendRespSolTwoEmailGenerator);
    }

}
