package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class CaseTakenOfflineAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public CaseTakenOfflineAllPartiesEmailGenerator(
            CaseTakenOfflineAppSolOneEmailDTOGenerator appSolOneGenerator,
            CaseTakenOfflineRespSolOneEmailDTOGenerator respSolOneGenerator,
            CaseTakenOfflineRespSolTwoEmailDTOGenerator respSolTwoGenerator
    ) {
        super(List.of(appSolOneGenerator, respSolOneGenerator, respSolTwoGenerator));
    }
}
