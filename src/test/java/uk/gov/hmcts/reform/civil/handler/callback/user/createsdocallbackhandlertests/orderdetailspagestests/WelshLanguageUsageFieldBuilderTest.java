package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.orderdetailspagestests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.orderdetailspages.WelshLanguageUsageFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WelshLanguageUsageFieldBuilderTest {

    @InjectMocks
    private WelshLanguageUsageFieldBuilder fieldBuilder;

    private CaseData.CaseDataBuilder<?, ?> caseDataBuilder;

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldSetWelshLanguageUsageDescriptions() {
        fieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();

        SdoR2WelshLanguageUsage expectedUsage = SdoR2WelshLanguageUsage.builder()
                .description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION)
                .build();

        assertThat(caseData.getSdoR2FastTrackUseOfWelshLanguage()).isEqualTo(expectedUsage);
        assertThat(caseData.getSdoR2SmallClaimsUseOfWelshLanguage()).isEqualTo(expectedUsage);
        assertThat(caseData.getSdoR2DisposalHearingUseOfWelshLanguage()).isEqualTo(expectedUsage);
    }
}