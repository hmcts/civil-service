package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder(value = {"caseReference", "documentId"})
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1700")
public class DocumentPurgeReference extends CaseReference {

    @JsonProperty
    private String documentId;
}
