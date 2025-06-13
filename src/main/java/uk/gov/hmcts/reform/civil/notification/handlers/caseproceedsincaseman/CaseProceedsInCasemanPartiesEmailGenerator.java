package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Slf4j
@Component
public class CaseProceedsInCasemanPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public CaseProceedsInCasemanPartiesEmailGenerator(CaseProceedsInCasemanAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
                                                      CaseProceedsInCasemanClaimantEmailDTOGenerator claimantEmailDTOGenerator,
                                                      CaseProceedsInCasemanRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
                                                      CaseProceedsInCasemanRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator) {
        super(List.of(appSolOneEmailDTOGenerator, claimantEmailDTOGenerator, respSolOneEmailDTOGenerator, respSolTwoEmailDTOGenerator));
    }

}
