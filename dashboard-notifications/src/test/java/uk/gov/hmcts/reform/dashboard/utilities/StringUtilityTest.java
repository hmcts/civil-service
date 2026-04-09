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

    @Test
    void shouldActivateLinkForViewDocument() {
        String input1 = "<a>View documents</a>";
        String input2 = "<a   >  View documents </a>";
        String input3 = "<a class=\"x\">View documents</a>";

        assertThat(StringUtility.activateLink(input1))
            .isEqualTo("<a class=\"govuk-link\" href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\">View documents</a>");
        assertThat(StringUtility.activateLink(input2))
            .isEqualTo("<a    class=\"govuk-link\" href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\">  View documents </a>");
        assertThat(StringUtility.activateLink(input3))
            .isEqualTo("<a class=\"x govuk-link\" href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\">View documents</a>");
    }

    @Test
    void shouldNotModifyLinkWithExistingHref() {
        String input = "<a href=\"/already\">View document</a>";
        assertThat(StringUtility.activateLink(input)).isEqualTo(input);
    }
}
