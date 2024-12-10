package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableMarkupServiceTest {

    private TableMarkupService tableMarkupService;

    @BeforeEach
    void setUp() {
        tableMarkupService = new TableMarkupService();
    }

    @Test
    void shouldBuildTableMarkupWithSingleRow() {
        Map<String, String> rows = new HashMap<>();
        rows.put("Label1", "Value1");

        String expectedHtml = """
            <div class='govuk-grid-column-two-thirds govuk-grid-row'><span class="heading-h4">Message</span>
            <table>
            <tr>
            <td width="50%" class='govuk-header__logotype-crown'><span class='heading-h4'>Label1</span></td>
            <td width="50%" class='govuk-header__logotype-crown'><span class='form-label'>Value1</span></td>
            </tr>
            </table>
            </div>
            """;

        String actualHtml = tableMarkupService.buildTableMarkUp(rows);

        assertEquals(normaliseHtml(expectedHtml), normaliseHtml(actualHtml));
    }

    @Test
    void shouldBuildTableMarkupWithMultipleRows() {
        Map<String, String> rows = new HashMap<>();
        rows.put("Label1", "Value1");
        rows.put("Label2", "Value2");

        String expectedHtml = """
            <div class='govuk-grid-column-two-thirds govuk-grid-row'><span class="heading-h4">Message</span>
            <table>
            <tr>
            <td width="50%" class='govuk-header__logotype-crown'><span class='heading-h4'>Label1</span></td>
            <td width="50%" class='govuk-header__logotype-crown'><span class='form-label'>Value1</span></td>
            </tr>
            <tr>
            <td width="50%" class='govuk-header__logotype-crown'><span class='heading-h4'>Label2</span></td>
            <td width="50%" class='govuk-header__logotype-crown'><span class='form-label'>Value2</span></td>
            </tr>
            </table>
            </div>
            """;

        String actualHtml = tableMarkupService.buildTableMarkUp(rows);

        assertEquals(normaliseHtml(expectedHtml), normaliseHtml(actualHtml));
    }

    @Test
    void shouldBuildTableMarkupWithEmptyRows() {
        Map<String, String> rows = new HashMap<>();

        String expectedHtml = """
            <div class='govuk-grid-column-two-thirds govuk-grid-row'><span class="heading-h4">Message</span>
            <table>
            </table>
            </div>
            """;

        String actualHtml = tableMarkupService.buildTableMarkUp(rows);

        assertEquals(normaliseHtml(expectedHtml), normaliseHtml(actualHtml));
    }

    @Test
    void shouldBuildTableMarkupWithNullRows() {
        String expectedHtml = """
            <div class='govuk-grid-column-two-thirds govuk-grid-row'><span class="heading-h4">Message</span>
            <table>
            </table>
            </div>
            """;

        String actualHtml = tableMarkupService.buildTableMarkUp(null);

        assertEquals(normaliseHtml(expectedHtml), normaliseHtml(actualHtml));
    }

    private String normaliseHtml(String input) {
        return input.replaceAll("(?m)^[ \t]*\r?\n", "")
            .replaceAll(">\n<", "><")
            .trim();
    }
}
