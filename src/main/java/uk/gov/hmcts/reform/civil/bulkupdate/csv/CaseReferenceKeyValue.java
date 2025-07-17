package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@EqualsAndHashCode
@JsonPropertyOrder(value = {"caseReference", "previousHCKey", "previousHCValue", "previousCourtListKey", "previousCourtListValue"})
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1700")
public class CaseReferenceKeyValue {

    @JsonProperty
    private String caseReference;

    @JsonProperty
    private String previousHCKey;

    @JsonProperty
    private String previousHCValue;

    @JsonProperty
    private String previousCourtListKey;

    @JsonProperty
    private String previousCourtListValue;

}
