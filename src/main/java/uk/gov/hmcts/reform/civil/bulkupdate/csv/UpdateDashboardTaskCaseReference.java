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
public class UpdateDashboardTaskCaseReference extends CaseReference implements ExcelMappable {

    @JsonProperty
    private String taskListId;
    @JsonProperty
    private String currentStatus;
    @JsonProperty
    private String nextStatus;
    @JsonProperty
    private String updatedBy;
    @JsonProperty
    private String taskNameEn;
    @JsonProperty
    private String taskNameCy;

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) throws Exception {
        if (rowValues.containsKey("caseReference")) {
            setCaseReference(asString(rowValues.get("caseReference")));
        }
        if (rowValues.containsKey("taskListId")) {
            setTaskListId(asString(rowValues.get("taskListId")));
        }
        if (rowValues.containsKey("taskNameEn")) {
            setTaskNameEn(asString(rowValues.get("taskNameEn")));
        }
        if (rowValues.containsKey("taskNameCy")) {
            setTaskNameCy(asString(rowValues.get("taskNameCy")));
        }
        if (rowValues.containsKey("currentStatus")) {
            setCurrentStatus(asString(rowValues.get("currentStatus")));
        }
        if (rowValues.containsKey("nextStatus")) {
            setNextStatus(asString(rowValues.get("nextStatus")));
        }
        if (rowValues.containsKey("updatedBy")) {
            setUpdatedBy(asString(rowValues.get("updatedBy")));
        }
    }
}
