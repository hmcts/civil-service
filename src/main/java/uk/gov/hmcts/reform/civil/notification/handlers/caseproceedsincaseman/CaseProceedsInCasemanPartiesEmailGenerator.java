package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;

import java.util.List;
import java.util.Set;

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
