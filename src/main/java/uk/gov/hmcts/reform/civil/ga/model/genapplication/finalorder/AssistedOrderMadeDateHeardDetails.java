package uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AssistedOrderMadeDateHeardDetails {

    private AssistedOrderDateHeard singleDateSelection;
    private AssistedOrderDateHeard dateRangeSelection;
    private AssistedOrderDateHeard beSpokeRangeSelection;

    @JsonCreator
    AssistedOrderMadeDateHeardDetails(@JsonProperty("singleDateSelection") AssistedOrderDateHeard singleDateSelection,
                                       @JsonProperty("dateRangeSelection") AssistedOrderDateHeard dateRangeSelection,
                                      @JsonProperty("bespokeRangeSelection") AssistedOrderDateHeard beSpokeRangeSelection) {

        this.singleDateSelection = singleDateSelection;
        this.dateRangeSelection = dateRangeSelection;
        this.beSpokeRangeSelection = beSpokeRangeSelection;
    }
}
