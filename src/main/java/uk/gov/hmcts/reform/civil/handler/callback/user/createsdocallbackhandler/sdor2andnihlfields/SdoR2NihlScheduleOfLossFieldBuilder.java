package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.sdor2andnihlfields;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Slf4j
@Component
public class SdoR2NihlScheduleOfLossFieldBuilder implements SdoR2AndNihlFieldsCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder()
                .sdoR2ScheduleOfLossClaimantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_CLAIMANT)
                .isClaimForPecuniaryLoss(NO)
                .sdoR2ScheduleOfLossClaimantDate(LocalDate.now().plusDays(364))
                .sdoR2ScheduleOfLossDefendantText(SdoR2UiConstantFastTrack.SCHEDULE_OF_LOSS_DEFENDANT)
                .sdoR2ScheduleOfLossDefendantDate(LocalDate.now().plusDays(378))
                .sdoR2ScheduleOfLossPecuniaryLossTxt(SdoR2UiConstantFastTrack.PECUNIARY_LOSS)
                .build());
    }
}
