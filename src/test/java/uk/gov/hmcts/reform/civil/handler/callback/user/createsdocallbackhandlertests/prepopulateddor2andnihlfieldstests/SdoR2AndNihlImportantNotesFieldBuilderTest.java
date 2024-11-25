package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlImportantNotesFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlImportantNotesFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlImportantNotesFieldBuilder sdoR2AndNihlImportantNotesFieldBuilder;

    @Test
    void shouldBuildSdoR2ImportantNotes() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlImportantNotesFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        assertEquals(SdoR2UiConstantFastTrack.IMPORTANT_NOTES, caseData.getSdoR2ImportantNotesTxt());
        assertEquals(LocalDate.now().plusDays(7), caseData.getSdoR2ImportantNotesDate());
    }
}