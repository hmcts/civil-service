package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.prepopulateorderdetailspages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.orderdetailspages.OrderDetailsPagesCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

@Component
@Slf4j
public class OrderDetailsPagesWelshLanguageUsageFieldBuilder implements OrderDetailsPagesCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Setting Welsh language usage descriptions for caseId: {}", updatedData.build().getCcdCaseReference());
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
