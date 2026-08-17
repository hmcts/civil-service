package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"caseReference"})
@NoArgsConstructor
public class DismissedDeadlineCaseReference extends CaseReference implements ExcelMappable {

    @JsonProperty
    private String dismissedDeadline;

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) throws Exception {
        if (rowValues.containsKey("caseReference")) {
            setCaseReference(asString(rowValues.get("caseReference")));
        }
        if (rowValues.containsKey("dismissedDeadline")) {
            setDismissedDeadline(asString(rowValues.get("dismissedDeadline")));
        }
    }
}
