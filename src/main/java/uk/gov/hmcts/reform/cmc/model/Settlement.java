package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

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
    public boolean isRejectedByClaimant() {
        return getPartyStatementStream()
            .anyMatch(PartyStatement::isRejected);
    }

    @JsonIgnore
    public boolean isAcceptedByDefendant() {
        return getPartyStatementStream()
            .anyMatch(partyStatement -> partyStatement.isAccepted()
                && partyStatement.isMadeByDefendant());
    }

    @JsonIgnore
    public boolean isRejectedByDefendant() {
        return getPartyStatementStream()
                .anyMatch(partyStatement -> partyStatement.isRejected()
                        && partyStatement.isMadeByDefendant());
    }

    @JsonIgnore
    public boolean isOfferAccepted() {
        return getPartyStatementStream().anyMatch(PartyStatement::isAccepted);
    }

    @JsonIgnore
    public boolean isOfferRejected() {
        return getPartyStatementStream().anyMatch(PartyStatement::isRejected);
    }

    @JsonIgnore
    public boolean isSettled() {
        Stream<PartyStatement> partyStatementsStream = getPartyStatementStream();
        return partyStatementsStream.anyMatch(PartyStatement::isCounterSigned);
    }

    public boolean isThroughAdmissions() {
        if (CollectionUtils.isEmpty(getPartyStatements()) || !hasOffer()) {
            return false;
        }

        Stream<PartyStatement> partyStatementsStream = getPartyStatementStream();
        return partyStatementsStream
            .filter(PartyStatement::hasOffer)
            .reduce((first, second) -> second)
            .stream()
            .noneMatch(PartyStatement::hasPaymentIntention);
    }

    public boolean hasOffer() {
        return getPartyStatementStream()
            .anyMatch(PartyStatement::hasOffer);
    }

    private Stream<PartyStatement> getPartyStatementStream() {
        Stream<PartyStatement> partyStatementsStream = Optional.ofNullable(partyStatements)
            .map(Collection::stream).orElseGet(Stream::empty);
        return partyStatementsStream;
    }

}
