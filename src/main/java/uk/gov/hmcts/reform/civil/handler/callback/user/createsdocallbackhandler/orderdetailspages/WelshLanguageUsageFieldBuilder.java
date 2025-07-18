package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.orderdetailspages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

@Slf4j
@Component
public class WelshLanguageUsageFieldBuilder implements OrderDetailsPagesCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Setting Welsh language usage descriptions");
        updatedData.sdoR2FastTrackUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder()
                .description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION)
                .build());
        updatedData.sdoR2SmallClaimsUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder()
                .description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION)
                .build());
        updatedData.sdoR2DisposalHearingUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder()
                .description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION)
                .build());
    }
}
