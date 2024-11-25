package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

@Component
public class SdoR2AndNihlWelshLanguageUsageFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2NihlUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder()
                .description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION)
                .build());
    }
}
