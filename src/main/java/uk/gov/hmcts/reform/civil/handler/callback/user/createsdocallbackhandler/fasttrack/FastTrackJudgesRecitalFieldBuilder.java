package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;

@Slf4j
@Component
public class FastTrackJudgesRecitalFieldBuilder implements SdoCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackJudgesRecital(FastTrackJudgesRecital.builder()
                .input(
                        "Upon considering the statements of case and the information provided by the parties,")
                .build());
    }
}
