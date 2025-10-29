package uk.gov.hmcts.reform.dashboard.utilities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilityTest {

    @Test
    void shouldRemoveAnchorsFromHtml() {
        String input = "<a href=\"#\">Link</a> with <a class=\"text\">content</a>";

        String result = StringUtility.removeAnchor(input);

        assertThat(result).isEqualTo("Link with content");
    }

    @Test
    void shouldReturnOriginalWhenTextIsBlank() {
        assertThat(StringUtility.removeAnchor("   ")).isEqualTo("   ");
        assertThat(StringUtility.removeAnchor(null)).isNull();
    }
}
