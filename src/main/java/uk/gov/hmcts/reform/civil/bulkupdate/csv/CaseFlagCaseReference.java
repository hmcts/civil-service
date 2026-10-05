package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"caseReference", "comment"})
@NoArgsConstructor
public class CaseFlagCaseReference extends CaseReference implements ExcelMappable {

    @JsonProperty
    private String comment;

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) {
        setCaseReference(asString(rowValues.get("caseReference")));
        setComment(asString(rowValues.get("comment")));
    }

    private String asString(Object value) {
        return value == null ? null : value.toString().trim();
    }
}
