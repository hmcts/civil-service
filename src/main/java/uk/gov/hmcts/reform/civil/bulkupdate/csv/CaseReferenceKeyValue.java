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
@JsonPropertyOrder(value = {"caseReference", "previousFRCKey", "previousFRCValue", "previousCourtListKey", "previousCourtListValue"})
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1700")
public class CaseReferenceKeyValue {

    @JsonProperty
    private String caseReference;

    @JsonProperty
    private String previousFRCKey;

    @JsonProperty
    private String previousFRCValue;

    @JsonProperty
    private String previousCourtListKey;

    @JsonProperty
    private String previousCourtListValue;

}
