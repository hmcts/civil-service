package uk.gov.hmcts.reform.civil;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.befta.dse.ccd.CcdEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class HighLevelDataSetupAppTest {

    @Test
    void shouldSkipCcdSetupForSharedAatWhenFlagEnabled() {
        assertThat(HighLevelDataSetupApp.shouldSkipCcdSetup(CcdEnvironment.AAT, "true")).isTrue();
    }

    @Test
    void shouldNotSkipCcdSetupForSharedAatWhenFlagDisabled() {
        assertThat(HighLevelDataSetupApp.shouldSkipCcdSetup(CcdEnvironment.AAT, "false")).isFalse();
    }

    @Test
    void shouldNotSkipCcdSetupForPreviewEvenWhenFlagEnabled() {
        assertThat(HighLevelDataSetupApp.shouldSkipCcdSetup(CcdEnvironment.PREVIEW, "true")).isFalse();
    }
}
