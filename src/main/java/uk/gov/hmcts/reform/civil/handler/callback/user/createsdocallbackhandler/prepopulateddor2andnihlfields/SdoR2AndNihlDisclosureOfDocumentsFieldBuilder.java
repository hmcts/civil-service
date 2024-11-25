package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;

import java.time.LocalDate;

@Component
public class SdoR2AndNihlDisclosureOfDocumentsFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                .standardDisclosureTxt(SdoR2UiConstantFastTrack.STANDARD_DISCLOSURE)
                .standardDisclosureDate(LocalDate.now().plusDays(28))
                .inspectionTxt(SdoR2UiConstantFastTrack.INSPECTION)
                .inspectionDate(LocalDate.now().plusDays(42))
                .requestsWillBeCompiledLabel(SdoR2UiConstantFastTrack.REQUEST_COMPILED_WITH)
                .build());
    }
}
