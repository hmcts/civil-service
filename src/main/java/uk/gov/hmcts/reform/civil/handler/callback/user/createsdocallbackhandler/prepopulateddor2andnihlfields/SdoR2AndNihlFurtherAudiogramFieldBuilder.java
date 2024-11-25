package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;

import java.time.LocalDate;

@Component
public class SdoR2AndNihlFurtherAudiogramFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

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
