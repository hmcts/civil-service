package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlFurtherAudiogramFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlFurtherAudiogramFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlFurtherAudiogramFieldBuilder sdoR2AndNihlFurtherAudiogramFieldBuilder;

    @Test
    void shouldBuildSdoR2FurtherAudiogram() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlFurtherAudiogramFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2FurtherAudiogram furtherAudiogram = caseData.getSdoR2FurtherAudiogram();

        assertEquals(SdoR2UiConstantFastTrack.CLAIMANT_SHALL_UNDERGO, furtherAudiogram.getSdoClaimantShallUndergoTxt());
        assertEquals(SdoR2UiConstantFastTrack.SERVICE_REPORT, furtherAudiogram.getSdoServiceReportTxt());
        assertEquals(LocalDate.now().plusDays(42), furtherAudiogram.getSdoClaimantShallUndergoDate());
        assertEquals(LocalDate.now().plusDays(98), furtherAudiogram.getSdoServiceReportDate());
    }
}