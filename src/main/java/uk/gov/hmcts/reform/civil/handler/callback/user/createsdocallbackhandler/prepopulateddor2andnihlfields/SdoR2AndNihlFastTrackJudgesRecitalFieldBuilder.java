package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;

@Component
public class SdoR2AndNihlFastTrackJudgesRecitalFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoFastTrackJudgesRecital(FastTrackJudgesRecital.builder()
                .input(SdoR2UiConstantFastTrack.JUDGE_RECITAL).build());
    }
}
