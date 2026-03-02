package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;

import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
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
