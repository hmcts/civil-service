package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlScheduleOfLossFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlScheduleOfLossFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlScheduleOfLossFieldBuilder sdoR2AndNihlScheduleOfLossFieldBuilder;

    @Test
    void shouldBuildSdoR2ScheduleOfLoss() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlScheduleOfLossFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2ScheduleOfLoss scheduleOfLoss = caseData.getSdoR2ScheduleOfLoss();

        assertEquals(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT, scheduleOfLoss.getSdoR2ScheduleOfLossClaimantText());
        assertEquals(NO, scheduleOfLoss.getIsClaimForPecuniaryLoss());
        assertEquals(LocalDate.now().plusDays(364), scheduleOfLoss.getSdoR2ScheduleOfLossClaimantDate());
        assertEquals(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT, scheduleOfLoss.getSdoR2ScheduleOfLossDefendantText());
        assertEquals(LocalDate.now().plusDays(378), scheduleOfLoss.getSdoR2ScheduleOfLossDefendantDate());
        assertEquals(SdoR2UiConstantFastTrack.PECUNIARY_LOSS, scheduleOfLoss.getSdoR2ScheduleOfLossPecuniaryLossTxt());
    }
}