package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateorderdetailspagestests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateorderdetailspages.OrderDetailsPagesWelshLanguageUsageFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderDetailsPagesWelshLanguageUsageFieldBuilderTest {

    @InjectMocks
    private OrderDetailsPagesWelshLanguageUsageFieldBuilder orderDetailsPagesWelshLanguageUsageFieldBuilder;

    @Test
    void shouldBuildWelshLanguageUsageFields() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        orderDetailsPagesWelshLanguageUsageFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2WelshLanguageUsage fastTrackWelshLanguageUsage = caseData.getSdoR2FastTrackUseOfWelshLanguage();
        SdoR2WelshLanguageUsage smallClaimsWelshLanguageUsage = caseData.getSdoR2SmallClaimsUseOfWelshLanguage();
        SdoR2WelshLanguageUsage disposalHearingWelshLanguageUsage = caseData.getSdoR2DisposalHearingUseOfWelshLanguage();

        assertEquals(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION, fastTrackWelshLanguageUsage.getDescription());
        assertEquals(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION, smallClaimsWelshLanguageUsage.getDescription());
        assertEquals(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION, disposalHearingWelshLanguageUsage.getDescription());
    }
}