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
@JsonPropertyOrder({"caseReference"})
@SuppressWarnings("java:S1700")
@NoArgsConstructor
public class AssignCaseReference extends CaseReference implements ExcelMappable {

    @JsonProperty
    private String userId;
    @JsonProperty
    private String organisationId;
    @JsonProperty
    private String caseRole;

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) throws Exception {
        if (rowValues.containsKey("caseReference")) {
            Object value = rowValues.get("caseReference");
            setCaseReference(value != null ? value.toString() : null);
        }
        if (rowValues.containsKey("userId")) {
            Object value = rowValues.get("userId");
            setUserId(value != null ? value.toString() : null);
        }
        if (rowValues.containsKey("organisationId")) {
            Object value = rowValues.get("organisationId");
            setOrganisationId(value != null ? value.toString() : null);
        }
        if (rowValues.containsKey("caseRole")) {
            Object value = rowValues.get("caseRole");
            setCaseRole(value != null ? value.toString() : null);
        }
    }
}
