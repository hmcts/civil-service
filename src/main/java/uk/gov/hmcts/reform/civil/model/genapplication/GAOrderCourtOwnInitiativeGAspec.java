package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Data
@Builder(toBuilder = true)
public class GAOrderCourtOwnInitiativeGAspec {

    private String orderCourtOwnInitiative;
    private LocalDate orderCourtOwnInitiativeDate;

    @JsonCreator
    GAOrderCourtOwnInitiativeGAspec(@JsonProperty("orderCourtOwnInitiative") String orderCourtOwnInitiative,
                                @JsonProperty("orderCourtOwnInitiativeDate") LocalDate  orderCourtOwnInitiativeDate) {

        this.orderCourtOwnInitiative = orderCourtOwnInitiative;
        this.orderCourtOwnInitiativeDate = orderCourtOwnInitiativeDate;
    }
}
