package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlFastTrackJudgesRecitalFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlFastTrackJudgesRecitalFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlFastTrackJudgesRecitalFieldBuilder sdoR2AndNihlFastTrackJudgesRecitalFieldBuilder;

    @Test
    void shouldBuildSdoFastTrackJudgesRecital() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlFastTrackJudgesRecitalFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackJudgesRecital judgesRecital = caseData.getSdoFastTrackJudgesRecital();

        assertEquals(SdoR2UiConstantFastTrack.JUDGE_RECITAL, judgesRecital.getInput());
    }
}