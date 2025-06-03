package uk.gov.hmcts.reform.civil.notification.handlers.adddefendantlitigantfriend;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class AddDefLitFriendAllLegalRepsEmailGenerator extends AllPartiesEmailGenerator {

    public AddDefLitFriendAllLegalRepsEmailGenerator(
        AddDefLitFriendAppSolOneEmailDTOGenerator appSolOneAddDefLitFriendEmailGenerator,
        AddDefLitFriendRespSolOneEmailDTOGenerator addDefLitFriendRespSolOneEmailGenerator,
        AddDefLitFriendRespSolTwoEmailDTOGenerator addDefLitFriendRespSolTwoEmailGenerator
    ) {
        super(List.of(appSolOneAddDefLitFriendEmailGenerator,
            addDefLitFriendRespSolOneEmailGenerator,
            addDefLitFriendRespSolTwoEmailGenerator));
    }

}
