package uk.gov.hmcts.reform.civil.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Setter
@Data
@Builder(toBuilder = true)
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
