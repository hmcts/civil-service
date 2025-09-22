package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableMarkupService {

    private static final String TABLE_BEGIN = "<table>";
    private static final String TABLE_END = "</table>";
    private static final String TABLE_ROW_BEGIN = "<tr>";
    private static final String TABLE_ROW_END = "</tr>";
    private static final String TABLE_ROW_DATA_BEGIN = "<td width=\"50%\" class='govuk-header__logotype-crown'>";
    private static final String TABLE_ROW_DATA_END = "</td>";
    private static final String MESSAGE_TABLE_HEADER =
        "<div class='govuk-grid-column-two-thirds govuk-grid-row'><span class=\"heading-h4\">Message</span>";

    private static final String TABLE_ROW_LABEL = "<span class='heading-h4'>";
    private static final String TABLE_ROW_VALUE = "<span class='form-label'>";
    private static final String SPAN_END = "</span>";
    private static final String DIV_END = "</div>";
    private static final String BREAK = "</br>";

    private void addRowToTable(List<String> lines,
                                      String label,
                                      String value) {
        if (isNotBlank(value)) {
            lines.add(TABLE_ROW_BEGIN);
            lines.add(TABLE_ROW_DATA_BEGIN + TABLE_ROW_LABEL + label + SPAN_END);
            lines.add(TABLE_ROW_DATA_END);
            lines.add(TABLE_ROW_DATA_BEGIN + TABLE_ROW_VALUE + value + SPAN_END);
            lines.add(TABLE_ROW_DATA_END);
            lines.add(TABLE_ROW_END);
        }
    }

    public String buildTableMarkUp(Map<String, String> rows) {
        List<String> tableRows = new LinkedList<>();
        tableRows.add(MESSAGE_TABLE_HEADER);
        tableRows.add(TABLE_BEGIN);
        ofNullable(rows).orElse(new HashMap<>()).entrySet().forEach(entry -> addRowToTable(tableRows, entry.getKey(), entry.getValue()));
        tableRows.add(TABLE_END);
        tableRows.add(DIV_END);
        return  String.join("\n\n", tableRows);
    }
}
