package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.sdor2andnihlfields;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;

@Slf4j
@Component
public class SdoR2NihlImportantNotesFieldBuilder implements SdoR2AndNihlFieldsCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2ImportantNotesTxt(SdoR2UiConstantFastTrack.IMPORTANT_NOTES);
        updatedData.sdoR2ImportantNotesDate(LocalDate.now().plusDays(7));
    }
}
