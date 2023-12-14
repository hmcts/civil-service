package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PartyStatement {

    private StatementType type;
    private MadeBy madeBy;
    private Offer offer;

    @JsonIgnore
    public boolean isAccepted() {
        return StatementType.ACCEPTATION == type;
    }

    @JsonIgnore
    public boolean isRejected() {
        return StatementType.REJECTION == type;
    }

    @JsonIgnore
    public boolean isCounterSigned() {
        return StatementType.COUNTERSIGNATURE == type;
    }

    @JsonIgnore
    public boolean hasOffer() {
        return StatementType.OFFER == type;
    }

    @JsonIgnore
    public boolean isMadeByDefendant() {
        return MadeBy.DEFENDANT == madeBy;
    }

    @JsonIgnore
    public boolean isMadeByClaimant() {
        return MadeBy.CLAIMANT == madeBy;
    }

    @JsonIgnore
    public boolean hasPaymentIntention() {
        return offer != null && offer.hasPaymentIntention();
    }

}
