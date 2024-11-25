package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlDisclosureOfDocumentsFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlDisclosureOfDocumentsFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlDisclosureOfDocumentsFieldBuilder sdoR2AndNihlDisclosureOfDocumentsFieldBuilder;

    @Test
    void shouldBuildSdoR2DisclosureOfDocuments() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlDisclosureOfDocumentsFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2DisclosureOfDocuments disclosureOfDocuments = caseData.getSdoR2DisclosureOfDocuments();

        assertEquals(SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE, disclosureOfDocuments.getStandardDisclosureTxt());
        assertEquals(LocalDate.now().plusDays(28), disclosureOfDocuments.getStandardDisclosureDate());
        assertEquals(SdoR2UiConstantFastTrack.INSPECTION, disclosureOfDocuments.getInspectionTxt());
        assertEquals(LocalDate.now().plusDays(42), disclosureOfDocuments.getInspectionDate());
        assertEquals(SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH, disclosureOfDocuments.getRequestsWillBeCompiledLabel());
    }
}