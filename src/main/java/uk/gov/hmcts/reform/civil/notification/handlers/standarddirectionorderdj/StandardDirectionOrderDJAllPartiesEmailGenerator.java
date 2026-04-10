package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class StandardDirectionOrderDJAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public StandardDirectionOrderDJAllPartiesEmailGenerator(
        StandardDirectionOrderDJAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        StandardDirectionOrderDJRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        StandardDirectionOrderDJRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(List.of(
            appSolOneEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator
        ));
    }
}
