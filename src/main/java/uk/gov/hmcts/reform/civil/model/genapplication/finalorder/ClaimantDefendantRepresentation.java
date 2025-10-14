package uk.gov.hmcts.reform.civil.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.dq.ClaimantRepresentationType;
import uk.gov.hmcts.reform.civil.enums.dq.DefendantRepresentationType;

@Setter
@Data
@Builder(toBuilder = true)
public class ClaimantDefendantRepresentation {

    private ClaimantRepresentationType claimantRepresentation;
    private DefendantRepresentationType defendantRepresentation;
    private DefendantRepresentationType defendantTwoRepresentation;
    private HeardClaimantNotAttend heardFromClaimantNotAttend;
    private HeardDefendantNotAttend heardFromDefendantNotAttend;
    private HeardDefendantTwoNotAttend heardFromDefendantTwoNotAttend;
    private String detailsRepresentationText;

    private String claimantPartyName;
    private String defendantPartyName;
    private String defendantTwoPartyName;

    @JsonCreator
    ClaimantDefendantRepresentation(@JsonProperty("claimantRepresentation")
                                    ClaimantRepresentationType claimantRepresentation,
                                    @JsonProperty("defendantRepresentation")
                                    DefendantRepresentationType defendantRepresentation,
                                    @JsonProperty("defendantTwoRepresentation")
                                    DefendantRepresentationType defendantTwoRepresentation,
                                    @JsonProperty("heardFromClaimantNotAttend")
                                    HeardClaimantNotAttend heardFromClaimantNotAttend,
                                    @JsonProperty("heardFromDefendantNotAttend")
                                    HeardDefendantNotAttend heardFromDefendantNotAttend,
                                    @JsonProperty("heardFromDefTwoNotAttend")
                                    HeardDefendantTwoNotAttend heardFromDefendantTwoNotAttend,
                                    @JsonProperty("detailsRepresentationText")
                                    String detailsRepresentationText,
                                    @JsonProperty("claimantPartyName")
                                    String claimantPartyName,
                                    @JsonProperty("defendantOnePartyName")
                                    String defendantPartyName,
                                    @JsonProperty("defendantTwoPartyName")
                                    String defendantTwoPartyName) {

        this.claimantRepresentation = claimantRepresentation;
        this.defendantRepresentation = defendantRepresentation;
        this.defendantTwoRepresentation = defendantTwoRepresentation;
        this.heardFromClaimantNotAttend = heardFromClaimantNotAttend;
        this.heardFromDefendantNotAttend = heardFromDefendantNotAttend;
        this.heardFromDefendantTwoNotAttend = heardFromDefendantTwoNotAttend;
        this.detailsRepresentationText = detailsRepresentationText;
        this.claimantPartyName = claimantPartyName;
        this.defendantPartyName = defendantPartyName;
        this.defendantTwoPartyName = defendantTwoPartyName;
    }
}
