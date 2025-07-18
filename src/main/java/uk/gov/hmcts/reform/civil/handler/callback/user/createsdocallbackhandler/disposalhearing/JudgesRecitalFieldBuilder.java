package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;

import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.UPON_CONSIDERING;

@Slf4j
@Component
public class JudgesRecitalFieldBuilder implements SdoCaseFieldBuilder {

    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting judges recital");
        updatedData.disposalHearingJudgesRecital(DisposalHearingJudgesRecital.builder()
                .input(UPON_CONSIDERING)
                .build());
    }
}
