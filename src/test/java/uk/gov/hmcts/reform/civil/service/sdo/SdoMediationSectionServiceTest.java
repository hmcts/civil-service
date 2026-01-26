package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;

import static org.assertj.core.api.Assertions.assertThat;

class SdoMediationSectionServiceTest {

    private final SdoMediationSectionService service = new SdoMediationSectionService();

    @Test
    void shouldShowSectionWhenMediationProvidedAndCarmEnabled() {
        SmallClaimsMediation mediation = new SmallClaimsMediation();
        mediation.setInput("ADR details");

        SdoMediationSectionService.MediationSection result = service.resolve(
            mediation,
            true,
            SmallClaimsMediation::getInput
        );

        assertThat(result.show()).isTrue();
        assertThat(result.text()).isEqualTo("ADR details");
    }

    @Test
    void shouldHideSectionWhenCarmDisabled() {
        SdoR2SmallClaimsMediation mediation = new SdoR2SmallClaimsMediation();
        mediation.setInput("Text");

        SdoMediationSectionService.MediationSection result = service.resolve(
            mediation,
            false,
            SdoR2SmallClaimsMediation::getInput
        );

        assertThat(result.show()).isFalse();
        assertThat(result.text()).isEqualTo("Text");
    }

    @Test
    void shouldReturnNullTextWhenMediationMissing() {
        SdoMediationSectionService.MediationSection result = service.resolve(
            null,
            true,
            SmallClaimsMediation::getInput
        );

        assertThat(result.show()).isFalse();
        assertThat(result.text()).isNull();
    }
}
