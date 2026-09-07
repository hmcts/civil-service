package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder(value = {"caseReference", "documentId"})
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1700")
public class DocumentPurgeReference extends CaseReference implements ExcelMappable {

    @JsonProperty
    private String documentId;

    @JsonProperty
    private String incidentId;

    @Override
    public void fromExcelRow(Map<String, Object> rowValues) throws Exception {
        if (rowValues.containsKey("caseReference")) {
            setCaseReference(asString(rowValues.get("caseReference")));
        }
        if (rowValues.containsKey("documentId")) {
            setDocumentId(asString(rowValues.get("documentId")));
        }
        if (rowValues.containsKey("incidentId")) {
            setIncidentId(asString(rowValues.get("incidentId")));
        }
    }

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }
}
