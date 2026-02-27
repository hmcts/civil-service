package uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.ga.enums.dq.HeardFromRepresentationTypes;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AssistedOrderHeardRepresentation {

    private HeardFromRepresentationTypes representationType;
    private ClaimantDefendantRepresentation claimantDefendantRepresentation;
    private DetailText otherRepresentation;

    @JsonCreator
    AssistedOrderHeardRepresentation(@JsonProperty("representationType")
                                     HeardFromRepresentationTypes representationType,
                                     @JsonProperty("claimantDefendantRepresentation")
                                     ClaimantDefendantRepresentation claimantDefendantRepresentation,
                                     @JsonProperty("otherRepresentation") DetailText otherRepresentation) {
        this.representationType = representationType;
        this.claimantDefendantRepresentation = claimantDefendantRepresentation;
        this.otherRepresentation = otherRepresentation;
    }
}
