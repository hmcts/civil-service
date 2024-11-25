package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlWelshLanguageUsageFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlWelshLanguageUsageFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlWelshLanguageUsageFieldBuilder sdoR2AndNihlWelshLanguageUsageFieldBuilder;

    @Test
    void shouldBuildSdoR2WelshLanguageUsage() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlWelshLanguageUsageFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2WelshLanguageUsage welshLanguageUsage = caseData.getSdoR2NihlUseOfWelshLanguage();

        assertEquals(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION, welshLanguageUsage.getDescription());
    }
}