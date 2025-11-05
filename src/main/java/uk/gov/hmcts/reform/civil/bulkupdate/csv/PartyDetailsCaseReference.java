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
@JsonPropertyOrder({"caseReference"})
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1700")
public class PartyDetailsCaseReference extends AbstractCaseReferenceWithRole<Party> {

    @JsonProperty
    private Party party;

    @Override
    protected Class<Party> getObjectType() {
        return Party.class;
    }

    @Override
    public Party getDataObject() {
        return dataObject;
    }

    @Override
    public void setDataObject(Party party) {
        this.dataObject = party;
    }
}
