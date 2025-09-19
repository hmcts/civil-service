package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.fasttrack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;

@Slf4j
@Component
public class FastTrackJudgesRecitalFieldBuilder implements SdoCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Building FastTrackHousingDisrepair fields for caseId: {}", updatedData.build().getCcdCaseReference());
        updatedData.fastTrackJudgesRecital(FastTrackJudgesRecital.builder()
                .input(
                        "Upon considering the statements of case and the information provided by the parties,")
                .build());
    }
}
