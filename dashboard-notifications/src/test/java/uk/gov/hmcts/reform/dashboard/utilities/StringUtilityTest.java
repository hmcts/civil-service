package uk.gov.hmcts.reform.dashboard.utilities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilityTest {

    @Test
    void shouldRemoveAnchorFromText() {
        String input = "<a href=\"somewhere\">Link name</A >";

        assertThat(StringUtility.removeAnchor(input)).isEqualTo("Link name");
    }

    @Test
    void shouldRemoveMultipleAnchorFromText() {
        String input = "<a href=\"somewhere\">Link name</A > is a test for <a href=\"somewhere\">Multiple</A >";

        assertThat(StringUtility.removeAnchor(input)).isEqualTo("Link name is a test for Multiple");
    }
}
