package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;

@Component
public class SdoR2AndNihlImportantNotesFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2ImportantNotesTxt(SdoR2UiConstantFastTrack.IMPORTANT_NOTES);
        updatedData.sdoR2ImportantNotesDate(LocalDate.now().plusDays(7));
    }
}
