package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.model.Party;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder(value = {"caseReference"})
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1700")
public class PartyDetailsCaseReference extends CaseReference {

    @JsonProperty
    private Party party;
    private boolean isApplicant1;
    private boolean isApplicant2;
    private boolean isRespondent1;
    private boolean isRespondent2;
}
