package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlAddendumReportFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlAddendumReportFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlAddendumReportFieldBuilder sdoR2AndNihlAddendumReportFieldBuilder;

    @Test
    void shouldBuildSdoR2AddendumReport() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlAddendumReportFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2AddendumReport addendumReport = caseData.getSdoR2AddendumReport();

        assertEquals(SdoR2UiConstantFastTrack.ADDENDUM_REPORT, addendumReport.getSdoAddendumReportTxt());
        assertEquals(LocalDate.now().plusDays(56), addendumReport.getSdoAddendumReportDate());
    }
}