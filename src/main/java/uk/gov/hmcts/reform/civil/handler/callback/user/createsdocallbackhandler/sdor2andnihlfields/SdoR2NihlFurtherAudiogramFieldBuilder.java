package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.sdor2andnihlfields;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;

import java.time.LocalDate;

@Slf4j
@Component
public class SdoR2NihlFurtherAudiogramFieldBuilder implements SdoR2AndNihlFieldsCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2FurtherAudiogram(SdoR2FurtherAudiogram.builder()
                .sdoClaimantShallUndergoTxt(SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO)
                .sdoServiceReportTxt(SdoR2UiConstantFastTrack.SERVICE_REPORT)
                .sdoClaimantShallUndergoDate(LocalDate.now().plusDays(42))
                .sdoServiceReportDate(LocalDate.now().plusDays(98))
                .build());
    }
}
