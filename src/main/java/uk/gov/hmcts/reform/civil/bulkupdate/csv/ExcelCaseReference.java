package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"caseReference"})
@SuppressWarnings("java:S1700")
@NoArgsConstructor
public class ExcelCaseReference extends CaseReference implements ExcelMappable {

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) throws Exception {
        if (rowValues.containsKey("caseReference")) {
            Object value = rowValues.get("caseReference");
            setCaseReference(value != null ? value.toString() : null);
        }
    }
}
