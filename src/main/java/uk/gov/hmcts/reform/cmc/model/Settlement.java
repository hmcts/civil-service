package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Settlement {

    private List<PartyStatement> partyStatements;

    @JsonIgnore
    public boolean isAcceptedByClaimant() {
        boolean isAcceptedByClaimant = getPartyStatementStream()
            .anyMatch(partyStatement -> partyStatement.isAccepted()
                && partyStatement.isMadeByClaimant());
        boolean isNotRejectedByDefendant = getPartyStatementStream()
            .noneMatch(partyStatement -> partyStatement.isRejected()
                && partyStatement.isMadeByDefendant());
        return isAcceptedByClaimant && isNotRejectedByDefendant;
    }

    @JsonIgnore
    public boolean isAcceptedByDefendant() {
        return getPartyStatementStream()
            .anyMatch(partyStatement -> partyStatement.isAccepted()
                && partyStatement.isMadeByDefendant());
    }

    @JsonIgnore
    public boolean isSettled() {
        Stream<PartyStatement> partyStatementsStream = getPartyStatementStream();
        return partyStatementsStream.anyMatch(PartyStatement::isCounterSigned);
    }

    public boolean isThroughAdmissions() {
        Stream<PartyStatement> partyStatementsStream = getPartyStatementStream();
        return partyStatementsStream.filter(PartyStatement::hasOffer)
            .reduce((first, second) -> second)
            .stream()
            .noneMatch(offer -> offer.hasPaymentIntention());
    }

    private Stream<PartyStatement> getPartyStatementStream() {
        Stream<PartyStatement> partyStatementsStream = Optional.ofNullable(partyStatements)
            .map(Collection::stream).orElseGet(Stream::empty);
        return partyStatementsStream;
    }

}
