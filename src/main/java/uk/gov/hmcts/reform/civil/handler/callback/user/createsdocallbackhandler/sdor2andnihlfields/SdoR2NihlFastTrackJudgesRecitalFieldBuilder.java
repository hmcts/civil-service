package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.sdor2andnihlfields;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;

@Slf4j
@Component
public class SdoR2NihlFastTrackJudgesRecitalFieldBuilder implements SdoR2AndNihlFieldsCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoFastTrackJudgesRecital(FastTrackJudgesRecital.builder()
                .input(SdoR2UiConstantFastTrack.JUDGE_RECITAL).build());
    }
}
